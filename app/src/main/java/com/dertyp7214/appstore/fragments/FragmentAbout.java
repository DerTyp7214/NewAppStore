/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.dertyp7214.appstore.BuildConfig;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.SecretConfig;
import com.dertyp7214.appstore.ThemeStore;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.components.ChangelogDialog;
import com.dertyp7214.appstore.screens.SettingsScreen;
import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.github.Repository;
import com.dertyp7214.githubsource.helpers.ColorStyle;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD2ClauseLicense;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static com.dertyp7214.appstore.SecretConfig.CONFIG_CLIENT_ID;
import static com.dertyp7214.appstore.SecretConfig.CONFIG_CLIENT_ID_SANDBOX;
import static com.dertyp7214.appstore.Utils.addAlpha;
import static com.dertyp7214.appstore.Utils.getSettings;
import static com.dertyp7214.appstore.Utils.setCursorColor;
import static com.dertyp7214.appstore.Utils.tintWidget;

@SuppressLint("ValidFragment")
public class FragmentAbout extends MaterialAboutFragment {

    public static HashMap<String, HashMap<String, Object>> users = new HashMap<>();
    public BottomSheetBehavior bottomSheetBehavior;
    private boolean setUp = false;

    private Activity activity;

    private static final String CONFIG_CLIENT_ID = SecretConfig.CONFIG_CLIENT_ID;
    private static final String CONFIG_CLIENT_ID_SANDBOX = SecretConfig.CONFIG_CLIENT_ID_SANDBOX;
    private static final String CONFIG_ENVIRONMENT =
            BuildConfig.DEBUG ? PayPalConfiguration.ENVIRONMENT_SANDBOX
                    : PayPalConfiguration.ENVIRONMENT_PRODUCTION;

    private static final int REQUEST_CODE_PAYMENT = 1;

    public FragmentAbout(Activity activity) {
        this.activity = activity;

        if (! setUp)
            setUpBottomSheet();

        LinearLayout bottomSheet = activity.findViewById(R.id.bottom_sheet);
        View backGround = activity.findViewById(R.id.bg);

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

        backGround.setOnClickListener(v -> bottomSheetBehavior.setState(STATE_HIDDEN));
    }

