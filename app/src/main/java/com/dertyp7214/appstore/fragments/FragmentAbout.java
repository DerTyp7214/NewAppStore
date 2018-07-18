/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD2ClauseLicense;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class FragmentAbout extends MaterialAboutFragment {

    public static HashMap<String, HashMap<String, Object>> users = new HashMap<>();

    public FragmentAbout() {

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

        MaterialAboutCard card = new MaterialAboutCard.Builder()
                .title(getString(R.string.text_authors))
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(getString(R.string.text_main_author))
                        .subText(getString(R.string.text_josua_lengwenath))
                        .icon(Utils.drawableFromUrl(context,
                                "https://avatars0.githubusercontent.com/u/37804065"))
                        .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                        .setOnClickAction(() -> openGitHubProfile("DerTyp7214"))
                        .build())
                .build();

        MaterialAboutCard translators = new MaterialAboutCard.Builder()
                .title(getString(R.string.text_translators))
                .addItem(translator(
                        getString(R.string.text_english) + ", " + getString(R.string.text_german),
                        getString(R.string.text_dertyp7214),
                        context))
                .addItem(translator(getString(R.string.text_spainish),
                        getString(R.string.text_enol_simon),
                        context))
                .build();

        MaterialAboutCard libraries = new MaterialAboutCard.Builder()
                .title(getString(R.string.text_libraries))
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(getString(R.string.text_licenses))
                        .icon(getResources().getDrawable(R.drawable.ic_public_black_24dp))
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
                .addCard(card)
                .addCard(translators)
                .addCard(libraries)
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
        }
        users.put(userName, userMap);
        return userMap;
    }

    private String getJSONObject(String url, Context context) {
        String api_key = Utils.getSettings(context).getString("API_KEY", null);
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
        Intent gitIntent =
                new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/" + userName));
        startActivity(gitIntent);
    }
}
