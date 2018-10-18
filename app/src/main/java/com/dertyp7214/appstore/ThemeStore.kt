/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import com.dertyp7214.appstore.Config.ACTIVE_OVERLAY
import com.dertyp7214.appstore.Utils.Companion.manipulateColor
import java.util.*

@Suppress("DEPRECATION")
class ThemeStore private constructor(private val context: Context) {

    private val sharedPreferences: SharedPreferences

    val primaryColor: Int
        get() = if (ACTIVE_OVERLAY(context))
            context.resources
                    .getColor(R.color.colorPrimary)
        else
            sharedPreferences
                    .getInt(COLOR_PRIMARY, context.resources.getColor(R.color.colorAccent))

    val primaryDarkColor: Int
        get() = if (ACTIVE_OVERLAY(context))
            context.resources
                    .getColor(R.color.colorPrimaryDark)
        else
            manipulateColor(primaryColor, 0.6f)

    val primaryTextColor: Int
        get() = if (Utils.isColorBright(primaryColor)) Color.BLACK else Color.WHITE

    val accentColor: Int
        get() {
            val hsv = FloatArray(3)
            Color.colorToHSV(primaryColor, hsv)
            Log.d("BEFORE", Arrays.toString(hsv))
            hsv[0] -= (if (hsv[0] - 100 < 0) 100 - 360 else 100).toFloat()
            hsv[1] -= 0.03f
            hsv[2] -= 0.13f
            Log.d("AFTER", Arrays.toString(hsv))
            return if (ACTIVE_OVERLAY(context))
                context.resources
                        .getColor(R.color.colorAccent)
            else
                Color.HSVToColor(hsv)
        }

    init {
        instance = this
        this.sharedPreferences = try {
            context.getSharedPreferences("colors_" + Config.UID(context)!!, Context.MODE_PRIVATE)
        } catch (e: Exception) {
            context.getSharedPreferences("colors_splash", Context.MODE_PRIVATE)
        }
    }

    fun getPrimaryHue(degree: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(primaryColor, hsv)
        hsv[0] += (if (hsv[0] + degree > 360) degree - 360 else degree).toFloat()
        return Color.HSVToColor(hsv)
    }

    companion object {

        private const val COLOR_PRIMARY = "color_primary"

        @SuppressLint("StaticFieldLeak")
        private var instance: ThemeStore? = null

        fun resetInstance(context: Context): ThemeStore {
            instance = ThemeStore(context)
            return instance as ThemeStore
        }

        fun getInstance(context: Context): ThemeStore? {
            if (instance == null)
                ThemeStore(context)
            return instance
        }
    }
}
