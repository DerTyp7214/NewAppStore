/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.animation.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.os.*;
import android.support.annotation.*;
import android.support.v4.content.*;
import android.support.v4.view.animation.*;
import android.text.format.DateFormat;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

import com.dertyp7214.appstore.BuildConfig;
import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.LocalJSON;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.MVAccelerateDecelerateInterpolator;
import com.dertyp7214.appstore.dev.Logs;
import com.dertyp7214.appstore.helpers.SQLiteHandler;
import com.dertyp7214.appstore.helpers.SessionManager;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static com.dertyp7214.appstore.Config.API_URL;

public class Splashscreen extends Utils {
    /**
     * Called when the activity is first created.
     */
    Thread splashTread;
    int duration = 500;
    int restDuration = duration;
    int oldPercentage = 0;
    Logs logs;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        logs = new Logs(this);

        checkPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permission
                ) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
            StartAnimations();
        }
    }

    private int getDuration(int percentage) {
        int dur = (duration / 100 * (percentage - oldPercentage));
        restDuration -= dur;
        oldPercentage = percentage;
        logs.info("PERCENTAGE", percentage + "\n" + dur + "\n" + restDuration);
        return dur;
    }

    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.setInterpolator(new MVAccelerateDecelerateInterpolator());
        anim.reset();
        LinearLayout l = findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

        ImageView imgLauncher = findViewById(R.id.img_launcher);

        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                imgLauncher,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        );
        scaleDown.setDuration(700);
        scaleDown.setInterpolator(new FastOutSlowInInterpolator());
        scaleDown.setRepeatCount(ObjectAnimator.INFINITE);
        scaleDown.setRepeatMode(ObjectAnimator.REVERSE);
        scaleDown.start();

        ProgressBar progressBar = findViewById(R.id.splash);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);

        splashTread = new Thread(() -> {
            try {

                Config.SERVER_ONLINE = serverOnline();

                setProgress(
                        progressBar, 10, true, getDuration(10),
                        getString(R.string.splash_checkLogin)
                );

                if (!BuildConfig.DEBUG) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1 && Utils
                            .appInstalled(this, BuildConfig.APPLICATION_ID + ".debug")) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setClassName(
                                BuildConfig.APPLICATION_ID + ".debug",
                                BuildConfig.APPLICATION_ID + ".debug.screens.SplashScreen"
                        );

                        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "debug_store")
                                .setShortLabel("Debug Version")
                                .setLongLabel("Debug Version of the AppStore")
                                .setIcon(Icon.createWithResource(this, R.drawable.ic_launcher))
                                .setIntent(intent)
                                .build();

                        assert shortcutManager != null;
                        shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
                    }
                }

                if (new SessionManager(getApplicationContext()).isLoggedIn()) {

                    SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                    HashMap<String, String> user = db.getUserDetails();
                    String userID = user.get("uid");

                    if (new JSONObject(LocalJSON.getJSON(this)).getBoolean("error")
                            || !getSettings(this).getString("last_refresh", "000000")
                            .contentEquals(DateFormat.format("yyyyMMdd", new Date()))) {
                        getSettings(this).edit().putString("last_refresh",
                                String.valueOf(DateFormat.format("yyyyMMdd", new Date()))).apply();
                        LocalJSON.setJSON(this,
                                getWebContent(Config.API_URL + "/apps/list.php?user="+Config.UID(this)));
                    }

                    setProgress(
                            progressBar, 20, true, getDuration(20),
                            getString(R.string.splash_getUserData)
                    );

                    String url = API_URL + "/apps/pic/" + URLEncoder.encode(userID, "UTF-8")
                            .replace("+", "_") + ".png";
                    File imgFile = new File(getFilesDir(), userID + ".png");
                    if (!imgFile.exists()) {
                        Drawable profilePic = Utils.drawableFromUrl(this, url);
                        FileOutputStream fileOutputStream = new FileOutputStream(imgFile);
                        drawableToBitmap(profilePic)
                                .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    }

                    setProgress(
                            progressBar, 40, true, getDuration(40),
                            getString(R.string.splash_getUserIMage)
                    );

                    syncPreferences();
                }

                setProgress(
                        progressBar, 60, true, getDuration(60),
                        getString(R.string.splash_synsPreferences)
                );

                for (int i = 0; i < 55; i++) {
                    setProgress(
                            progressBar, 60 + (int) (((float) 40 / 55) * i), true,
                            getDuration(60 + (int) (((float) 40 / 55) * i)),
                            String.format(
                                    getString(R.string.splash_writePreferences),
                                    (int) (((float) 100 / 55) * i) + "%"
                            )
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
                logs.info("ERROR", e.toString());
            } finally {

                runOnUiThread(() -> ((TextView) findViewById(R.id.txt_loading))
                        .setText(getString(R.string.splash_applying)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(100, true);
                } else {
                    progressBar.setProgress(100);
                }

                logs.info("FINALLY", restDuration + "");

                Intent intent = new Intent(
                        Splashscreen.this,
                        LoginActivity.class
                );
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
            }

        });
        splashTread.start();
    }

    private void setProgress(ProgressBar progress, int percent, boolean animated, int waittime, String devString) throws InterruptedException {
        runOnUiThread(() -> ((TextView) findViewById(R.id.txt_loading)).setText(devString));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(percent, animated);
            Thread.sleep(waittime);
        } else {
            progress.setProgress(percent);
        }
    }

    public boolean serverOnline() {
        try {
            URL url = new URL(Config.API_URL);
            SocketAddress sockaddr = new InetSocketAddress(
                    InetAddress.getByName(url.getHost()), 80);
            Socket sock = new Socket();
            int timeoutMs = 2000;
            sock.connect(sockaddr, timeoutMs);
            return true;
        } catch (IOException ignored) {
        }
        return false;
    }

}