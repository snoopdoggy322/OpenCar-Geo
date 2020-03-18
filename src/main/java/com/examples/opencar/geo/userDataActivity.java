package com.examples.opencar.geo;

import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.files.BackendlessFile;
import com.mikepenz.iconics.typeface.FontAwesome;
import com.squareup.picasso.Picasso;


public class userDataActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_user_data);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_back);
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
              onBackPressed();
           }});

        TextView nameText = findViewById(R.id.nameText);
        ImageView passpView=findViewById(R.id.imageView2);
        ImageView licView=findViewById(R.id.imageView3);
        BackendlessUser user=Backendless.UserService.CurrentUser();
        String passpUrl="https://backendlessappcontent.com/9CDA83AE-D089-4AE8-9EB7-0F4A21D360A8/2F059F50-9AC3-4D2C-A47C-9918C6ABC5F8/files/passpImages/"
                +user.getEmail()+".png";
        String licUrl="https://backendlessappcontent.com/9CDA83AE-D089-4AE8-9EB7-0F4A21D360A8/2F059F50-9AC3-4D2C-A47C-9918C6ABC5F8/files/licImages/"
                +user.getEmail()+".png";

        nameText.setText((String)user.getProperty("name"));
        loadImageFromUrl(passpUrl,passpView);
        loadImageFromUrl(licUrl,licView);




    }

    private void loadImageFromUrl(String url,ImageView IV){
        Picasso.with(this).load(url).placeholder(R.drawable.header)
                .error(R.mipmap.ic_launcher)
                .into(IV,new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
    }
}