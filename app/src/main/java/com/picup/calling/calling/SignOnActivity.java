package com.picup.calling;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.picup.calling.base.PicupActivity;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.dialog.AuthenticateDialogFragment;
import com.picup.calling.dialog.ErrorDialogFragment;
import com.picup.calling.network.AuthenticateInfo;
import com.picup.calling.network.PicupService;
import com.picup.calling.network.Token;
import com.picup.calling.util.Logger;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableMap;
import com.picup.calling.R;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignOnActivity extends PicupActivity implements TextWatcher, View.OnClickListener, View.OnFocusChangeListener, ErrorDialogFragment.OnErrorDialogFragmentListener {
    private static final String TAG = SignOnActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 1;
    private static final int PERMISSION_REQUEST_ALL = 2;

    private TextView errorMessageTextView = null;
    private ImageView usernameImageView = null;
    private EditText usernameEditText = null;
    private ImageView clearUsernameImageView = null;
    private ImageView passwordImageView = null;
    private EditText passwordEditText = null;
    private ImageView clearPasswordImageView = null;
    private TextView forgotPasswordTextView = null;
    private Button loginButton = null;
    private Button facebookLoginButton = null;
    private Button linkedinLoginButton = null;
    private Button signupButton = null;

    private static CountDownTimer countDownTimer = null;
    private DialogFragment authenticateDialogFragment = null;

    private static SharedPreferences prefs = null;
    private static boolean phoneNumberConfirming = false;
    private final int requestCodeTandC = 1001;

/*
    private static final int UI_OPTIONS = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                          View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("SignOnActivity - onCreate");
        setContentView(R.layout.login);

        errorMessageTextView = (TextView)findViewById(R.id.error_message_textview);
        errorMessageTextView.setVisibility(View.INVISIBLE);

        usernameImageView = (ImageView)findViewById(R.id.username_icon_imageview);
        usernameEditText = (EditText)findViewById(R.id.username_edittext);
        usernameEditText.addTextChangedListener(this);
        usernameEditText.setOnFocusChangeListener(this);
        clearUsernameImageView = (ImageView)findViewById(R.id.clear_username_imageview);
        clearUsernameImageView.setOnClickListener(this);

        passwordImageView = (ImageView)findViewById(R.id.password_icon_imageview);
        passwordEditText = (EditText)findViewById(R.id.password_edittext);
        passwordEditText.addTextChangedListener(this);
        passwordEditText.setOnFocusChangeListener(this);
        clearPasswordImageView = (ImageView)findViewById(R.id.clear_password_imageview);
        clearPasswordImageView.setOnClickListener(this);

        forgotPasswordTextView = (TextView)findViewById(R.id.forgot_password_textview);
        forgotPasswordTextView.setOnClickListener(this);

        loginButton = (Button)findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        facebookLoginButton = (Button)findViewById(R.id.facebook_login_button);
        facebookLoginButton.setOnClickListener(this);
        linkedinLoginButton = (Button)findViewById(R.id.linkedin_login_button);
        linkedinLoginButton.setOnClickListener(this);

        signupButton = (Button)findViewById(R.id.signup_button);
        signupButton.setOnClickListener(this);

        prefs = getSharedPreferences(PicupApplication.appId, Context.MODE_PRIVATE);

/*
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == ic_key_0) {
                   decorView.setSystemUiVisibility(UI_OPTIONS);
                }
            }
        });
*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        clearUsernameImageView.setVisibility(usernameEditText.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
        clearPasswordImageView.setVisibility(passwordEditText.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
        String emailAddress = prefs.getString("emailAddress", "");
        if (!TextUtils.isEmpty(emailAddress)) {
            usernameEditText.setText(emailAddress);
            passwordEditText.requestFocus();
        } else {
            usernameEditText.requestFocus();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.log("SignOnActivity - onActivityResult - requestCode:"+requestCode+" resultCode:"+resultCode);
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
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        //Log.d(TAG, editable.toString());
        View focusedView = getWindow().getCurrentFocus();
        // user is editing username/password fields. Reset UI widgets
        errorMessageTextView.setVisibility(View.INVISIBLE);
        if (focusedView != null) {
            if (focusedView.getId() == usernameEditText.getId()) {
                //usernameImageView.setImageResource(R.drawable.contacts24dp_white);
                boolean hasText = usernameEditText.getText().length() > 0;
                if (hasText) {
                    if (errorMessageTextView.getVisibility() == View.VISIBLE) {
                        errorMessageTextView.setVisibility(View.GONE);
                    }
                }
                clearUsernameImageView.setVisibility(hasText ? View.VISIBLE : View.INVISIBLE);

            } else if (focusedView.getId() == passwordEditText.getId()) {
                //passwordImageView.setImageResource(R.drawable.passwordwhite24dp);
                boolean hasText = passwordEditText.getText().length() > 0;
                if (hasText) {
                    if (errorMessageTextView.getVisibility() == View.VISIBLE) {
                        errorMessageTextView.setVisibility(View.GONE);
                    }
                }
                clearPasswordImageView.setVisibility(passwordEditText.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
            }
        }
        if (usernameEditText.getText().length() > 0 && passwordEditText.getText().length() > 0) {
            loginButton.setBackgroundResource(R.drawable.brighter_purple_button);
            loginButton.setAlpha(1.0f);
        } else {
            loginButton.setBackgroundResource(R.drawable.lightergray_transparent_button);
            loginButton.setAlpha(0.5f);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //getWindow().getDecorView().setSystemUiVisibility(UI_OPTIONS);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == clearUsernameImageView.getId()) {
            usernameEditText.setText("");
            if (!usernameEditText.requestFocus()) {
                Logger.log("SignOnActivity - onClick - failed to gain focus for " + usernameEditText.toString());
            }
        } else if (view.getId() == clearPasswordImageView.getId()) {
            passwordEditText.setText("");
            if (!passwordEditText.requestFocus()) {
                Logger.log("SignOnActivity - onClick - failed to gain focus for " + passwordEditText.toString());
            }
        } else if (view.getId() == forgotPasswordTextView.getId()) {
            //Intent forgotPasswordIntent = new Intent(this, ForgotPasswordActivity.class);
            //startActivity(forgotPasswordIntent);
            Intent settingsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.picup.com/account/forgotpassword"));
            if (settingsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(settingsIntent);
            }
        } else if (view.getId() == facebookLoginButton.getId()) {
            Intent facebookLoginIntent = new Intent(this, FacebookLoginActivity.class);
            startActivity(facebookLoginIntent);
        } else if (view.getId() == linkedinLoginButton.getId()) {
            Intent linkedinLoginIntent = new Intent(this, LinkedInLoginActivity.class);
            startActivity(linkedinLoginIntent);
        } else if (view.getId() == signupButton.getId()) {
            Intent settingsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.picup.com/account/choosenumber"));
            if (settingsIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(settingsIntent);
            }
        } else if (view.getId() == loginButton.getId()) {
            login();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == usernameEditText.getId()) {
            if (hasFocus) {
                clearUsernameImageView.setVisibility(usernameEditText.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
            } else {
                // hire 'usernameImageView' when lost focus
                clearUsernameImageView.setVisibility(View.INVISIBLE);
            }
        } else if (v.getId() == passwordEditText.getId()) {
            if (hasFocus) {
                clearPasswordImageView.setVisibility(passwordEditText.getText().length() > 0 ? View.VISIBLE : View.INVISIBLE);
            } else {
                // hire 'passwordImageView' when lost focus
                clearPasswordImageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void login() {
        Logger.log("SignOnActivity - login - username:"+usernameEditText.getText()+ " password:"+passwordEditText.getText());

        if (TextUtils.isEmpty(usernameEditText.getText()) && TextUtils.isEmpty(passwordEditText.getText())) {
            errorMessageTextView.setVisibility(View.VISIBLE);
            return;
        }
        errorMessageTextView.setVisibility(View.INVISIBLE);
        usernameImageView.setBackgroundResource(R.drawable.contacts24dp_white);
        passwordImageView.setBackgroundResource(R.drawable.passwordwhite24dp);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(passwordEditText.getWindowToken(), 0);
        authenticateDialogFragment = AuthenticateDialogFragment.newInstance();
        authenticateDialogFragment.show(getSupportFragmentManager(), "CupFillingTag");

        PicupService picupService = PicupService.retrofit.create(PicupService.class);
        ImmutableMap<String, String> authenticateParams2 = ImmutableMap.of("emailAddress", usernameEditText.getText().toString(),
                                                                           "password", passwordEditText.getText().toString(),
                                                                           "applicationId", PicupApplication.apiId);
        //final Call<AuthenticateInfo> call = picupService.authenticate(usernameEditText.getText().toString(), passwordEditText.getText().toString(), "MobileAPI");
        final Call<AuthenticateInfo> call = picupService.authenticate(authenticateParams2);
        call.enqueue(new Callback<AuthenticateInfo>() {
            @Override
            public void onResponse(Call<AuthenticateInfo> call, Response<AuthenticateInfo> response) {
                authenticateDialogFragment.dismiss();
                countDownTimer.cancel();
                if (response.isSuccessful()) {
                    Logger.log("SignOnActivity - login - onResponse - isSuccessful");
                    PicupApplication.setEmailAddress(usernameEditText.getText().toString());
                    PicupApplication.setPassword(passwordEditText.getText().toString());
                    String emailAddress = prefs.getString("emailAddress", null);
                    if (!TextUtils.equals(emailAddress, usernameEditText.getText().toString())) {
                        prefs.edit().putString("emailAddress", usernameEditText.getText().toString()).apply();
                    }
                    PicupApplication.authenticateInfo = response.body();
                    if (PicupApplication.authenticateInfo != null) {
                        Token token = PicupApplication.authenticateInfo.getToken();
                        if (token != null) {
                            String refreshTokenId = token.getRefreshId();
                            if (!TextUtils.isEmpty(refreshTokenId)) {
                                prefs.edit().putString("refreshToken", refreshTokenId).apply();
                            }
                        }
                    }
                    String confirmedPhoneNumber = prefs.getString("confirmedPhoneNumber", null);
                    if (!TextUtils.equals(emailAddress, usernameEditText.getText().toString()) || TextUtils.isEmpty(confirmedPhoneNumber)) {
                        if (ContextCompat.checkSelfPermission(SignOnActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            phoneNumberConfirming = true;
                            ActivityCompat.requestPermissions(SignOnActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
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
                        //Intent mainIntent = new Intent(SignOnActivity.this, MainActivity.class);
                        //startActivity(mainIntent);
                        //finish();
                        startMain();
                    }
                } else {
                    try {
                        Logger.log("SignOnActivity - login - request authenticate - onResponse - failure error:" + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    errorMessageTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<AuthenticateInfo> call, Throwable t) {
                //network failure
                authenticateDialogFragment.dismiss();
                //errorMessageTextView.setVisibility(View.VISIBLE);
                ErrorDialogFragment errorDialogFragment = ErrorDialogFragment.newInstance(1);
                errorDialogFragment.show(getSupportFragmentManager(), "errorDialogFragment");
                //Toast.makeText(SignOnActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                Logger.log("SignOnActivity - login - onFailure - "+t.getMessage());
            }
        });
        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Logger.log("SignOnActivity - login - onTick - Getting anything from backend yet?");
            }

            @Override
            public void onFinish() {
                if (!call.isExecuted()) {
                    call.cancel();
                }
                if (authenticateDialogFragment.isVisible()) {
                    authenticateDialogFragment.dismiss();
                }
            }
        }.start();
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

    @Override
    public void reAuthenticate() {
        Logger.log("SignOnActivity - reAuthenticate");
        //stay here...since this is the reauthenticate behavior anyway...
    }

    @Override
    public void appSettings() {
        Logger.log("SignOnActivity - appSettings");
        Intent settingsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.picup.com/account/"));
        if (settingsIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(settingsIntent);
        }
    }

    private void startTandC() {
        Logger.log("SignOnActivity - startTandC");
        Intent tandcIntent = new Intent(SignOnActivity.this, TermsActivity.class);
        tandcIntent.putExtra("acceptTC", true   );
        startActivityForResult(tandcIntent,requestCodeTandC);
        //finish();
    }

    private void startNumberConfirming() {
        boolean tAndc = false;
        if (prefs != null) {
            tAndc = prefs.getBoolean("tandcAccept", false);
        }
        Logger.log("SignOnActivity - startNumberConfirming - tAndc:"+tAndc);
        if (!prefs.getBoolean("tandcAccept", false)) {
            startTandC();
            return;
        }

        Intent numberConfirmIntent = new Intent(SignOnActivity.this, NumberConfirmActivity.class);
        numberConfirmIntent.putExtra("inputUserId", usernameEditText.getText().toString());
        startActivity(numberConfirmIntent);
        finish();
    }

    private void startMain() {
        boolean tAndc = false;
        if (prefs != null) {
            tAndc = prefs.getBoolean("tandcAccept", false);
        }
        Logger.log("SignOnActivity - startMain - tAndc:"+tAndc);
        if (!prefs.getBoolean("tandcAccept", false)) {
            startTandC();
            return;
        }

        Intent mainIntent = new Intent(SignOnActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
