package com.examples.opencar.geo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import android.os.Handler;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class RentActivity extends AppCompatActivity {
TextView textView;
    TextView cashText;
private int seconds=0;
private double cash=0;
private double plan=0;
private HashMap currentMarker=null;
private Button buttonEnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);
        textView=findViewById(R.id.textView14);
        buttonEnd=findViewById(R.id.buttonEnd);
        cashText=findViewById(R.id.CashText);
        Intent intent = getIntent();
        currentMarker= (HashMap) intent.getSerializableExtra("currentMarker");
        textView.setText(currentMarker.toString());
        plan= Double.parseDouble(currentMarker.get("cost").toString());
buttonEnd.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
    Backendless.Data.of("Orders").findById(currentMarker.get("orderId").toString(), new AsyncCallback<Map>() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleResponse(Map response) {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            LocalDateTime time_start = LocalDateTime.now();
            LocalDateTime time_end=LocalDateTime.now();;
            try {
                time_start = convertToLocalDateTimeViaInstant((Date)response.get("time_start_rent"));
                time_end = LocalDateTime.parse(getTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("timeparse",e.toString());
            }
            Log.d("timeparse",time_start.toString());
            Log.d("timeparse",time_end.toString());
            Duration duration = Duration.between(time_end, time_start);
           long diff = Math.abs(duration.getSeconds());
            endRent(diff);
        }

        @Override
        public void handleFault(BackendlessFault fault) {

        }
    });


    }
});

        LatLng ll=new LatLng((double)currentMarker.get("lat"),(double)currentMarker.get("lon"));
        GeoPoint GP = new GeoPoint(ll.latitude,ll.longitude);
        GP.setObjectId(currentMarker.get("markerId").toString());
        GP.setCategories(Collections.singleton("onLineCars"));
        Backendless.Geo.savePoint(GP, new AsyncCallback<GeoPoint>() {
            @Override
            public void handleResponse(GeoPoint response) {

            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
        runTimer();



    }

private void endRent(long rentSec){
    AlertDialog.Builder builder = new AlertDialog.Builder(
            RentActivity.this);
    builder.setTitle("Вы действительно желаете выполнить завершение бронирования "+currentMarker.get("model")+"?");
    builder.setMessage("С вашего счета будет списано - "+String.format("%32.2f",(rentSec*plan/60))+"  "+rentSec);
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
                    onBackPressed();onBackPressed();
                     finish();
                }
            });
    builder.show();
}



    private void runTimer(){
        final TextView textTimer=findViewById(R.id.textView15);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                int hours=seconds/300;
                int min=(seconds%3600)/60;
                int secon=seconds%60;
                cash=(plan*seconds)/60;
                String time = String.format("Время аренды - %d:%02d:%02d",hours,min,secon);
            cashText.setText(String.format("Сумма - %32.2f ₽",cash));
            textTimer.setText(time);

seconds=seconds+5;
handler.postDelayed(this,10000);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public LocalDateTime convertToLocalDateTimeViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
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
