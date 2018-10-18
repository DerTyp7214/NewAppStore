/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.dertyp7214.appstore.receivers.PackageUpdateReceiver
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.gw.swipeback.tools.WxSwipeBackActivityManager
import shortbread.Shortbread

class AppController : Application() {
    private var mRequestQueue: RequestQueue? = null
    private var themeManager: ThemeManager? = null

    private val requestQueue: RequestQueue
        get() {
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(applicationContext)
            }
            return mRequestQueue!!
        }

    override fun onCreate() {
        super.onCreate()
        Shortbread.create(this)
        WxSwipeBackActivityManager.getInstance().init(this)
        instance = this

        registerActivityLifecycleCallbacks(object : ActivityLifecycleAdapter() {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR
            }
        })

        themeManager = ThemeManager.getInstance(this)

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addDataScheme("package")
        registerReceiver(PackageUpdateReceiver(), intentFilter)
    }

    fun <T> addToRequestQueue(req: Request<T>, tag: String) {
        req.tag = if (TextUtils.isEmpty(tag)) TAG else tag
        requestQueue.add(req)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        req.tag = TAG
        requestQueue.add(req)
    }

    fun cancelPendingRequests(tag: Any) {
        if (mRequestQueue != null) {
            mRequestQueue!!.cancelAll(tag)
        }
    }

    private open class ActivityLifecycleAdapter : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityDestroyed(activity: Activity) {
        }
    }

    companion object {
        @get:Synchronized
        var instance: AppController? = null
            get() {
                return field!!
            }

        val TAG: String = AppController::class.java.simpleName
    }
}