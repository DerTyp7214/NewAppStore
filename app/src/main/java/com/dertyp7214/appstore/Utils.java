/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dertyp7214.appstore.components.CustomAppBarLayout;
import com.dertyp7214.appstore.components.CustomToolbar;
import com.dertyp7214.appstore.dev.Logs;
import com.dertyp7214.appstore.items.SearchItem;
import com.dertyp7214.appstore.receivers.PackageUpdateReceiver;
import com.dertyp7214.appstore.screens.MainActivity;
import com.dertyp7214.appstore.settings.Settings;
import com.dertyp7214.themeablecomponents.utils.ThemeManager;
import com.gw.swipeback.SwipeBackLayout;
import com.gw.swipeback.WxSwipeBackLayout;
import com.gw.swipeback.tools.Util;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.drawable.DrawableCompat;

import static com.dertyp7214.appstore.Config.API_URL;
import static com.dertyp7214.appstore.Config.UID;

@SuppressLint("Registered")
public class Utils extends AppCompatActivity {

    public final static String COLORED_NAVIGATIONBAR = "colored_nav_bar";
    protected static final String PACKAGE_NAME = "com.dertyp7214.appstore";
    public static HashMap<String, Drawable> userImageHashMap = new HashMap<>();
    public static HashMap<String, SearchItem> appsList = new HashMap<>();
    public static SearchItem currentApp;
    private static HashMap<String, Drawable> icons = new HashMap<>();
    private static Thread rainbow;
    public Logs logs;
    public CustomToolbar toolbar;
    protected int PERMISSIONS = 10;
    protected String oldAppPackageName = "com.hacker.appstore";
    protected ThemeStore themeStore;
    protected ThemeManager themeManager;
    private int statusColor = - 1;
    private Callback callback;

    public static String colorToString(@ColorInt int intColor) {
        return String.format("#%06X", (0xFFFFFF & intColor));
    }

    public static String addAlpha(String originalColor, double alpha) {
        long alphaFixed = Math.round(alpha * 255);
        String alphaHex = Long.toHexString(alphaFixed);
        if (alphaHex.length() == 1) {
            alphaHex = "0" + alphaHex;
        }
        if (originalColor.replace("#", "").length() > 6)
            originalColor = originalColor.replaceFirst("#..", "#");
        originalColor = originalColor.replace("#", "#" + alphaHex);
        return originalColor;
    }

    public static void tintWidget(View view, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(view.getBackground());
        DrawableCompat.setTint(wrappedDrawable.mutate(), color);
        view.setBackground(wrappedDrawable);
    }

