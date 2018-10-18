/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.dertyp7214.appstore.*
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.adapter.StartActivity
import com.dertyp7214.appstore.components.MVAccelerateDecelerateInterpolator
import com.dertyp7214.appstore.dev.Logs
import com.dertyp7214.appstore.fragments.FragmentAbout
import com.dertyp7214.appstore.helpers.SQLiteHandler
import com.dertyp7214.appstore.helpers.SessionManager
import org.json.JSONObject
import java.io.*
import java.net.*
import java.util.*

class Splashscreen : Utils() {
    /**
     * Called when the activity is first created.
     */
    private lateinit var splashTread: Thread
    internal var duration = 500
    private var restDuration = duration
    private var oldPercentage = 0
    override lateinit var logs: Logs
    private var finishedUsers = HashMap<String, Boolean>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val window = window
        window.setFormat(PixelFormat.RGBA_8888)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen, false)

        statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            navigationBarColor = Color.WHITE
            findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        logs = Logs(this)

        checkPermissions()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                                this,
                                permission
                        ) != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
            startAnimations()
        }
    }

    private fun getDuration(percentage: Int): Int {
        val dur = duration / 100 * (percentage - oldPercentage)
        restDuration -= dur
        oldPercentage = percentage
        logs.info("PERCENTAGE", percentage.toString() + "\n" + dur + "\n" + restDuration)
        return dur
    }

    private fun startAnimations() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.alpha)
        anim.interpolator = MVAccelerateDecelerateInterpolator()
        anim.reset()
        val l = findViewById<LinearLayout>(R.id.lin_lay)
        l.clearAnimation()
        l.startAnimation(anim)

        val imgLauncher = findViewById<ImageView>(R.id.img_launcher)

        val scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                imgLauncher,
                PropertyValuesHolder.ofFloat("scaleX", 1.1f),
                PropertyValuesHolder.ofFloat("scaleY", 1.1f)
        )
        scaleDown.duration = 700
        scaleDown.interpolator = FastOutSlowInInterpolator()
        scaleDown.repeatCount = ObjectAnimator.INFINITE
        scaleDown.repeatMode = ObjectAnimator.REVERSE
        scaleDown.start()

        val progressBar = findViewById<ProgressBar>(R.id.splash)
        progressBar.isIndeterminate = false
        progressBar.progress = 0

        splashTread = Thread {
            val names = arrayOf(getString(R.string.text_dertyp7214), getString(R.string.text_enol_simon))
            try {
                Config.SERVER_ONLINE = serverOnline()
                setProgress(
                        progressBar, 10, getDuration(10),
                        getString(R.string.splash_checkLogin)
                )
                if (!BuildConfig.DEBUG) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1 && Utils
                                    .appInstalled(this, "com.dertyp7214.appstore.debug")) {

                        val intent = Intent(this, StartActivity::class.java)
                        intent.action = Intent.ACTION_MAIN
                        intent.putExtra("action", "startDebug")

                        val shortcutManager = getSystemService(ShortcutManager::class.java)
                        val shortcut = ShortcutInfo.Builder(this, "debug_store")
                                .setShortLabel("Debug Version")
                                .setLongLabel("Debug Version of the AppStore")
                                .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                                .setIntent(intent)
                                .build()

                        assert(shortcutManager != null)
                    } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                        val shortcutManager = getSystemService(ShortcutManager::class.java)!!

                        shortcutManager.removeAllDynamicShortcuts()
                    }
                }
                setProgress(
                        progressBar, 10, getDuration(10),
                        getString(R.string.splash_getUserData)
                )
                for (userName in names) {
                    if (!FragmentAbout.users.containsKey(userName)) {
                        Thread {
                            logs.info("Loading Userdata", userName)
                            try {
                                val user = HashMap<String, Any>()
                                val jsonObject = JSONObject(
                                        getJSONObject("https://api.github.com/users/$userName"))
                                user["id"] = jsonObject.getString("id")
                                val name = if (jsonObject.getString("name") == "null")
                                    jsonObject
                                            .getString("login")
                                else
                                    jsonObject.getString("name")
                                user["name"] = name
                                user["image"] = Utils.drawableFromUrl(this,
                                        "https://avatars0.githubusercontent.com/u/" + user["id"]!!)
                                FragmentAbout.users[userName] = user
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            logs.info("Loading Userdata finished", userName)
                            finishedUsers[userName] = true
                        }.start()
                    } else {
                        finishedUsers[userName] = true
                    }
                }

                if (SessionManager(applicationContext).isLoggedIn) {
                    val db = SQLiteHandler(applicationContext)
                    val user = db.userDetails
                    val userID = user["uid"]
                    if (JSONObject(LocalJSON.getJSON(this)).getBoolean("error") || !Utils.getSettings(this).getString("last_refresh", "000000")!!
                                    .contentEquals(DateFormat.format("yyyyMMdd", Date()))) {
                        Utils.getSettings(this).edit().putString("last_refresh",
                                DateFormat.format("yyyyMMdd", Date()).toString()).apply()
                        LocalJSON.setJSON(this,
                                Utils.getWebContent(Config.API_URL + "/apps/list.php?user=" + Config
                                        .UID(this))!!)
                    }
                    setProgress(
                            progressBar, 20, getDuration(20),
                            getString(R.string.splash_getUserData)
                    )
                    for (name in arrayOf(userID!!, userID + "_bg")) {
                        val url = "$API_URL/apps/pic/" + URLEncoder.encode(name, "UTF-8")
                                .replace("+", "_") + ".png"
                        val imgFile = File(filesDir, "$name.png")
                        if (!imgFile.exists()) {
                            val pic = Utils.drawableFromUrl(this, url,
                                    if (name.endsWith("_bg")) R.drawable.demo else R.drawable.ic_person)
                            val fileOutputStream = FileOutputStream(imgFile)
                            Utils.drawableToBitmap(pic)!!
                                    .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                            Utils.userImageHashMap[name] = pic
                        } else {
                            val options = BitmapFactory.Options()
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888
                            val pic = BitmapDrawable(resources,
                                    BitmapFactory.decodeFile(imgFile.absolutePath, options))
                            Utils.userImageHashMap[name] = pic
                        }
                    }
                    setProgress(
                            progressBar, 40, getDuration(40),
                            getString(R.string.splash_getUserImage)
                    )
                    syncPreferences()
                }
                setProgress(
                        progressBar, 60, getDuration(60),
                        getString(R.string.splash_synsPreferences)
                )
                for (i in 0..54) {
                    setProgress(
                            progressBar, 60 + (40.toFloat() / 55 * i).toInt(),
                            getDuration(60 + (40.toFloat() / 55 * i).toInt()),
                            String.format(
                                    getString(R.string.splash_writePreferences),
                                    (100.toFloat() / 55 * i).toInt().toString() + "%"
                            )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logs.error("ERROR", e.toString())
            } finally {
                runOnUiThread {
                    (findViewById<View>(R.id.txt_loading) as TextView).text = getString(R.string.splash_applying)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(100, true)
                } else {
                    progressBar.progress = 100
                }
                logs.info("FINALLY", restDuration.toString() + "")
                var count = 0
                var finished = true
                for (name in names)
                    if (!finishedUsers.containsKey(name))
                        finished = false

                while (!finished) {
                    var contains = true
                    count++
                    logs.info("Waiting for about to finish", count)
                    try {
                        Thread.sleep(10)
                    } catch (ignored: Exception) {
                    }
                    for (name in names)
                        if (!finishedUsers.containsKey(name))
                            contains = false
                    finished = contains
                    if (count > 100) finished = true
                }
                val intent = Intent(
                        this@Splashscreen,
                        LoginActivity::class.java
                )
                intent.flags = Intent.FLAG_ACTIVITY_NO_ANIMATION
                startActivity(intent)
                finish()
            }
        }
        splashTread.start()
    }

    @Throws(InterruptedException::class)
    private fun setProgress(progress: ProgressBar, percent: Int, waittime: Int, devString: String) {
        runOnUiThread { (findViewById<View>(R.id.txt_loading) as TextView).text = devString }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progress.setProgress(percent, true)
            Thread.sleep(waittime.toLong())
        } else {
            progress.progress = percent
        }
    }

    override fun serverOnline(): Boolean {
        return try {
            val url = URL(Config.API_URL)
            val sockaddr = InetSocketAddress(
                    InetAddress.getByName(url.host), 80)
            val sock = Socket()
            val timeoutMs = 2000
            sock.connect(sockaddr, timeoutMs)
            true
        } catch (ignored: IOException) {
            false
        }
    }

    private fun getJSONObject(url: String): String {
        val api_key = Utils.getSettings(this).getString("API_KEY", null)
        return try {
            val web = URL(url)
            val connection = web.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "token " + api_key!!)
            val bufferedReader: BufferedReader

            bufferedReader = if (api_key == "")
                BufferedReader(InputStreamReader(web.openStream()))
            else
                BufferedReader(InputStreamReader(connection.inputStream))

            val ret = StringBuilder()
            var line: String? = null

            while ({line = bufferedReader.readLine(); line}() != null)
                ret.append(line!!)

            bufferedReader.close()
            ret.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            "{\"message\": \"Something went wrong.\"}"
        }
    }
}
