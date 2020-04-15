package com.examples.opencar.geo;

import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

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
               "\nГос. Номер -"+currentMarker.get("number").toString());



       buttonStart.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent Intent = new Intent(ReserveActivity.this, RentActivity.class);
               Intent.putExtra("currentMarker", currentMarker);
               startActivity(Intent);
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
}