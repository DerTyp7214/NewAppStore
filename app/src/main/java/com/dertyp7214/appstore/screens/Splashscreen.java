/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dertyp7214.appstore.BuildConfig;
import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.MVAccelerateDecelerateInterpolator;
import com.dertyp7214.appstore.dev.Logs;
import com.dertyp7214.appstore.helpers.SQLiteHandler;
import com.dertyp7214.appstore.helpers.SessionManager;

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
import java.util.HashMap;
import java.util.Objects;

import static com.dertyp7214.appstore.Config.API_URL;

public class Splashscreen extends Utils {
    int onStartCount = 0;
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    /** Called when the activity is first created. */
    Thread splashTread;
    int duration = 500;
    int restDuration = duration;
    int oldPercentage = 0;
    Logs logs;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        logs = new Logs(this);

        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
            StartAnimations();
        }
    }

    private int getDuration(int percentage){
        int dur = (duration/100*(percentage-oldPercentage));
        restDuration = restDuration-dur;
        oldPercentage=percentage;
        logs.info("PERCENTAGE", percentage + "\n" + dur + "\n" + restDuration);
        return dur;
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                PropertyValuesHolder.ofFloat("scaleY", 1.1f));
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

                setProgress(progressBar, 10, true, getDuration(10), getString(R.string.splash_checkLogin));

                if(!BuildConfig.DEBUG) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1 && Utils.appInstalled(this, BuildConfig.APPLICATION_ID + ".debug")) {

                        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "debug_store")
                                .setShortLabel("Debug Version")
                                .setLongLabel("Debug Version of the AppStore")
                                .setIcon(Icon.createWithResource(this, R.drawable.ic_launcher))
                                .setIntent(Objects.requireNonNull(getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID + ".debug")))
                                .build();

                        assert shortcutManager != null;
                        shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
                    }
                }

                if (new SessionManager(getApplicationContext()).isLoggedIn()) {

                    SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                    HashMap<String, String> user = db.getUserDetails();
                    String userName = user.get("name");

                    setProgress(progressBar, 20, true, getDuration(20), getString(R.string.splash_getUserData));

                    String url = API_URL + "/apps/pic/" + URLEncoder.encode(userName, "UTF-8").replace("+", "_") + ".png";
                    File imgFile = new File(getFilesDir(), userName + ".png");
                    if (!imgFile.exists()) {
                        Drawable profilePic = Utils.drawableFromUrl(this, url);
                        FileOutputStream fileOutputStream = new FileOutputStream(imgFile);
                        drawableToBitmap(profilePic).compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    }

                    setProgress(progressBar, 40, true, getDuration(40), getString(R.string.splash_getUserIMage));

                    syncPreferences();
                }

                setProgress(progressBar, 60, true, getDuration(60), getString(R.string.splash_synsPreferences));

                for(int i=0;i<55;i++) {
                    setProgress(progressBar, 60+(int)(((float)40/55)*i), true, getDuration(60+(int)(((float)40/55)*i)), String.format(getString(R.string.splash_writePreferences), (int)(((float)100/55)*i)+"%"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                runOnUiThread(() -> ((TextView) findViewById(R.id.txt_loading)).setText(getString(R.string.splash_applying)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(100, true);
                } else {
                    progressBar.setProgress(100);
                }

                Intent intent = new Intent(Splashscreen.this,
                        LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                Splashscreen.this.finish();
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

    public boolean serverOnline(){
        try {
            URL url = new URL(Config.API_URL);
            SocketAddress sockaddr = new InetSocketAddress(InetAddress.getByName(url.getHost()), 80);
            Socket sock = new Socket();
            int timeoutMs = 2000;
            sock.connect(sockaddr, timeoutMs);
            return true;
        } catch(IOException ignored) {
        }
        return false;
    }

}
