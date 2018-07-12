/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.CustomSnackbar;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.CustomAppBarLayout;
import com.dertyp7214.appstore.components.CustomToolbar;
import com.dertyp7214.appstore.components.Notifications;
import com.dertyp7214.appstore.fragments.FragmentAppInfo;
import com.dertyp7214.appstore.fragments.FragmentChangeLogs;
import com.dertyp7214.appstore.interfaces.MyInterface;
import com.dertyp7214.appstore.items.SearchItem;

import java.io.File;
import java.util.Objects;
import java.util.Random;

import static com.dertyp7214.appstore.Config.API_URL;
import static com.dertyp7214.appstore.Config.APP_URL;

public class AppScreen extends Utils implements View.OnClickListener, MyInterface {

    @ColorInt
    private int dominantColor, oldColor;
    private FloatingActionButton fab;
    private SearchItem searchItem;
    private Random random = new Random();
    private ThemeStore themeStore;
    private Button uninstall, open;
    private boolean installed;
    private String version;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FragmentChangeLogs changeLogs;
    private MenuItem shareMenu;

    public void onPostExecute() {
        Bundle extra = getIntent().getExtras();
        if (searchItem == null)
            searchItem = Utils.appsList.get(checkExtra(extra).getString("id"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_screen);
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

        if (appInstalled(this, searchItem.getId())) {
            installed = true;
            open.setText(getString(R.string.text_open));
            uninstall.setText(getString(R.string.text_uninstall));
            uninstall.setVisibility(View.VISIBLE);
            uninstall.setOnClickListener(this);
            open.setOnClickListener(this);
            checkUpdates();
        } else {
            installed = false;
            open.setText(getString(R.string.text_install));
            uninstall.setVisibility(View.INVISIBLE);
            open.setOnClickListener(this);
        }

        fab = findViewById(R.id.fab);
        fab.setColorFilter(
                Utils.isColorBright(themeStore.getAccentColor()) ? Color.BLACK : Color.WHITE);
        fab.setBackgroundTintList(ColorStateList.valueOf(themeStore.getAccentColor()));
        fab.setVisibility(View.GONE);
        fab.setOnClickListener(this::share);
    }

    private void checkUpdates() {
        new Thread(() -> {
            String serverVersion = getServerVersion();
            String localVersion = getLocalVersion();
            Log.d("VERSIONS", "Server: " + serverVersion + "\nLocal: " + localVersion);
            if (! serverVersion.equals(localVersion) && ! serverVersion.equals("0")) {
                runOnUiThread(() -> {
                    uninstall.setText(getString(R.string.text_update));
                    uninstall.setOnClickListener(this::downloadApp);
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

    private void share(View view) {
        String url = APP_URL(searchItem.getId());
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, url);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void downloadApp(View view) {
        downloadApp(new DownloadListener() {
            @Override
            public void started() {
                new CustomSnackbar(AppScreen.this, getWindow().getNavigationBarColor())
                        .make(view, "Download started", CustomSnackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }

            @Override
            public void finished(File file) {
                if (getSettings(AppScreen.this).getBoolean("root_install", false)) {
                    executeCommand("rm -rf /data/local/tmp/app.apk");
                    executeCommand("mv " + file.getAbsolutePath() + " /data/local/tmp/app.apk");
                    executeCommand("pm install -r /data/local/tmp/app.apk\n");
                } else
                    Utils.install_apk(AppScreen.this, file);
            }

            @Override
            public void error(String errorMessage) {

            }
        });
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
                    downloadApp(v);
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

    private interface DownloadListener {
        void started();

        void finished(File file);

        void error(String errorMessage);
    }

    private void downloadApp(DownloadListener downloadListener) {
        new Thread(() -> {
            Looper.prepare();
            int notiId = random.nextInt(65536);
            int finishedNotiId = random.nextInt(65536);
            Notifications notifications = new Notifications(
                    AppScreen.this,
                    notiId,
                    getString(R.string.app_name) + " - " + searchItem.getAppTitle(),
                    getString(R.string.app_name) + " - " + searchItem.getAppTitle(),
                    "",
                    null,
                    true);
            runOnUiThread(notifications::showNotification);
            downloadListener.started();
            Download download = new Download(API_URL + (Config.APK_PATH
                    .replace("{id}", searchItem.getId())
                    .replace("{uid}", Config.UID(this))));
            File file = new File(Environment.getExternalStorageDirectory(), ".appStore");
            File apk = download.startDownload(file, notiId,
                    (pro) -> runOnUiThread(() -> notifications.setProgress(pro)));
            if (apk.exists()) {
                runOnUiThread(() -> {
                    notifications.removeNotification();
                    finishedNotification(
                            finishedNotiId,
                            getString(R.string.app_name) + " - " + searchItem.getAppTitle(),
                            false).showNotification();
                });
                downloadListener.finished(apk);
            } else {
                runOnUiThread(() -> {
                    notifications.removeNotification();
                    finishedNotification(
                            finishedNotiId,
                            getString(R.string.app_name) + " - " + searchItem.getAppTitle(),
                            true).showNotification();
                });
                downloadListener.error("ERROR");
            }
        }).start();
    }

    private Notifications finishedNotification(int id, String title, boolean error) {
        Notifications notifications = new Notifications(
                AppScreen.this,
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

    private static class Download {
        private String url;

        Download(String url) {
            this.url = url;
        }

        File startDownload(File path, int id, DownloadState downloadState) {
            return Utils.getWebContent(url, path, id, downloadState::state);
        }

        interface DownloadState {
            void state(int percentage);
        }
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
                share(null);
        }

        return super.onOptionsItemSelected(item);
    }
}
