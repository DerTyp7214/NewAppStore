/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.support.v7.widget.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.webkit.*;
import android.widget.*;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dertyp7214.appstore.BuildConfig;
import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.SecretConfig;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.adapter.SettingsAdapter;
import com.dertyp7214.appstore.components.InputDialog;
import com.dertyp7214.appstore.helpers.SQLiteHandler;
import com.dertyp7214.appstore.interfaces.MyInterface;
import com.dertyp7214.appstore.settings.Settings;
import com.dertyp7214.appstore.settings.SettingsColor;
import com.dertyp7214.appstore.settings.SettingsPlaceholder;
import com.dertyp7214.appstore.settings.SettingsSlider;
import com.dertyp7214.appstore.settings.SettingsSwitch;
import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.github.Repository;
import com.dertyp7214.githubsource.helpers.ColorStyle;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.support.design.widget.BottomSheetBehavior.*;
import static com.dertyp7214.appstore.Config.API_URL;
import static com.dertyp7214.appstore.Config.UID;

public class SettingsScreen extends Utils {

    private ProgressDialog prog;
    private boolean profileImage = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        themeStore = ThemeStore.getInstance(this);

        if (! getSettings(this).getBoolean("root_install", false)) {
            Config.root = isRooted();
        } else {
            Config.root = true;
        }

        setColors();

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        RecyclerView settingList = findViewById(R.id.setting_rv);

