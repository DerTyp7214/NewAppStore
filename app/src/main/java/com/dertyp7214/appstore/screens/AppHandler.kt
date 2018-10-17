/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens

import android.content.Intent
import android.os.Bundle

import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.LocalJSON
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.helpers.SessionManager
import com.dertyp7214.appstore.items.SearchItem

import org.json.JSONObject

class AppHandler : Utils() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_handler)

        if (!SessionManager(applicationContext).isLoggedIn) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        Thread {
            try {
                if (JSONObject(LocalJSON.getJSON(this)).getBoolean("error"))
                    LocalJSON.setJSON(this, Utils.getWebContent(Config.API_URL + "/apps/list.php")!!)

                val `object` = JSONObject(LocalJSON.getJSON(this))
                val array = `object`.getJSONArray("apps")

                val data = intent.dataString!!

                if (!data.contains("/apps/app.php")) finish()
                val id = data.split("id=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

                for (i in 0 until array.length() - 1) {
                    val obj = array.getJSONObject(i)
                    if (obj.getString("ID") == id) {

                        val searchItem = SearchItem(obj.getString("title"), obj.getString("ID"),
                                Utils.drawableFromUrl(this, obj.getString("image")))

                        Utils.appsList[searchItem.id] = searchItem

                        val intent = Intent(this, AppScreen::class.java)
                        intent.putExtra("id", searchItem.id)
                        startActivity(intent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            finish()
        }.start()
    }
}
