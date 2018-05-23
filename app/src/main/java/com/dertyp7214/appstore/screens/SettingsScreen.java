/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.dertyp7214.appstore.BuildConfig;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.SettingsAdapter;
import com.dertyp7214.appstore.components.InputDialog;
import com.dertyp7214.appstore.settings.Settings;
import com.dertyp7214.appstore.settings.SettingsPlaceholder;
import com.dertyp7214.appstore.settings.SettingsSwitch;
import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.github.Repository;
import com.dertyp7214.githubsource.helpers.ColorStyle;

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

        setNavigationBarColor(this, getWindow().getDecorView(), ThemeStore.getInstance(this).getPrimaryColor(), 300);

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
                new Settings("version", getString(R.string.text_version), this).setSubTitle(BuildConfig.VERSION_NAME),
                new Settings("check_update", getString(R.string.text_check_update), this).setSubTitle(getString(R.string.text_click_check)).addSettingsOnClick((name, instance, subTitle, imageRight) -> {
                    checkForUpdate(instance, subTitle, imageRight);
                }),
                new Settings("sourcecode", "Sourcecode", this).setSubTitle(getString(R.string.text_sourcecode)).addSettingsOnClick((name, setting, subTitle, imageRight) -> {
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Loading");
                    progressDialog.show();
                    new Thread(() -> {
                        ThemeStore store = ThemeStore.getInstance(SettingsScreen.this);
                        GitHubSource.getInstance(SettingsScreen.this, new Repository("dertyp7214", "NewAppStore", getSettings(SettingsScreen.this).getString("API_KEY", null)))
                                .setColorStyle(new ColorStyle(store.getPrimaryColor(), store.getPrimaryDarkColor(), store.getAccentColor()))
                                .open();
                        runOnUiThread(progressDialog::dismiss);
                    }).start();
                }),
                new SettingsPlaceholder("preferences", getString(R.string.text_prefs), this),
                new Settings("api_key", getString(R.string.text_api_key), this).setSubTitle(cutString(getSettings(SettingsScreen.this).getString("API_KEY", getString(R.string.text_not_set)), 30)).addSettingsOnClick((name, setting, subTitle, imageRight) -> {
                    InputDialog dialog = new InputDialog(getString(R.string.text_api_key), getSettings(SettingsScreen.this).getString("API_KEY", ""), getString(R.string.text_api_key), SettingsScreen.this);
                    dialog.setListener(new InputDialog.Listener() {
                        @Override
                        public void onSubmit(String text) {
                            getSharedPreferences("settings", MODE_PRIVATE).edit().putString("API_KEY", text).apply();
                            subTitle.setText(cutString(getSettings(SettingsScreen.this).getString("API_KEY", getString(R.string.text_not_set)), 30));
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    dialog.show();
                }),
                new SettingsSwitch("colored_navigationbar", getString(R.string.text_colored_navbar), this, getSettings(this).getBoolean("colored_nav_bar", false)).setCheckedChangeListener(value -> {
                    getSettings(SettingsScreen.this).edit().putBoolean("colored_nav_bar", value).apply();
                    setNavigationBarColor(this, getWindow().getDecorView(), ThemeStore.getInstance(this).getPrimaryColor(), 300);
                })
        ));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
