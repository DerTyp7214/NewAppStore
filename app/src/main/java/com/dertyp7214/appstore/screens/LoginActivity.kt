/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package com.dertyp7214.appstore.screens

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.toolbox.StringRequest
import com.dertyp7214.appstore.BuildConfig
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.Config.UID
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.Utils.Companion.getSettings
import com.dertyp7214.appstore.Utils.Companion.getWebContent
import com.dertyp7214.appstore.dev.Logs
import com.dertyp7214.appstore.helpers.SQLiteHandler
import com.dertyp7214.appstore.helpers.SessionManager
import com.dertyp7214.appstore.loginout.AppConfig
import com.dertyp7214.appstore.AppController
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@Suppress("NAME_SHADOWING")
class LoginActivity : Activity() {
    private var btnLogin: Button? = null
    private var btnLinkToRegister: Button? = null
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var pDialog: ProgressDialog? = null
    private var session: SessionManager? = null
    private var db: SQLiteHandler? = null
    private val REQ_EXTERNAL_STORAGE = 42

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.bg_login)
            window.statusBarColor = resources.getColor(R.color.bg_login)
        }

        if (ActivityCompat.checkSelfPermission(this@LoginActivity,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@LoginActivity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQ_EXTERNAL_STORAGE)
        }

        inputEmail = findViewById(R.id.email)
        inputPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btnLogin)
        btnLinkToRegister = findViewById(R.id.btnLinkToRegisterScreen)

        pDialog = ProgressDialog(this)
        pDialog!!.setCancelable(false)

        db = SQLiteHandler(applicationContext)

        session = SessionManager(applicationContext)

        if (session!!.isLoggedIn) {
            val am = AccountManager.get(this)
            for (account in am.accounts) {
                if (account.name == db!!.userDetails["email"]) {
                    Thread {
                        syncPreferences()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.start()
                }
            }
        }

        btnLogin!!.setOnClickListener {
            val email = inputEmail!!.text.toString()
            val password = inputPassword!!.text.toString()

            if (!email.isEmpty() && !password.isEmpty()) {
                checkLogin(email, password)
            } else {
                Toast.makeText(applicationContext,
                        "Please enter the credentials!", Toast.LENGTH_LONG)
                        .show()
            }
        }

        btnLinkToRegister!!.setOnClickListener {
            val i = Intent(applicationContext,
                    RegisterActivity::class.java).putExtra("appstore", true)
            startActivity(i)
            finish()
        }

    }

    private fun syncPreferences() {
        Thread {
            val preferences = getSettings(this)
            val editor = preferences.edit()
            val colors = getSharedPreferences("colors_" + UID(this), Context.MODE_PRIVATE)
            val editorColor = colors.edit()

            try {
                val jsonObject = JSONObject(
                        getWebContent(API_URL + "/apps/prefs.php?user=" + UID(this)))

                run {
                    val it = jsonObject.getJSONObject("prefs").keys()
                    while (it.hasNext()) {
                        val key = it.next()
                        val obj = jsonObject.getJSONObject("prefs").get(key)
                        when (obj) {
                            is String -> editor.putString(key, obj)
                            is Int -> editor.putInt(key, obj)
                            is Long -> editor.putLong(key, obj)
                            is Float -> editor.putFloat(key, obj)
                            is Boolean -> editor.putBoolean(key, obj)
                            is Set<*> -> editor.putStringSet(key, obj as Set<String>)
                        }
                    }
                }

                val it = jsonObject.getJSONObject("colors").keys()
                while (it.hasNext()) {
                    val key = it.next()
                    val obj = jsonObject.getJSONObject("colors").get(key)
                    when (obj) {
                        is String -> editorColor.putString(key, obj)
                        is Int -> editorColor.putInt(key, obj)
                        is Long -> editorColor.putLong(key, obj)
                        is Float -> editorColor.putFloat(key, obj)
                        is Boolean -> editorColor.putBoolean(key, obj)
                        is Set<*> -> editorColor.putStringSet(key, obj as Set<String>)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            editor.apply()
            editorColor.apply()
            Log.d("PREFS", preferences.all.toString())
            Log.d("COLORS", colors.all.toString())
        }.start()
    }

    private fun checkLogin(email: String, password: String) {
        val tagStringReq = "req_login"

        pDialog!!.setMessage("Logging in ...")
        showDialog()

        val strReq = object : StringRequest(Method.POST,
                AppConfig.URL_LOGIN, { response ->
            Log.d(TAG, "Login Response: $response")
            hideDialog()

            try {
                val jObj = JSONObject(response)
                val error = jObj.getBoolean("error")

                if (!error) {
                    session!!.setLogin(true)
                    db!!.deleteUsers()

                    val uid = jObj.getString("uid")

                    val user = jObj.getJSONObject("user")
                    val name = user.getString("name")
                    val email1 = user.getString("email")
                    val createdAt = user
                            .getString("created_at")

                    db!!.addUser(name, email1, uid, createdAt)

                    val userData = Bundle()
                    userData.putString("name", name)
                    userData.putString("uid", uid)
                    userData.putString("created_at", createdAt)

                    val accountManager = AccountManager.get(this@LoginActivity)
                    val account = Account(email1, BuildConfig.APPLICATION_ID + ".ACCOUNT")
                    val success = accountManager.addAccountExplicitly(account, password, userData)
                    if (success) {
                        Log.d(TAG, "Account created")
                    } else {
                        Log.d(TAG, "Account creation failed. Look at previous logs to investigate")
                    }

                    Thread {
                        syncPreferences()
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.start()
                } else {
                    val errorMsg = jObj.getString("error_msg")
                    Toast.makeText(applicationContext,
                            errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                Logs.getInstance(this).error("Json error", e.message!!)
            }
        }, { error ->
            Log.e(TAG, "Login Error: " + error.message)
            Toast.makeText(applicationContext,
                    error.message, Toast.LENGTH_LONG).show()
            hideDialog()
        }) {

            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["email"] = email
                params["password"] = password
                return params
            }

        }

        AppController.instance!!.addToRequestQueue(strReq, tagStringReq)
    }

    private fun showDialog() {
        if (!pDialog!!.isShowing)
            pDialog!!.show()
    }

    private fun hideDialog() {
        if (pDialog!!.isShowing)
            pDialog!!.dismiss()
    }

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
    }
}