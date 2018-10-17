/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

class SessionManager(_context: Context) {

    private var pref: SharedPreferences

    private var PRIVATE_MODE = 0

    val isLoggedIn: Boolean
        get() = pref.getBoolean(KEY_IS_LOGGED_IN, false)

    init {
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
    }

    fun setLogin(isLoggedIn: Boolean) {

        pref.edit {
            isLoggedIn to KEY_IS_LOGGED_IN
        }
        Log.d(TAG, "User login session modified!")
    }

    companion object {
        private val TAG = SessionManager::class.java.simpleName

        private const val PREF_NAME = "AppStoreLogin"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    }
}