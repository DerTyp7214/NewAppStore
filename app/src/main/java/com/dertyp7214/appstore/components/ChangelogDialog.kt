/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebView
import com.afollestad.materialdialogs.MaterialDialog
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.AppController.Companion.TAG
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class ChangelogDialog @SuppressLint("InflateParams")
constructor(context: Context) {

    init {
        val customView: View = try {
            LayoutInflater.from(context).inflate(R.layout.dialog_web_view, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.localizedMessage)
            MaterialDialog.Builder(context)
                    .title(android.R.string.dialog_alert_title)
                    .content(
                            "This device doesn't support web view, which is necessary to view the change log. It is missing a system component.")
                    .positiveText(android.R.string.ok)
                    .build().show()
            View(context)
        }

        val dialog = MaterialDialog.Builder(context)
                .title(R.string.text_changes)
                .customView(customView, false)
                .positiveText(android.R.string.ok)
                .build()
        val webView = customView.findViewById<WebView>(R.id.web_view)
        try {
            val buf = StringBuilder()
            val json: InputStream = try {
                context.assets
                        .open("appstore-changelog-" + context.getString(R.string.language_model)
                                + ".html")
            } catch (e: Exception) {
                context.assets.open("appstore-changelog.html")
            }

            val reader = BufferedReader(InputStreamReader(json, "UTF-8"))
            var line: String? = null

            while ({ line = reader.readLine(); line }() != null) buf.append(line)

            reader.close()
            webView.loadData(buf.toString(), "text/html", "UTF-8")
        } catch (e: Throwable) {
            webView.loadData("<h1>Unable to load</h1><p>" + e.localizedMessage + "</p>",
                    "text/html", "UTF-8")
        }
        dialog.show()
    }
}
