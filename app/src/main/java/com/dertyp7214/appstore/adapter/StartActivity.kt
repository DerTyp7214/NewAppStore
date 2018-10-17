/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.adapter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import com.dertyp7214.appstore.BuildConfig
import com.dertyp7214.appstore.dev.Logs
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class StartActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        val intent = intent

        try {
            if (Objects.requireNonNull(intent.extras).getString("action") == "startDebug") {
                val startDebug = Intent()
                startDebug.setClassName(
                        BuildConfig.APPLICATION_ID + ".debug",
                        BuildConfig.APPLICATION_ID + ".screens.Splashscreen"
                )
                startActivity(startDebug)
            }
        } catch (e: Exception) {
            Logs(this).error("ERROR", e.toString())
        }
    }
}
