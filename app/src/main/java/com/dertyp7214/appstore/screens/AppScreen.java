/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.CustomAppBarLayout;
import com.dertyp7214.appstore.components.CustomSnackbar;
import com.dertyp7214.appstore.components.CustomToolbar;
import com.dertyp7214.appstore.components.Notifications;
import com.dertyp7214.appstore.fragments.FragmentAppInfo;
import com.dertyp7214.appstore.fragments.FragmentChangeLogs;
import com.dertyp7214.appstore.interfaces.MyInterface;
import com.dertyp7214.appstore.items.SearchItem;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import static com.dertyp7214.appstore.Config.API_URL;
import static com.dertyp7214.appstore.Config.APP_URL;
import static com.dertyp7214.appstore.helpers.QRCodeHelper.generateQRCode;

public class AppScreen extends Utils implements View.OnClickListener, MyInterface {

    private static final int MENU_UNINSTALL = Menu.FIRST + 2;
    @SuppressLint("UseSparseArrays")
    public static HashMap<Integer, Download> downloadHashMap = new HashMap<>();
    @SuppressLint("StaticFieldLeak")
    private static AppScreen instance;
    @SuppressLint("UseSparseArrays")
    private static HashMap<Integer, Thread> threadHashMap = new HashMap<>();
    @ColorInt
    private int dominantColor, oldColor;
    private FloatingActionButton fab;
    private SearchItem searchItem;
    private ThemeStore themeStore;
    private Button uninstall, open;
    private boolean installed;
    private String version;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FragmentChangeLogs changeLogs;
    private MenuItem shareMenu;
    private ProgressDialog progressDialog;

    public static boolean hasInstance() {
        return instance != null;
    }

    public static AppScreen getInstance() {
        return instance;
    }

    public static void downloadApp(Activity activity, String title, String id, View view) {
        downloadApp(activity, title, id, new DownloadListener() {
            @Override
            public void started() {
                int color = activity.getWindow().getNavigationBarColor() == Color.BLACK ?
                        activity.getWindow().getStatusBarColor() : activity.getWindow()
                        .getNavigationBarColor();
                new CustomSnackbar(activity, color)
                        .make(view, "Download started", CustomSnackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            @Override
            public void finished(File file) {
                if (getSettings(activity).getBoolean("root_install", false)) {
                    executeCommand(activity, "rm -rf /data/local/tmp/app.apk");
                    executeCommand(activity,
                            "mv " + file.getAbsolutePath() + " /data/local/tmp/app.apk");
                    executeCommand(activity, "pm install -r /data/local/tmp/app.apk\n");
                } else
                    Utils.install_apk(activity, file);
            }

            @Override
            public void error(String errorMessage) {

            }
        });
    }

    private static void downloadApp(Activity activity, String title, String id, DownloadListener downloadListener) {
        Random random = new Random();
        int notiId = random.nextInt(65536);
        int finishedNotiId = random.nextInt(65536);
        Thread thread = new Thread(() -> {
            Looper.prepare();
            Notifications notifications = new Notifications(
                    activity,
                    notiId,
                    activity.getString(R.string.app_name) + " - " + title,
                    activity.getString(R.string.app_name) + " - " + title,
                    "",
                    null,
                    true);
            //notifications.setCancelButton();
            activity.runOnUiThread(notifications::showNotification);
            downloadListener.started();
            Download download = new Download(API_URL + (Config.APK_PATH
                    .replace("{id}", id)
                    .replace("{uid}", Config.UID(activity))));
            downloadHashMap.put(notiId, download);
            File file = new File(Environment.getExternalStorageDirectory(), ".appStore");
            File apk = download.startDownload(file, notiId,
                    (pro) -> activity.runOnUiThread(() -> notifications.setProgress(pro)));
            if (apk != null && apk.exists()) {
                activity.runOnUiThread(() -> {
                    notifications.removeNotification();
                    finishedNotification(
                            activity,
                            finishedNotiId,
                            activity.getString(R.string.app_name) + " - " + title,
                            false).showNotification();
                });
                downloadListener.finished(apk);
            } else {
                activity.runOnUiThread(() -> {
                    notifications.removeNotification();
                    finishedNotification(
                            activity,
                            finishedNotiId,
                            activity.getString(R.string.app_name) + " - " + title,
                            true).showNotification();
                });
                downloadListener.error("ERROR");
            }
        });
        thread.start();
        threadHashMap.put(notiId, thread);
    }

    private static Notifications finishedNotification(Activity activity, int id, String title, boolean error) {
        Notifications notifications = new Notifications(
                activity,
                id,
                title,
                title,
                "",
                null,
                false);
        if (error)
            notifications.setCanceled("ERROR");
        else
            notifications.setFinished();
        return notifications;
    }

    public void onPostExecute() {
        Bundle extra = getIntent().getExtras();
        if (searchItem == null)
            searchItem = Utils.appsList.get(checkExtra(extra).getString("id"));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_screen, false);
        CustomToolbar toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        setSupportActionBar(toolbar);

        new MyTask(this).execute();
        themeStore = ThemeStore.getInstance(AppScreen.this);

        uninstall = findViewById(R.id.btn_uninstall);
        open = findViewById(R.id.btn_open);

        dominantColor = Palette.from(Utils.drawableToBitmap(searchItem.getAppIcon()))
                .generate()
                .getDominantColor(ThemeStore.getInstance(this).getPrimaryColor());

        CustomAppBarLayout appBarLayout = getAppBar();

        setTitle(searchItem.getAppTitle());
        appBarLayout.setAppBarBackgroundColor(dominantColor);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ((ImageView) findViewById(R.id.app_icon)).setImageDrawable(searchItem.getAppIcon());
        collapsingToolbarLayout.setCollapsedTitleTextColor(themeStore.getPrimaryTextColor());
        collapsingToolbarLayout.setExpandedTitleColor(
                Utils.isColorBright(dominantColor) ? Color.BLACK : Color.WHITE);
        collapsingToolbarLayout.setContentScrimColor(themeStore.getPrimaryColor());
        collapsingToolbarLayout.setStatusBarScrimColor(themeStore.getPrimaryDarkColor());
        setButtonColor(themeStore.getAccentColor(), open, uninstall);

        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {
            int totalScroll = appBarLayout1.getTotalScrollRange();
            int currentScroll = totalScroll + verticalOffset;

            int colorDark = calculateColor(Utils.manipulateColor(dominantColor, 0.6F),
                    themeStore.getPrimaryDarkColor(), totalScroll, currentScroll);
            int color = calculateColor(dominantColor, themeStore.getPrimaryColor(), totalScroll,
                    currentScroll);

            getWindow().setNavigationBarColor(colorDark);
            getWindow().setStatusBarColor(colorDark);

            setColors(toolbar, appBarLayout1, color);
            collapsingToolbarLayout.setStatusBarScrimColor(colorDark);
            collapsingToolbarLayout.setContentScrimColor(color);
        });

        setUpButtons();

        fab = findViewById(R.id.fab);
        fab.setColorFilter(
                Utils.isColorBright(themeStore.getAccentColor()) ? Color.BLACK : Color.WHITE);
        fab.setBackgroundTintList(ColorStateList.valueOf(themeStore.getAccentColor()));
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(v -> share());

        instance = this;
    }

