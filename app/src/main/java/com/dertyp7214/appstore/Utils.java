/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils extends AppCompatActivity {

    private int PERMISSIONS = 10;
    private String oldAppPackageName = "com.hacker.appstore";

    public static String getWebContent(String url) {
        try {
            URL web = new URL(url);
            BufferedReader in = new BufferedReader(new InputStreamReader(web.openStream()));

            String inputLine;
            StringBuilder ret = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                ret.append(inputLine);

            in.close();
            return ret.toString();
        } catch (Exception ignored) {
        }
        return null;
    }

    public void checkForOldAppStore(){
        if(appInstalled(this, oldAppPackageName)){
            DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        Intent intent = new Intent(Intent.ACTION_DELETE);
                        intent.setData(Uri.parse("package:"+oldAppPackageName));
                        startActivity(intent);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.oldAppStoreFound));
            builder.setPositiveButton(android.R.string.yes, onClickListener);
            builder.setNegativeButton(android.R.string.no, onClickListener);
            builder.show();
        }
    }

    public void checkPermissions() {
        ActivityCompat.requestPermissions(this,
                permissons().toArray(new String[0]),
                PERMISSIONS);
    }

    public void setTimeOut(int duration, Callback callback) {
        new Thread(() -> {
            try {
                Thread.sleep(duration);
                runOnUiThread(callback::run);
            } catch (InterruptedException e) {
                runOnUiThread(callback::run);
            }
        }).start();
    }

    interface Callback {
        void run();
    }

    private List<String> permissons() {
        List<String> permissons = new ArrayList<>(Arrays.asList(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ));

        return permissons;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    public static boolean appInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return false;
    }

    public static Drawable drawableFromUrl(Context context, String url){
        try {
            Bitmap bmp;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();

            bmp = BitmapFactory.decodeStream(input);
            return new BitmapDrawable(context.getResources(), bmp);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
