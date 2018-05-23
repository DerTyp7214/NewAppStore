/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.helpers;

import android.support.annotation.ColorInt;

public class ColorStyle {

    @ColorInt
    private final int primaryColor, primaryColorDark, accentColor;

    public ColorStyle(@ColorInt int primaryColor, @ColorInt int primaryColorDark, @ColorInt int accentColor){
        this.primaryColor=primaryColor;
        this.primaryColorDark=primaryColorDark;
        this.accentColor=accentColor;
    }

    @ColorInt
    public int getPrimaryColor(){
        return primaryColor;
    }


    @ColorInt
    public int getPrimaryColorDark(){
        return primaryColorDark;
    }


    @ColorInt
    public int getAccentColor(){
        return accentColor;
    }

}
