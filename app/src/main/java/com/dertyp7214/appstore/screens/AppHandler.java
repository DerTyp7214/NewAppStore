/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dertyp7214.appstore.Config;
import com.dertyp7214.appstore.LocalJSON;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.Utils;
import com.dertyp7214.appstore.helpers.SessionManager;
import com.dertyp7214.appstore.items.SearchItem;

import org.json.JSONArray;
import org.json.JSONObject;

public class AppHandler extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_handler);

        if (!new SessionManager(getApplicationContext()).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        new Thread(() -> {
            try {
                if (new JSONObject(LocalJSON.getJSON(this)).getBoolean("error"))
                    LocalJSON.setJSON(this, Utils.getWebContent(Config.API_URL + "/apps/list.php"));

                JSONObject object = new JSONObject(LocalJSON.getJSON(this));
                JSONArray array = object.getJSONArray("apps");

                String data = getIntent().getDataString();

                assert data != null;
                if(!data.contains("/apps/app.php")) finish();
                String id = data.split("id=")[1].split("&")[0];

                for (int i = 0; i < array.length() - 1; i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (obj.getString("ID").equals(id)) {

                        SearchItem searchItem = new SearchItem(obj.getString("title"), obj.getString("ID"), Utils.drawableFromUrl(this, obj.getString("image")));

                        Utils.appsList.put(searchItem.getId(), searchItem);

                        Intent intent = new Intent(this, AppScreen.class);
                        intent.putExtra("id", searchItem.getId());
                        startActivity(intent);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        }).start();
    }
}
