/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.Utils.Companion.manipulateColor
import com.dertyp7214.appstore.helpers.SQLiteHandler
import com.dertyp7214.appstore.helpers.SessionManager
import com.dertyp7214.themeablecomponents.colorpicker.ColorUtil.calculateColor
import com.gw.swipeback.SwipeBackLayout
import com.gw.swipeback.WxSwipeBackLayout
import com.gw.swipeback.tools.Util

@Suppress("DEPRECATION")
class LogOut : Activity() {

    private var txtName: TextView? = null
    private var txtEmail: TextView? = null
    private var btnLogout: Button? = null

    private var db: SQLiteHandler? = null
    private var session: SessionManager? = null

    private var statusColor = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
        val themeStore = ThemeStore.getInstance(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT < 28)
                window.navigationBarColor = resources.getColor(R.color.bg_loggedin)
            else {
                window.navigationBarColor = Color.WHITE
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.statusBarColor = resources.getColor(R.color.bg_loggedin)
        }

        val wxSwipeBackLayout = WxSwipeBackLayout(this)
        wxSwipeBackLayout.directionMode = SwipeBackLayout.FROM_LEFT
        wxSwipeBackLayout.attachToActivity(this)
        wxSwipeBackLayout.setSwipeBackListener(object : SwipeBackLayout.OnSwipeBackListener {
            override fun onViewPositionChanged(mView: View, swipeBackFraction: Float, swipeBackFactor: Float) {
                wxSwipeBackLayout.invalidate()
                Util.onPanelSlide(swipeBackFraction)
                if (statusColor == -1) statusColor = Utils.getStatusBarColor(this@LogOut)
                try {
                    Utils.setStatusBarColor(this@LogOut,
                            calculateColor(statusColor, manipulateColor(MainActivity.color, 0.6f),
                                    100, (swipeBackFraction * 100).toInt()))
                } catch (ignored: Exception) {
                }

            }

            override fun onViewSwipeFinished(mView: View, isEnd: Boolean) {
                if (isEnd) {
                    Utils.setStatusBarColor(this@LogOut, Color.TRANSPARENT)
                    wxSwipeBackLayout.finish()
                }
                Util.onPanelReset()
            }
        })

        txtName = findViewById(R.id.name)
        txtEmail = findViewById(R.id.email)
        btnLogout = findViewById(R.id.btnLogout)

        db = SQLiteHandler(applicationContext)
        session = SessionManager(applicationContext)

        if (!session!!.isLoggedIn) {
            logoutUser()
        }

        val user = db!!.userDetails

        val name = user["name"]
        val email = user["email"]

        txtName!!.text = name
        txtEmail!!.text = email

        btnLogout!!.setOnClickListener { logoutUser() }
    }

    private fun logoutUser() {
        session!!.setLogin(false)

        db!!.deleteUsers()

        val intent = Intent(this@LogOut, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}