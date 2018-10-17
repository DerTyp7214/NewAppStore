/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.receivers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.dertyp7214.appstore.dev.Logs
import com.dertyp7214.appstore.fragments.FragmentAppGroups
import com.dertyp7214.appstore.screens.AppScreen
import java.util.*

class PackageUpdateReceiver : BroadcastReceiver() {

    private var logs: Logs? = null

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = Objects.requireNonNull<Uri>(intent.data).encodedSchemeSpecificPart
        if (activity != null)
            logs = Logs.getInstance(activity!!)
        log("info", "INTENT ACTION", intent.action)
        if (finished && context.getSharedPreferences("json", Context.MODE_PRIVATE)
                        .getString("json", "{\"error\": true}")!!.contains(packageName)) {
            finished = false
            log("info", "PACKAGES", packageName)
            if (FragmentAppGroups.hasInstance()) {
                val appGroups = FragmentAppGroups.getInstance()
                appGroups.refreshLayout.isRefreshing = true
                appGroups.getAppList(appGroups.refreshLayout, true)
            }
            if (AppScreen.hasInstance()) {
                val appScreen = AppScreen.instance
                appScreen!!.setUpButtons()
            }
            finished = true
        }
    }

    private fun log(type: String, title: String, content: Any?) {
        if (logs != null)
            when (type) {
                "info" -> logs!!.info(title, content!!)
                "error" -> logs!!.error(title, content!!)
            }
    }

    companion object {
        private var finished = true

        @SuppressLint("StaticFieldLeak")
        var activity: Activity? = null
    }
}
