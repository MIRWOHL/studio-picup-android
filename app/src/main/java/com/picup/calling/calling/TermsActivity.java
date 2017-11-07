package com.picup.calling;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import com.picup.calling.base.PicupApplication;
import com.picup.calling.util.Logger;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.picup.calling.R;

public class TermsActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView backImageView = null;
    private Button acceptButton = null;
    private TextView titleText = null;
    private RelativeLayout bottomLayout = null;
    private RelativeLayout northLayout = null;
    private WebView disclaimer;
    private SharedPreferences prefs = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_app);

        backImageView = (ImageView)findViewById(R.id.back_imageview);
        backImageView.setOnClickListener(this);

        acceptButton = (Button)findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(this);

        northLayout = (RelativeLayout)findViewById(R.id.profile_north_layout);
        bottomLayout = (RelativeLayout)findViewById(R.id.bottom_layout);
        titleText = (TextView)findViewById(R.id.title_textview);

        prefs = getSharedPreferences(PicupApplication.appId, Context.MODE_PRIVATE);

        if (getIntent().hasExtra("acceptTC")) {
            bottomLayout.setVisibility(View.VISIBLE);
            //titleText.setText("");
            northLayout.setVisibility(View.INVISIBLE);
        } else {
            bottomLayout.setVisibility(View.GONE);
        }

        disclaimer = (WebView) findViewById(R.id.generic_web_view);
        if (disclaimer != null) {
            disclaimer.getSettings().setJavaScriptEnabled(true);
            disclaimer.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    Logger.log("TermsActivity - onPageFinished");
                }

                @Override
                public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                    Logger.log("TermsActivity - onReceivedHttpError - " + errorResponse.toString());

                    if (view.canGoBack()) {
                        view.goBack();
                    }
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError errorResponse) {
                    Logger.log("TermsActivity - onReceivedError - " + errorResponse.toString());

                    if (view.canGoBack()) {
                        view.goBack();
                    }
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    Logger.log("TermsActivity - onReceivedSslError - "+ error.toString());

                    //for testing just proceed but may need to remove before production
                    //handler.proceed();

                    if (isFinishing())
                        handler.cancel();

                    try {
                        //Warn certificate issue
                        final SslErrorHandler finalHandler = handler;
                        final AlertDialog.Builder builder = new AlertDialog.Builder(TermsActivity.this);
                        builder.setMessage(R.string.notification_error_ssl_cert_invalid);
                        builder.setPositiveButton(getResources().getString(R.string.ok_cap_label), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finalHandler.proceed();
                            }
                        });
                        builder.setNegativeButton(getResources().getString(R.string.cancel_cap_label), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finalHandler.cancel();
                            }
                        });
                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    } catch (Exception e) { handler.cancel(); }

                }
            });

            disclaimer.loadUrl("https://www.picup.com/home/mobileappterms");
        }

    }

    private void loadWebView(String tcUrl) {
        Logger.log("TermsActivity - loadWebView");
        if (disclaimer != null && !isFinishing()) {
            disclaimer.loadUrl(tcUrl);
        }
    }




    @Override
    public void onClick(View v) {
        Logger.log("TermsActivity - onClick");
        if (v.getId() == backImageView.getId()) {
            setResult(-1);
            super.finish();
        } else if (v.getId() == acceptButton.getId()) {
            Logger.log("TermsActivity - onClick - accept");
            //store in shared pref and do activity result stuff...
            prefs.edit().putBoolean("tandcAccept", true).apply();
            setResult(0);
            super.finish();
        }
    }


}
