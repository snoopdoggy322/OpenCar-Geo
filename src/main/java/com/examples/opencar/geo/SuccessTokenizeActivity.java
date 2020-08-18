package com.examples.opencar.geo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;
import ru.yandex.money.android.sdk.Checkout;

public final class SuccessTokenizeActivity extends AppCompatActivity {

    public static final String TOKEN_EXTRA = "paymentToken";
    public static final String TYPE_EXTRA = "type";
    public static final int REQUEST_CODE_3DS = 34;

    @NonNull
    private String url3ds = "";

    @NonNull
    public static Intent createIntent(@NonNull Context context, @NonNull String paymentToken, @NonNull String type) {
        return new Intent(context, SuccessTokenizeActivity.class)
                .putExtra(SuccessTokenizeActivity.TOKEN_EXTRA, paymentToken)
                .putExtra(SuccessTokenizeActivity.TYPE_EXTRA, type);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success_tokenize);

        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        int show3dsContainer = shouldShow3dsContainer();
        findViewById(R.id.container3ds).setVisibility(show3dsContainer);
        if (show3dsContainer == View.VISIBLE) {
            findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    open3dsConfirmation();
                }
            });
            ((EditText) findViewById(R.id.url3ds)).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    url3ds = s.toString();
                }
            });
        }

        findViewById(R.id.showToken).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = SuccessTokenizeActivity.this.getIntent();
                final String token = intent.getStringExtra(TOKEN_EXTRA);
                final String type = intent.getStringExtra(TYPE_EXTRA);
                new AlertDialog.Builder(SuccessTokenizeActivity.this)
                        .setMessage("Token: " + token + "\nType: " + type)
                        .setPositiveButton("token_copy", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                if (clipboard != null) {
                                    ClipData clip = ClipData.newPlainText("token", token);
                                    clipboard.setPrimaryClip(clip);
                                }
                            }
                        })
                        .setNegativeButton("R.string.token_cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_3DS) {
            switch (resultCode) {
                case RESULT_OK:
                    show3dsAlertDialog("R.string.success_3ds");
                    break;
                case RESULT_CANCELED:
                    show3dsAlertDialog("R.string.cancel_3ds");
                    break;
                case Checkout.RESULT_ERROR:
                    String error =
                            data.toString();
                    show3dsAlertDialog(error);
                    break;
            }
        }
    }

    private void openLink(int linkResId) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(linkResId))));
    }

    private void show3dsAlertDialog(@NonNull String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("R.string.token_cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }

    private void open3dsConfirmation() {
        if (URLUtil.isHttpsUrl(url3ds) || URLUtil.isAssetUrl(url3ds)) {
            Intent intent = Checkout.create3dsIntent(this, url3ds);
            startActivityForResult(intent, REQUEST_CODE_3DS);
        } else {
            Toast.makeText(this, "R.string.error_wrong_url", Toast.LENGTH_SHORT).show();
        }
    }

    private int shouldShow3dsContainer() {
        return BuildConfig.DEBUG ? View.VISIBLE : View.GONE;
    }
}