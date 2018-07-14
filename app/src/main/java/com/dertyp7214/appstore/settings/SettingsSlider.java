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

package com.dertyp7214.appstore.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.ProgressBar;
import android.widget.TextView;

import static com.dertyp7214.appstore.Utils.getSettings;

public class SettingsSlider extends Settings {

    private int progress = 20;

    public SettingsSlider(String name, String text, Context context){
        super(name, text, context);
        loadSetting();
    }

    public void onUpdate(int progress){
        this.progress=progress;
    }

    @Override
    public void saveSetting(){
        SharedPreferences preferences = getSettings(context);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(name, progress);
        editor.apply();
    }

    @Override
    public void loadSetting(){
        SharedPreferences preferences = getSettings(context);
        this.progress=preferences.getInt(name, progress);
    }

    public int getProgress() {
        return this.progress;
    }
}
