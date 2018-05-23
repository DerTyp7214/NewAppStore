/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dertyp7214.appstore.BuildConfig;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.SettingsAdapter;
import com.dertyp7214.appstore.settings.Settings;
import com.dertyp7214.appstore.settings.SettingsPlaceholder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SettingsScreen extends Utils {

    private SettingsAdapter settingsAdapter;
    private RecyclerView settingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setStatusBarColor(ThemeStore.getInstance(this).getPrimaryDarkColor());
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setNavigationBarColor(this, getWindow().getDecorView(), ThemeStore.getInstance(this).getPrimaryColor(), 300);
        }

        settingList = findViewById(R.id.setting_rv);

        settingsAdapter = new SettingsAdapter(getSettings(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        settingList.setLayoutManager(layoutManager);
        settingList.setItemAnimator(new DefaultItemAnimator());
        settingList.setAdapter(settingsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(settingList.getContext(), layoutManager.getOrientation());
        settingList.addItemDecoration(dividerItemDecoration);

    }

    private List<Settings> getSettings() {
        return new ArrayList<>(Arrays.asList(
                new SettingsPlaceholder("appdetails", getString(R.string.text_appdetails), this),
                new Settings("version", getString(R.string.text_version), this).setSubTitle(BuildConfig.VERSION_NAME)
        ));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
