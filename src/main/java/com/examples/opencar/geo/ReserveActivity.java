package com.examples.opencar.geo;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ReserveActivity extends AppCompatActivity {
    private Button buttonCancell;
    private Button buttonStart;
private HashMap currentMarker=null;
private static final long START_TIME=900000;
private  TextView TextTimer;
private CountDownTimer CountDownTimer;
private long TimeLeft=START_TIME;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
        buttonCancell=findViewById(R.id.buttonCancell);
        buttonStart=findViewById(R.id.buttonStart);
        currentMarker= (HashMap) intent.getSerializableExtra("currentMarker");

        TextTimer   =findViewById(R.id.textView_timer);
        TextView textView = findViewById(R.id.textView3);
        WebView webview = (WebView) findViewById(R.id.webView);

        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("http://maps.google.com/maps?" + "saddr="+currentMarker.get("myLat").toString()+","
                +currentMarker.get("myLon").toString()
                +"&daddr="+currentMarker.get("lat").toString()+","
                +currentMarker.get("lon").toString()+
                "&travelmode=walking");
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
startTimer();


       textView.setText("Ваш автомобиль-"+currentMarker.get("model").toString()+
               "\nГос. Номер -"+currentMarker.get("number").toString()+currentMarker.toString());



       buttonStart.setOnClickListener(new View.OnClickListener() {
           @RequiresApi(api = Build.VERSION_CODES.O)
           @Override
           public void onClick(View view) {
               HashMap order=new HashMap();
               order.put("objectId",currentMarker.get("orderId"));
               order.put("status","Начата аренда");
               LocalDateTime dateTime = LocalDateTime.parse(getTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
               order.put("time_end_reserve",dateTime.toString());
               order.put("time_start_rent",dateTime.toString());
               Backendless.Data.of( "Orders" ).save( order, new AsyncCallback<Map>() {
                   public void handleResponse( Map response )
                   {
                       Intent Intent = new Intent(ReserveActivity.this, RentActivity.class);
                       Intent.putExtra("currentMarker", currentMarker);
                       startActivity(Intent);
                   }

                   public void handleFault( BackendlessFault fault )
                   {
                       // an error has occurred, the error code can be retrieved with fault.getCode()
                   }
               });

           }
       });
       buttonCancell.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               reserveCancel();
           }
       });
    }
private void startTimer(){

CountDownTimer=new CountDownTimer(TimeLeft,1000) {
    @Override
    public void onTick(long l) {
    TimeLeft=l;
    updateCountDownText();
    }

    @Override
    public void onFinish() {

    }
};
CountDownTimer.start();
}
private void updateCountDownText(){
        int min=(int) (TimeLeft/1000)/60;
        int sec =(int) (TimeLeft/1000)%60;
        String timeFormat=String.format(Locale.getDefault(),"Бесплатное бронирование - %02d:%02d",min,sec);
        TextTimer.setText(timeFormat);
}

private void reserveCancel(){
    AlertDialog.Builder builder = new AlertDialog.Builder(
            ReserveActivity.this);
    builder.setTitle("Вы действительно желаете выполнить отмену брони "+currentMarker.get("model")+"?" );
    builder.setMessage("С вашего счета ничего не будет списано");
    builder.setNeutralButton("Отмена",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                                    int which) {
                    Toast.makeText(getApplicationContext(),"Отмена",Toast.LENGTH_LONG).show();
                }
            });
    builder.setPositiveButton("Да",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                                    int which) {
                    Toast.makeText(getApplicationContext(),"Отмена...",Toast.LENGTH_LONG).show();
                    LatLng ll=new LatLng((double)currentMarker.get("lat"),(double)currentMarker.get("lon"));
                    GeoPoint GP = new GeoPoint(ll.latitude,ll.longitude);
                    GP.setObjectId(currentMarker.get("markerId").toString());
                    GP.setCategories(Collections.singleton("readyCars"));
                    Backendless.Geo.savePoint(GP, new AsyncCallback<GeoPoint>() {
                        @Override
                        public void handleResponse(GeoPoint response) {

                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                        }
                    });
                    ReserveActivity.super.onBackPressed();
                    finish();
                }
            });
    builder.show();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


    protected String getTime() {
        String responce="";
        try {
            responce=new AsyncTask<Void, String, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    String s = "";
                    try {
                        s = MapShowActivity.doGet("http://worldtimeapi.org/api/timezone/Europe/Simferopol");
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