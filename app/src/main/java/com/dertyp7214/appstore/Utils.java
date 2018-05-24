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
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dertyp7214.appstore.components.CustomAppBarLayout;
import com.dertyp7214.appstore.items.SearchItem;
import com.dertyp7214.appstore.settings.Settings;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class Utils extends AppCompatActivity {

    private int PERMISSIONS = 10;
    private String oldAppPackageName = "com.hacker.appstore";
    private static HashMap<String, Drawable> icons = new HashMap<>();

    public void checkForUpdate(Settings settings, TextView subTitle, ProgressBar progressBar) {
        new Thread(() -> {
            try {
                Utils.this.runOnUiThread(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                });
            } finally {
                Utils.this.runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setIndeterminate(false);
                });
            }
        }).start();
    }

    public void sleep(int duration){
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFromResource(@DrawableRes int resource){
        return BitmapFactory.decodeResource(getResources(), resource);
    }

    public void startActivity(Activity context, Class aClass){
        startActivity(new Intent(context, aClass));
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void startActivity(Activity context, Class aClass, Bundle options){
        startActivity(new Intent(context, aClass), options);
        //overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public String cutString(String string, int cutAt){
        if(string.length()<cutAt)
            return string;
        StringBuilder ret = new StringBuilder();
        for(int i=0;i<cutAt;i++)
            ret.append(string.charAt(i));
        ret.append("...");
        return ret.toString();
    }

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

    public static void setNavigationBarColor(Activity activity, View view, @ColorInt int color, int duration){
        if(getSettings(activity).getBoolean(COLORED_NAVIGATIONBAR, false) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Window window = activity.getWindow();
            ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), window.getNavigationBarColor(), Color.BLACK);
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

    public interface Listener{
        void run(int progress);
    }

    public static File getWebContent(String url, File path, int id, Listener listener) {
        try {
            URL fileurl = new URL(url);
            URLConnection urlConnection = fileurl.openConnection();
            urlConnection.connect();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream(), 8192);

            if(!path.exists())
                path.mkdirs();

            File downloadedFile = new File(path, "download_app_"+id+".apk");

            OutputStream outputStream = new FileOutputStream(downloadedFile);

            byte[] buffer = new byte[8192];

            int fileSize = urlConnection.getContentLength();
            int read;
            long total = 0;

            while ((read = inputStream.read(buffer)) != -1) {
                total += read;
                if(fileSize > 0)
                    listener.run((int) (total * 100 / fileSize));
                outputStream.write(buffer, 0, read);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return downloadedFile;
        } catch (Exception ignored) {
        }
        return null;
    }

    public boolean checkAppDir(){
        File appDir = new File(Environment.getExternalStorageDirectory(), ".appStore");
        deleteDirectory(appDir);
        return appDir.mkdirs();
    }

    private boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return(path.delete());
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

    public static class ByteBuffer{
        private final byte[] bytes;
        private final int buffer;
        public ByteBuffer(byte[] bytes, int buffer){
            this.bytes=bytes;
            this.buffer=buffer;
        }
        public byte[] getBytes() {
            return bytes;
        }
        public int getBuffer() {
            return buffer;
        }
    }

    public static void install_apk(Context context, File file) {
        try {
            if (file.exists()) {
                String[] fileNameArray = file.getName().split(Pattern.quote("."));
                if (fileNameArray[fileNameArray.length - 1].equals("apk")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri downloaded_apk = getFileUri(context, file);
                        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(downloaded_apk,
                                "application/vnd.android.package-archive");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(file),
                                "application/vnd.android.package-archive");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(context,
                context.getApplicationContext().getPackageName() + ".GenericFileProvider", file);
    }

    public static void n(Object o){

    }
}