    public void setUpButtons() {
        if (applicationInstalled(this, searchItem.getId())) {
            installed = true;
            open.setText(getString(R.string.text_open));
            uninstall.setText(getString(R.string.text_uninstall));
            uninstall.setVisibility(View.VISIBLE);
            uninstall.setOnClickListener(this);
            open.setOnClickListener(this);
            if (! Utils.verifyInstallerId(this, searchItem.getId()))
                checkUpdates();
        } else {
            installed = false;
            open.setText(getString(R.string.text_install));
            uninstall.setVisibility(View.INVISIBLE);
            open.setOnClickListener(this);
        }
    }

    private void checkUpdates() {
        new Thread(() -> {
            String serverVersion = getServerVersion();
            String localVersion = getLocalVersion();
            Log.d("VERSIONS", "Server: " + serverVersion + "\nLocal: " + localVersion);
            if (! serverVersion.equals(localVersion) && ! serverVersion.equals("0")) {
                runOnUiThread(() -> {
                    uninstall.setText(getString(R.string.text_update));
                    uninstall.setOnClickListener(
                            v -> downloadApp(this, searchItem.getAppTitle(), searchItem.getId(),
                                    v));
                });
            }
        }).start();
    }

    private String getServerVersion() {
        if (version == null)
            version = getWebContent(API_URL + "/apps/list.php?version=" + searchItem.getId());
        return version;
    }

    private String getLocalVersion() {
        try {
            PackageInfo pinfo = getPackageManager().getPackageInfo(searchItem.getId(), 0);
            return pinfo.versionName;
        } catch (Exception e) {
            return getServerVersion();
        }
    }

