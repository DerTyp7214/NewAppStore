/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.MVAccelerateDecelerateInterpolator;
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
import java.util.HashMap;

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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        checkPermissions();

        StartAnimations();
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

        ((ProgressBar) findViewById(R.id.splash)).setIndeterminate(true);

        splashTread = new Thread(() -> {
            try {

                Config.SERVER_ONLINE = serverOnline();

                if (new SessionManager(getApplicationContext()).isLoggedIn()) {

                    SQLiteHandler db = new SQLiteHandler(getApplicationContext());
                    HashMap<String, String> user = db.getUserDetails();
                    String userName = user.get("name");

                    String url = API_URL + "/apps/pic/" + URLEncoder.encode(userName, "UTF-8").replace("+", "_") + ".png";
                    File imgFile = new File(getFilesDir(), userName + ".png");
                    if (!imgFile.exists()) {
                        Drawable profilePic = Utils.drawableFromUrl(this, url);
                        FileOutputStream fileOutputStream = new FileOutputStream(imgFile);
                        drawableToBitmap(profilePic).compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    }

                    syncPreferences();
                }

                for(int i=0;i<55;i++)
                    Thread.sleep(10);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Intent intent = new Intent(Splashscreen.this,
                        LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                Splashscreen.this.finish();
            }

        });
        splashTread.start();
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
