/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class CustomSnackbar {

    private Snackbar snackbar;
    private ThemeStore themeStore;

    public CustomSnackbar(Context context){
        themeStore = ThemeStore.getInstance(context);
    }

    public CustomSnackbar make(View view, CharSequence text, int duration){
        snackbar = Snackbar.make(view, text, duration);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(themeStore.getPrimaryColor());
        snackbar.setActionTextColor(themeStore.getPrimaryTextColor());
        TextView textView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(themeStore.getPrimaryTextColor());
        return this;
    }

    public CustomSnackbar setAction(String text, View.OnClickListener listener){
        snackbar.setAction(text, listener);
        return this;
    }

    public void show(){
        snackbar.show();
    }

}
