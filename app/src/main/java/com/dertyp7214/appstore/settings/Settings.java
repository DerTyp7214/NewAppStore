/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.settings;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Settings {

    protected String text;
    protected String name;
    protected String subTitle;
    protected Context context;
    protected settingsOnClickListener onClickListener;

    public Settings(String name, String text, Context context) {
        this.name = name;
        this.text = text;
        this.context = context;
    }

    public Settings setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        return this;
    }

    public String getSubTitle() {
        return subTitle != null ? subTitle : "";
    }

    public String getText() {
        return this.text;
    }

    public void saveSetting() {
    }

    public void loadSetting() {
    }

    public Settings addSettingsOnClick(settingsOnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    public void onClick(TextView subTitle, ProgressBar imageRight) {
        if (onClickListener != null)
            onClickListener.onClick(name, this, subTitle, imageRight);
    }

    public interface settingsOnClickListener {
        void onClick(String name, Settings setting, TextView subTitle, ProgressBar imageRight);
    }
}
