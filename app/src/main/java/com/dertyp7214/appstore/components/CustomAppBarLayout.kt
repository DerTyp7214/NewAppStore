/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import com.google.android.material.appbar.AppBarLayout


class CustomAppBarLayout : AppBarLayout {

    constructor(context: Context) : super(context) {
        setUp()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setUp()
    }

    fun setAppBarBackgroundColor(@ColorInt color: Int) {
        this.setBackgroundColor(color)
    }

    private fun setUp() {
    }
}
