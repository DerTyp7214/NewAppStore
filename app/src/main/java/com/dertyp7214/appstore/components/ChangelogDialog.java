/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dertyp7214.appstore.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ChangelogDialog {

    @SuppressLint("InflateParams")
    public ChangelogDialog(Context context) {
        final View customView;
        try {
            customView = LayoutInflater.from(context).inflate(R.layout.dialog_web_view, null);
        } catch (InflateException e) {
            e.printStackTrace();
            new MaterialDialog.Builder(context)
                    .title(android.R.string.dialog_alert_title)
                    .content(
                            "This device doesn't support web view, which is necessary to view the change log. It is missing a system component.")
                    .positiveText(android.R.string.ok)
                    .build().show();
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.text_changes)
                .customView(customView, false)
                .positiveText(android.R.string.ok)
                .build();
        final WebView webView = customView.findViewById(R.id.web_view);
        try {
            StringBuilder buf = new StringBuilder();
            InputStream json;
            try {
                json = context.getAssets()
                        .open("appstore-changelog-" + context.getString(R.string.language_model)
                                + ".html");
                ;
            } catch (Exception e) {
                json = context.getAssets().open("appstore-changelog.html");
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(json, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null)
                buf.append(str);
            in.close();

            webView.loadData(buf.toString(), "text/html", "UTF-8");
        } catch (Throwable e) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.getLocalizedMessage() + "</p>",
                    "text/html", "UTF-8");
        }
        dialog.show();
    }
}
