/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class DownloadService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
