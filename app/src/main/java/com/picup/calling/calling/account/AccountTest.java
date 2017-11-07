package com.picup.calling.account;

import android.accounts.AccountManager;
import android.content.Context;

/**
 * Created by frank.truong on 3/24/2017.
 */

public class AccountTest {
    private static Context context = null;

    public AccountTest(Context context) {

    }

    public void testGetAccount() {
        AccountManager am = AccountManager.get(context);

    }

}