    private void share() {
        String url = APP_URL(searchItem.getId());

        new MaterialDialog.Builder(this)
                .title(R.string.share)
                .content(R.string.popup_share_content)
                .positiveColor(themeStore.getAccentColor())
                .negativeColor(themeStore.getAccentColor())
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive((dialog, which) -> new Thread(() -> {
                    Looper.prepare();
                    runOnUiThread(() -> progressDialog = ProgressDialog
                            .show(this, "", getString(R.string.loading_generating_qr_code)));
                    dialog.dismiss();
                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(generateQRCode(this, url));
                    imageView.setLayoutParams(
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT));
                    runOnUiThread(() -> {
                        imageView.setOnClickListener(v -> {
                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.setType("image/jpeg");
                            Bitmap b = drawableToBitmap(imageView.getDrawable());
                            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                            b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                            String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                                    b, "Title", null);
                            Uri imageUri = Uri.parse(path);
                            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            startActivity(
                                    Intent.createChooser(sendIntent, getString(R.string.app_name)));
                        });
                        AlertDialog alertDialog = new AlertDialog.Builder(this)
                                .create();
                        alertDialog.setView(imageView);
                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
                                getString(android.R.string.yes),
                                (dialog1, which1) -> dialog1.dismiss());
                        alertDialog.show();
                        progressDialog.dismiss();
                    });
                }).start())
                .onNegative((dialog, which) -> {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)));
                    dialog.dismiss();
                })
                .build()
                .show();
    }

    private void setButtonColor(@ColorInt int color, Button button, Button button2) {
        GradientDrawable bg =
                (GradientDrawable) getResources().getDrawable(R.drawable.button_border);
        bg.setStroke(3, color);
        button2.setBackgroundDrawable(bg);
        button2.setTextColor(color);
        button.setTextColor(Utils.isColorBright(color) ? Color.BLACK : Color.WHITE);
        button.getBackground().setTint(color);
    }

    private void setColors(CustomToolbar customToolbar, AppBarLayout customAppBarLayout, @ColorInt int color) {
        customAppBarLayout.setBackgroundColor(color);
        customToolbar.setToolbarIconColor(color);
        changeLogs.setColor(color);
        if (shareMenu != null)
            shareMenu.getIcon().setTint(Utils.isColorBright(color) ? Color.BLACK : Color.WHITE);
    }

    private void navigationBarColor(Activity activity, AppBarLayout appBarLayout, @ColorInt int color, int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setNavigationBarColor(activity, appBarLayout, color, duration);
        }
    }

    private void statusBarColor(Activity activity, AppBarLayout appBarLayout, @ColorInt int color, int duration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setStatusBarColor(activity, appBarLayout, color, duration);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open:
                if (installed)
                    openApp();
                else
                    downloadApp(this, searchItem.getAppTitle(), searchItem.getId(), v);
                break;
            case R.id.btn_uninstall:
                removeApp();
        }
    }

    private void openApp() {
        Intent intent = getPackageManager().getLaunchIntentForPackage(searchItem.getId());
        startActivity(intent);
    }

    private void removeApp() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + searchItem.getId()));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fab.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        onPostExecute();
        if (fragment instanceof FragmentChangeLogs) {
            changeLogs = ((FragmentChangeLogs) fragment);
            new Thread(() -> changeLogs
                    .getChangeLogs(searchItem, (textView, text) -> runOnUiThread(() -> {
                        if (text != null) {
                            textView.setText(text);
                            textView.setMovementMethod(LinkMovementMethod.getInstance());
                            textView.setVisibility(View.VISIBLE);
                        }
                    }))).start();
        } else if (fragment instanceof FragmentAppInfo) {
            FragmentAppInfo appInfo = ((FragmentAppInfo) fragment);
            appInfo.getAppInfo(searchItem);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        new Thread(() -> {
            String serverVersion = getServerVersion();
            String localVersion = getLocalVersion();
            if (! serverVersion.equals(localVersion) && ! serverVersion.equals("0")
                    && menu.findItem(MENU_UNINSTALL) == null)
                runOnUiThread(
                        () -> menu.add(0, MENU_UNINSTALL, Menu.NONE, R.string.text_uninstall));
        }).start();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.app_menu, menu);

        int iconTint =
                Utils.isColorBright(themeStore.getPrimaryColor()) ? Color.BLACK : Color.WHITE;

        shareMenu = menu.findItem(R.id.action_share);
        shareMenu.getIcon().setTint(iconTint);

        MenuItem updateItem = menu.findItem(R.id.action_update);
        updateItem.setChecked(searchItem.isUpdate());
        updateItem.setOnMenuItemClickListener(item -> {
            item.setChecked(! item.isChecked());
            searchItem = new SearchItem(searchItem.getAppTitle(), searchItem.getId(),
                    searchItem.getAppIcon(), searchItem.getVersion(), item.isChecked());
            Utils.appsList.put(searchItem.getId(),
                    searchItem);
            new Thread(() -> Log.d("RETURN", getWebContent(
                    Config.API_URL + "/apps/myapps.php?update=" + item.isChecked() + "&uid="
                            + Config.UID(this) + "&app_id=" + searchItem.getId()))).start();
            return true;
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_share:
                share();
                break;
            case MENU_UNINSTALL:
                removeApp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private interface DownloadListener {
        void started();

        void finished(File file);

        void error(String errorMessage);
    }

    public static class Download {
        private String url;
        private Thread thread;
        private boolean finished = false;
        private File content;
        private int id;

        Download(String url) {
            this.url = url;
        }

        File startDownload(File path, int id, DownloadState downloadState) {
            this.id = id;
            thread = new Thread(() -> {
                content = Utils.getWebContent(url, path, id, downloadState::state);
                finished = true;
            });
            thread.start();
            while (! finished) {
                Log.d("WAITING", "...");
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return content;
        }

        public void cancel() {
            if (thread != null)
                thread.interrupt();
            try {
                threadHashMap.get(id).interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        interface DownloadState {
            void state(int percentage);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTask extends AsyncTask<Void, Void, Void> {
        MyInterface myinterface;

        MyTask(MyInterface mi) {
            myinterface = mi;
        }

        @Override
        protected Void doInBackground(Void... params) {
            myinterface.onPostExecute();
            return null;
        }
    }
}
