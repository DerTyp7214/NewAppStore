/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.adapter.ModuleAdapter
import com.dertyp7214.appstore.items.ModuleItem
import java.util.*

class ModulesScreen : Utils() {

    private val modules = ArrayList<ModuleItem>()
    private var recyclerView: RecyclerView? = null
    private var adapter: ModuleAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modules_screen)
        themeStore = ThemeStore.getInstance(this)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(true)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        applyTheme()

        adapter = ModuleAdapter(modules)
        recyclerView = findViewById(R.id.rv)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = adapter

        getModules()

        adapter!!.notifyDataSetChanged()
    }

    private fun getName(info: ApplicationInfo): String {
        return packageManager.getApplicationLabel(info).toString()
    }

    private fun getIcon(info: ApplicationInfo): Drawable {
        return packageManager.getApplicationIcon(info)
    }

    private fun getModules() {
        modules.clear()
        for (info in Utils.getInstalledApps(this)) {
            if (info.packageName.contains("dertyp7214.module") && isModule(info)) {
                modules.add(ModuleItem(getIcon(info), getName(info), info.packageName))
            } else if (info.packageName.contains("hacker.module")) {
                modules.add(ModuleItem(getIcon(info), getName(info), info.packageName))
            }
        }
    }

    private fun isModule(info: ApplicationInfo): Boolean {
        return try {
            info.metaData.getBoolean("isModule")
        } catch (e: Exception) {
            false
        }
    }
}
