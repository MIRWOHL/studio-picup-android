package com.picup.calling;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.picup.calling.util.Logger;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.picup.calling.R;

public class AboutAppActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView backImageView = null;
    private ImageButton fbButton = null;
    private ImageButton linkedInButton = null;
    private ImageButton twitterButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);

        backImageView = (ImageView)findViewById(R.id.back_imageview);
        backImageView.setOnClickListener(this);

        TextView versionInfo = (TextView)findViewById(R.id.about_version_text);
        TextView versionCodeInfo = (TextView)findViewById(R.id.about_version_code_text);

        String version = getVersionInfo();
        if (versionInfo != null) {
            versionInfo.setText("PicuP App " + version);
        }
        if (versionCodeInfo != null) {
            versionCodeInfo.setText("Version " + version);
        }

        fbButton = (ImageButton)findViewById(R.id.facebook_link);
        fbButton.setOnClickListener(this);
        linkedInButton = (ImageButton)findViewById(R.id.linkedin_link);
        linkedInButton.setOnClickListener(this);
        twitterButton = (ImageButton)findViewById(R.id.twitter_link);
        twitterButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        Logger.log("AboutAppActivity - onClick");
        if (v.getId() == backImageView.getId()) {
            super.finish();
        } else if (v.getId() == fbButton.getId()) {
            Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/picupcalls"));
            if (linkIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(linkIntent);
            }
        } else if (v.getId() == linkedInButton.getId()) {
            Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/company-beta/7593688/"));
            if (linkIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(linkIntent);
            }
        } else if (v.getId() == twitterButton.getId()) {
            Intent linkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/picupcalls"));
            if (linkIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(linkIntent);
            }
        }
    }

    public String getVersionInfo() {
        String mVersionName = "";
        try {
            String pkg = getPackageName();
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = null;
            if (packageManager != null) {
                packageInfo = packageManager.getPackageInfo(pkg, 0);
            }
            if (packageInfo != null) {
                mVersionName = packageInfo.versionName;
            }
        } catch (Throwable t) {

        }
        return mVersionName;
    }
}
