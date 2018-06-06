/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class CustomSnackbar {

    public static final int LENGTH_LONG = Snackbar.LENGTH_LONG;
    public static final int LENGTH_INDEFINITE = Snackbar.LENGTH_INDEFINITE;
    public static final int LENGTH_SHORT = Snackbar.LENGTH_SHORT;

    private Snackbar snackbar;
    private ThemeStore themeStore;
    private int color = 0;

    public CustomSnackbar(Context context){
        themeStore = ThemeStore.getInstance(context);
    }

    public CustomSnackbar(Context context, @ColorInt int color){
        themeStore = ThemeStore.getInstance(context);
        this.color = color;
    }

    public CustomSnackbar make(View view, CharSequence text, int duration){
        int backgroundColor = themeStore.getPrimaryColor();
        int textColor = themeStore.getPrimaryTextColor();
        if(color != 0)
            backgroundColor = color;
        if(Utils.isColorBright(backgroundColor))
            if(Utils.isColorBright(textColor))
                textColor = Color.BLACK;
        if(!Utils.isColorBright(backgroundColor))
            if(!Utils.isColorBright(textColor))
                textColor = Color.WHITE;
        snackbar = Snackbar.make(view, text, duration);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(backgroundColor);
        snackbar.setActionTextColor(textColor);
        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(textColor);
        return this;
    }

    public CustomSnackbar setAction(String text, View.OnClickListener listener){
        snackbar.setAction(text, listener);
        return this;
    }

    public CustomSnackbar setCallBack(Snackbar.Callback callBack){
        snackbar.addCallback(callBack);
        return this;
    }

    public void show(){
        snackbar.show();
    }

}
