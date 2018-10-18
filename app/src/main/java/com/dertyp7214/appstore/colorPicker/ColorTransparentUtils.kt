/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.colorPicker

import android.util.Log

import com.dertyp7214.appstore.R

object ColorTransparentUtils {

    private const val defaultColor = R.color.colorAccent
    private const val TAG = "ColorTransparentUtils"

    private fun convert(trans: Int): String {
        val hexString = Integer.toHexString(Math.round((255 * trans / 100).toFloat()))
        return (if (hexString.length < 2) "0" else "") + hexString
    }

    fun transparentColor10(colorCode: Int): String {
        return convertIntoColor(colorCode, 10)
    }

    fun transparentColor20(colorCode: Int): String {
        return convertIntoColor(colorCode, 20)
    }

    fun transparentColor30(colorCode: Int): String {
        return convertIntoColor(colorCode, 30)
    }

    fun transparentColor40(colorCode: Int): String {
        return convertIntoColor(colorCode, 40)
    }

    fun transparentColor50(colorCode: Int): String {
        return convertIntoColor(colorCode, 50)
    }

    fun transparentColor60(colorCode: Int): String {
        return convertIntoColor(colorCode, 60)
    }

    fun transparentColor70(colorCode: Int): String {
        return convertIntoColor(colorCode, 70)
    }

    fun transparentColor80(colorCode: Int): String {
        return convertIntoColor(colorCode, 80)
    }

    fun transparentColor90(colorCode: Int): String {
        return convertIntoColor(colorCode, 90)
    }

    fun transparentColor100(colorCode: Int): String {
        return convertIntoColor(colorCode, 100)
    }

    private fun convertIntoColor(colorCode: Int, transCode: Int): String {
        val color = Integer.toHexString(colorCode).toUpperCase().substring(2)
        if (!color.isEmpty() && transCode > 100) {
            return if (color.trim { it <= ' ' }.length == 6) {
                "#" + convert(transCode) + color
            } else {
                Log.d(TAG, "Color is already with transparency")
                convert(transCode) + color
            }
        }
        return "#" + Integer.toHexString(defaultColor).toUpperCase().substring(2)
    }
}