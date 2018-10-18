/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.R
import com.dertyp7214.themeablecomponents.colorpicker.ColorPicker

class SettingsColor : Settings {

    internal var colorInt: Int = 0
    private var colorString: String? = null
    private var isString: Boolean = false
    private var onClickListener: settingsOnClickListener? = null

    constructor(name: String, text: String, context: Context, color: Int) : super(name, text, context) {
        this.colorInt = color
        this.isString = false
        loadSetting()
    }

    constructor(name: String, text: String, context: Context, color: String) : super(name, text, context) {
        this.colorString = color
        this.isString = true
        loadSetting()
    }

    fun addSettingsOnClick(onClickListener: SettingsColor.settingsOnClickListener): SettingsColor {
        this.onClickListener = onClickListener
        return this
    }

    fun getColorString(): String? {
        return if (isString) colorString else String.format("#%06X", colorInt)
    }

    fun getColorInt(): Int {
        return if (isString) Color.parseColor(colorString) else colorInt
    }

    fun onClick(colorPlate: View) {
        if (onClickListener != null) {
            val colorPicker = ColorPicker(context)
            colorPicker.setMinMaxBrighness(0.45f, 1f)
            colorPicker.setListener(object : ColorPicker.Listener {
                override fun color(i: Int) {

                    if (isString)
                        colorString = String.format("#%06X", i)
                    else
                        colorInt = i

                    val bgDrawable = colorPlate.background as LayerDrawable
                    val shape = bgDrawable.findDrawableByLayerId(R.id.plate_color) as GradientDrawable
                    shape.setColor(if (isString) Color.parseColor(colorString) else colorInt)

                    colorPicker.cancel()

                    onClickListener!!
                            .onClick(name, if (isString) Color.parseColor(colorString) else colorInt,
                                    this@SettingsColor)

                }

                override fun update(i: Int) {
                }

                override fun cancel() {
                    colorPicker.cancel()
                }
            })
            colorPicker.setAnimationTime(300)
            if (isString)
                colorPicker.setColor(colorString)
            else
                colorPicker.setColor(colorInt)
            colorPicker.show()
        }
    }

    override fun saveSetting() {
        val preferences = context.getSharedPreferences("colors_" + Config.UID(context), Context.MODE_PRIVATE)
        @SuppressLint("CommitPrefEdits")
        val editor = preferences.edit()
        editor.putInt(name, getColorInt())
        editor.apply()
    }

    override fun loadSetting() {
        val preferences = context.getSharedPreferences("colors_" + Config.UID(context), Context.MODE_PRIVATE)
        this.colorInt = if (isString)
            Color.parseColor(preferences.getString(name, colorString))
        else
            preferences
                    .getInt(name, colorInt)
    }

    interface settingsOnClickListener {
        fun onClick(name: String, Color: Int, settingsColor: SettingsColor)
    }
}
