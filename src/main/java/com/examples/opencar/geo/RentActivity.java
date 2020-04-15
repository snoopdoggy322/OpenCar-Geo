package com.examples.opencar.geo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Collections;
import java.util.HashMap;
import android.os.Handler;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.google.android.gms.maps.model.LatLng;

public class RentActivity extends AppCompatActivity {
TextView textView;
private int seconds=0;
private HashMap currentMarker=null;
private Button buttonEnd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);
        textView=findViewById(R.id.textView14);
        buttonEnd=findViewById(R.id.buttonEnd);
        Intent intent = getIntent();
        currentMarker= (HashMap) intent.getSerializableExtra("currentMarker");
        textView.setText(currentMarker.toString());

buttonEnd.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        endRent();
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

private void endRent(){
    AlertDialog.Builder builder = new AlertDialog.Builder(
            RentActivity.this);
    builder.setTitle("Вы действительно желаете выполнить завершение бронирования "+currentMarker.get("model")+"?" );
    builder.setMessage("С вашего счета будет списано - ");
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
                String time = String.format("%d:%02d:%02d",hours,min,secon);
            textTimer.setText(time);
seconds++;
handler.postDelayed(this,1000);
            }
        });
    }
}
