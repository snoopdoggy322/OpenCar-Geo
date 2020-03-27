package com.examples.opencar.geo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessException;
import com.backendless.exceptions.BackendlessFault;
import com.google.android.gms.wallet.PaymentData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.utf8.UTF8;
import ru.yandex.money.android.sdk.Amount;
import ru.yandex.money.android.sdk.Checkout;
import ru.yandex.money.android.sdk.ColorScheme;
import ru.yandex.money.android.sdk.MockConfiguration;
import ru.yandex.money.android.sdk.PaymentParameters;
import ru.yandex.money.android.sdk.SavePaymentMethod;
import ru.yandex.money.android.sdk.TestParameters;
import ru.yandex.money.android.sdk.TokenizationResult;
import ru.yandex.money.android.sdk.UiParameters;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


public class ValletActivity extends AppCompatActivity {
Button btn;
    private static int REQUEST_CODE_TOKENIZE;
    private  BigDecimal Value= BigDecimal.valueOf(100.00);
    BackendlessUser user= Backendless.UserService.CurrentUser();
    double CurrentValue= Double.valueOf(user.getProperty("vallet").toString());
    EditText AmountEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vallet);
        btn = findViewById(R.id.button3);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar_back);
        myToolbar.setTitle(Double.toString(CurrentValue));
        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                onBackPressed();

            }});

        TextView nameText = findViewById(R.id.valletText);
        AmountEdit=findViewById(R.id.editText);
        nameText.setText(Double.toString(CurrentValue)+" \u20BD");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AmountEdit.getText().length()!=0)
                Value=new BigDecimal(AmountEdit.getText().toString());
                timeToStartCheckout();
            }
        });
    }



    void timeToStartCheckout() {
        PaymentParameters paymentParameters = new PaymentParameters( new Amount(Value, Currency.getInstance("RUB")),
                "Название товара",
                "Описание товара",
                "test_NjgxNzY0hC97jKlgGsQ9YjZS7odXlvoRW0LUlPusFNo",
                "680005",
                SavePaymentMethod.USER_SELECTS
        );
       TestParameters testParameters = new TestParameters(true, true);
         //       ,new MockConfiguration(false, true, 5, new com.examples.opencar.geo.Amount(BigDecimal.TEN, Currency.getInstance("RUB"))));
        UiParameters uiParameters = new UiParameters(false, new ColorScheme(Color.rgb(0, 114, 245)));
        Intent intent = Checkout.createTokenizeIntent(this, paymentParameters,testParameters,uiParameters);
        startActivityForResult(intent, REQUEST_CODE_TOKENIZE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 777) {
            switch (resultCode) {
                case RESULT_OK:
                    double curval=new Double(user.getProperty("vallet").toString());
                    Log.d("server_msg1", String.valueOf(curval));
                    Log.d("server_msg1", String.valueOf(Value.doubleValue()));
                    updateCurrentUserVallet("vallet",curval+Value.doubleValue());
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Счет был успешно пополнен").setTitle("+"+Value.doubleValue());
                    builder.create();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                    break;
                case RESULT_CANCELED:
                    Toast toast=Toast.makeText(this,"3D Secure не пройдено", Toast.LENGTH_LONG);
                    break;
                case Checkout.RESULT_ERROR:
                    toast = Toast.makeText(this,"Ошибка 3D Secure", Toast.LENGTH_LONG);
                    // Во время 3-D Secure произошла какая-то ошибка
                    break;
            }
        }



        if (requestCode == REQUEST_CODE_TOKENIZE) {
            switch (resultCode) {
                case RESULT_OK:
                    // successful tokenization
                    TokenizationResult result = Checkout.createTokenizationResult(data);
                    TextView tw = (TextView)findViewById(R.id.textView7);
                    tw.setText(result.toString());
                    try {
                        doHTTP(result.getPaymentToken());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //  Checkout.create3dsIntent(this,"");
                   // startActivity(SuccessTokenizeActivity.createIntent(
                     //     this, result.getPaymentToken(), result.getPaymentMethodType().name()));
                    break;
                case RESULT_CANCELED:
                    // user canceled tokenization
                    Toast.makeText(this, "Отмена пользователем", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }


    public class HttpPostAsyncTask extends AsyncTask<String, Void, Void> {
        // This is the JSON body of the post
        JSONObject postData;
        // This is a constructor that allows you to pass in the JSON body
        public HttpPostAsyncTask(JSONObject postData) {
            if (postData != null) {
                this.postData = postData;
            }
        }

        // This is a function that we are overriding from AsyncTask. It takes Strings as parameters because that is what we defined for the parameters of our async task
        @Override
        protected Void doInBackground(String... params) {

            try {
                // This is getting the url from the string we passed in
                URL url = new URL(params[0]);
                // Create the urlConnection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                String authString ="681764" + ":" + "test_Ku88CFQrkYZ4pzqiGr2V0rJ0N8hZhftAcjgNrBjbibY";
                urlConnection.setRequestProperty("Authorization", "Basic NjgxNzY0OnRlc3RfS3U4OENGUXJrWVo0cHpxaUdyMlYwckowTjhoWmhmdEFjamdOckJqYmliWQ==");

                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");

                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Idempotence-Key",  UUID.randomUUID().toString());

                // Send the post body
                if (this.postData != null) {
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    writer.write(postData.toString());
                    writer.flush();
                }

                int statusCode = urlConnection.getResponseCode();
                Log.d("server_msg", String.valueOf(statusCode));
                if (statusCode ==  200) {

                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();

                    JSONObject json=new JSONObject(sb.toString());
                    JSONObject conf=json.getJSONObject("confirmation");

                    String conf_url = conf.get("confirmation_url").toString();
                    Log.d("server_msg", conf_url);
                    Intent intent =Checkout.create3dsIntent(ValletActivity.this,conf_url);
                    startActivityForResult(intent, 777);

                    // From here you can convert the string to JSON with whyatever JSON parser you like to use
                    // After converting the string to JSON, I call my custom callback. You can follow this process too, or you can implement the onPostExecute(Result) method
                } else {
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                    String response = inputStream.toString();
                    Log.d("server_msg", "Ошибка - "+response);
                }

            } catch (Exception e) {
                Log.d("server_msg", e.getLocalizedMessage());
            }
            return null;
        }
    }
    public void doHTTP(String token) throws JSONException {
        Log.d("server_msg", "Шота пашло"+token);

        MyPayment payment = new MyPayment();
        payment.setPayment_token(token);
        com.examples.opencar.geo.Amount amount = new com.examples.opencar.geo.Amount();
        amount.setCurrency("RUB");
        amount.setValue(Value.toString());
        payment.setAmount(amount);
        Confirmation confirmation = new Confirmation();
        confirmation.setType("redirect");
        confirmation.setEnforce("false");
        confirmation.setReturn_url("https://www.merchant-website.com/return_url");
        payment.setConfirmation(confirmation);
        payment.setCapture("false");
        payment.setDescription("Пополнение счета");
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(payment);// obj is your object
        JSONObject jo = new JSONObject(json);
        Log.d("server_msg", jo.toString());
        HttpPostAsyncTask task = new HttpPostAsyncTask(jo);
        task.execute( "https://payment.yandex.net/api/v3/payments");
    }

    protected void updateCurrentUserVallet(String key,double value){
        user.setProperty(key,value);
        Backendless.UserService.update(user, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {

            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });
    }
    }
