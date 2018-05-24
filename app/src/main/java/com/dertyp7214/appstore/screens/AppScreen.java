/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.CustomSnackbar;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.AppBarStateChangeListener;
import com.dertyp7214.appstore.components.CustomAppBarLayout;
import com.dertyp7214.appstore.components.CustomToolbar;
import com.dertyp7214.appstore.components.Notifications;
import com.dertyp7214.appstore.items.SearchItem;

import java.io.File;
import java.util.Objects;
import java.util.Random;

public class AppScreen extends Utils {

    @ColorInt
    private int dominantColor;
    private FloatingActionButton fab;
    private SearchItem searchItem;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_screen);
        CustomToolbar toolbar = findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);

        Bundle extra = getIntent().getExtras();

        searchItem = Utils.appsList.get(checkExtra(extra).getString("id"));

        dominantColor = Palette.from(Utils.drawableToBitmap(searchItem.getAppIcon()))
                .generate()
                .getDominantColor(ThemeStore.getInstance(this).getPrimaryColor());

        CustomAppBarLayout appBarLayout = getAppBar();

        setTitle(searchItem.getAppTitle());
        appBarLayout.setAppBarBackgroundColor(dominantColor);
        navigationBarColor(this, appBarLayout, dominantColor, 300);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(searchItem.getAppIcon());
        collapsingToolbarLayout.setCollapsedTitleTextColor(ThemeStore.getInstance(this).getPrimaryTextColor());
        collapsingToolbarLayout.setExpandedTitleColor(Utils.isColorBright(dominantColor) ? Color.BLACK : Color.WHITE);

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state){
                    case EXPANDED:
                        toolbar.setToolbarIconColor(dominantColor, AppScreen.this);
                        navigationBarColor(AppScreen.this, appBarLayout, dominantColor, 300);
                        if(isColorBright(dominantColor) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        else
                            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                        break;
                    case COLLAPSED:
                        toolbar.setToolbarIconColor(ThemeStore.getInstance(AppScreen.this).getPrimaryColor(), AppScreen.this);
                        navigationBarColor(AppScreen.this, appBarLayout, ThemeStore.getInstance(AppScreen.this).getPrimaryColor(), 300);
                        if(isColorBright(ThemeStore.getInstance(AppScreen.this).getPrimaryColor()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        else
                            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            downloadApp(new DownloadListener() {
                @Override
                public void started() {
                    new CustomSnackbar(AppScreen.this, getWindow().getNavigationBarColor()).make(view, "Download started", CustomSnackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

                @Override
                public void finished(File file) {
                    Utils.install_apk(AppScreen.this, file);
                }

                @Override
                public void error(String errorMessage) {

                }
            });
        });
    }

    private void navigationBarColor(Activity activity, AppBarLayout appBarLayout, @ColorInt int color, int duration){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setNavigationBarColor(activity, appBarLayout, color, duration);
        }
    }

    private interface DownloadListener{
        void started();
        void finished(File file);
        void error(String errorMessage);
    }

    private void downloadApp(DownloadListener downloadListener){
        new Thread(() -> {
            int notiId = random.nextInt(65536);
            Notifications notifications = new Notifications(
                    AppScreen.this,
                    notiId,
                    getString(R.string.app_name)+" - "+searchItem.getAppTitle(),
                    getString(R.string.app_name)+" - "+searchItem.getAppTitle(),
                    "",
                    null,
                    true);
            runOnUiThread(notifications::showNotification);
            downloadListener.started();
            Download download = new Download(Config.API_URL+(Config.APK_PATH.replace("{id}", searchItem.getId())));
            File file = new File(Environment.getExternalStorageDirectory(), ".appStore");
            File apk = download.startDownload(file, notiId, (pro) -> runOnUiThread(() -> notifications.setProgress(pro)));
            if(apk.exists()){
                runOnUiThread(notifications::setFinished);
                downloadListener.finished(apk);
            } else {
                runOnUiThread(notifications::setCanceled);
                downloadListener.error("ERROR");
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static class Download{
        private String url;
        Download(String url){
            this.url=url;
        }
        File startDownload(File path, int id, DownloadState downloadState){
            return Utils.getWebContent(url, path, id, downloadState::state);
        }
        interface DownloadState{
            void state(int percentage);
        }
    }
}