    public static void setCursorColor(EditText view, @ColorInt int color) {
        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(view);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(view);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(view.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {
        }
    }

    public static void removeMyApp(String packageName, Context context) {
        getWebContent(Config.API_URL + "/apps/myapps.php?uid=" + Config
                .UID(context) + "&remove=" + packageName);
    }

    public static List<ApplicationInfo> getInstalledApps(@NonNull Context context) {
        final PackageManager pm = context.getPackageManager();
        return pm.getInstalledApplications(PackageManager.GET_META_DATA);
    }

    public static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        ApplicationInfo info = null;
        try {
            info = context.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (Exception ignored) {
        }
        return info;
    }

    public static String encodeToBase64(Drawable drawable) {
        Bitmap image = drawableToBitmap(drawable);
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Drawable decodeBase64(Context context, String input) {
        byte[] decodedBytes = Base64.decode(input, 0);
        return new BitmapDrawable(
                context.getResources(), BitmapFactory
                .decodeByteArray(decodedBytes, 0, decodedBytes.length));
    }

    public static SharedPreferences getSettings(@NonNull Context context) {
        return context.getSharedPreferences("settings_" + Config.UID(context), MODE_PRIVATE);
    }

    public static void setSettings(@NonNull Context context, @NonNull SharedPreferences settings) {
        Map<String, ?> objectMap = settings.getAll();
        SharedPreferences.Editor prefs = getSettings(context).edit();
        for (String key : objectMap.keySet()) {
            Object obj = objectMap.get(key);
            if (obj instanceof Float)
                prefs.putFloat(key, (float) obj);
            else if (obj instanceof String)
                prefs.putString(key, (String) obj);
            else if (obj instanceof Boolean)
                prefs.putBoolean(key, (boolean) obj);
            else if (obj instanceof Integer)
                prefs.putInt(key, (int) obj);
            else if (obj instanceof Long)
                prefs.putLong(key, (long) obj);
            else if (obj instanceof Set)
                prefs.putStringSet(key, (Set<String>) obj);
        }
        prefs.apply();
    }

    public static void setNavigationBarColor(Activity activity, View view, @ColorInt int color, int duration) {
        if ((getSettings(activity).getBoolean(COLORED_NAVIGATIONBAR,
                false
        ) || Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view != null) {
            Window window = activity.getWindow();
            ValueAnimator animator = ValueAnimator
                    .ofObject(new ArgbEvaluator(), window.getNavigationBarColor(), color);
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
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view != null) {
            Window window = activity.getWindow();
            ValueAnimator animator = ValueAnimator
                    .ofObject(new ArgbEvaluator(), window.getNavigationBarColor(), Color.BLACK);
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

    public static void setStatusBarColor(Activity activity, View view, @ColorInt int color, int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = activity.getWindow();
            ValueAnimator animator = ValueAnimator
                    .ofObject(new ArgbEvaluator(), window.getStatusBarColor(), color);
            animator.setDuration(duration);
            animator.addUpdateListener(animation -> {
                int c = (int) animation.getAnimatedValue();
                if (isColorBright(c))
                    view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                else
                    view.setSystemUiVisibility(View.VISIBLE);
                window.setStatusBarColor(c);
            });
            animator.start();
        }
    }

    public static boolean isColorBright(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color
                .blue(color)) / 255;
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
        return Color.argb(
                a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255)
        );
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1,
                    Bitmap.Config.ARGB_8888
            ); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap
                    .createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888
                    );
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getWebContent(String url, File path, int id, Listener listener) {
        try {
            URL fileurl = new URL(url);
            URLConnection urlConnection = fileurl.openConnection();
            urlConnection.connect();

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream(), 8192);

            if (! path.exists())
                path.mkdirs();

            File downloadedFile = new File(path, "download_app_" + id + ".apk");

            OutputStream outputStream = new FileOutputStream(downloadedFile);

            byte[] buffer = new byte[8192];

            int last = 0;
            long fileSize = urlConnection.getContentLength();
            long read;
            long total = 0;

            listener.run(0);

            while ((read = inputStream.read(buffer)) != - 1) {
                int tmp = (int) (total * 100 / fileSize);
                total += read;
                if (fileSize > 0 && tmp > last + 2) {
                    last = tmp;
                    listener.run(tmp);
                }
                outputStream.write(buffer, 0, (int) read);
            }

            listener.run(100);

            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return downloadedFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void executeCommand(Activity activity, String cmds) {
        Logs logs = new Logs(activity);
        logs.info("COMMAND", cmds);
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());

            os.writeBytes(cmds + "\n");

            os.writeBytes("exit\n");
            os.flush();
            os.close();

            process.waitFor();
        } catch (Exception ignored) {
        }
    }

    public static boolean appInstalled(Activity context, String uri) {
        return applicationInstalled(context, uri) && ! verifyInstallerId(context, uri);
    }

    public static boolean verifyInstallerId(Activity context, String packageName) {
        try {
            List<String> validInstallers = new ArrayList<>(
                    Arrays.asList("com.android.vending", "com.google.android.feedback"));

            final String installer =
                    context.getPackageManager().getInstallerPackageName(packageName);

            return installer != null && validInstallers.contains(installer);
        } catch (Exception e) {
            Logs.getInstance(context).error("verifyInstallerId", e.toString());
            return false;
        }
    }

    public static boolean applicationInstalled(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return false;
    }

    public static Drawable drawableFromUrl(Context context, String url) {
        return drawableFromUrl(context, url, R.drawable.ic_person);
    }

