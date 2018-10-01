/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.dertyp7214.appstore.BuildConfig;
import com.dertyp7214.appstore.dev.Logs;

import java.util.Objects;

import androidx.annotation.Nullable;

public class StartActivity extends Activity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        Intent intent = getIntent();

        try {
            if (Objects.equals(Objects.requireNonNull(intent.getExtras()).getString("action"),
                    "startDebug")) {
                Intent startDebug = new Intent();
                startDebug.setClassName(
                        BuildConfig.APPLICATION_ID + ".debug",
                        BuildConfig.APPLICATION_ID + ".screens.Splashscreen"
                );
                startActivity(startDebug);
            }
        } catch (Exception e) {
            new Logs(this).error("ERROR", e.toString());
        }
    }
}
