
package com.examples.opencar.geo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.async.callback.UploadCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import java.io.File;
import java.io.IOException;

public class RegisterActivity extends Activity {
	private final static java.text.SimpleDateFormat SIMPLE_DATE_FORMAT = new java.text.SimpleDateFormat("yyyy/MM/dd");

	private EditText nameField;
	private EditText emailField;
	private EditText passwordField;
	private EditText phoneField;
	private Button registerButton;
	private ImageButton passportButton;
	private ImageButton licenseButton;
	private String name;
	private String email;
	private String password;
	private String phone;
	private Uri PassportUri=null;
	private Uri licenseUri=null;
	Bitmap passportImg=null;
	Bitmap licenseImg=null;
	private BackendlessUser user;

	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		initUI();
	}

	private void initUI() {
		nameField = (EditText) findViewById(R.id.nameField);
		emailField = (EditText) findViewById(R.id.emailField);
		passwordField = (EditText) findViewById(R.id.passwordField);
		registerButton = (Button) findViewById(R.id.registerButton);
		passportButton = (ImageButton) findViewById(R.id.passportButton);
		licenseButton = (ImageButton) findViewById(R.id.licenseButton);
		registerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onRegisterButtonClicked();
			}
		});
		passportButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPassportButtonClicked();
			}
		});
		licenseButton.setOnClickListener(new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			onLicenseButtonClicked();
		}
	});
		phoneField = findViewById(R.id.phoneText);

	}
private void onPassportButtonClicked(){
	Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
	photoPickerIntent.setType("image/*");
	startActivityForResult(photoPickerIntent, 1);

}
private void onLicenseButtonClicked(){
		Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, 2);

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode)
		{
			case 1:
			{
				if (resultCode == RESULT_OK)
				{PassportUri = data.getData();
					try {
						passportImg= MediaStore.Images.Media.getBitmap(getContentResolver(), PassportUri  );
					} catch (IOException e) {
						e.printStackTrace();
					}
					passportButton.setBackground(new BitmapDrawable(getResources(),passportImg));
				}
				break;
			}
			case 2:{
				if (resultCode == RESULT_OK)
				{licenseUri = data.getData();
					try {
						licenseImg = MediaStore.Images.Media.getBitmap(getContentResolver(), licenseUri );
					} catch (IOException e) {
						e.printStackTrace();
					}
					licenseButton.setBackground(new BitmapDrawable(getResources(),licenseImg));
				}

				break;
			}
		}
	}






	private void onRegisterButtonClicked() {
		String nameText = nameField.getText().toString().trim();
		String emailText = emailField.getText().toString().trim();
		String passwordText = passwordField.getText().toString().trim();
		String phoneText = phoneField.getText().toString().trim();
		if (emailText.isEmpty()) {
			Toast.makeText(this, "Field 'email' cannot be empty.", Toast.LENGTH_SHORT).show();
			return;
		} else
			email = emailText;

		if (passwordText.isEmpty()) {
			Toast.makeText(this, "Field 'password' cannot be empty.", Toast.LENGTH_SHORT).show();
			return;
		}
		else
			password = passwordText;


		if (phoneText.isEmpty()) {
			Toast.makeText(this, "Заполните номер телефона", Toast.LENGTH_SHORT).show();
			return;
		}
		else
			phone = phoneText;

		if(PassportUri==null){
			Toast.makeText(this, "Выберите фото вашего пасспорта", Toast.LENGTH_SHORT).show();
			return;
		}
		if (licenseUri==null){
			Toast.makeText(this, "Выберите фото вашего водительского удостоверения", Toast.LENGTH_SHORT).show();
			return;
		}

		if (!nameText.isEmpty()) {
			name = nameText;
		}

		final BackendlessUser user = new BackendlessUser();

		if (email != null) {
			user.setEmail(email);
		}

		if (password != null) {
			user.setPassword(password);
		}

		if (name != null) {
			user.setProperty("name", name);
		}
		if (phone != null) {
			user.setProperty("phone", phone);
		}
		if (PassportUri!=null){
				Backendless.Files.Android.upload(passportImg,
										Bitmap.CompressFormat.PNG,
										100,
										email+".png",
										"/passpImages",
										true,
		new AsyncCallback<BackendlessFile>() {
				@Override
				public void handleResponse(BackendlessFile response) {

				}
				@Override
				public void handleFault(BackendlessFault fault) {
					Toast.makeText( RegisterActivity.this,
							fault.toString(),
							Toast.LENGTH_LONG).show();
				}
			}
				);
			user.setProperty("passport", "https://backendlessappcontent.com/9CDA83AE-D089-4AE8-9EB7-0F4A21D360A8/2F059F50-9AC3-4D2C-A47C-9918C6ABC5F8/files/passpImages/"
			+user.getEmail()+".png");
		}
		if (licenseUri!=null){
			Backendless.Files.Android.upload(licenseImg,
					Bitmap.CompressFormat.PNG,
					100,
					email+".png",
					"/licImages",true,
					new AsyncCallback<BackendlessFile>() {
						@Override
						public void handleResponse(BackendlessFile response) {

						}

						@Override
						public void handleFault(BackendlessFault fault) {
							Toast.makeText( RegisterActivity.this,
									fault.toString(),
									Toast.LENGTH_LONG ).show();
						}
					}
			);
				user.setProperty("license", "https://backendlessappcontent.com/9CDA83AE-D089-4AE8-9EB7-0F4A21D360A8/2F059F50-9AC3-4D2C-A47C-9918C6ABC5F8/files/licImages/"
					+user.getEmail()+".png");
		}

		Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
			@Override
			public void handleResponse(BackendlessUser response) {
				Resources resources = getResources();
				String message = String.format(resources.getString(R.string.registration_success_message), resources.getString(R.string.app_name));

				AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
				builder.setMessage(message).setTitle(R.string.registration_success);

				DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent Intent=new Intent(RegisterActivity.this,MainLogin.class);
						startActivity(Intent);
					}
				};
				builder.setPositiveButton("OK",dialogClickListener);
				AlertDialog dialog = builder.create();

				dialog.show();

			}

			@Override
			public void handleFault(BackendlessFault fault) {
				AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
				builder.setMessage(fault.getMessage()).setTitle(R.string.registration_error);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}
}

