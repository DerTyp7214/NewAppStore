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

package com.dertyp7214.appstore.components

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.google.android.material.snackbar.Snackbar

class CustomSnackbar {

    private var snackbar: Snackbar? = null
    private var themeStore: ThemeStore? = null
    private var color = 0

    constructor(context: Context) {
        themeStore = ThemeStore.getInstance(context)
    }

    constructor(context: Context, @ColorInt color: Int) {
        themeStore = ThemeStore.getInstance(context)
        this.color = color
    }

    fun make(view: View, text: CharSequence, duration: Int): CustomSnackbar {
        var backgroundColor = themeStore!!.primaryColor
        var textColor = themeStore!!.primaryTextColor
        if (color != 0)
            backgroundColor = color
        if (Utils.isColorBright(backgroundColor))
            if (Utils.isColorBright(textColor))
                textColor = Color.BLACK
        if (!Utils.isColorBright(backgroundColor))
            if (!Utils.isColorBright(textColor))
                textColor = Color.WHITE
        snackbar = Snackbar.make(view, text, duration)
        val snackbarView = snackbar!!.view
        snackbarView.setBackgroundColor(backgroundColor)
        snackbar!!.setActionTextColor(textColor)
        return this
    }

    fun setAction(text: String, listener: View.OnClickListener): CustomSnackbar {
        snackbar!!.setAction(text, listener)
        return this
    }

    fun setCallBack(callBack: Snackbar.Callback): CustomSnackbar {
        snackbar!!.addCallback(callBack)
        return this
    }

    fun show() {
        snackbar!!.show()
    }

    companion object {
        const val LENGTH_LONG = Snackbar.LENGTH_LONG
        const val LENGTH_INDEFINITE = Snackbar.LENGTH_INDEFINITE
        const val LENGTH_SHORT = Snackbar.LENGTH_SHORT
    }
}
