/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.dertyp7214.appstore.CustomSnackbar;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.AppBarStateChangeListener;
import com.dertyp7214.appstore.components.CustomAppBarLayout;
import com.dertyp7214.appstore.components.CustomToolbar;
import com.dertyp7214.appstore.items.SearchItem;

public class AppScreen extends Utils {

    @ColorInt
    private int dominantColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_screen);
        CustomToolbar toolbar = findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);

        Bundle extra = getIntent().getExtras();

        SearchItem searchItem = Utils.appsList.get(checkExtra(extra).getString("id"));

        dominantColor = Palette.from(Utils.drawableToBitmap(searchItem.getAppIcon()))
                .generate()
                .getDominantColor(ThemeStore.getInstance(this).getPrimaryColor());

        CustomAppBarLayout appBarLayout = getAppBar();

        setTitle(searchItem.getAppTitle());
        appBarLayout.setAppBarBackgroundColor(dominantColor);
        navigationBarColor(this, appBarLayout, dominantColor, 300);
        collapsingToolbarLayout.setCollapsedTitleTextColor(ThemeStore.getInstance(this).getPrimaryTextColor());
        collapsingToolbarLayout.setExpandedTitleColor(Utils.isColorBright(dominantColor) ? Color.BLACK : Color.WHITE);

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state){
                    case EXPANDED:
                        navigationBarColor(AppScreen.this, appBarLayout, dominantColor, 300);
                        if(isColorBright(dominantColor) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        else
                            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                        break;
                    case COLLAPSED:
                        navigationBarColor(AppScreen.this, appBarLayout, ThemeStore.getInstance(AppScreen.this).getPrimaryColor(), 300);
                        if(isColorBright(ThemeStore.getInstance(AppScreen.this).getPrimaryColor()) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        else
                            getWindow().getDecorView().setSystemUiVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> new CustomSnackbar(this, getWindow().getNavigationBarColor()).make(view, "Replace with your own action", CustomSnackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    private void navigationBarColor(Activity activity, AppBarLayout appBarLayout, @ColorInt int color, int duration){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setNavigationBarColor(activity, appBarLayout, color, duration);
        }
    }
}
