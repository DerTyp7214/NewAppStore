/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.githubsource.ui;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.dertyp7214.githubsource.GitHubSource;
import com.dertyp7214.githubsource.R;
import com.dertyp7214.githubsource.helpers.ColorStyle;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.dertyp7214.githubsource.github.Repository.api_key;

public class Info extends AppCompatActivity {

    public static final String EXTRA_CIRCULAR_REVEAL_X = "EXTRA_CIRCULAR_REVEAL_X";
    public static final String EXTRA_CIRCULAR_REVEAL_Y = "EXTRA_CIRCULAR_REVEAL_Y";

    View rootLayout;

    private TextView core, search, graphql, coreTime, searchTime, graphqlTime;
    private List<TextView> textViewList = new ArrayList<>();

    private int revealX;
    private int revealY;

    @SuppressLint("FindViewByIdCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        final Intent intent = getIntent();

        ColorStyle colorStyle = GitHubSource.colorStyle;

        rootLayout = findViewById(R.id.root_layout);
        core = findViewById(R.id.txt_core);
        search = findViewById(R.id.txt_search);
        graphql = findViewById(R.id.txt_graphql);
        coreTime = findViewById(R.id.txt_core_reset);
        searchTime = findViewById(R.id.txt_search_reset);
        graphqlTime = findViewById(R.id.txt_graphql_reset);

        CardView cardCore = findViewById(R.id.card_core);
        CardView cardSearch = findViewById(R.id.card_search);
        CardView cardGraphql = findViewById(R.id.card_graphql);

        textViewList.add(core);
        textViewList.add(search);
        textViewList.add(graphql);
        textViewList.add(coreTime);
        textViewList.add(searchTime);
        textViewList.add(graphqlTime);
        textViewList.add(findViewById(R.id.core_text));
        textViewList.add(findViewById(R.id.search_text));
        textViewList.add(findViewById(R.id.graphql_text));
        textViewList.add(findViewById(R.id.limit_core));
        textViewList.add(findViewById(R.id.limit_search));
        textViewList.add(findViewById(R.id.limit_graphql));
        textViewList.add(findViewById(R.id.reset_core));
        textViewList.add(findViewById(R.id.reset_search));
        textViewList.add(findViewById(R.id.reset_graphql));

        rootLayout.setBackgroundColor(colorStyle.getPrimaryColor());

        cardCore.setCardBackgroundColor(colorStyle.getPrimaryColorDark());
        cardSearch.setCardBackgroundColor(colorStyle.getPrimaryColorDark());
        cardGraphql.setCardBackgroundColor(colorStyle.getPrimaryColorDark());

        for (TextView textView : textViewList)
            textView.setTextColor(isColorBright(colorStyle.getPrimaryColorDark())?Color.BLACK:Color.WHITE);

        loadJSON();

        getWindow().setNavigationBarColor(colorStyle.getPrimaryColor());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isColorBright(getWindow().getNavigationBarColor())) {
            core.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        if (savedInstanceState == null &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_X) &&
                intent.hasExtra(EXTRA_CIRCULAR_REVEAL_Y)) {
            rootLayout.setVisibility(View.INVISIBLE);

            revealX = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_X, 0);
            revealY = intent.getIntExtra(EXTRA_CIRCULAR_REVEAL_Y, 0);


            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        revealActivity(revealX, revealY);
                        rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            }
        } else {
            rootLayout.setVisibility(View.VISIBLE);
        }
    }

    private void loadJSON(){
        new Thread(() -> {
            try {
                JSONObject jsonObject = new JSONObject(getJSONObject("https://api.github.com/rate_limit"));
                JSONObject resources = jsonObject.getJSONObject("resources");
                JSONObject coreJSON = resources.getJSONObject("core");
                JSONObject searchJSON = resources.getJSONObject("search");
                JSONObject graphqlJSON = resources.getJSONObject("graphql");
                String coreRemaining = coreJSON.getString("remaining") + "/" + coreJSON.getString("limit");
                String searchRemaining = searchJSON.getString("remaining") + "/" + searchJSON.getString("limit");
                String graphqlRemaining = graphqlJSON.getString("remaining") + "/" + graphqlJSON.getString("limit");
                String coreRemainingTime = new Date(coreJSON.getLong("reset")*1000).toString();
                String searchRemainingTime = new Date(searchJSON.getLong("reset")*1000).toString();
                String graphqlRemainingTime = new Date(graphqlJSON.getLong("reset")*1000).toString();
                runOnUiThread(() -> {
                    core.setText(coreRemaining);
                    search.setText(searchRemaining);
                    graphql.setText(graphqlRemaining);
                    coreTime.setText(coreRemainingTime);
                    searchTime.setText(searchRemainingTime);
                    graphqlTime.setText(graphqlRemainingTime);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static boolean isColorBright(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        return darkness < 0.5;
    }

    protected void revealActivity(int x, int y) {
        float finalRadius = (float) (Math.max(rootLayout.getWidth(), rootLayout.getHeight()) * 1.1);

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0, finalRadius);
        circularReveal.setDuration(400);
        circularReveal.setInterpolator(new AccelerateInterpolator());

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    private String getJSONObject(String url){
        try {
            URL web = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) web.openConnection();
            connection.setRequestProperty("Authorization", "token "+api_key);
            BufferedReader in;

            if(api_key==null || api_key.equals(""))
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
}