        SettingsAdapter settingsAdapter = new SettingsAdapter(getSettings(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        settingList.setLayoutManager(layoutManager);
        settingList.setItemAnimator(new DefaultItemAnimator());
        settingList.setAdapter(settingsAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                settingList.getContext(), layoutManager.getOrientation());
        settingList.addItemDecoration(dividerItemDecoration);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10 && resultCode == RESULT_OK) {
            final File f = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            profileImage = true;
            CropImage.activity(Uri.fromFile(f))
                    .setAspectRatio(1, 1)
                    .start(this);
        } else if (requestCode == 11 && resultCode == RESULT_OK) {
            final File f = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            profileImage = false;
            CropImage.activity(Uri.fromFile(f))
                    .setAspectRatio(16, 9)
                    .start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
                && resultCode == RESULT_OK) {

            SQLiteHandler db = new SQLiteHandler(getApplicationContext());
            HashMap<String, String> user = db.getUserDetails();
            final String userID = user.get("uid");

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri uri = result.getUri();

            File f = new File(Objects.requireNonNull(uri.getPath()));

            Thread t = new Thread(() -> {
                String content_type = getMimeType(f.getPath());
                final String file_path = f.getAbsolutePath();
                OkHttpClient client = new OkHttpClient();
                final RequestBody file_body = RequestBody.create(MediaType.parse(content_type), f);
                RequestBody request_body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", content_type)
                        .addFormDataPart(
                                "uploaded_file",
                                file_path.substring(file_path.lastIndexOf("/") + 1), file_body
                        )
                        .addFormDataPart("name",
                                userID.replace(" ", "_") + (profileImage ? "" : "_bg"))
                        .build();
                Request request = new Request.Builder()
                        .url(API_URL + "/apps/upload.php")
                        .post(request_body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    assert response.body() != null;
                    Log.d("RESPONSE:", response.body().string());
                    if (! response.isSuccessful()) {
                        throw new IOException("Error : " + response);
                    }
                    File imgFile =
                            new File(getFilesDir(), userID + (profileImage ? "" : "_bg") + ".png");
                    if (imgFile.exists())
                        if (imgFile.delete()) logs.info("changeProfileImage", "deleted!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.start();
        }
    }

    private String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private List<Settings> getSettings() {
        List<Settings> settingsList = new ArrayList<>(Arrays.asList(
                new SettingsPlaceholder("preferences", getString(R.string.text_prefs), this),
                new Settings("api_key", getString(R.string.text_api_key), this).setSubTitle(
                        cutString(
                                getSettings(SettingsScreen.this)
                                        .getString("API_KEY", getString(R.string.text_not_set)),
                                30
                        )).addSettingsOnClick((name, setting, subTitle, imageRight) -> {
                    InputDialog dialog = new InputDialog(
                            getString(R.string.text_api_key), getSettings(SettingsScreen.this)
                            .getString("API_KEY", ""), getString(R.string.text_api_key),
                            SettingsScreen.this
                    );
                    dialog.setListener(new InputDialog.Listener() {
                        @Override
                        public void onSubmit(String text) {
                            Utils.getSettings(SettingsScreen.this).edit().putString("API_KEY", text)
                                    .apply();
                            subTitle.setText(cutString(
                                    getSettings(SettingsScreen.this).getString("API_KEY", getString(
                                            R.string.text_not_set)), 30));
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    dialog.show();
                }),
                new SettingsSwitch(
                        "dev_mode", getString(R.string.text_dev_mode), this,
                        getSettings(this).getBoolean("dev_mode", false)
                ).setCheckedChangeListener(value -> getSettings(SettingsScreen.this).edit()
                        .putBoolean("dev_mode", value).apply())
        ));
        if (appInstalled(this, oldAppPackageName)) {
            SettingsSwitch settingsSwitch = new SettingsSwitch(
                    "old_appstore", getString(R.string.askForUninstallOldAppstore), this,
                    getSettings(this).getBoolean("old_appstore", false)
            );
            settingsSwitch.setCheckedChangeListener(
                    value -> getSettings(this).edit().putBoolean("old_appstore", value).apply());
            settingsList.add(settingsSwitch);
        }
        if (Config.root) {
            SettingsSwitch settingsSwitch = new SettingsSwitch(
                    "root_install", getString(R.string.text_root_install), this, getSettings(this)
                    .getBoolean("root_install", false));
            settingsSwitch.setCheckedChangeListener(value -> {
                if (value) {
                    if (runCommand("su"))
                        getSettings(SettingsScreen.this).edit().putBoolean("root_install", true)
                                .apply();
                    else
                        settingsSwitch.setChecked(false);
                } else {
                    getSettings(SettingsScreen.this).edit().putBoolean("root_install", false)
                            .apply();
                }
            });
            settingsList.add(settingsSwitch);
        }
        settingsList.addAll(new ArrayList<>(Arrays.asList(
                new SettingsPlaceholder("style", getString(R.string.text_style), this),
                new SettingsColor(
                        "color_primary", getString(R.string.text_color), this,
                        themeStore.getPrimaryColor()
                ).addSettingsOnClick((name, Color, settingsColor) -> {
                    settingsColor.saveSetting();
                    setColors();
                }),
                new SettingsSwitch(
                        "colored_nav_bar", getString(R.string.text_colored_navbar), this,
                        getSettings(this).getBoolean("colored_nav_bar", false)
                ).setCheckedChangeListener(value -> {
                    getSettings(SettingsScreen.this).edit().putBoolean("colored_nav_bar", value)
                            .apply();
                    setNavigationBarColor(
                            this, getWindow().getDecorView(),
                            ThemeStore.getInstance(this).getPrimaryColor(), 300
                    );
                }),
                new SettingsSlider("search_bar_radius", getString(R.string.search_bar_radius),
                        this),
                new SettingsPlaceholder(
                        "user_preferences", getString(R.string.text_user_preferences), this),
                new Settings(
                        "change_profile_pic", getString(R.string.text_change_profile_pic), this)
                        .addSettingsOnClick(
                                (name, instance, subTitle, imageRight) -> new MaterialFilePicker()
                                        .withActivity(SettingsScreen.this)
                                        .withRequestCode(10)
                                        .withFilter(Pattern.compile(".*\\.(png|jpg|jpeg)$"))
                                        .start()),
                new Settings(
                        "change_bg_pic", getString(R.string.text_change_bg_pic), this)
                        .addSettingsOnClick(
                                (name, instance, subTitle, imageRight) -> new MaterialFilePicker()
                                        .withActivity(SettingsScreen.this)
                                        .withRequestCode(11)
                                        .withFilter(Pattern.compile(".*\\.(png|jpg|jpeg)$"))
                                        .start())
        )));
        if (BuildConfig.DEBUG) {
            SettingsSwitch hideIcon = new SettingsSwitch(
                    "hide_appicon", getString(R.string.text_hide_icon), this, getSettings(this)
                    .getBoolean("hide_appicon", false))
                    .setCheckedChangeListener(value -> {
                        getSettings(this).edit().putBoolean("hide_appicon", value).apply();
                        PackageManager p = getPackageManager();
                        ComponentName componentName = new ComponentName(this, Launcher.class);
                        if (value)
                            p.setComponentEnabledSetting(
                                    componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    PackageManager.DONT_KILL_APP
                            );
                        else
                            p.setComponentEnabledSetting(
                                    componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                    PackageManager.DONT_KILL_APP
                            );
                    });
            settingsList
                    .add(new SettingsPlaceholder("debug", getString(R.string.text_debug), this));
            settingsList.add(hideIcon);
        }
        return settingsList;
    }

    @Override
    public void onBackPressed() {
        syncPreferencesToServer();
        super.onBackPressed();
    }

    private void syncPreferencesToServer() {
        new Thread(() -> {
            SharedPreferences preferences = getSettings(this);
            SharedPreferences colors = getSharedPreferences("colors_" + UID(this), MODE_PRIVATE);
            JSONObject jsonObject = new JSONObject();
            JSONObject prefs = new JSONObject();
            JSONObject color = new JSONObject();

            try {

                for (String key : preferences.getAll().keySet()) {
                    prefs.put(key, preferences.getAll().get(key));
                }
                for (String key : colors.getAll().keySet()) {
                    color.put(key, colors.getAll().get(key));
                }

                jsonObject.put("prefs", prefs);
                jsonObject.put("colors", color);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            getWebContent(API_URL + "/apps/prefs.php?user=" + UID(this) + "&prefs=" + jsonObject
                    .toString());
        }).start();
    }
}
