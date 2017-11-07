package com.picup.calling.base;

import com.picup.calling.network.AuthenticateInfo;
import android.os.AsyncTask;

import retrofit2.Callback;

/**
 * Created by frank.truong on 3/22/2017.
 */

public class AuthenticateTask extends AsyncTask<String, String, AuthenticateInfo> {
    private Callback<AuthenticateInfo> callback = null;

    public AuthenticateTask(Callback<AuthenticateInfo> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected AuthenticateInfo doInBackground(String... credentials) {
/*
        PicupService authenticateService = PicupService.retrofit.create(PicupService.class);
        Call<AuthenticateInfo> call = authenticateService.authenticate(credentials[0], credentials[1]);
        call.enqueue(callback);
*/

        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(AuthenticateInfo authenticateInfo) {
        super.onPostExecute(authenticateInfo);
    }

}
