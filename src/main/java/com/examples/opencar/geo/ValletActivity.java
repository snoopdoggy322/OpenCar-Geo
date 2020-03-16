package com.examples.opencar.geo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;


public class ValletActivity extends AppCompatActivity {

    BackendlessUser user= Backendless.UserService.CurrentUser();
    double CurrentValue= Double.valueOf(user.getProperty("vallet").toString());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vallet);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_back);
        myToolbar.setTitle(Double.toString(CurrentValue));
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }});

        TextView nameText = findViewById(R.id.valletText);
        nameText.setText(Double.toString(CurrentValue)+" \u20BD");

    }

}