/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.settings

import android.annotation.SuppressLint
import android.content.Context
import com.dertyp7214.appstore.Utils.Companion.getSettings

class SettingsSlider(name: String, text: String, context: Context) : Settings(name, text, context) {

    var progress = 20
        private set

    init {
        loadSetting()
    }

    fun onUpdate(progress: Int) {
        this.progress = progress
    }

    override fun saveSetting() {
        val preferences = getSettings(context)
        @SuppressLint("CommitPrefEdits")
        val editor = preferences.edit()
        editor.putInt(name, progress)
        editor.apply()
    }

    override fun loadSetting() {
        val preferences = getSettings(context)
        this.progress = preferences.getInt(name, progress)
    }
}
