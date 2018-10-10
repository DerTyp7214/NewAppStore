/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.helpers.SQLiteHandler;
import com.dertyp7214.appstore.helpers.SessionManager;
import com.gw.swipeback.SwipeBackLayout;
import com.gw.swipeback.WxSwipeBackLayout;
import com.gw.swipeback.tools.Util;

import java.util.HashMap;

import static com.dertyp7214.appstore.Utils.manipulateColor;
import static com.dertyp7214.themeablecomponents.colorpicker.ColorUtil.calculateColor;

public class LogOut extends Activity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;

    private SQLiteHandler db;
    private SessionManager session;

    private int statusColor = - 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);
        ThemeStore themeStore = ThemeStore.getInstance(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT < 28)
                getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_loggedin));
            else {
                getWindow().setNavigationBarColor(Color.WHITE);
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            getWindow().setStatusBarColor(getResources().getColor(R.color.bg_loggedin));
        }

        WxSwipeBackLayout wxSwipeBackLayout = new WxSwipeBackLayout(this);
        wxSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_LEFT);
        wxSwipeBackLayout.attachToActivity(this);
        wxSwipeBackLayout.setSwipeBackListener(new SwipeBackLayout.OnSwipeBackListener() {
            @Override
            public void onViewPositionChanged(View mView, float swipeBackFraction, float swipeBackFactor) {
                wxSwipeBackLayout.invalidate();
                Util.onPanelSlide(swipeBackFraction);
                if (statusColor == - 1) statusColor = Utils.getStatusBarColor(LogOut.this);
                try {
                    Utils.setStatusBarColor(LogOut.this,
                            calculateColor(statusColor, manipulateColor(MainActivity.color, 0.6F),
                                    100, (int) (swipeBackFraction * 100)));
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onViewSwipeFinished(View mView, boolean isEnd) {
                if (isEnd) {
                    Utils.setStatusBarColor(LogOut.this, Color.TRANSPARENT);
                    wxSwipeBackLayout.finish();
                }
                Util.onPanelReset();
            }
        });

        txtName = findViewById(R.id.name);
        txtEmail = findViewById(R.id.email);
        btnLogout = findViewById(R.id.btnLogout);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (! session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String email = user.get("email");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(LogOut.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}