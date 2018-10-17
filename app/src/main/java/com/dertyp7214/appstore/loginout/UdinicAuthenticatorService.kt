/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.loginout

import android.app.Service
import android.content.Intent
import android.os.IBinder

class UdinicAuthenticatorService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        val authenticator = AccountAuthenticator(applicationContext)
        return authenticator.iBinder
    }
}