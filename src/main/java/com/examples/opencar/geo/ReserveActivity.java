package com.examples.opencar.geo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ReserveActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve);
       TextView textView = findViewById(R.id.textView3);
       textView.setText(intent.getStringExtra("currentMarker"));

    }
}