    public static Drawable drawableFromUrl(Context context, String url, @DrawableRes int def) {
        if (icons.containsKey(url))
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
        } catch (Exception e) {
            e.printStackTrace();
            Bitmap bitmap = drawableToBitmap(context.getDrawable(def));
            int color = ThemeStore.getInstance(context).getAccentColor();
            return new BitmapDrawable(
                    context.getResources(), overlay(createBitmap(bitmap, color), bitmap));
        }
    }

    private static Bitmap createBitmap(Bitmap copy, @ColorInt int color) {
        Bitmap bmp = Bitmap
                .createBitmap(copy.getWidth(), copy.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(color);
        return bmp;
    }

    private static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(bmp2, new Matrix(), null);
        return bmOverlay;
    }

    public static void install_apk(Context context, File file) {
        try {
            if (file.exists()) {
                String[] fileNameArray = file.getName().split(Pattern.quote("."));
                if (fileNameArray[fileNameArray.length - 1].equals("apk")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Uri downloaded_apk = getFileUri(context, file);
                        Intent intent = new Intent(Intent.ACTION_VIEW).setDataAndType(
                                downloaded_apk,
                                "application/vnd.android.package-archive"
                        );
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(
                                Uri.fromFile(file),
                                "application/vnd.android.package-archive"
                        );
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
                context.getApplicationContext()
                        .getPackageName() + ".GenericFileProvider", file
        );
    }

    public static void n(Object o) {

    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void rainbow(Activity context) {
        rainbow = getRainbow(context);
        if (getSettings(context).getBoolean("rainbow_mode", false)) rainbow.start();
    }

    private static Thread getRainbow(Activity context) {
        return new Thread(() -> {
            Looper.prepare();
            while (rainbow != null) {
                for (ThemeManager.Component component : ThemeManager.getInstance(context)
                        .getComponents()) {
                    sleep(500);
                    context.runOnUiThread(() -> {
                        ValueAnimator animator = ValueAnimator.ofInt(0, 360);
                        animator.setDuration(3000);
                        animator.addUpdateListener(animation -> {
                            int color = (Integer) animation.getAnimatedValue();
                            component.changeColor(
                                    changeHue(ThemeStore.getInstance(context).getAccentColor(),
                                            color));
                        });
                        animator.start();
                    });
                }
                sleep(3000 + 500);
            }
        });
    }

    @ColorInt
    public static int changeHue(@ColorInt int color, int degree) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] += degree;
        while (hsv[0] > 360) hsv[0] -= 360;
        return Color.HSVToColor(hsv);
    }

    public static void toggleRainBow(boolean enabled, Activity context) {
        if (enabled && rainbow == null) {
            rainbow = getRainbow(context);
            rainbow.start();
        } else {
            rainbow = null;
        }
    }

    public static int getResId(String resName, Class<?> c) {
        try {
            Field idField = c.getDeclaredField(resName);
            return idField.getInt(idField);
        } catch (Exception e) {
            e.printStackTrace();
            return - 1;
        }
    }

    public static int getStatusBarColor(Activity activity) {
        return activity.getWindow().getStatusBarColor();
    }

    public static void setStatusBarColor(Activity activity, @ColorInt int color) {
        activity.getWindow().setStatusBarColor(color);
    }

    public void setContentView(int layoutResID, boolean swipe) {
        super.setContentView(layoutResID);
        if (swipe) {
            WxSwipeBackLayout wxSwipeBackLayout = new WxSwipeBackLayout(this);
            wxSwipeBackLayout.setDirectionMode(SwipeBackLayout.FROM_LEFT);
            wxSwipeBackLayout.attachToActivity(this);
            wxSwipeBackLayout.setSwipeBackListener(new SwipeBackLayout.OnSwipeBackListener() {
                @Override
                public void onViewPositionChanged(View mView, float swipeBackFraction, float swipeBackFactor) {
                    wxSwipeBackLayout.invalidate();
                    Util.onPanelSlide(swipeBackFraction);
                    if (statusColor == - 1) statusColor = getStatusBarColor();
                    try {
                        setStatusBarColor(calculateColor(manipulateColor(MainActivity.color, 0.6F),
                                statusColor, 100,
                                (int) (swipeBackFraction * 100)));
                    } catch (Exception ignored) {
                    }
                }

                @Override
                public void onViewSwipeFinished(View mView, boolean isEnd) {
                    if (isEnd) {
                        if (callback != null) callback.run();
                        setStatusBarColor(Color.TRANSPARENT);
                        wxSwipeBackLayout.finish();
                    }
                    Util.onPanelReset();
                }
            });
        }
    }

    protected void setSwipeBackCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setContentView(int layoutResID) {
        setContentView(layoutResID, true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        themeStore = ThemeStore.getInstance(this);
        themeManager = ThemeManager.getInstance(this);
        themeManager.changeAccentColor(themeStore.getAccentColor());
        themeManager.changePrimaryColor(this, themeStore.getPrimaryColor(), true,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P, false);
        rainbow(this);
        if (PackageUpdateReceiver.activity == null)
            PackageUpdateReceiver.activity = this;
    }

    protected void applyTheme() {
        ThemeStore themeStore = ThemeStore.getInstance(this);
        setStatusBarColor(themeStore.getPrimaryDarkColor());
        if (Build.VERSION.SDK_INT < 28)
            setNavigationBarColor(this, getWindow().getDecorView(), themeStore.getPrimaryColor(),
                    300);
        else
            setNavigationBarColor(this, getWindow().getDecorView(), Color.WHITE, 300);
        if (toolbar != null) {
            toolbar.setBackgroundColor(themeStore.getPrimaryColor());
            toolbar.setToolbarIconColor(themeStore.getPrimaryColor());
        } else {
            Objects.requireNonNull(getSupportActionBar())
                    .setBackgroundDrawable(new ColorDrawable(themeStore.getPrimaryColor()));
            getSupportActionBar().setTitle(Html.fromHtml("<font color='" + String.format("#%06X",
                    0xFFFFFF & themeStore
                            .getPrimaryTextColor()
            ) + "'>" + getSupportActionBar().getTitle() + "</font>"));
        }
    }

    public void checkForUpdate(Settings settings, TextView subTitle, ProgressBar progressBar) {
        new Thread(() -> {
            try {
                Utils.this.runOnUiThread(() -> {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    progressBar.setProgressTintList(
                            ColorStateList.valueOf(ThemeStore.getInstance(this).getAccentColor()));
                    subTitle.setText(getString(R.string.text_loading));
                });
                Thread.sleep(300);
                String version = getWebContent(
                        Config.API_URL + "/apps/list.php?version=" + getPackageName());
                if (version == null)
                    throw new InterruptedException();
                if (! version.equals(BuildConfig.VERSION_NAME)) {
                    runOnUiThread(() -> subTitle.setText(getString(R.string.text_touch_to_update)));
                    settings.addSettingsOnClick(
                            (name, setting, subTitle1, imageRight) -> new Thread(() -> {
                                Looper.prepare();
                                runOnUiThread(() -> {
                                    progressBar.setVisibility(View.VISIBLE);
                                    progressBar.setMax(100);
                                });
                                File file = getWebContent(
                                        Config.API_URL + Config.APK_PATH
                                                .replace("{id}", getPackageName())
                                                .replace("{uid}", Config.UID(this)),
                                        new File(
                                                Environment.getExternalStorageDirectory(),
                                                ".appStore"
                                        ),
                                        42,
                                        progress -> runOnUiThread(() -> {
                                            if (Build.VERSION_CODES.N <= Build.VERSION.SDK_INT)
                                                progressBar.setProgress(progress, true);
                                            else
                                                progressBar.setProgress(progress);
                                        })
                                );
                                install_apk(this, file);
                                runOnUiThread(() -> progressBar.setVisibility(View.INVISIBLE));
                            }).start());
                } else {
                    runOnUiThread(() -> subTitle.setText(getString(R.string.text_latest_version)));
                }
            } catch (InterruptedException ignored) {
                runOnUiThread(() -> subTitle.setText(getString(R.string.text_can_not_check)));
            } finally {
                Utils.this.runOnUiThread(() -> {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar.setIndeterminate(false);
                });
            }
        }).start();
    }

    public void sleep(long duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFromResource(@DrawableRes int resource) {
        return BitmapFactory.decodeResource(getResources(), resource);
    }

    public void startActivity(Activity context, Class aClass) {
        startActivity(new Intent(context, aClass));
    }

    public void startActivity(Activity context, Class aClass, Bundle options) {
        startActivity(new Intent(context, aClass), options);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void setMargins(View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    public String cutString(String string, int cutAt) {
        if (string.length() < cutAt)
            return string;
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < cutAt; i++)
            ret.append(string.charAt(i));
        ret.append("...");
        return ret.toString();
    }

    public Bundle checkExtra(Bundle extra) {
        if (extra == null) finish();
        assert extra != null;
        if (extra.size() > 1) finish();
        return extra;
    }

    protected void setColors() {
        logs = Logs.getInstance(this);
        logs.info("setColor", String.valueOf(themeStore != null));
        if (themeStore != null) {
            setStatusBarColor(themeStore.getPrimaryDarkColor());
            toolbar.setBackgroundColor(themeStore.getPrimaryColor());
            toolbar.setToolbarIconColor(themeStore.getPrimaryColor());
            if (Build.VERSION.SDK_INT < 28)
                setNavigationBarColor(this, getWindow().getDecorView(),
                        themeStore.getPrimaryColor(),
                        300);
            else
                setNavigationBarColor(this, getWindow().getDecorView(), Color.WHITE, 300);
        }
    }

    protected void setBackButton() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    public Object checkExtraKey(Bundle extra, String key) {
        checkExtra(extra);
        if (extra.get(key) == null) finish();
        return extra.get(key);
    }

    protected String readFromFileInputStream(FileInputStream fileInputStream) {
        StringBuilder retBuf = new StringBuilder();

        try {
            if (fileInputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String lineData = bufferedReader.readLine();
                while (lineData != null) {
                    retBuf.append(lineData);
                    lineData = bufferedReader.readLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retBuf.toString();
    }

    public CustomAppBarLayout getAppBar() {
        return findViewById(R.id.app_bar);
    }

    public <V extends View> Collection<V> findChildrenByClass(Class<V> clazz, ViewGroup... viewGroups) {
        Collection<V> collection = new ArrayList<>();
        for (ViewGroup viewGroup : viewGroups)
            collection.addAll(gatherChildrenByClass(viewGroup, clazz, new ArrayList<V>()));
        return collection;
    }

    private <V extends View> Collection<V> gatherChildrenByClass(@NonNull ViewGroup viewGroup, Class<V> clazz, @NonNull Collection<V> childrenFound) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            final View child = viewGroup.getChildAt(i);
            if (clazz.isAssignableFrom(child.getClass())) {
                childrenFound.add((V) child);
            }
            if (child instanceof ViewGroup) {
                gatherChildrenByClass((ViewGroup) child, clazz, childrenFound);
            }
        }

        return childrenFound;
    }

    public void importSettings(Map<String, Object> settings) {
        SharedPreferences.Editor prefs = getSettings(this).edit();
        for (String key : settings.keySet()) {
            Object obj = settings.get(key);
            if (obj instanceof Float)
                prefs.putFloat(key, (float) obj);
            else if (obj instanceof String)
                prefs.putString(key, (String) obj);
            else if (obj instanceof Boolean)
                prefs.putBoolean(key, (boolean) obj);
            else if (obj instanceof Integer)
                prefs.putInt(key, (int) obj);
            else if (obj instanceof Long)
                prefs.putLong(key, (long) obj);
            else if (obj instanceof Set)
                prefs.putStringSet(key, (Set<String>) obj);
        }
        prefs.apply();
    }

    public String exportSettings() {
        Map<String, ?> settings = getSettings(this).getAll();
        return new JSONObject(settings).toString();
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

    public boolean haveConnection() {
        try {
            URL url = new URL("http://www.google.de");
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

    public int calculateColor(int color1, int color2, int max, int current) {

        String strC1 = Integer.toHexString(color1);
        String strC2 = Integer.toHexString(color2);

        StringBuilder retColor = new StringBuilder("#");

        for (int i = 2; i < strC1.length(); i++) {

            String tmp1 = strC1.charAt(i) + "" + strC1.charAt(i + 1);
            String tmp2 = strC2.charAt(i) + "" + strC2.charAt(i + 1);

            int tmp1Color = (int) Long.parseLong(tmp1, 16);
            int tmp2Color = (int) Long.parseLong(tmp2, 16);

            int dif = tmp1Color - tmp2Color;

            double difCalc = (double) dif / max;

            int colorMerge = (int) (difCalc * current);

            String add = Integer.toHexString(tmp2Color + colorMerge);

            if (add.length() < 2)
                add = "0" + add;

            retColor.append(add);

            i++;

        }

        return Color.parseColor(retColor.toString());

    }

    public int getNavigationBarColor() {
        return getWindow().getNavigationBarColor();
    }

    public void setNavigationBarColor(@ColorInt int color) {
        getWindow().setNavigationBarColor(color);
    }

    public int getStatusBarColor() {
        return getWindow().getStatusBarColor();
    }

    public void setStatusBarColor(@ColorInt int color) {
        getWindow().setStatusBarColor(color);
    }

    public boolean checkAppDir() {
        File appDir = new File(Environment.getExternalStorageDirectory(), ".appStore");
        deleteDirectory(appDir);
        return appDir.mkdirs();
    }

    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }

    public void checkForOldAppStore() {
        if (! getSettings(this).getBoolean("old_appstore", false)) {
            if (appInstalled(this, oldAppPackageName)) {
                DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = new Intent(Intent.ACTION_DELETE);
                            intent.setData(Uri.parse("package:" + oldAppPackageName));
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
    }

    public boolean isRooted() {

        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception ignored) {
        }

        return runCommand("/system/xbin/which su")
                || runCommand("/system/bin/which su") || runCommand("which su");
    }

    public boolean runCommand(String command) {
        logs = new Logs(this);
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            logs.error("ERROR", e.getMessage());
            executedSuccesfully = false;
        }
        return executedSuccesfully;
    }

    public void checkPermissions() {
        ActivityCompat.requestPermissions(
                this,
                permissons().toArray(new String[0]),
                PERMISSIONS
        );
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

    private List<String> permissons() {
        return new ArrayList<>(Arrays.asList(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS
        ));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this,
                        permission
                ) != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }

    protected void syncPreferences() {
        new Thread(() -> {
            SharedPreferences preferences = getSettings(this);
            SharedPreferences.Editor editor = preferences.edit();
            SharedPreferences colors = getSharedPreferences("colors_" + UID(this), MODE_PRIVATE);
            SharedPreferences.Editor editorColor = colors.edit();

            try {
                JSONObject jsonObject = new JSONObject(
                        getWebContent(API_URL + "/apps/prefs.php?user=" + UID(this)));

                for (
                        Iterator<String> it = jsonObject.getJSONObject("prefs").keys(); it
                        .hasNext(); ) {
                    String key = it.next();
                    Object obj = jsonObject.getJSONObject("prefs").get(key);
                    if (obj instanceof String)
                        editor.putString(key, (String) obj);
                    else if (obj instanceof Integer)
                        editor.putInt(key, (int) obj);
                    else if (obj instanceof Long)
                        editor.putLong(key, (long) obj);
                    else if (obj instanceof Float)
                        editor.putFloat(key, (float) obj);
                    else if (obj instanceof Boolean)
                        editor.putBoolean(key, (boolean) obj);
                    else if (obj instanceof Set)
                        editor.putStringSet(key, (Set<String>) obj);

                }

                for (
                        Iterator<String> it = jsonObject.getJSONObject("colors").keys(); it
                        .hasNext(); ) {
                    String key = it.next();
                    Object obj = jsonObject.getJSONObject("colors").get(key);
                    if (obj instanceof String)
                        editorColor.putString(key, (String) obj);
                    else if (obj instanceof Integer)
                        editorColor.putInt(key, (int) obj);
                    else if (obj instanceof Long)
                        editorColor.putLong(key, (long) obj);
                    else if (obj instanceof Float)
                        editorColor.putFloat(key, (float) obj);
                    else if (obj instanceof Boolean)
                        editorColor.putBoolean(key, (boolean) obj);
                    else if (obj instanceof Set)
                        editorColor.putStringSet(key, (Set<String>) obj);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            editor.apply();
            editorColor.apply();
            Log.d("PREFS", preferences.getAll().toString());
            Log.d("COLORS", colors.getAll().toString());
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (toolbar != null)
            toolbar.setToolbarIconColor(ThemeStore.getInstance(this).getPrimaryColor());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public interface Async {

        void run(Activity activity, Class aClass, Bundle options);
    }

    public interface Listener {

        void run(int progress);
    }

    public interface Callback {

        void run();
    }

    public static class ByteBuffer {

        private final byte[] bytes;
        private final int buffer;

        public ByteBuffer(byte[] bytes, int buffer) {
            this.bytes = bytes;
            this.buffer = buffer;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public int getBuffer() {
            return buffer;
        }
    }

    public class startActivityAsync {

        private Activity activity;
        private Class aClass;
        private Bundle options;
        private long time = 0;

        public startActivityAsync(Activity activity, Class aClass) {
            this(activity, aClass, null);
        }

        public startActivityAsync(Activity activity, Class aClass, Bundle options) {
            this.activity = activity;
            this.aClass = aClass;
            this.options = options;
        }

        public startActivityAsync setTime(long time) {
            this.time = time;
            return this;
        }

        public void start(Async async) {
            new Thread(() -> {
                sleep(time);
                async.run(activity, aClass, options);
            }).start();
        }
    }
}
