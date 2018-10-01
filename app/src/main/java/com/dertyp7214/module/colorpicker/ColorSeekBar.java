package com.dertyp7214.module.colorpicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.appcompat.widget.AppCompatSeekBar;

/**
 * Created by lengw on 20.09.2017.
 */

public class ColorSeekBar extends AppCompatSeekBar {

    public ColorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        ColorStateList black = new ColorStateList(new int[][]{{}}, new int[]{Color.BLACK});

        setThumbTintList(black);
        setProgressTintList(black);
    }

    public void setColor(@ColorInt int c){
        ColorStateList color = new ColorStateList(new int[][]{{}}, new int[]{c});

        setThumbTintList(color);
        setProgressTintList(color);
    }
}
