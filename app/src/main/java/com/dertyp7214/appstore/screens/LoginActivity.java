/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.dertyp7214.appstore.R;
import com.dertyp7214.appstore.helpers.SQLiteHandler;
import com.dertyp7214.appstore.helpers.SessionManager;
import com.dertyp7214.appstore.loginout.AppConfig;
import com.dertyp7214.appstore.loginout.AppController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.dertyp7214.appstore.Config.API_URL;
import static com.dertyp7214.appstore.Config.UID;
import static com.dertyp7214.appstore.Utils.getSettings;
import static com.dertyp7214.appstore.Utils.getWebContent;

public class LoginActivity extends Activity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    private final int REQ_EXTERNAL_STORAGE = 42;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            getWindow().setNavigationBarColor(getResources().getColor(R.color.bg_login));
            getWindow().setStatusBarColor(getResources().getColor(R.color.bg_login));

        }

        if (ActivityCompat.checkSelfPermission(LoginActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {


        } else {

            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_EXTERNAL_STORAGE);

        }

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        btnLinkToRegister = findViewById(R.id.btnLinkToRegisterScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            AccountManager am = AccountManager.get(this);
            for(Account account : am.getAccounts()){
                if((account.name).equals(db.getUserDetails().get("email"))) {
                    new Thread(() -> {
                        syncPreferences();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }).start();
                }
            }
        }

        // Login button Click Event
        btnLogin.setOnClickListener(view -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            // Check for empty data in the form
            if (!email.isEmpty() && !password.isEmpty()) {
                // login user
                checkLogin(email, password);
            } else {
                // Prompt user to enter credentials
                Toast.makeText(getApplicationContext(),
                        "Please enter the credentials!", Toast.LENGTH_LONG)
                        .show();
            }
        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(),
                    RegisterActivity.class).putExtra("appstore", true);
            startActivity(i);
            finish();
        });

    }

    private void syncPreferences() {
        new Thread(() -> {
            SharedPreferences preferences = getSettings(this);
            SharedPreferences.Editor editor = preferences.edit();
            SharedPreferences colors = getSharedPreferences("colors_"+UID(this), MODE_PRIVATE);
            SharedPreferences.Editor editorColor = colors.edit();

            try {
                JSONObject jsonObject = new JSONObject(getWebContent(API_URL + "/apps/prefs.php?user=" + UID(this)));

                for (Iterator<String> it = jsonObject.getJSONObject("prefs").keys(); it.hasNext(); ) {
                    String key = it.next();
                    Object obj = jsonObject.getJSONObject("prefs").get(key);
                    if (obj instanceof String)
                        editor.putString(key, (String) obj);
                    else if (obj instanceof Integer)
                        editor.putInt(key, (int) obj);
                    else if (obj instanceof Long)
                        editor.putLong(key, (long) obj);
                    else if (obj instanceof Float)
                        editor.putFloat(key, (float) obj);
                    else if (obj instanceof Boolean)
                        editor.putBoolean(key, (boolean) obj);
                    else if (obj instanceof Set)
                        editor.putStringSet(key, (Set<String>) obj);

                }

                for (Iterator<String> it = jsonObject.getJSONObject("colors").keys(); it.hasNext(); ) {
                    String key = it.next();
                    Object obj = jsonObject.getJSONObject("colors").get(key);
                    if (obj instanceof String)
                        editorColor.putString(key, (String) obj);
                    else if (obj instanceof Integer)
                        editorColor.putInt(key, (int) obj);
                    else if (obj instanceof Long)
                        editorColor.putLong(key, (long) obj);
                    else if (obj instanceof Float)
                        editorColor.putFloat(key, (float) obj);
                    else if (obj instanceof Boolean)
                        editorColor.putBoolean(key, (boolean) obj);
                    else if (obj instanceof Set)
                        editorColor.putStringSet(key, (Set<String>) obj);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            editor.apply();
            editorColor.apply();
            Log.d("PREFS", preferences.getAll().toString());
            Log.d("COLORS", colors.getAll().toString());
        }).start();
    }

    /**
     * function to verify login details in mysql db
     * */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_LOGIN, response -> {
                    Log.d(TAG, "Login Response: " + response);
                    hideDialog();

                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {
                            // user successfully logged in
                            // Create login session
                            session.setLogin(true);

                            db.deleteUsers();

                            // Now store the user in SQLite
                            String uid = jObj.getString("uid");

                            JSONObject user = jObj.getJSONObject("user");
                            String name = user.getString("name");
                            String email1 = user.getString("email");
                            String created_at = user
                                    .getString("created_at");

                            // Inserting row in users table
                            db.addUser(name, email1, uid, created_at);

                            AccountManager accountManager = AccountManager.get(LoginActivity.this); //this is Activity
                            Account account = new Account(email1,"com.dertyp7214.appstore.ACCOUNT");
                            boolean success = accountManager.addAccountExplicitly(account,password,null);
                            if(success){
                                Log.d(TAG,"Account created");
                            }else{
                                Log.d(TAG,"Account creation failed. Look at previous logs to investigate");
                            }

                            // Launch main activity
                            new Thread(() -> {
                                syncPreferences();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }).start();
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }, error -> {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}