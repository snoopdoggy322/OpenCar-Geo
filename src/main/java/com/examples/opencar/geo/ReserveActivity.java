package com.examples.opencar.geo;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.GeoPoint;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collections;
import java.util.HashMap;

public class ReserveActivity extends AppCompatActivity {
private HashMap currentMarker=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
        currentMarker= (HashMap) intent.getSerializableExtra("currentMarker");
        WebView webview = (WebView) findViewById(R.id.webView);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
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
        webview.loadUrl("http://maps.google.com/maps?" + "saddr="+currentMarker.get("myLat").toString()+","
                                                                 +currentMarker.get("myLon").toString()
                                                                 +"&daddr="+currentMarker.get("lat").toString()+","
                                                                 +currentMarker.get("lon").toString()+
                "&travelmode=walking");
       TextView textView = findViewById(R.id.textView3);

       textView.setText(currentMarker.toString());
    }

}
