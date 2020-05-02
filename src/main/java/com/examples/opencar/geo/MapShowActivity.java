
package com.examples.opencar.geo;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.geo.Units;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MapShowActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {

  static final String userInfo_key = "BackendlessUserInfo";
  static final String logoutButtonState_key = "LogoutButtonState";

  private TextView categoryTitle, ModelText, LocationText, CostText, NumberText;
  private Button showAsTextButton, filterButton, reserveButton;

  public static String whereClause = "";
  public static String category = "";
  private static final int DEFAULT_ZOOM = 13;
  private final LatLng mDefaultLocation = new LatLng(44.587930, 33.540463);
  private Location mLastKnownLocation;
  public static GoogleApiClient mGoogleApiClient;
  private Drawer.Result drawerResult = null;
  public static boolean searchInRadius = true;
  public static double radius = 50;
  public static Units units = Units.KILOMETERS;
  public FusedLocationProviderClient mFusedLocationProviderClient;
  public HashMap currentMarker = null;
  String userData = null;
  private BackendlessUser user;
  private final BackendlessGeoQuery backendlessGeoQuery = new BackendlessGeoQuery();
  @Override
  public void onCreate(Bundle savedInstanceState) {
    user=Backendless.UserService.CurrentUser();
    userData = Backendless.UserService.CurrentUser().toString();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map_show);


    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    drawerResult = new Drawer()
            .withActivity(this)
            .withToolbar(toolbar)
            .withActionBarDrawerToggle(true)
            //.withHeader(R.layout.drawer_header)
            .addDrawerItems(

                    new PrimaryDrawerItem().withName("Профиль").withIcon(FontAwesome.Icon.faw_edit).withBadge("1").withIdentifier(1),
                    new PrimaryDrawerItem().withName("Кошелёк").withIcon(FontAwesome.Icon.faw_money).withIdentifier(2),
                    new PrimaryDrawerItem().withName("Мои поездки").withIcon(FontAwesome.Icon.faw_eye).withBadge("6").withIdentifier(3),
                    new SectionDrawerItem().withName(R.string.drawer_item_settings),
                    new SecondaryDrawerItem().withName(R.string.drawer_item_help).withIcon(FontAwesome.Icon.faw_cog),
                    new SecondaryDrawerItem().withName(R.string.drawer_item_open_source).withIcon(FontAwesome.Icon.faw_question).setEnabled(false),
                    new DividerDrawerItem(),
                    new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(FontAwesome.Icon.faw_github).withBadge("12+").withIdentifier(1)
            )
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                if (drawerItem.getIdentifier() == 1) {
                  Intent Intent = new Intent(MapShowActivity.this, userDataActivity.class);
                  Intent.putExtra("userData", userData);
                  startActivity(Intent);

                }
                if (drawerItem.getIdentifier() == 2) {
                  Intent Intent = new Intent(MapShowActivity.this, ValletActivity.class);
                  Intent.putExtra("userData", userData);
                  startActivity(Intent);

                }
              }
            })
            .build();


    if (category.isEmpty()) {
      category = getIntent().getStringExtra("category");
    }
    if (mGoogleApiClient == null) {
      mGoogleApiClient = new GoogleApiClient.Builder(this)
              .addApi(LocationServices.API)
              .addConnectionCallbacks(this)
              .addOnConnectionFailedListener(this)
              .build();
      mGoogleApiClient.connect();
      mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    List<String> chosenCategories = new ArrayList<String>();
    chosenCategories.add(category);
  // backendlessGeoQuery = new BackendlessGeoQuery();
    backendlessGeoQuery.setCategories(chosenCategories);
    backendlessGeoQuery.setIncludeMeta(true);
    if (!whereClause.isEmpty()) {
      backendlessGeoQuery.setWhereClause(whereClause);
    }
    if (searchInRadius) {
      if (mGoogleApiClient.isConnected()) {
        Location mCurrentLocation = MapShowActivity.getLastLocation(this);
        backendlessGeoQuery.setLatitude(mCurrentLocation.getLatitude());
        backendlessGeoQuery.setLongitude(mCurrentLocation.getLongitude());
        backendlessGeoQuery.setRadius(radius);
        backendlessGeoQuery.setUnits(units);
        backendlessGeoQuery.setIncludeMeta(true);
        mLastKnownLocation = MapShowActivity.getLastLocation(this);
        ;
      } else {
        Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
      }
    }

    initUI();

    Backendless.Geo.getPoints(backendlessGeoQuery, new DefaultCallback<List<GeoPoint>>(this) {
      @Override
      public void handleResponse(final List<GeoPoint> response) {

        final List<GeoPoint> geoPoints = response;
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
          @Override
          public void onMapReady(final GoogleMap googleMap) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);


            googleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())
                            , DEFAULT_ZOOM));
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                    .fillColor(0x00000000)
                    .strokeColor(-3355444)
                    .strokeWidth(10f)
                    .radius(2000); // In meters
            googleMap.addCircle(circleOptions);


            for (GeoPoint geoPoint : geoPoints) {
              String snip = "NO DATA";
              for (Map.Entry entry : geoPoint.getMetadata().entrySet()) {
                snip += entry.getKey() + " - " + entry.getValue();

              }
              HashMap carMap = (HashMap) geoPoint.getMetadata();
              HashMap[] carDataMap = null;
              if (carMap.get("CarData") != null) {
                carDataMap = (HashMap[]) carMap.get("CarData");
                carDataMap[0].put("lat",geoPoint.getLatitude());
                carDataMap[0].put("lon",geoPoint.getLongitude());
                carDataMap[0].put("markerId",geoPoint.getObjectId());
                snip = (String) carDataMap[0].toString();
              }

              googleMap.addMarker(new MarkerOptions().position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                      .snippet(snip)
                      .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon))
                      .title("ObjectId: " + geoPoint.getObjectId())
              ).setTag(carDataMap);
              ;
              googleMap.setTrafficEnabled(false);


            }
            //if (geoPoints.size() == backendlessGeoQuery.getPageSize()) {
            //  backendlessGeoQuery.prepareForNextPage();
            //  Backendless.Geo.getPoints( backendlessGeoQuery,this);
            // }
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
              @Override
              public boolean onMarkerClick(Marker marker) {
                setMapHeight(-1);
                Toast.makeText(MapShowActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
                HashMap[] map = (HashMap[]) marker.getTag();
                ModelText.setText((String) map[0].get("model"));
                CostText.setText(map[0].get("cost").toString() + " ₽/Мин");

                LocationText.setText(getAddressFromLocation(marker.getPosition().latitude,marker.getPosition().longitude));
                NumberText.setText((String) map[0].get("number"));

                currentMarker=map[0];
                return false;
              }
            });


          }

        });

        super.handleResponse(response);
      }
    });

  }

  public static Location getLastLocation(Context ctx) {
    return LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

  }

  public void setMapHeight(int re) {
    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) findViewById(R.id.map).getLayoutParams(); // получаем параметры
    if (re == -1) {
      params.height = 1000; // меняем высоту
    } else
      params.height = params.MATCH_PARENT;

    findViewById(R.id.map).setLayoutParams(params);

  }

  private void initUI() {
    categoryTitle = (TextView) findViewById(R.id.categoryTitle);
    ModelText = findViewById(R.id.textView4);
    NumberText = findViewById(R.id.textView11);
    LocationText = findViewById(R.id.textView12);
    CostText = findViewById(R.id.textView13);

    showAsTextButton = (Button) findViewById(R.id.showAsTextButton);
    filterButton = (Button) findViewById(R.id.filterButton);
    reserveButton = (Button) findViewById(R.id.reserveButton);
    String title = String.format(getResources().getString(R.string.browsing_category_page_title), MapShowActivity.category);
//    categoryTitle.setText( title );

    showAsTextButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //startActivity(new Intent(MapShowActivity.this, ShowGeoPointsTextActivity.class));
        updatePoints(backendlessGeoQuery);
      }
    });

    filterButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MapShowActivity.this, FilterActivity.class));
      }
    });
    reserveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                MapShowActivity.this);
        builder.setTitle("Вы действительно желаете забронировать автомобиль - "+currentMarker.get("model")+"?" );
        builder.setMessage("Вам будет предоставлено 15 минут для того, что бы добратся до автомобиля и продолжить."+currentMarker.toString());
        builder.setNeutralButton("Отмена",
                new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog,
                                      int which) {
                    Toast.makeText(getApplicationContext(),"Отмена",Toast.LENGTH_LONG).show();
                  }
                });
        builder.setPositiveButton("Да",
                new DialogInterface.OnClickListener() {
                  @RequiresApi(api = Build.VERSION_CODES.O)
                  public void onClick(DialogInterface dialog,
                                      int which) {
                    Toast.makeText(getApplicationContext(),"Бронирование...",Toast.LENGTH_LONG).show();
                    HashMap order=new HashMap();
                        order.put("car_id",currentMarker.get("objectId"));
                        order.put("client_id",user.getObjectId());
                        order.put("status","забронировано");
                         order.put("marker_id",currentMarker.get("markerId"));
                        LocalDateTime dateTime = LocalDateTime.parse(getTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        order.put("time_start_reserve",dateTime.toString());
                        order.put("time_end_reserve",dateTime.plusMinutes(15).toString());
                   Backendless.Data.of( "Orders" ).save( order, new AsyncCallback<Map>() {
                      public void handleResponse( Map response )
                      {
                        Log.d("serv_msg",response.toString());
                        currentMarker.put("orderId",response.get("objectId"));
                        Intent Intent = new Intent(MapShowActivity.this, ReserveActivity.class);
                        currentMarker.put("myLat",mLastKnownLocation.getLatitude());
                        currentMarker.put("myLon",mLastKnownLocation.getLongitude());
                        Intent.putExtra("currentMarker", currentMarker);
                        startActivity(Intent);
                        LatLng ll=new LatLng((double)currentMarker.get("lat"),(double)currentMarker.get("lon"));
                        GeoPoint GP = new GeoPoint(ll.latitude,ll.longitude);
                        GP.setObjectId(currentMarker.get("markerId").toString());
                        GP.setCategories(Collections.singleton("reservedCars"));
                        Backendless.Geo.savePoint(GP, new AsyncCallback<GeoPoint>() {
                          @Override
                          public void handleResponse(GeoPoint response) {

                          }

                          @Override
                          public void handleFault(BackendlessFault fault) {

                          }
                        });
                       //finish();
                      }

                      public void handleFault( BackendlessFault fault )
                      {
                        // an error has occurred, the error code can be retrieved with fault.getCode()
                      }
                    });
//                    Intent Intent = new Intent(MapShowActivity.this, ReserveActivity.class);
//                    currentMarker.put("myLat",mLastKnownLocation.getLatitude());
//                    currentMarker.put("myLon",mLastKnownLocation.getLongitude());
//                    Intent.putExtra("currentMarker", currentMarker);
//                    startActivity(Intent);
                  }
                });
        builder.show();

      }
    });
  }

  @Override
  public void onConnected(Bundle bundle) {
    LocationRequest mLocationRequest = new LocationRequest();
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    mLastKnownLocation = MapShowActivity.getLastLocation(this);
    ;
  }

  @Override
  public void onBackPressed() {
    if (drawerResult.isDrawerOpen()) {
      drawerResult.closeDrawer();
    }
    if (findViewById(R.id.map).getHeight() == 1000) {
      setMapHeight(1);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onConnectionSuspended(int i) {

  }

  @Override
  public void onConnectionFailed(ConnectionResult connectionResult) {

  }

  @Override
  public void onLocationChanged(Location location) {

  }

  private String getAddressFromLocation(double latitude, double longitude) {

    Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);


    try {
      List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

      if (addresses.size() > 0) {
        Address fetchedAddress = addresses.get(0);
        StringBuilder strAddress = new StringBuilder();
        for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++) {
          strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
        }

        return strAddress.toString();

      } else {
        return "Searching Current Address";
      }

    } catch (IOException e) {
      e.printStackTrace();
      return "Could not get address..!"+e.getMessage();
    }
  }
