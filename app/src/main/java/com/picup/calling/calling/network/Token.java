package com.picup.calling.network;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by frank.truong on 3/22/2017.
 */

public class Token {
    @SerializedName("token")
    private String id = null;
    @SerializedName("expiryInSeconds")
    private int lifeInSeconds = 0;
    @SerializedName("refresh_token")
    private String refreshId = null;
    @SerializedName("expiry")
    private Calendar expireOn = null;


/*
    public Token(String id, int lifeInSeconds, String refreshId, Calendar expireOn) {
        this.id = id;
        this.lifeInSeconds = lifeInSeconds;
        this.refreshId = refreshId;
        this.expireOn = expireOn;
    }
*/

    public boolean isExpired() {
        boolean expired = true;

        if (expireOn != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -5); // token will be considered expired if its time-to-live is less than 5 minutes.
            expired = calendar.after(expireOn);
        }
        return expired;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLifeInSeconds() {
        return lifeInSeconds;
    }

    public void setLifeInSeconds(int lifeInSeconds) {
        this.lifeInSeconds = lifeInSeconds;
    }

    public String getRefreshId() {
        return refreshId;
    }

    public void setRefreshId(String refreshId) {
        this.refreshId = refreshId;
    }

    public Calendar getExpireOn() {
        return expireOn;
    }

    public void setExpireOn(Calendar expireOn) {
        this.expireOn = expireOn;
    }
}
