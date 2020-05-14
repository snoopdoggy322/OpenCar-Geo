
package com.examples.opencar.geo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainLogin extends AppCompatActivity {

	private boolean isLoggedInBackendless = false;
	private CheckBox rememberLoginBox;

	
	// backendless
	private TextView registerLink, restoreLink;
	private EditText identityField, passwordField;
	private Button bkndlsLoginButton;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_login);

		Backendless.initApp( this, getString(R.string.backendless_AppId), getString(R.string.backendless_ApiKey));
		Backendless.setUrl(getString(R.string.backendless_ApiHost));

		initUI();
		initUIBehaviour();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			checkPermission();
		}
		Backendless.UserService.isValidLogin(new DefaultCallback<Boolean>(this) {
			@Override
			public void handleResponse(Boolean isValidLogin) {
				super.handleResponse(null);
				if (isValidLogin && Backendless.UserService.CurrentUser() == null) {
					String currentUserId = Backendless.UserService.loggedInUser();

					if (!currentUserId.equals("")) {
						Backendless.UserService.findById(currentUserId, new DefaultCallback<BackendlessUser>(MainLogin.this, "Вход...") {
							@Override
							public void handleResponse(BackendlessUser currentUser) {
									super.handleResponse(currentUser);
									isLoggedInBackendless = true;
									Backendless.UserService.setCurrentUser(currentUser);
									startLoginResult(currentUser);
								}
						});
					}
				}
				super.handleResponse(isValidLogin);
			}
		});
	}

	private void initUI() {
		rememberLoginBox = (CheckBox) findViewById( R.id.rememberLoginBox );

		
		// backendless
		registerLink = (TextView) findViewById( R.id.registerLink );
		restoreLink = (TextView) findViewById( R.id.restoreLink );
		identityField = (EditText) findViewById( R.id.identityField );
		passwordField = (EditText) findViewById( R.id.passwordField );
		bkndlsLoginButton = (Button) findViewById( R.id.bkndlsLoginButton);
		
	}

	private void initUIBehaviour() {
		
		// backendless
		bkndlsLoginButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick( View view )
			{
				onLoginWithBackendlessButtonClicked();
			}
		} );
		registerLink.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View view )
			{
				onRegisterLinkClicked();
			}
		} );
		restoreLink.setOnClickListener( new View.OnClickListener()
		{
			@Override
			public void onClick( View view )
			{
				onRestoreLinkClicked();
			}
		} );
		
	}

	private void startLoginResult(BackendlessUser user)
	{
		String msg = "ObjectId: " + user.getObjectId() + "\n"
				+ "UserId: " + user.getUserId() + "\n"
				+ "Email: " + user.getEmail() + "\n"
				+ "Properties: " + "\n";

		for (Map.Entry<String, Object> entry : user.getProperties().entrySet())
			msg += entry.getKey() + " : " + entry.getValue() + "\n";



		String whereClause = "status='забронировано' and client_id='"+user.getObjectId()+"'";
		DataQueryBuilder queryBuilder = DataQueryBuilder.create();
		queryBuilder.setWhereClause( whereClause );
		Backendless.Data.of( "Orders" ).find(queryBuilder, new AsyncCallback<List<Map>>() {
			@Override
			public void handleResponse(List<Map> response) {
			    if(response.size()!=0){
				HashMap currentMarker = new HashMap();
				Intent Intent = new Intent(MainLogin.this, ReserveActivity.class);
				currentMarker.put("orderId",(String)response.get(0).get("objectId"));
				Log.d("DEBUGG",response.get(0).toString());
				Intent.putExtra("currentMarker", currentMarker);
				startActivity(Intent);
				finish();}

			}

			@Override
			public void handleFault(BackendlessFault fault) {

			}
		});
		String whereClause2 = "status='Начата аренда' and client_id='"+user.getObjectId()+"'";
		DataQueryBuilder queryBuilder2 = DataQueryBuilder.create();
		queryBuilder2.setWhereClause( whereClause2 );
		Backendless.Data.of( "Orders" ).find(queryBuilder2, new AsyncCallback<List<Map>>() {
			@Override
			public void handleResponse(List<Map> response) {
				if(response.size()!=0){
					HashMap currentMarker = new HashMap();
					Intent Intent = new Intent(MainLogin.this, RentActivity.class);
					currentMarker.put("orderId",(String)response.get(0).get("objectId"));
					Log.d("DEBUGG",response.get(0).toString());
					Intent.putExtra("currentMarker", currentMarker);
					startActivity(Intent);
					finish();}

			}

			@Override
			public void handleFault(BackendlessFault fault) {

			}
		});
		if ((int )user.getProperty("status")!=1){
		startActivity( new Intent( this, GeoCategoriesListActivity.class ) );
		finish();}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Ваш аккаут проходит проверку..").setTitle("Ожидайте...");

			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					System.exit(0);
				}
			};
			builder.setPositiveButton("OK",dialogClickListener);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
	}

	private void startLoginResult(String msg, boolean logoutButtonState)
	{
		Intent intent = new Intent(this, LoginResult.class);
		intent.putExtra(LoginResult.userInfo_key, msg);
		intent.putExtra(LoginResult.logoutButtonState_key, logoutButtonState);
		startActivity(intent);
	}

	
	private void onLoginWithBackendlessButtonClicked()
	{
		String identity = identityField.getText().toString();
		String password = passwordField.getText().toString();
		boolean rememberLogin = rememberLoginBox.isChecked();

		Backendless.UserService.login( identity, password, new DefaultCallback<BackendlessUser>( MainLogin.this )
		{
			public void handleResponse( BackendlessUser backendlessUser )
			{
				super.handleResponse( backendlessUser );
				isLoggedInBackendless = true;
				startLoginResult(backendlessUser);
			}

			@Override
			public void handleFault(BackendlessFault fault) {
				super.handleFault(fault);
				startLoginResult(fault.toString(), false);
			}
		}, rememberLogin );
	}

	private void onRegisterLinkClicked()
	{
		startActivity( new Intent( this, RegisterActivity.class ) );
	}

	private void onRestoreLinkClicked()
	{
		startActivity( new Intent( this, RestorePasswordActivity.class ) );
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		
	}
	public void checkPermission(){
		if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
		){//Can add more as per requirement

			ActivityCompat.requestPermissions(this,
					new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION},
					123);
		}
	}
}
    