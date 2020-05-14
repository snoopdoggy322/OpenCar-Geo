package com.examples.opencar.geo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
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
        final Button editButton=findViewById(R.id.edit_button);
        final Button saveButton=findViewById(R.id.save_button);
        final TextView nameText = findViewById(R.id.nameText);
        TextView mailText = findViewById(R.id.mailText);
        ImageView passpView=findViewById(R.id.imageView2);
        ImageView licView=findViewById(R.id.imageView3);
        final BackendlessUser user=Backendless.UserService.CurrentUser();
        String passpUrl="https://backendlessappcontent.com/9CDA83AE-D089-4AE8-9EB7-0F4A21D360A8/2F059F50-9AC3-4D2C-A47C-9918C6ABC5F8/files/passpImages/"
                +user.getEmail()+".png";
        String licUrl="https://backendlessappcontent.com/9CDA83AE-D089-4AE8-9EB7-0F4A21D360A8/2F059F50-9AC3-4D2C-A47C-9918C6ABC5F8/files/licImages/"
                +user.getEmail()+".png";
        mailText.setText(user.getEmail());
        nameText.setText((String)user.getProperty("name"));
        loadImageFromUrl(passpUrl,passpView);
        loadImageFromUrl(licUrl,licView);


editButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {

        if(saveButton.getVisibility()!=View.VISIBLE){
            saveButton.setVisibility(View.VISIBLE);
            nameText.setEnabled(true);

        }
        else{
        saveButton.setVisibility(View.INVISIBLE);
        nameText.setEnabled(false); }
    }
});
saveButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        user.setProperty("name",nameText.getText().toString());
        Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                Toast.makeText(userDataActivity.this, "Профиль успешно обновлен", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("DEBUGG",fault.getMessage());
                nameText.getBackground().setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP);
            }
        });
        saveButton.setVisibility(View.INVISIBLE);
        nameText.setEnabled(false);
    }
});

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