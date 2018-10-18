/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import com.afollestad.materialdialogs.MaterialDialog
import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.Config.APP_URL
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.components.CustomSnackbar
import com.dertyp7214.appstore.components.CustomToolbar
import com.dertyp7214.appstore.components.Notifications
import com.dertyp7214.appstore.dev.Logs
import com.dertyp7214.appstore.fragments.FragmentAppInfo
import com.dertyp7214.appstore.fragments.FragmentChangeLogs
import com.dertyp7214.appstore.interfaces.MyInterface
import com.dertyp7214.appstore.items.SearchItem
import com.dertyp7214.qrcodedialog.components.QRCodeDialog
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.util.*

@Suppress("DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "UNREACHABLE_CODE", "NAME_SHADOWING", "IMPLICIT_CAST_TO_ANY")
class AppScreen : Utils(), View.OnClickListener, MyInterface {
    @ColorInt
    private var dominantColor: Int = 0
    @ColorInt
    private var fab: FloatingActionButton? = null
    private var searchItem: SearchItem? = null
    override var themeStore: ThemeStore? = null
    private var uninstall: Button? = null
    private var open: Button? = null
    private var installed: Boolean = false
    private var version: String? = null
    private var collapsingToolbarLayout: CollapsingToolbarLayout? = null
    private var changeLogs: FragmentChangeLogs? = null
    private var shareMenu: MenuItem? = null

    private val serverVersion: String
        get() {
            if (version == null)
                version = Utils.getWebContent(API_URL + "/apps/list.php?version=" + searchItem!!.id)
            return version!!
        }

    private val localVersion: String
        get() {
            return try {
                val packageInfo = packageManager.getPackageInfo(searchItem!!.id, 0)
                packageInfo.versionName
            } catch (e: Exception) {
                serverVersion
            }
        }

    override fun onPostExecute() {
        val extra = intent.extras
        if (searchItem == null)
            searchItem = Utils.appsList[checkExtra(extra).getString("id")]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_screen, false)
        val toolbar = findViewById<CustomToolbar>(R.id.toolbar)
        collapsingToolbarLayout = findViewById(R.id.toolbar_layout)
        setSupportActionBar(toolbar)

        MyTask(this).execute()
        themeStore = ThemeStore.getInstance(this@AppScreen)

        uninstall = findViewById(R.id.btn_uninstall)
        open = findViewById(R.id.btn_open)

        dominantColor = Palette.from(Utils.drawableToBitmap(searchItem!!.appIcon)!!)
                .generate()
                .getDominantColor(ThemeStore.getInstance(this)!!.primaryColor)

        val appBarLayout = appBar

        title = searchItem!!.appTitle
        appBarLayout.setAppBarBackgroundColor(dominantColor)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(true)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        (findViewById<View>(R.id.app_icon) as ImageView).setImageDrawable(searchItem!!.appIcon)
        collapsingToolbarLayout!!.setCollapsedTitleTextColor(themeStore!!.primaryTextColor)
        collapsingToolbarLayout!!.setExpandedTitleColor(
                if (Utils.isColorBright(dominantColor)) Color.BLACK else Color.WHITE)
        collapsingToolbarLayout!!.setContentScrimColor(themeStore!!.primaryColor)
        collapsingToolbarLayout!!.setStatusBarScrimColor(themeStore!!.primaryDarkColor)
        setButtonColor(themeStore!!.accentColor, open!!, uninstall!!)

        appBarLayout.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout1, verticalOffset ->
            val totalScroll = appBarLayout1!!.totalScrollRange
            val currentScroll = totalScroll + verticalOffset

            val colorDark = calculateColor(Utils.manipulateColor(dominantColor, 0.6f),
                    themeStore!!.primaryDarkColor, totalScroll, currentScroll)
            val color = calculateColor(dominantColor, themeStore!!.primaryColor, totalScroll,
                    currentScroll)

            window.navigationBarColor = colorDark
            window.statusBarColor = colorDark

            setColors(toolbar, appBarLayout1, color)
            collapsingToolbarLayout!!.setStatusBarScrimColor(colorDark)
            collapsingToolbarLayout!!.setContentScrimColor(color)
        })

        setUpButtons()

        fab = findViewById(R.id.fab)
        fab!!.setColorFilter(
                if (Utils.isColorBright(themeStore!!.accentColor)) Color.BLACK else Color.WHITE)
        fab!!.backgroundTintList = ColorStateList.valueOf(themeStore!!.accentColor)
        fab!!.visibility = View.GONE
        fab!!.setOnClickListener { share() }

        instance = this
    }

    fun setUpButtons() {
        if (Utils.applicationInstalled(this, searchItem!!.id)) {
            installed = true
            open!!.text = getString(R.string.text_open)
            uninstall!!.text = getString(R.string.text_uninstall)
            uninstall!!.visibility = View.VISIBLE
            uninstall!!.setOnClickListener(this)
            open!!.setOnClickListener(this)
            if (!Utils.verifyInstallerId(this, searchItem!!.id))
                checkUpdates()
        } else {
            installed = false
            open!!.text = getString(R.string.text_install)
            uninstall!!.visibility = View.INVISIBLE
            open!!.setOnClickListener(this)
        }
    }

    private fun checkUpdates() {
        Thread {
            val serverVersion = serverVersion
            val localVersion = localVersion
            Log.d("VERSIONS", "Server: $serverVersion\nLocal: $localVersion")
            if (serverVersion != localVersion && serverVersion != "0") {
                runOnUiThread {
                    uninstall!!.text = getString(R.string.text_update)
                    uninstall!!.setOnClickListener { v ->
                        downloadApp(this, searchItem!!.appTitle, searchItem!!.id,
                                v)
                    }
                }
            }
        }.start()
    }

    private fun share() {
        val url = APP_URL(searchItem!!.id)

        MaterialDialog.Builder(this)
                .title(R.string.share)
                .content(R.string.popup_share_content)
                .positiveColor(themeStore!!.accentColor)
                .negativeColor(themeStore!!.accentColor)
                .positiveText(R.string.yes)
                .negativeText(R.string.no)
                .onPositive { _, _ ->
                    val qrCodeDialog = QRCodeDialog(this)
                    qrCodeDialog.customImageTint(Utils.drawableToBitmap(searchItem!!.appIcon)!!)
                    qrCodeDialog.show(url)
                }
                .onNegative { dialog, _ ->
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.type = "text/plain"
                    sendIntent.putExtra(Intent.EXTRA_TEXT, url)
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.app_name)))
                    dialog.dismiss()
                }
                .build()
                .show()
    }

    private fun setButtonColor(@ColorInt color: Int, button: Button, button2: Button) {
        val bg = resources.getDrawable(R.drawable.button_border) as GradientDrawable
        bg.setStroke(3, color)
        button2.setBackgroundDrawable(bg)
        button2.setTextColor(color)
        button.setTextColor(if (Utils.isColorBright(color)) Color.BLACK else Color.WHITE)
        button.background.setTint(color)
    }

    private fun setColors(customToolbar: CustomToolbar, customAppBarLayout: AppBarLayout, @ColorInt color: Int) {
        customAppBarLayout.setBackgroundColor(color)
        customToolbar.setToolbarIconColor(color)
        changeLogs!!.setColor(color)
        if (shareMenu != null)
            shareMenu!!.icon.setTint(if (Utils.isColorBright(color)) Color.BLACK else Color.WHITE)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_open -> if (installed)
                openApp()
            else
                downloadApp(this, searchItem!!.appTitle, searchItem!!.id, v)
            R.id.btn_uninstall -> removeApp()
        }
    }

    private fun openApp() {
        val intent = packageManager.getLaunchIntentForPackage(searchItem!!.id)
        startActivity(intent)
    }

    private fun removeApp() {
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:" + searchItem!!.id)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        fab!!.visibility = View.INVISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)
        onPostExecute()
        if (fragment is FragmentChangeLogs) {
            changeLogs = fragment
            Thread {
                changeLogs!!
                        .getChangeLogs(searchItem!!, object : FragmentChangeLogs.Callback {
                            override fun run(textView: TextView?, text: Spanned?) {
                                runOnUiThread {
                                    if (textView != null) {
                                        textView.text = text
                                        textView.movementMethod = LinkMovementMethod.getInstance()
                                        textView.visibility = View.VISIBLE
                                    }
                                }
                            }
                        })
            }.start()
        } else if (fragment is FragmentAppInfo) {
            val appInfo = fragment as FragmentAppInfo?
            appInfo!!.getAppInfo(searchItem!!)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        Thread {
            val serverVersion = serverVersion
            val localVersion = localVersion
            if (serverVersion != localVersion && serverVersion != "0"
                    && menu.findItem(MENU_UNINSTALL) == null)
                runOnUiThread { menu.add(0, MENU_UNINSTALL, Menu.NONE, R.string.text_uninstall) }
        }.start()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(R.menu.app_menu, menu)

        val iconTint = if (Utils.isColorBright(themeStore!!.primaryColor)) Color.BLACK else Color.WHITE

        shareMenu = menu.findItem(R.id.action_share)
        shareMenu!!.icon.setTint(iconTint)

        val updateItem = menu.findItem(R.id.action_update)
        updateItem.isChecked = searchItem!!.isUpdate
        updateItem.setOnMenuItemClickListener { item ->
            item.isChecked = !item.isChecked
            searchItem = SearchItem(searchItem!!.appTitle, searchItem!!.id,
                    searchItem!!.appIcon, searchItem!!.version, item.isChecked)
            Utils.appsList[searchItem!!.id] = searchItem!!
            Thread {
                Log.d("RETURN", Utils.getWebContent(
                        Config.API_URL + "/apps/myapps.php?update=" + item.isChecked + "&uid="
                                + Config.UID(this) + "&app_id=" + searchItem!!.id))
            }.start()
            true
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when (id) {
            R.id.action_share -> share()
            MENU_UNINSTALL -> removeApp()
        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class MyTask internal constructor(internal var myInterface: MyInterface) : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {
            myInterface.onPostExecute()
            return null
        }
    }

    companion object {
        private const val MENU_UNINSTALL = Menu.FIRST + 2
        @SuppressLint("StaticFieldLeak")
        var instance: AppScreen? = null
            private set

        fun hasInstance(): Boolean {
            return instance != null
        }

        fun downloadApp(activity: Activity, title: String, id: String, view: View) {
            val file = File(Environment.getExternalStorageDirectory(), ".appStore")
            val url = API_URL + Config.APK_PATH
                    .replace("{id}", id)
                    .replace("{uid}", Config.UID(activity)!!)
            val path = file.absolutePath
            var notifications: Notifications? = null
            val random = Random()
            val notiId = random.nextInt(65536)
            val finishedNotiId = random.nextInt(65536)
            val fileName = "download_$notiId.apk"
            var lastMilli: Long = System.currentTimeMillis()
            val downloadId: Int = PRDownloader.download(url, path, fileName)
                    .build()
                    .setOnStartOrResumeListener {
                        val color = if (activity.window.navigationBarColor == Color.BLACK)
                            activity.window.statusBarColor
                        else
                            activity.window
                                    .navigationBarColor
                        CustomSnackbar(activity, color)
                                .make(view, "Download started", CustomSnackbar.LENGTH_LONG)
                                .show()
                        notifications = Notifications(
                                activity,
                                notiId,
                                activity.getString(R.string.app_name) + " - " + title,
                                activity.getString(R.string.app_name) + " - " + title,
                                "",
                                null,
                                true)
                    }
                    .setOnProgressListener {
                        val percentage = ((it.currentBytes * 100L) / it.totalBytes).toInt()
                        val current = System.currentTimeMillis()
                        if (current - lastMilli >= 100) {
                            notifications!!.setProgress(percentage, "${humanReadableByteCount(it.currentBytes, true)} / ${humanReadableByteCount(it.totalBytes, true)}")
                            lastMilli = current
                        }
                    }
                    .setOnCancelListener {
                        notifications!!.removeNotification()
                    }
                    .start(object : OnDownloadListener {
                        override fun onDownloadComplete() {
                            notifications!!.removeNotification()
                            val f = File(file, fileName)
                            finishedNotification(
                                    activity,
                                    finishedNotiId,
                                    activity.getString(R.string.app_name) + " - " + title,
                                    false, "").showNotification()
                            if (Utils.getSettings(activity).getBoolean("root_install", false)) {
                                Utils.executeCommand(activity, "rm -rf /data/local/tmp/app.apk")
                                Utils.executeCommand(activity,
                                        "mv " + f.absolutePath + " /data/local/tmp/app.apk")
                                Utils.executeCommand(activity, "pm install -r /data/local/tmp/app.apk\n")
                            } else
                                Utils.installApk(activity, f)
                        }

                        override fun onError(error: Error?) {
                            notifications!!.removeNotification()
                            val errorMessage = when {
                                error == null -> ""
                                error.isConnectionError -> activity.getString(R.string.notification_connection_error)
                                error.isServerError -> activity.getString(R.string.notification_server_error)
                                else -> ""
                            }
                            finishedNotification(
                                    activity,
                                    finishedNotiId,
                                    activity.getString(R.string.app_name) + " - " + title,
                                    true, errorMessage).showNotification()
                        }
                    })
            Logs.getInstance(activity).info("DownloadId", downloadId)
        }

        private fun finishedNotification(activity: Activity, id: Int, title: String, error: Boolean, errorMessage: String): Notifications {
            val notifications = Notifications(
                    activity,
                    id,
                    title,
                    title,
                    errorMessage,
                    null,
                    false)
            if (error)
                notifications.setCanceled("ERROR")
            else
                notifications.setFinished()
            return notifications
        }
    }
}
