/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.dev

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.dertyp7214.appstore.Utils
import com.shashank.sony.fancytoastlib.FancyToast
import java.util.*

class Logs(private val context: Activity) {

    fun info(title: String, vararg content: Any) {
        Log.i(title, Arrays.toString(content))
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread {
                FancyToast
                        .makeText(context, title + ":  " + Arrays.toString(content),
                                FancyToast.LENGTH_LONG, FancyToast.INFO, false)
                        .show()
            }
    }

    fun warn(title: String, vararg content: Any) {
        Log.w(title, Arrays.toString(content))
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread {
                FancyToast
                        .makeText(context, title + ":  " + Arrays.toString(content),
                                FancyToast.LENGTH_LONG, FancyToast.WARNING, false)
                        .show()
            }
    }

    fun error(title: String, vararg content: Any) {
        Log.e(title, Arrays.toString(content))
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread {
                FancyToast
                        .makeText(context, title + ":  " + Arrays.toString(content),
                                FancyToast.LENGTH_LONG, FancyToast.ERROR, false)
                        .show()
            }
    }

    fun debug(title: String, vararg content: Any) {
        Log.d(title, Arrays.toString(content))
        if (Utils.getSettings(context).getBoolean("dev_mode", false))
            context.runOnUiThread {
                FancyToast
                        .makeText(context, title + ":  " + Arrays.toString(content),
                                FancyToast.LENGTH_LONG, FancyToast.CONFUSING, false)
                        .show()
            }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: Logs? = null
        fun getInstance(context: Activity): Logs {
            if (instance == null)
                instance = Logs(context)
            return instance as Logs
        }
    }
}
