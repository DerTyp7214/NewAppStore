/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.colorPicker.ColorPicker;

public class SettingsColor extends Settings {

    private int colorInt;
    private String colorString;
    private boolean isString;
    private settingsOnClickListener onClickListener;

    public SettingsColor(String name, String text, Context context, int color) {
        super(name, text, context);
        this.colorInt=color;
        this.isString=false;
        loadSetting();
    }

    public SettingsColor(String name, String text, Context context, String color) {
        super(name, text, context);
        this.colorString=color;
        this.isString=true;
        loadSetting();
    }

    public SettingsColor addSettingsOnClick(SettingsColor.settingsOnClickListener onClickListener){
        this.onClickListener=onClickListener;
        return this;
    }

    public String getColorString(){
        return isString?colorString:String.format("#%06X", colorInt);
    }

    public int getColorInt() {
        return isString?Color.parseColor(colorString):colorInt;
    }

    public void onClick(final View colorPlate){
        if(onClickListener!=null) {
            final ColorPicker colorPicker = new ColorPicker(context);
            colorPicker.setListener(new ColorPicker.Listener() {
                @Override
                public void color(int i) {

                    if(isString)
                        colorString=String.format("#%06X", i);
                    else
                        colorInt = i;

                    LayerDrawable bgDrawable = (LayerDrawable) colorPlate.getBackground();
                    final GradientDrawable shape = (GradientDrawable) bgDrawable.findDrawableByLayerId(R.id.plate_color);
                    shape.setColor(isString?Color.parseColor(colorString):colorInt);

                    colorPicker.cancel();

                    onClickListener.onClick(name, isString?Color.parseColor(colorString):colorInt, SettingsColor.this);

                }

                @Override
                public void updateColor(int i) {

                }

                @Override
                public void cancel() {
                    colorPicker.cancel();
                }
            });
            colorPicker.show();
            colorPicker.setAnimationTime(300);
            if(isString)
                colorPicker.setColor(colorString);
            else
                colorPicker.setColor(colorInt);
        }
    }

    public interface settingsOnClickListener{
        void onClick(String name, int Color, SettingsColor settingsColor);
    }

    @Override
    public void saveSetting(){
        SharedPreferences preferences = context.getSharedPreferences("colors_"+Config.UID(context), Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(name, getColorInt());
        editor.apply();

    }

    @Override
    public void loadSetting(){
        SharedPreferences preferences = context.getSharedPreferences("colors_"+Config.UID(context), Context.MODE_PRIVATE);
        this.colorInt=isString?Color.parseColor(preferences.getString(name, colorString)):preferences.getInt(name, colorInt);
    }
}
