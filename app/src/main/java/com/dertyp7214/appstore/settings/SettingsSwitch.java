/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import static com.dertyp7214.appstore.Utils.getSettings;

public class SettingsSwitch extends Settings {

    private boolean checked;
    private CheckedChangeListener checkedChangeListener;

    public SettingsSwitch(String name, String text, Context context, boolean checked) {
        super(name, text, context);
        this.checked=checked;
        loadSetting();
    }

    public boolean isChecked(){
        return this.checked;
    }

    public void setChecked(boolean checked){
        this.checked=checked;
        saveSetting();
    }

    public Settings setCheckedChangeListener(CheckedChangeListener changeListener){
        this.checkedChangeListener=changeListener;
        return this;
    }

    public void onCheckedChanged(boolean value){
        setChecked(value);
        if(checkedChangeListener!=null)
            checkedChangeListener.onChangeChecked(checked);
    }

    public interface CheckedChangeListener{
        void onChangeChecked(boolean value);
    }

    @Override
    public void saveSetting(){
        SharedPreferences preferences = getSettings(context);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(name, checked);
        editor.apply();
    }

    @Override
    public void loadSetting(){
        SharedPreferences preferences = getSettings(context);
        this.checked=preferences.getBoolean(name, checked);
    }
}
