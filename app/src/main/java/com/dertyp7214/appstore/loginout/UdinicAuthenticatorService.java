/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.loginout;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UdinicAuthenticatorService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        AccountAuthenticator authenticator = new AccountAuthenticator(getApplicationContext());
        return authenticator.getIBinder();
    }
}