package com.picup.calling.data;

/**
 * Created by frank.truong on 3/22/2017.
 */

public class AuthenticateParam {
    private String username =  null;
    private String password = null;

    public AuthenticateParam(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
