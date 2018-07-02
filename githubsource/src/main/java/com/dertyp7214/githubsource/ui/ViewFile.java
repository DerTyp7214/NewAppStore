/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.R;
import com.dertyp7214.githubsource.adapter.FileAdapter;
import com.dertyp7214.githubsource.helpers.ColorStyle;
import com.dertyp7214.githubsource.helpers.DefaultColorStyle;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static com.dertyp7214.githubsource.github.Repository.api_key;
import static com.dertyp7214.githubsource.styles.Css.getMarkDownStyle;

public class ViewFile extends AppCompatActivity {

    private String fileName, contentUrl;
    private WebView content;
    private Thread load;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);

        ColorStyle colorStyle = new DefaultColorStyle();

        if(GitHubSource.colorStyle!=null)
            colorStyle=GitHubSource.colorStyle;

        content = findViewById(R.id.content);
        WebSettings settings = content.getSettings();
        settings.setJavaScriptEnabled(true);

        getWindow().setStatusBarColor(colorStyle.getPrimaryColorDark());
        getWindow().setNavigationBarColor(colorStyle.getPrimaryColor());
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(colorStyle.getPrimaryColor()));

        Bundle extra = getIntent().getExtras();

        if(extra==null)
            finish();

        assert extra != null;
        fileName = extra.getString("filename");
        contentUrl = extra.getString("url");

        setTitle(fileName);
        load = new Thread(() -> {
            String c = getJSONObject(contentUrl);
            runOnUiThread(() -> {
                content.loadData(fileName.endsWith(".md") ? markdownToHTML(c) : Html.escapeHtml(c).replace("&#10;", "<br/>"), "text/html", "utf-8");
                FileAdapter.progressBar.setIndeterminate(false);
                FileAdapter.progressBar.setVisibility(View.INVISIBLE);
            });
        });
        load.start();
    }

    private String markdownToHTML(String markdown){
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String html = renderer.render(document);
        return getMarkDownStyle()+html;
    }

    private String getJSONObject(String url){
        try {
            URL web = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) web.openConnection();
            connection.setRequestProperty("Authorization", "token "+api_key);
            BufferedReader in;

            if(api_key==null)
                in = new BufferedReader(new InputStreamReader(web.openStream()));
            else
                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            StringBuilder ret = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                ret.append(inputLine).append("\n");

            in.close();
            return ret.toString();
        } catch (Exception ignored) {
            return "";
        }
    }

    @Override
    public void onBackPressed() {
        load.interrupt();
        super.onBackPressed();
    }

    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return true;
    }
}
