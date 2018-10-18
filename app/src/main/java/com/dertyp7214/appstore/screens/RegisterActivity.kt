/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package com.dertyp7214.appstore.screens

import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.toolbox.StringRequest
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.helpers.SQLiteHandler
import com.dertyp7214.appstore.helpers.SessionManager
import com.dertyp7214.appstore.loginout.AppConfig
import com.dertyp7214.appstore.AppController
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class RegisterActivity : Activity() {
    private var btnRegister: Button? = null
    private var btnLinkToLogin: Button? = null
    private var inputFullName: EditText? = null
    private var inputEmail: EditText? = null
    private var inputPassword: EditText? = null
    private var pDialog: ProgressDialog? = null
    private var session: SessionManager? = null
    private var db: SQLiteHandler? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            window.navigationBarColor = resources.getColor(R.color.bg_register)
            window.statusBarColor = resources.getColor(R.color.bg_register)

        }

        val bundle = intent.extras

        inputFullName = findViewById(R.id.name)
        inputEmail = findViewById(R.id.email)
        inputPassword = findViewById(R.id.password)
        btnRegister = findViewById(R.id.btnRegister)
        btnLinkToLogin = findViewById(R.id.btnLinkToLoginScreen)

        pDialog = ProgressDialog(this)
        pDialog!!.setCancelable(false)

        session = SessionManager(applicationContext)

        db = SQLiteHandler(applicationContext)

        if (bundle == null || !bundle.getBoolean("appstore")) {
            logoutUser()
        }

        if (session!!.isLoggedIn) {
            val am = AccountManager.get(this)
            for (account in am.accounts) {
                if (account.name == db!!.userDetails["email"]) {
                    val intent = Intent(this@RegisterActivity,
                            MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        btnRegister!!.setOnClickListener {
            val name = inputFullName!!.text.toString()
            val email = inputEmail!!.text.toString()
            val password = inputPassword!!.text.toString()

            if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty()) {
                registerUser(name, email, password)
            } else {
                Toast.makeText(applicationContext,
                        "Please enter your details!", Toast.LENGTH_LONG)
                        .show()
            }
        }

        btnLinkToLogin!!.setOnClickListener {
            val i = Intent(applicationContext,
                    LoginActivity::class.java)
            startActivity(i)
            finish()
        }

    }

    private fun registerUser(name: String, email: String,
                             password: String) {
        val tagStringReq = "req_register"

        pDialog!!.setMessage("Registering ...")
        showDialog()

        val strReq = object : StringRequest(Method.POST,
                AppConfig.URL_REGISTER, { response ->
            Log.d(TAG, "Register Response: $response")
            hideDialog()

            try {
                val jObj = JSONObject(response)
                val error = jObj.getBoolean("error")
                if (!error) {
                    val uid = jObj.getString("uid")

                    val user = jObj.getJSONObject("user")
                    val name1 = user.getString("name")
                    val email1 = user.getString("email")
                    val created_at = user
                            .getString("created_at")

                    db!!.addUser(name1, email1, uid, created_at)

                    Toast.makeText(applicationContext, "User successfully registered. Try login now!", Toast.LENGTH_LONG).show()

                    val intent = Intent(
                            this@RegisterActivity,
                            LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {

                    val errorMsg = jObj.getString("error_msg")
                    Toast.makeText(applicationContext,
                            errorMsg, Toast.LENGTH_LONG).show()
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }, { error ->
            Log.e(TAG, "Registration Error: " + error.message)
            Toast.makeText(applicationContext,
                    error.message, Toast.LENGTH_LONG).show()
            hideDialog()
        }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
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

    private fun logoutUser() {
        session!!.setLogin(false)

        db!!.deleteUsers()
    }

    companion object {
        private val TAG = RegisterActivity::class.java.simpleName
        const val AUTH_TOKEN = "auth_token"
    }
}