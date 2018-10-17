/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

@file:Suppress("DEPRECATION")

package com.dertyp7214.appstore.screens

import android.app.Activity
import android.app.ProgressDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.core.content.edit
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.*
import com.dertyp7214.appstore.Config.ACTIVE_OVERLAY
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.Config.UID
import com.dertyp7214.appstore.adapter.SettingsAdapter
import com.dertyp7214.appstore.components.InputDialog
import com.dertyp7214.appstore.helpers.SQLiteHandler
import com.dertyp7214.appstore.settings.*
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.nbsp.materialfilepicker.MaterialFilePicker
import com.nbsp.materialfilepicker.ui.FilePickerActivity
import com.theartofdev.edmodo.cropper.CropImage
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

class SettingsScreen : Utils() {

    private val prog: ProgressDialog? = null
    private var profileImage = true

    private val settings: List<Settings>
        get() {
            val settingsList = ArrayList(Arrays.asList(
                    SettingsPlaceholder("preferences", getString(R.string.text_prefs), this),
                    Settings("api_key", getString(R.string.text_api_key), this).setSubTitle(
                            cutString(
                                    Utils.getSettings(this@SettingsScreen)
                                            .getString("API_KEY", getString(R.string.text_not_set))!!,
                                    30
                            )).addSettingsOnClick(object : Settings.settingsOnClickListener {
                        override fun onClick(name: String, setting: Settings, subTitle: TextView, imageRight: ProgressBar) {
                            val dialog = InputDialog(
                                    getString(R.string.text_api_key), Utils.getSettings(this@SettingsScreen)
                                    .getString("API_KEY", "")!!, getString(R.string.text_api_key),
                                    this@SettingsScreen
                            )
                            dialog.setListener(object : InputDialog.Listener {
                                override fun onSubmit(text: String) {
                                    Utils.getSettings(this@SettingsScreen).edit().putString("API_KEY", text)
                                            .apply()
                                    subTitle.text = cutString(
                                            Utils.getSettings(this@SettingsScreen).getString("API_KEY", getString(
                                                    R.string.text_not_set))!!, 30)
                                }

                                override fun onCancel() {
                                }
                            })
                            return dialog.show()
                        }
                    }),
                    SettingsSwitch(
                            "dev_mode", getString(R.string.text_dev_mode), this,
                            Utils.getSettings(this).getBoolean("dev_mode", false)
                    ).setCheckedChangeListener(object : SettingsSwitch.CheckedChangeListener {
                        override fun onChangeChecked(value: Boolean) {
                            return Utils.getSettings(this@SettingsScreen).edit()
                                    .putBoolean("dev_mode", value).apply()
                        }
                    })
            ))
            if (Utils.appInstalled(this, oldAppPackageName)) {
                val settingsSwitch = SettingsSwitch(
                        "old_appstore", getString(R.string.askForUninstallOldAppstore), this,
                        Utils.getSettings(this).getBoolean("old_appstore", false)
                )
                settingsSwitch.setCheckedChangeListener(object : SettingsSwitch.CheckedChangeListener {
                    override fun onChangeChecked(value: Boolean) {
                        return Utils.getSettings(this@SettingsScreen).edit().putBoolean("old_appstore", value).apply()
                    }
                })
                settingsList.add(settingsSwitch)
            }
            if (Config.root) {
                val settingsSwitch = SettingsSwitch(
                        "root_install", getString(R.string.text_root_install), this, Utils
                        .getSettings(this)
                        .getBoolean("root_install", false))
                settingsSwitch.setCheckedChangeListener(object : SettingsSwitch.CheckedChangeListener {
                    override fun onChangeChecked(value: Boolean) {
                        return if (value) {
                            if (runCommand("su"))
                                Utils.getSettings(this@SettingsScreen).edit().putBoolean("root_install", true)
                                        .apply()
                            else
                                settingsSwitch.isChecked = false
                        } else {
                            Utils.getSettings(this@SettingsScreen).edit().putBoolean("root_install", false)
                                    .apply()
                        }
                    }
                })
                settingsList.add(settingsSwitch)
            }
            settingsList.add(SettingsPlaceholder("style", getString(R.string.text_style), this))
            if (!ACTIVE_OVERLAY(this))
                settingsList.add(SettingsColor(
                        "color_primary", getString(R.string.text_color), this,
                        themeStore!!.primaryColor
                ).addSettingsOnClick(object : SettingsColor.settingsOnClickListener {
                    override fun onClick(name: String, Color: Int, settingsColor: SettingsColor) {
                        settingsColor.saveSetting()
                        themeManager.changeAccentColor(themeStore!!.accentColor)
                        themeManager
                                .changePrimaryColor(this@SettingsScreen, themeStore!!.primaryColor, true,
                                        Build.VERSION.SDK_INT < Build.VERSION_CODES.P, true)
                        MainActivity.settingsChanged = true
                    }
                }))
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                settingsList.add(SettingsSwitch(
                        "colored_nav_bar", getString(R.string.text_colored_navbar), this,
                        Utils.getSettings(this).getBoolean("colored_nav_bar", false)
                ).setCheckedChangeListener(object : SettingsSwitch.CheckedChangeListener {
                    override fun onChangeChecked(value: Boolean) {
                        Utils
                                .getSettings(this@SettingsScreen).edit().putBoolean("colored_nav_bar", value)
                                .apply()
                        return Utils.setNavigationBarColor(
                                this@SettingsScreen, window.decorView,
                                ThemeStore.getInstance(this@SettingsScreen)!!.primaryColor, 300
                        )
                    }
                }))
            }
            if (Utils.getSettings(this).getBoolean("easter_egg", false)) {
                settingsList.addAll(Arrays.asList(
                        SettingsSwitch(
                                "rainbow_mode", getString(R.string.text_rainbow_mode), this,
                                Utils.getSettings(this).getBoolean("rainbow_mode", false)
                        ).setCheckedChangeListener(object : SettingsSwitch.CheckedChangeListener {
                            override fun onChangeChecked(value: Boolean) {
                                Utils.getSettings(this@SettingsScreen).edit().putBoolean("rainbow_mode", value)
                                        .apply()
                                return Utils.toggleRainBow(value, this@SettingsScreen)
                            }
                        }),
                        Settings("disable_easter_egg",
                                getString(R.string.text_disable_easter_egg), this)
                                .addSettingsOnClick(object : Settings.settingsOnClickListener {
                                    override fun onClick(name: String, setting: Settings, subTitle: TextView, imageRight: ProgressBar) {
                                        return Utils.getSettings(this@SettingsScreen)
                                                .edit().putBoolean("easter_egg", false)
                                                .putBoolean("rainbow_mode", false).apply()
                                    }
                                })
                )
                )
            }
            settingsList.addAll(ArrayList(Arrays.asList(
                    SettingsSlider("search_bar_radius", getString(R.string.search_bar_radius),
                            this),
                    SettingsPlaceholder(
                            "user_preferences", getString(R.string.text_user_preferences), this),
                    Settings(
                            "change_profile_pic", getString(R.string.text_change_profile_pic), this)
                            .addSettingsOnClick(object : Settings.settingsOnClickListener {
                                override fun onClick(name: String, setting: Settings, subTitle: TextView, imageRight: ProgressBar) {
                                    return MaterialFilePicker()
                                            .withActivity(this@SettingsScreen)
                                            .withRequestCode(10)
                                            .withFilter(Pattern.compile(".*\\.(png|jpg|jpeg)$"))
                                            .start()
                                }
                            }),
                    Settings(
                            "change_bg_pic", getString(R.string.text_change_bg_pic), this)
                            .addSettingsOnClick(object : Settings.settingsOnClickListener {
                                override fun onClick(name: String, setting: Settings, subTitle: TextView, imageRight: ProgressBar) {
                                    return MaterialFilePicker()
                                            .withActivity(this@SettingsScreen)
                                            .withRequestCode(11)
                                            .withFilter(Pattern.compile(".*\\.(png|jpg|jpeg)$"))
                                            .start()
                                }
                            })
            )))
            if (BuildConfig.DEBUG) {
                val hideIcon = SettingsSwitch(
                        "hide_appicon", getString(R.string.text_hide_icon), this, Utils.getSettings(this)
                        .getBoolean("hide_appicon", false))
                        .setCheckedChangeListener(object : SettingsSwitch.CheckedChangeListener {
                            override fun onChangeChecked(value: Boolean) {
                                Utils.getSettings(this@SettingsScreen).edit {
                                    value to "hide_appicon"
                                }
                                val p = packageManager
                                val componentName = ComponentName(this@SettingsScreen, Launcher::class.java)
                                return if (value)
                                    p.setComponentEnabledSetting(
                                            componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                            PackageManager.DONT_KILL_APP
                                    )
                                else
                                    p.setComponentEnabledSetting(
                                            componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                            PackageManager.DONT_KILL_APP
                                    )
                            }
                        })
                settingsList
                        .add(SettingsPlaceholder("debug", getString(R.string.text_debug), this))
                settingsList.add(hideIcon)
            }
            return settingsList
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_screen)
        themeStore = ThemeStore.getInstance(this)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (!Utils.getSettings(this).getBoolean("root_install", false)) {
            Config.root = isRooted
        } else {
            Config.root = true
        }

        setSwipeBackCallback(object : Callback {
            override fun run() {
                return this@SettingsScreen.syncPreferencesToServer()
            }
        })

        setColors()

        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(true)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)

        val settingList = findViewById<RecyclerView>(R.id.setting_rv)

        val settingsAdapter = SettingsAdapter(settings, this)
        val layoutManager = LinearLayoutManager(applicationContext)
        settingList.layoutManager = layoutManager
        settingList.itemAnimator = DefaultItemAnimator()
        settingList.adapter = settingsAdapter
        val dividerItemDecoration = DividerItemDecoration(
                settingList.context, layoutManager.orientation)
        settingList.addItemDecoration(dividerItemDecoration)

        ThemeManager.attach(this
        ) { themeManager ->
            themeManager.changeAccentColor(themeStore!!.accentColor)
            themeManager.changePrimaryColor(themeStore!!.primaryColor)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            val f = File(data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH))
            profileImage = true
            CropImage.activity(Uri.fromFile(f))
                    .setAspectRatio(1, 1)
                    .start(this)
        } else if (requestCode == 11 && resultCode == Activity.RESULT_OK) {
            val f = File(data!!.getStringExtra(FilePickerActivity.RESULT_FILE_PATH))
            profileImage = false
            CropImage.activity(Uri.fromFile(f))
                    .setAspectRatio(16, 9)
                    .start(this)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val db = SQLiteHandler(applicationContext)
            val user = db.userDetails
            val userID = user["uid"]

            val result = CropImage.getActivityResult(data)
            val uri = result.uri

            val f = File(Objects.requireNonNull(uri.path))

            val t = Thread {
                val contentType = getMimeType(f.path)
                val filePath = f.absolutePath
                val client = OkHttpClient()
                val fileBody = RequestBody.create(MediaType.parse(contentType!!), f)
                val requestBody = MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", contentType)
                        .addFormDataPart(
                                "uploaded_file",
                                filePath.substring(filePath.lastIndexOf("/") + 1), fileBody
                        )
                        .addFormDataPart("name",
                                userID!!.replace(" ", "_") + if (profileImage) "" else "_bg")
                        .build()
                val request = Request.Builder()
                        .url("$API_URL/apps/upload.php")
                        .post(requestBody)
                        .build()
                try {
                    val response = client.newCall(request).execute()
                    assert(response.body() != null)
                    Log.d("RESPONSE:", response.body()!!.string())
                    if (!response.isSuccessful) {
                        throw IOException("Error : $response")
                    }
                    val imgFile = File(filesDir, userID + (if (profileImage) "" else "_bg") + ".png")
                    if (imgFile.exists())
                        if (imgFile.delete()) logs.info("changeProfileImage", "deleted!")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            t.start()
        }
    }

    private fun getMimeType(path: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    override fun onBackPressed() {
        syncPreferencesToServer()
        super.onBackPressed()
    }

    private fun syncPreferencesToServer() {
        Thread {
            val preferences = Utils.getSettings(this)
            val colors = getSharedPreferences("colors_" + UID(this), Context.MODE_PRIVATE)
            val jsonObject = JSONObject()
            val prefs = JSONObject()
            val color = JSONObject()

            try {

                for (key in preferences.all.keys) {
                    prefs.put(key, preferences.all[key])
                }
                for (key in colors.all.keys) {
                    color.put(key, colors.all[key])
                }

                jsonObject.put("prefs", prefs)
                jsonObject.put("colors", color)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            Utils.getWebContent(API_URL + "/apps/prefs.php?user=" + UID(this) + "&prefs=" + jsonObject
                    .toString())
        }.start()
    }
}