public void updatePoints(BackendlessGeoQuery backendlessGeoQuery){
  Backendless.Geo.getPoints(backendlessGeoQuery, new AsyncCallback<List<GeoPoint>>() {
    @Override
    public void handleResponse(final List<GeoPoint> response) {

      final List<GeoPoint> geoPoints = response;
      ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {

          for (GeoPoint geoPoint : geoPoints) {
            String snip = "NO DATA";
            for (Map.Entry entry : geoPoint.getMetadata().entrySet()) {
              snip += entry.getKey() + " - " + entry.getValue();

            }
            HashMap carMap = (HashMap) geoPoint.getMetadata();
            HashMap[] carDataMap = null;
            if (carMap.get("CarData") != null) {
              carDataMap = (HashMap[]) carMap.get("CarData");
              carDataMap[0].put("lat",geoPoint.getLatitude());
              carDataMap[0].put("lon",geoPoint.getLongitude());
              carDataMap[0].put("markerId",geoPoint.getObjectId());
              snip = (String) carDataMap[0].toString();
            }

            googleMap.addMarker(new MarkerOptions().position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                    .snippet(snip)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon))
                    .title("ObjectId: " + geoPoint.getObjectId())
            ).setTag(carDataMap);
            ;
            googleMap.setTrafficEnabled(false);


          }
          //if (geoPoints.size() == backendlessGeoQuery.getPageSize()) {
          //  backendlessGeoQuery.prepareForNextPage();
          //  Backendless.Geo.getPoints( backendlessGeoQuery,this);
          // }
          googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
              Toast.makeText(MapShowActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
              HashMap[] map = (HashMap[]) marker.getTag();
              ModelText.setText((String) map[0].get("model"));
              CostText.setText(map[0].get("cost").toString() + " ₽/Мин");

              LocationText.setText(getAddressFromLocation(marker.getPosition().latitude,marker.getPosition().longitude));
              NumberText.setText((String) map[0].get("number"));
              setMapHeight(-1);
              currentMarker=map[0];
              return false;
            }
          });


        }

      });
    }

    @Override
    public void handleFault(BackendlessFault fault) {

    }
  });
}
  public static String doGet(String url)
          throws Exception {

    URL obj = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

    //add reuqest header
    connection.setRequestMethod("GET");
    connection.setRequestProperty("User-Agent", "Mozilla/5.0" );
    connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    connection.setRequestProperty("Content-Type", "application/json");

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = bufferedReader.readLine()) != null) {
      response.append(inputLine);
    }
    bufferedReader.close();

//      print result
    Log.d("serv_msg","Response string: " + response.toString());


    return response.toString();
  }
protected String getTime() {
String responce="";
  try {
    responce=new AsyncTask<Void, String, String>() {
       @Override
       protected String doInBackground(Void... voids) {
         String s = "";
         try {
           s = doGet("http://worldtimeapi.org/api/timezone/Europe/Simferopol");
         } catch (Exception e) {
           e.printStackTrace();
         }
         return s;
       }

       @Override
       protected void onPostExecute(final String result) {
         runOnUiThread(new Runnable() {
           @Override
           public void run() {
                     }
         });
       }
     }.execute().get();
  } catch (ExecutionException e) {
    e.printStackTrace();
  } catch (InterruptedException e) {
    e.printStackTrace();
  }
  JSONObject json= null;
  try {
    json = new JSONObject(responce);
    responce=json.getString("datetime");
    Log.d("serv_msg","responce "+responce);
  } catch (JSONException e) {
    e.printStackTrace();
  }

  return responce;
}

}