    @Override
    protected MaterialAboutList getMaterialAboutList(Context context) {
        final Notices notices = new Notices();
        notices.addNotice(
                new Notice("Android Support",
                        "https://android.googlesource.com",
                        "Copyright (C) 2006 The Android Open Source Project",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("GitHubSource",
                        "https://github.com/DerTyp7214/GitHubSource",
                        "Copyright (c) 2018 Josua Lengwenath",
                        new MITLicense()));
        notices.addNotice(
                new Notice("RootBeer",
                        "https://github.com/scottyab/rootbeer",
                        "Copyright (C) 2015, Scott Alexander-Bown, Mat Rollings",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("RoundedImageView",
                        "https://github.com/vinc3m1/RoundedImageView",
                        "Copyright 2017 Vincent Mi",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("material-about-library",
                        "https://github.com/daniel-stoneuk/material-about-library",
                        "Copyright 2016-2018 Daniel Stone",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("Android System Bar Tint",
                        "https://github.com/jgilfelt/SystemBarTint",
                        "Copyright 2013 readyState Software Limited",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("PayPal Android SDK",
                        "https://github.com/paypal/PayPal-Android-SDK",
                        "Copyright (c) 2014-2016 PayPal Holdings, Inc.",
                        new BSD2ClauseLicense()));
        notices.addNotice(
                new Notice("Material Dialogs",
                        "https://github.com/afollestad/material-dialogs",
                        "Copyright (c) 2014-2016 Aidan Michael Follestad",
                        new MITLicense()));
        notices.addNotice(
                new Notice("Lottie for Android",
                        "https://github.com/airbnb/lottie-android",
                        "Copyright 2018 Airbnb, Inc.",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("Volley",
                        "https://github.com/mcxiaoke/android-volley",
                        "Copyright (C) 2014,2015,2016 Xiaoke Zhang\nCopyright (C) 2011 The Android Open Source Project",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("Material SearchBar Android Material Design Search Bar for Android",
                        "https://github.com/mancj/MaterialSearchBar",
                        "Copyright (c) 2016 mancj",
                        new MITLicense()));
        notices.addNotice(
                new Notice("CircularImageView",
                        "https://github.com/lopspower/CircularImageView",
                        "Copyright 2018 LOPEZ Mikhael",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("Transitioner",
                        "https://github.com/dev-labs-bg/transitioner",
                        "Copyright (c) 2017 Radoslav Yankov",
                        new MITLicense()));
        notices.addNotice(
                new Notice("PRDownloader",
                        "https://github.com/MindorksOpenSource/PRDownloader",
                        "Copyright (C) 2017 MINDORKS NEXTGEN PRIVATE LIMITED",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("ExpansionPanel",
                        "https://github.com/florent37/ExpansionPanel",
                        "Copyright 2017 Florent37, Inc.",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("Butter Knife",
                        "https://github.com/JakeWharton/butterknife",
                        "Copyright 2013 Jake Wharton",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("FancyToast-Android",
                        "https://github.com/Shashank02051997/FancyToast-Android",
                        "Copyright 2017 Shashank Singhal",
                        new ApacheSoftwareLicense20()));
        notices.addNotice(
                new Notice("Android Image Cropper",
                        "https://github.com/ArthurHub/Android-Image-Cropper",
                        "Copyright 2016, Arthur Teplitzki 2013, Edmodo, Inc.",
                        new ApacheSoftwareLicense20()));

        MaterialAboutCard card = new MaterialAboutCard.Builder()
                .title(R.string.text_authors)
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.text_main_author)
                        .subText((String) getUSerMap(getString(R.string.text_dertyp7214), context)
                                .get("name"))
                        .icon((Drawable) getUSerMap(getString(R.string.text_dertyp7214), context)
                                .get("image"))
                        .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                        .setOnClickAction(() -> openGitHubProfile("DerTyp7214"))
                        .build())
                .build();

        MaterialAboutCard translators = new MaterialAboutCard.Builder()
                .title(R.string.text_translators)
                .addItem(translator(
                        getString(R.string.text_english) + ", " + getString(R.string.text_german),
                        getString(R.string.text_dertyp7214),
                        context))
                .addItem(translator(getString(R.string.text_spainish),
                        getString(R.string.text_enol_simon),
                        context))
                .build();

        MaterialAboutCard about = new MaterialAboutCard.Builder()
                .addItem(new MaterialAboutTitleItem.Builder()
                        .icon(R.mipmap.ic_launcher)
                        .text(R.string.app_name)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_info_outline_black)
                        .text(R.string.text_version)
                        .subText(BuildConfig.VERSION_NAME)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_build_black_24dp)
                        .text(R.string.text_build_type)
                        .subText(BuildConfig.BUILD_TYPE)
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_update_black_24dp)
                        .text(R.string.text_changes)
                        .setOnClickAction(() -> new ChangelogDialog(context))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.ic_donate)
                        .text(R.string.text_donate_sub)
                        .setOnClickAction(() -> bottomSheetBehavior
                                .setState(BottomSheetBehavior.STATE_EXPANDED))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .icon(R.drawable.github)
                        .text(R.string.text_project_github)
                        .setOnClickAction(() -> openSourceCode(context))
                        .build())
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(R.string.text_licenses)
                        .icon(R.drawable.file_icon)
                        .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                        .setOnClickAction(() -> {
                            try {
                                new LicensesDialog.Builder(Objects.requireNonNull(getActivity()))
                                        .setNotices(notices)
                                        .setIncludeOwnLicense(true)
                                        .build()
                                        .show();
                            } catch (Exception ignored) {
                            }
                        })
                        .build())
                .build();

        return new MaterialAboutList.Builder()
                .addCard(about)
                .addCard(card)
                .addCard(translators)
                .build();
    }

    private HashMap<String, Object> getUSerMap(String userName, Context context) {
        HashMap<String, Object> userMap = new HashMap<>();
        if (users.containsKey(userName))
            return users.get(userName);
        try {
            JSONObject jsonObject = new JSONObject(
                    getJSONObject("https://api.github.com/users/" + userName, context));
            userMap.put("id", jsonObject.getString("id"));
            String name = jsonObject.getString("name").equals("null") ? jsonObject
                    .getString("login") : jsonObject.getString("name");
            userMap.put("name", name);
            userMap.put("image", Utils.drawableFromUrl(context,
                    "https://avatars0.githubusercontent.com/u/" + userMap.get("id")));
        } catch (Exception e) {
            e.printStackTrace();
            userMap.put("image", context.getResources().getDrawable(R.mipmap.ic_launcher));
            userMap.put("name", context.getString(R.string.app_name));
        }
        users.put(userName, userMap);
        return userMap;
    }

    private String getJSONObject(String url, Context context) {
        String api_key = getSettings(context).getString("API_KEY", null);
        try {
            URL web = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) web.openConnection();
            connection.setRequestProperty("Authorization", "token " + api_key);
            BufferedReader in;

            if (api_key == null || api_key.equals(""))
                in = new BufferedReader(new InputStreamReader(web.openStream()));
            else
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder ret = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                ret.append(inputLine);

            in.close();
            return ret.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"message\": \"Something went wrong.\"}";
        }
    }

    private MaterialAboutItem translator(String language, String userName, Context context) {
        HashMap<String, Object> user = getUSerMap(userName, context);
        if (user.containsKey("id") && user.containsKey("name")) {
            return new MaterialAboutActionItem.Builder()
                    .text(language)
                    .subText((String) user.get("name"))
                    .icon((Drawable) user.get("image"))
                    .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                    .setOnClickAction(() -> openGitHubProfile(userName))
                    .build();
        } else {
            return new MaterialAboutActionItem.Builder()
                    .text(getString(R.string.text_error_data))
                    .subText(getString(R.string.text_error_data))
                    .icon(getResources().getDrawable(R.drawable.ic_error_outline_black_24dp, null))
                    .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                    .build();
        }
    }

    private void openGitHubProfile(String userName) {
        openUrl("https://github.com/" + userName);
    }

    private void openUrl(String url) {
        Intent gitIntent =
                new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(gitIntent);
    }

    private void openSourceCode(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Loading");
        progressDialog.show();
        new Thread(() -> {
            ThemeStore store = ThemeStore.getInstance(context);
            GitHubSource.getInstance(
                    context,
                    new Repository("dertyp7214", "NewAppStore",
                            getSettings(
                                    context)
                                    .getString(
                                            "API_KEY",
                                            null
                                    ))
            ).setColorStyle(new ColorStyle(
                    store.getPrimaryColor(),
                    store.getPrimaryDarkColor(),
                    store.getAccentColor()
            )).open();
            try {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(progressDialog::dismiss);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setUpBottomSheet() {
        setUp = true;
        EditText editText = activity.findViewById(R.id.text_amount);
        Button button = activity.findViewById(R.id.btn_pay);

        ThemeStore themeStore = ThemeStore.getInstance(activity);

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

        Intent service = new Intent(activity, PayPalService.class);
        service.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        activity.startService(service);

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
                new MaterialDialog.Builder(activity)
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
                                    activity,
                                    PaymentActivity.class
                            );

                            intent.putExtra(PaymentActivity.EXTRA_PAYMENT, donating);

                            startActivityForResult(intent, REQUEST_CODE_PAYMENT);
                        })
                        .show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

                        new MaterialDialog.Builder(activity)
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
        }
    }

    private String getH4(String string) {
        return "<h4 style=\"display:inline\">" + string + "</h4>";
    }

    @Override
    public void onDestroy() {
        try {
            activity.stopService(new Intent(activity, PayPalService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
