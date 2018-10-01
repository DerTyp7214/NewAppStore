/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.components;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import javax.annotation.Nullable;

public class BlackLine extends View {
    public BlackLine(Context context) {
        super(context);
        init();
    }

    public BlackLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BlackLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BlackLine(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        if (Build.VERSION.SDK_INT >= 28) {
            this.setBackgroundColor(Color.parseColor("#e7e7e7"));
        }
    }
}
