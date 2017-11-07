package com.picup.calling;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.picup.calling.base.PicupActivity;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.util.Logger;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.picup.calling.R;

public class SplashActivity extends PicupActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;
    private static final int PERMISSION_REQUEST_ALL = 2;
    private static boolean phoneNumberConfirming = false;
    private final int requestCodeTandC = 1001;

    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("SplashActivity - onCreate");
        setContentView(R.layout.splash_new);

        prefs = getSharedPreferences(PicupApplication.appId, Context.MODE_PRIVATE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String tokenId = PicupApplication.getToken();
                if (TextUtils.isEmpty(tokenId)) {
                    Logger.log("SplashActivity - onCreate - invalid token");
                    Intent signinIntent = new Intent(SplashActivity.this, SignOnActivity.class);
                    SplashActivity.this.startActivity(signinIntent);
                    SplashActivity.this.finish();
                } else {
                    Logger.log("SplashActivity - onCreate - valid token");
                    //even if have valid token, may not have done t&c OR may not have done number confirm...
                    String confirmedPhoneNumber = prefs.getString("confirmedPhoneNumber", null);
                    if (TextUtils.isEmpty(confirmedPhoneNumber)) {
                        if (ContextCompat.checkSelfPermission(SplashActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            phoneNumberConfirming = true;
                            ActivityCompat.requestPermissions(SplashActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
                        } else {
                            //Intent numberConfirmIntent = new Intent(SignOnActivity.this, NumberConfirmActivity.class);
                            //numberConfirmIntent.putExtra("inputUserId", usernameEditText.getText().toString());
                            //startActivity(numberConfirmIntent);
                            //finish();
                            startNumberConfirming();
                            return;
                        }
                    } else {
                        phoneNumberConfirming = false;
                    }
                    if (!phoneNumberConfirming) {
                        startMain();
                    }
                }

            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        //decorView.setOnSystemUiVisibilityChangeListener(SystemUiVisibilityChangeListener.getInstance(decorView));
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.log("SplashActivity - onActivityResult - requestCode:"+requestCode+" resultCode:"+resultCode);
        if (requestCode == requestCodeTandC) {
            if (resultCode == 0) {
                if (this != null && !this.isFinishing()) {
                    startNumberConfirming();
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_PHONE_STATE:
                //if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (phoneNumberConfirming) {
                    //Intent numberConfirmIntent = new Intent(SignOnActivity.this, NumberConfirmActivity.class);
                    //numberConfirmIntent.putExtra("inputUserId", usernameEditText.getText().toString());
                    //startActivity(numberConfirmIntent);
                    //finish();
                    startNumberConfirming();
                }
                //}
                break;
            case PERMISSION_REQUEST_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Intent mainIntent = new Intent(SignOnActivity.this, MainActivity.class);
                    //startActivity(mainIntent);
                    //finish();
                    startMain();
                }
                break;
            default:
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void startTandC() {
        Intent tandcIntent = new Intent(SplashActivity.this, TermsActivity.class);
        tandcIntent.putExtra("acceptTC", true   );
        startActivityForResult(tandcIntent,requestCodeTandC);
        //finish();
    }

    private void startNumberConfirming() {
        if (prefs != null) {
            if (!prefs.getBoolean("tandcAccept", false)) {
                startTandC();
                return;
            }
        }
        Intent numberConfirmIntent = new Intent(SplashActivity.this, NumberConfirmActivity.class);
        numberConfirmIntent.putExtra("inputUserId", PicupApplication.getEmailAddress());
        startActivity(numberConfirmIntent);
        finish();
    }

    private void startMain() {
        if (prefs != null) {
            if (!prefs.getBoolean("tandcAccept", false)) {
                startTandC();
                return;
            }
        }
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
