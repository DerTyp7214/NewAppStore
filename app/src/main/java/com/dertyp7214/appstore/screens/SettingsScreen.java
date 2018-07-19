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

public class SettingsScreen extends Utils implements MyInterface {

    private BottomSheetBehavior bottomSheetBehavior;
    private boolean setUp = false;

    private static final String CONFIG_CLIENT_ID = SecretConfig.CONFIG_CLIENT_ID;
    private static final String CONFIG_CLIENT_ID_SANDBOX = SecretConfig.CONFIG_CLIENT_ID_SANDBOX;
    private static final String CONFIG_ENVIRONMENT =
            BuildConfig.DEBUG ? PayPalConfiguration.ENVIRONMENT_SANDBOX
                    : PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    private static final int REQUEST_CODE_PAYMENT = 1;

    public void onPostExecute() {
        if (! setUp)
            setUpBottomSheet();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new MyTask(this).execute();

        themeStore = ThemeStore.getInstance(this);

        if (! getSettings(this).getBoolean("root_install", false)) {
            Config.root = isRooted();
        } else {
            Config.root = true;
        }

        LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
        View backGround = findViewById(R.id.bg);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        bottomSheetBehavior.setState(STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == STATE_HIDDEN)
                    backGround.setVisibility(View.GONE);
                else
                    backGround.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                float offset = (1 - (- slideOffset)) / 1 * 0.7F;
                if (String.valueOf(offset).equals("NaN"))
                    offset = 0.7F;
                try {
                    int color = Color.parseColor(addAlpha("#000000", offset));
                    backGround.setBackgroundColor(color);
                } catch (Exception ignored) {
                }
            }
        });

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
        if (requestCode == REQUEST_CODE_PAYMENT) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm = data
                        .getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        JSONObject jsonObject = confirm.toJSONObject();
                        JSONObject response = jsonObject.getJSONObject("response");
                        JSONObject payment = confirm.getPayment().toJSONObject();

                        @SuppressLint("SimpleDateFormat")
                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                "yyyy-MM-dd'T'HH:mm:ss'Z'");

                        String br = "<br/>";

                        String content = ""
                                + getH4("Id: ") + response.getString("id") + br
                                + getH4("Time: ") + dateFormat
                                .parse(response.getString("create_time")).toString() + br
                                + getH4("State: ") + response.getString("state") + br
                                + getH4("Amount: ") + payment.getString("amount") + getString(
                                R.string.currency) + br
                                + getH4("Description: ") + payment.getString("short_description");

                        new MaterialDialog.Builder(this)
                                .title("Results")
                                .content(Html.fromHtml(content))
                                .positiveText(android.R.string.yes)
                                .show();

                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d("PAYPAL", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.d(
                        "PAYPAL",
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs."
                );
            }
        } else if (requestCode == 10 && resultCode == RESULT_OK) {
            ProgressDialog prog = new ProgressDialog(SettingsScreen.this);
            prog.setMessage("Uploading");
            prog.setCancelable(false);
            prog.show();

            SQLiteHandler db = new SQLiteHandler(getApplicationContext());
            HashMap<String, String> user = db.getUserDetails();
            final String userID = user.get("uid");

            Thread t = new Thread(() -> {
                final File f = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
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
                        .addFormDataPart("name", userID.replace(" ", "_"))
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
                    File imgFile = new File(getFilesDir(), userID + ".png");
                    if (imgFile.exists()) if (imgFile.delete()) prog.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                prog.dismiss();
            });
            t.start();
        }
    }

    private String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private String getH4(String string) {
        return "<h4 style=\"display:inline\">" + string + "</h4>";
    }

    private void setUpBottomSheet() {
        setUp = true;
        EditText editText = findViewById(R.id.text_amount);
        Button button = findViewById(R.id.btn_pay);

        button.setTextColor(themeStore.getAccentColor());

        tintWidget(editText, themeStore.getAccentColor());
        setCursorColor(editText, themeStore.getAccentColor());

        final PayPalConfiguration config = new PayPalConfiguration()
                .environment(CONFIG_ENVIRONMENT)
                .clientId(BuildConfig.DEBUG ? CONFIG_CLIENT_ID_SANDBOX : CONFIG_CLIENT_ID)
                .merchantName("AppStore")
                .merchantPrivacyPolicyUri(
                        Uri.parse("https://www.example.com/privacy"))
                .merchantUserAgreementUri(
                        Uri.parse("https://www.example.com/legal"));

        Intent service = new Intent(this, PayPalService.class);
        service.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(service);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                int length = text.length();
                String[] strings = text.split("\\.");
                if (strings.length > 1)
                    if (length > 0 && ! Pattern.matches("[0-9]{0,2}", strings[1]) && strings[1]
                            .length() > 1)
                        s.delete(length - 1, length);
            }
        });

        button.setOnClickListener(v -> {
            bottomSheetBehavior.setState(STATE_HIDDEN);
            String amount = editText.getText().toString();
            if (amount.length() > 0) {
                new MaterialDialog.Builder(this)
                        .title(String.format(String.format(getString(R.string.text_pay_amount),
                                amount.replace(".", ",") + "%s"
                        ), getString(R.string.currency)))
                        .content(R.string.text_pay_content)
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.no)
                        .onPositive((dialog, which) -> {
                            PayPalPayment donating = new PayPalPayment(new BigDecimal(amount),
                                    getString(
                                            R.string.payment_lang),
                                    "Donation",
                                    PayPalPayment.PAYMENT_INTENT_SALE
                            );
                            Intent intent = new Intent(
                                    SettingsScreen.this,
                                    PaymentActivity.class
                            );

                            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, donating);

                            startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                        })
                        .show();
            }
        });
    }

    private List<Settings> getSettings() {
        List<Settings> settingsList = new ArrayList<>(Arrays.asList(
                new SettingsPlaceholder("appdetails", getString(R.string.text_appdetails), this),
                new Settings("version", getString(R.string.text_version), this)
                        .setSubTitle(BuildConfig.VERSION_NAME),
                new Settings("check_update", getString(R.string.text_check_update), this)
                        .setSubTitle(getString(R.string.text_click_check))
                        .addSettingsOnClick(
                                (name, instance, subTitle, imageRight) -> checkForUpdate(instance,
                                        subTitle, imageRight)),
                new Settings("sourcecode", "Sourcecode", this)
                        .setSubTitle(getString(R.string.text_sourcecode))
                        .addSettingsOnClick((name, setting, subTitle, imageRight) -> {
                            ProgressDialog progressDialog = new ProgressDialog(this);
                            progressDialog.setMessage("Loading");
                            progressDialog.show();
                            new Thread(() -> {
                                ThemeStore store = ThemeStore.getInstance(SettingsScreen.this);
                                GitHubSource.getInstance(
                                        SettingsScreen.this,
                                        new Repository("dertyp7214", "NewAppStore",
                                                getSettings(
                                                        SettingsScreen.this)
                                                        .getString(
                                                                "API_KEY",
                                                                null
                                                        ))
                                ).setColorStyle(new ColorStyle(
                                        store.getPrimaryColor(),
                                        store.getPrimaryDarkColor(),
                                        store.getAccentColor()
                                )).open();
                                runOnUiThread(progressDialog::dismiss);
                            }).start();
                        }),
                new Settings("donate_paypal", getString(R.string.text_donate), this)
                        .setSubTitle(getString(R.string.text_donate_sub))
                        .addSettingsOnClick(
                                (name, instance, subTitle, imageRight) -> bottomSheetBehavior
                                        .setState(BottomSheetBehavior.STATE_EXPANDED)),
                new Settings("text_build_type", getString(R.string.text_build_type), this)
                        .setSubTitle(BuildConfig.BUILD_TYPE),
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
        if (bottomSheetBehavior.getState() == STATE_EXPANDED)
            bottomSheetBehavior.setState(STATE_HIDDEN);
        else {
            syncPreferencesToServer();
            super.onBackPressed();
        }
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

    @Override
    public void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
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
