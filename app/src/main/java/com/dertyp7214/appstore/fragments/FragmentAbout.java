/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments;

import android.content.Context;

import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;

import java.util.Objects;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.BSD2ClauseLicense;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

public class FragmentAbout extends MaterialAboutFragment {

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
        MaterialAboutCard card = new MaterialAboutCard.Builder()
                .title(getString(R.string.text_authors))
                .addItem(new MaterialAboutActionItem.Builder()
                        .text(getString(R.string.text_main_author))
                        .subText(getString(R.string.text_josua_lengwenath))
                        .icon(Utils.drawableFromUrl(context,
                                "https://avatars0.githubusercontent.com/u/37804065"))
                        .setIconGravity(MaterialAboutActionItem.GRAVITY_MIDDLE)
                        .build())
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
                .addCard(libraries)
                .build();
    }
}
