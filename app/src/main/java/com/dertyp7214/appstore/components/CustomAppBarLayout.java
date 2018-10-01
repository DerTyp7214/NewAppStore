/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.appbar.AppBarLayout;

import androidx.annotation.ColorInt;


public class CustomAppBarLayout extends AppBarLayout {

    public CustomAppBarLayout(Context context) {
        super(context);
        setUp();
    }

    public CustomAppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp();
    }

    public void setAppBarBackgroundColor(@ColorInt int color) {
        this.setBackgroundColor(color);
    }

    private void setUp(){

    }
}
