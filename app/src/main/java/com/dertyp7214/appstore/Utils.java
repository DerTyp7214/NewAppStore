/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import com.dertyp7214.appstore.components.CustomAppBarLayout;
import com.dertyp7214.appstore.items.SearchItem;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class Utils extends AppCompatActivity {

    private int PERMISSIONS = 10;
    private String oldAppPackageName = "com.hacker.appstore";
    private static HashMap<String, Drawable> icons = new HashMap<>();

    public final static String COLORED_NAVIGATIONBAR = "colored_navigationbar";

    public static HashMap<String, SearchItem> appsList;

    public Bundle checkExtra(Bundle extra){
        if (extra == null) finish();
        assert extra != null;
        if (extra.size() > 1) finish();
        return extra;
    }

    public Parcelable checkExtraKey(Bundle extra, String key){
        if (extra == null) finish();
        assert extra != null;
        if (extra.getParcelable(key) == null) finish();
        return extra.getParcelable(key);
    }

    public CustomAppBarLayout getAppBar(){
        return findViewById(R.id.app_bar);
    }

    public static SharedPreferences getSettings(Context context){
        return context.getSharedPreferences("settings", MODE_PRIVATE);
    }

    public void importSettings(Map<String, Object> settings){
        SharedPreferences.Editor prefs = getSettings(this).edit();
        for(String key : settings.keySet()){
            Object obj = settings.get(key);
            if(obj instanceof Float)
                prefs.putFloat(key, (float) obj);
            else if(obj instanceof String)
                prefs.putString(key, (String) obj);
            else if(obj instanceof Boolean)
                prefs.putBoolean(key, (boolean) obj);
            else if(obj instanceof Integer)
                prefs.putInt(key, (int) obj);
            else if(obj instanceof Long)
                prefs.putLong(key, (long) obj);
            else if(obj instanceof Set)
                prefs.putStringSet(key, (Set<String>) obj);
        }
        prefs.apply();
    }

    public String exportSettings(){
        Map<String, ?> settings = getSettings(this).getAll();
        return new JSONObject(settings).toString();
    }

    public static void setSettings(Context context, SharedPreferences settings){
        Map<String, ?> objectMap = settings.getAll();
        SharedPreferences.Editor prefs = getSettings(context).edit();
        for(String key : objectMap.keySet()){
            Object obj = objectMap.get(key);
            if(obj instanceof Float)
                prefs.putFloat(key, (float) obj);
            else if(obj instanceof String)
                prefs.putString(key, (String) obj);
            else if(obj instanceof Boolean)
                prefs.putBoolean(key, (boolean) obj);
            else if(obj instanceof Integer)
                prefs.putInt(key, (int) obj);
            else if(obj instanceof Long)
                prefs.putLong(key, (long) obj);
            else if(obj instanceof Set)
                prefs.putStringSet(key, (Set<String>) obj);
        }
        prefs.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void setNavigationBarColor(Activity activity, View view, @ColorInt int color, int duration){
        if(getSettings(activity).getBoolean(COLORED_NAVIGATIONBAR, false)) {
            Window window = activity.getWindow();
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), window.getNavigationBarColor(), color);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                int c = (int) animation.getAnimatedValue();
                if (isColorBright(c) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                else
                    view.setSystemUiVisibility(View.VISIBLE);
                window.setNavigationBarColor(c);
            });
            animator.start();
        }
    }

    public void setNavigationBarColor(@ColorInt int color){
        getWindow().setNavigationBarColor(color);
    }

    public void setStatusBarColor(@ColorInt int color){
        getWindow().setStatusBarColor(color);
    }

    public int getNavigationBarColor(){
        return getWindow().getNavigationBarColor();
    }

    public int getStatusBarColor(){
        return getWindow().getStatusBarColor();
    }

    public static boolean isColorBright(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness < 0.5;
    }

    public static int getDominantColor(Drawable drawable) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(drawableToBitmap(drawable), 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

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

    public interface Callback {
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
        if(icons.containsKey(url))
            return icons.get(url);
        try {
            Bitmap bmp;

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();

            bmp = BitmapFactory.decodeStream(input);
            Drawable drawable = new BitmapDrawable(context.getResources(), bmp);
            icons.put(url, drawable);
            return drawable;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
