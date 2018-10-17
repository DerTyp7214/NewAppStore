/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.settings

import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView

open class Settings(protected var name: String, text: String, protected var context: Context) {

    var text: String
        protected set
    internal var subTitle: String? = null
    private var onClickListener: settingsOnClickListener? = null

    init {
        this.text = text
    }

    fun setSubTitle(subTitle: String): Settings {
        this.subTitle = subTitle
        return this
    }

    fun getSubTitle(): String {
        return if (subTitle != null) subTitle!! else ""
    }

    open fun saveSetting() {}

    open fun loadSetting() {}

    fun addSettingsOnClick(onClickListener: settingsOnClickListener): Settings {
        this.onClickListener = onClickListener
        return this
    }

    fun onClick(subTitle: TextView, imageRight: ProgressBar) {
        if (onClickListener != null)
            onClickListener!!.onClick(name, this, subTitle, imageRight)
    }

    interface settingsOnClickListener {
        fun onClick(name: String, setting: Settings, subTitle: TextView, imageRight: ProgressBar)
    }
}
