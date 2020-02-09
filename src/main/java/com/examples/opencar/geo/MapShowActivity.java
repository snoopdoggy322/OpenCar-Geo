
package com.examples.opencar.geo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapShowActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ActivityCompat.OnRequestPermissionsResultCallback
{
  private TextView categoryTitle;
  private Button showAsTextButton, filterButton;

  public static String whereClause = "";
  public static String category = "";
  private static final int DEFAULT_ZOOM = 13;
  private final LatLng mDefaultLocation = new LatLng(44.587930, 33.540463);
  private Location mLastKnownLocation;
  public static GoogleApiClient mGoogleApiClient;

  public static boolean searchInRadius = true;
  public static double radius=50;
  public static Units units= Units.KILOMETERS;
  public FusedLocationProviderClient mFusedLocationProviderClient;
  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    getActionBar().hide();
    super.onCreate( savedInstanceState );
    setContentView( R.layout.map_show );

    if( category.isEmpty() )
    {
      category = getIntent().getStringExtra( "category" );
    }
    if( mGoogleApiClient == null )
    {
      mGoogleApiClient = new GoogleApiClient.Builder( this )
              .addApi( LocationServices.API )
              .addConnectionCallbacks( this )
              .addOnConnectionFailedListener( this )
              .build();
      mGoogleApiClient.connect();
      mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    List<String> chosenCategories = new ArrayList<String>();
    chosenCategories.add( category );
    final BackendlessGeoQuery backendlessGeoQuery = new BackendlessGeoQuery();
    backendlessGeoQuery.setCategories( chosenCategories );
    backendlessGeoQuery.setIncludeMeta(true);
    if( !whereClause.isEmpty() )
    {
      backendlessGeoQuery.setWhereClause( whereClause );
    }
    if( searchInRadius )
    {
      if( mGoogleApiClient.isConnected() )
      {
        Location mCurrentLocation = MapShowActivity.getLastLocation(this);
        backendlessGeoQuery.setLatitude( mCurrentLocation.getLatitude() );
        backendlessGeoQuery.setLongitude( mCurrentLocation.getLongitude() );
        backendlessGeoQuery.setRadius( radius );
        backendlessGeoQuery.setUnits( units );
        backendlessGeoQuery.setIncludeMeta(true);
        mLastKnownLocation= MapShowActivity.getLastLocation(this);;
      }
      else
      {
        Toast.makeText( this, "Current location not available", Toast.LENGTH_SHORT ).show();
      }
    }

    initUI();
      Backendless.Geo.getPoints( backendlessGeoQuery, new DefaultCallback<List<GeoPoint>>( this )
    {
      @Override
      public void handleResponse(final List<GeoPoint> response )
      {

        final List<GeoPoint> geoPoints = response;
        ((MapFragment) getFragmentManager().findFragmentById( R.id.map )).getMapAsync(new OnMapReadyCallback()
        {
          @Override
          public void onMapReady(final GoogleMap googleMap )
          {
            googleMap.setMyLocationEnabled( true );
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);



            googleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom( new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())
                            , DEFAULT_ZOOM));
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                    .fillColor(0x00000000)
                    .strokeColor(-3355444)
                    .strokeWidth(10f)
                    .radius(2000); // In meters
            googleMap.addCircle(circleOptions);


            for( GeoPoint geoPoint : geoPoints )
            {
              String snip="";
              for (Map.Entry entry : geoPoint.getMetadata().entrySet())
                snip+=entry.getKey() + " - " + entry.getValue();


              googleMap.addMarker( new MarkerOptions().position( new LatLng( geoPoint.getLatitude(), geoPoint.getLongitude() ) )
                      .snippet(snip)
                      .title( "ObjectId: " + geoPoint.getObjectId())
              )
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
    Toast.makeText(MapShowActivity.this,marker.getTitle(),Toast.LENGTH_SHORT).show();
      TextView headerTitle = (TextView) findViewById(R.id.textView4);
    headerTitle.setText(marker.getSnippet());
    headerTitle.setText(getTheme().toString());
    return false;
  }
});


          }

        } );

        super.handleResponse( response );
      }
    } );
  }

  public static Location getLastLocation(Context ctx)
  {
    return LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient );

  }

  private void initUI()
  {

    categoryTitle = (TextView) findViewById( R.id.categoryTitle );
    showAsTextButton = (Button) findViewById( R.id.showAsTextButton );
    filterButton = (Button) findViewById( R.id.filterButton );

    String title = String.format( getResources().getString( R.string.browsing_category_page_title ), MapShowActivity.category );
//    categoryTitle.setText( title );

    showAsTextButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View v )
      {
        startActivity( new Intent( MapShowActivity.this, ShowGeoPointsTextActivity.class ) );
      }
    } );

    filterButton.setOnClickListener( new View.OnClickListener()
    {
      @Override
      public void onClick( View v )
      {
        startActivity( new Intent( MapShowActivity.this, FilterActivity.class ) );
      }
    } );
  }

  @Override
  public void onConnected( Bundle bundle )
  {
    LocationRequest mLocationRequest = new LocationRequest();
    LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient, mLocationRequest, this );
    mLastKnownLocation= MapShowActivity.getLastLocation(this);;
  }

  @Override
  public void onConnectionSuspended( int i )
  {

  }

  @Override
  public void onConnectionFailed( ConnectionResult connectionResult )
  {

  }

  @Override
  public void onLocationChanged( Location location )
  {

  }
}
                        