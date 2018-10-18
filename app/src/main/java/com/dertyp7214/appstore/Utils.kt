/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore

import android.Manifest
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.text.Html
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.DrawableCompat
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.Config.UID
import com.dertyp7214.appstore.components.CustomAppBarLayout
import com.dertyp7214.appstore.components.CustomToolbar
import com.dertyp7214.appstore.dev.Logs
import com.dertyp7214.appstore.items.SearchItem
import com.dertyp7214.appstore.receivers.PackageUpdateReceiver
import com.dertyp7214.appstore.screens.MainActivity
import com.dertyp7214.themeablecomponents.utils.ThemeManager
import com.gw.swipeback.SwipeBackLayout
import com.gw.swipeback.WxSwipeBackLayout
import com.gw.swipeback.tools.Util
import org.json.JSONObject
import java.io.*
import java.net.*
import java.util.*
import java.util.regex.Pattern

@Suppress("DEPRECATION", "UNCHECKED_CAST", "NAME_SHADOWING")
@SuppressLint("Registered")
abstract class Utils : AppCompatActivity() {
    var toolbar: CustomToolbar? = null
    open lateinit var logs: Logs
    protected var PERMISSIONS = 10
    protected var oldAppPackageName = "com.hacker.appstore"
    protected open var themeStore: ThemeStore? = null
    protected lateinit var themeManager: ThemeManager
    protected lateinit var wxSwipeBackLayout: WxSwipeBackLayout
    private var statusColor = -1
    private var callback: Callback? = null

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    val navigationBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    val appBar: CustomAppBarLayout
        get() = findViewById(R.id.app_bar)

    var navigationBarColor: Int
        get() = window.navigationBarColor
        set(@ColorInt color) {
            window.navigationBarColor = color
        }

    var statusBarColor: Int
        get() = window.statusBarColor
        set(@ColorInt color) {
            window.statusBarColor = color
        }

    val isRooted: Boolean
        get() {

            val buildTags = android.os.Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                return true
            }

            try {
                val file = File("/system/app/Superuser.apk")
                if (file.exists()) {
                    return true
                }
            } catch (ignored: Exception) {
            }

            return (runCommand("/system/xbin/which su")
                    || runCommand("/system/bin/which su") || runCommand("which su"))
        }

    fun setContentView(layoutResID: Int, swipe: Boolean) {
        super.setContentView(layoutResID)
        if (swipe) {
            wxSwipeBackLayout = WxSwipeBackLayout(this)
            wxSwipeBackLayout.directionMode = SwipeBackLayout.FROM_LEFT
            wxSwipeBackLayout.attachToActivity(this)
            wxSwipeBackLayout.setSwipeBackListener(object : SwipeBackLayout.OnSwipeBackListener {
                override fun onViewPositionChanged(mView: View, swipeBackFraction: Float, swipeBackFactor: Float) {
                    wxSwipeBackLayout.invalidate()
                    Util.onPanelSlide(swipeBackFraction)
                    if (statusColor == -1) statusColor = statusBarColor
                    try {
                        statusBarColor = calculateColor(manipulateColor(MainActivity.color, 0.6f),
                                statusColor, 100,
                                (swipeBackFraction * 100).toInt())
                    } catch (ignored: Exception) {
                    }

                }

                override fun onViewSwipeFinished(mView: View, isEnd: Boolean) {
                    if (isEnd) {
                        if (callback != null) callback!!.run()
                        statusBarColor = Color.TRANSPARENT
                        wxSwipeBackLayout.finish()
                    }
                    Util.onPanelReset()
                }
            })
        }
    }

    protected fun setSwipeBackCallback(callback: Callback) {
        this.callback = callback
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(layoutResID, true)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeStore = ThemeStore.getInstance(this)
        themeManager = ThemeManager.getInstance(this)
        themeManager.changeAccentColor(themeStore!!.accentColor)
        themeManager.changePrimaryColor(this, themeStore!!.primaryColor, true,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.P, false)
        rainbow(this)
        if (PackageUpdateReceiver.activity == null)
            PackageUpdateReceiver.activity = this
    }

    protected fun applyTheme() {
        val themeStore = ThemeStore.getInstance(this)
        statusBarColor = themeStore!!.primaryDarkColor
        if (Build.VERSION.SDK_INT < 28)
            setNavigationBarColor(this, window.decorView, themeStore.primaryColor,
                    300)
        else
            setNavigationBarColor(this, window.decorView, Color.WHITE, 300)
        if (toolbar != null) {
            toolbar!!.setBackgroundColor(themeStore.primaryColor)
            toolbar!!.setToolbarIconColor(themeStore.primaryColor)
        } else {
            Objects.requireNonNull<ActionBar>(supportActionBar)
                    .setBackgroundDrawable(ColorDrawable(themeStore.primaryColor))
            supportActionBar!!.title = Html.fromHtml("<font color='" + String.format("#%06X",
                    0xFFFFFF and themeStore
                            .primaryTextColor
            ) + "'>" + supportActionBar!!.title + "</font>")
        }
    }

    fun sleep(duration: Long) {
        try {
            Thread.sleep(duration)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

    fun getBitmapFromResource(@DrawableRes resource: Int): Bitmap {
        return BitmapFactory.decodeResource(resources, resource)
    }

    fun startActivity(context: Activity, aClass: Class<*>) {
        startActivity(Intent(context, aClass))
    }

    fun startActivity(context: Activity, aClass: Class<*>, options: Bundle?) {
        startActivity(Intent(context, aClass), options)
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = v.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    fun cutString(string: String, cutAt: Int): String {
        if (string.length < cutAt)
            return string
        val ret = StringBuilder()
        for (i in 0 until cutAt)
            ret.append(string[i])
        ret.append("...")
        return ret.toString()
    }

    fun checkExtra(extra: Bundle?): Bundle {
        if (extra == null) finish()
        assert(extra != null)
        if (extra!!.size() > 1) finish()
        return extra
    }

    protected fun setColors() {
        logs = Logs.getInstance(this)
        logs.info("setColor", (themeStore != null).toString())
        if (themeStore != null) {
            statusBarColor = themeStore!!.primaryDarkColor
            toolbar!!.setBackgroundColor(themeStore!!.primaryColor)
            toolbar!!.setToolbarIconColor(themeStore!!.primaryColor)
            if (Build.VERSION.SDK_INT < 28)
                setNavigationBarColor(this, window.decorView,
                        themeStore!!.primaryColor,
                        300)
            else
                setNavigationBarColor(this, window.decorView, Color.WHITE, 300)
        }
    }

    protected fun setBackButton() {
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(true)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
    }

    fun checkExtraKey(extra: Bundle, key: String): Any? {
        checkExtra(extra)
        if (extra.get(key) == null) finish()
        return extra.get(key)
    }

    protected fun readFromFileInputStream(fileInputStream: FileInputStream?): String {
        val retBuf = StringBuilder()

        try {
            if (fileInputStream != null) {
                val inputStreamReader = InputStreamReader(fileInputStream)
                val bufferedReader = BufferedReader(inputStreamReader)

                var lineData: String? = bufferedReader.readLine()
                while (lineData != null) {
                    retBuf.append(lineData)
                    lineData = bufferedReader.readLine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return retBuf.toString()
    }

    fun <V : View> findChildrenByClass(clazz: Class<V>, vararg viewGroups: ViewGroup): Collection<V> {
        val collection = ArrayList<V>()
        for (viewGroup in viewGroups)
            collection.addAll(gatherChildrenByClass(viewGroup, clazz, ArrayList()))
        return collection
    }

    private fun <V : View> gatherChildrenByClass(viewGroup: ViewGroup, clazz: Class<V>, childrenFound: MutableCollection<V>): Collection<V> {

        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (clazz.isAssignableFrom(child.javaClass)) {
                childrenFound.add(child as V)
            }
            if (child is ViewGroup) {
                gatherChildrenByClass(child, clazz, childrenFound)
            }
        }

        return childrenFound
    }

    fun importSettings(settings: Map<String, Any>) {
        val prefs = getSettings(this).edit()
        for (key in settings.keys) {
            val obj = settings[key]
            when (obj) {
                is Float -> prefs.putFloat(key, obj)
                is String -> prefs.putString(key, obj as String?)
                is Boolean -> prefs.putBoolean(key, obj)
                is Int -> prefs.putInt(key, obj)
                is Long -> prefs.putLong(key, obj)
                is Set<*> -> prefs.putStringSet(key, obj as Set<String>?)
            }
        }
        prefs.apply()
    }

    fun exportSettings(): String {
        val settings = getSettings(this).all
        return JSONObject(settings).toString()
    }

    open fun serverOnline(): Boolean {
        try {
            val url = URL(Config.API_URL)
            val sockaddr = InetSocketAddress(
                    InetAddress.getByName(url.host), 80)
            val sock = Socket()
            val timeoutMs = 2000
            sock.connect(sockaddr, timeoutMs)
            return true
        } catch (ignored: IOException) {
        }

        return false
    }

    fun haveConnection(): Boolean {
        try {
            val url = URL("http://www.google.de")
            val sockaddr = InetSocketAddress(
                    InetAddress.getByName(url.host), 80)
            val sock = Socket()
            val timeoutMs = 2000
            sock.connect(sockaddr, timeoutMs)
            return true
        } catch (ignored: IOException) {
        }

        return false
    }

    fun calculateColor(color1: Int, color2: Int, max: Int, current: Int): Int {
        val strC1 = Integer.toHexString(color1)
        val strC2 = Integer.toHexString(color2)

        val retColor = StringBuilder("#")
        var i = 2

        while (i < strC1.length) {
            val tmp1 = strC1[i] + "" + strC1[i + 1]
            val tmp2 = strC2[i] + "" + strC2[i + 1]

            val tmp1Color = java.lang.Long.parseLong(tmp1, 16).toInt()
            val tmp2Color = java.lang.Long.parseLong(tmp2, 16).toInt()

            val dif = tmp1Color - tmp2Color
            val difCalc = dif.toDouble() / max
            val colorMerge = (difCalc * current).toInt()
            var add = Integer.toHexString(tmp2Color + colorMerge)

            if (add.length < 2)
                add = "0$add"

            retColor.append(add)
            i++
            i++
        }

        return Color.parseColor(retColor.toString())
    }

    fun checkAppDir(): Boolean {
        val appDir = File(Environment.getExternalStorageDirectory(), ".appStore")
        deleteDirectory(appDir)
        return appDir.mkdirs()
    }

    private fun deleteDirectory(path: File): Boolean {
        if (path.exists()) {
            val files = path.listFiles()
            for (file in files) {
                if (file.isDirectory) {
                    deleteDirectory(file)
                } else {
                    file.delete()
                }
            }
        }
        return path.delete()
    }

    fun checkForOldAppStore() {
        if (!getSettings(this).getBoolean("old_appstore", false)) {
            if (appInstalled(this, oldAppPackageName)) {
                val onClickListener = { _: DialogInterface, which: Int ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val intent = Intent(Intent.ACTION_DELETE)
                            intent.data = Uri.parse("package:$oldAppPackageName")
                            startActivity(intent)
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.oldAppStoreFound))
                builder.setPositiveButton(android.R.string.yes, onClickListener)
                builder.setNegativeButton(android.R.string.no, onClickListener)
                builder.show()
            }
        }
    }

    fun runCommand(command: String): Boolean {
        logs = Logs(this)
        return try {
            Runtime.getRuntime().exec(command)
            true
        } catch (e: Exception) {
            logs.error("ERROR", e.message!!)
            false
        }
    }

    fun checkPermissions() {
        ActivityCompat.requestPermissions(
                this,
                permissions().toTypedArray(),
                PERMISSIONS
        )
    }

    fun setTimeOut(duration: Int, callback: Callback) {
        Thread {
            try {
                Thread.sleep(duration.toLong())
                runOnUiThread { callback.run() }
            } catch (e: InterruptedException) {
                runOnUiThread { callback.run() }
            }
        }.start()
    }

    private fun permissions(): List<String> {
        return ArrayList(Arrays.asList(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS
        ))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(this,
                                permission
                        ) != PackageManager.PERMISSION_GRANTED) {
                    finish()
                }
            }
        }
    }

    protected fun syncPreferences() {
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (toolbar != null)
            toolbar!!.setToolbarIconColor(ThemeStore.getInstance(this)!!.primaryColor)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    interface Async {

        fun run(activity: Activity, aClass: Class<*>, options: Bundle?)
    }

    interface Listener {
        fun run(progress: Int)
    }

    interface Callback {
        fun run()
    }

    class ByteBuffer(val bytes: ByteArray, val buffer: Int)

    class startActivityAsync @JvmOverloads constructor(private val activity: Activity, private val aClass: Class<*>, private val options: Bundle? = null) {
        private var time: Long = 0

        fun setTime(time: Long): startActivityAsync {
            this.time = time
            return this
        }

        fun start(async: Async) {
            Thread {
                sleep(time)
                async.run(activity, aClass, options)
            }.start()
        }
    }

    companion object {
        val COLORED_NAVIGATIONBAR = "colored_nav_bar"
        protected val PACKAGE_NAME = "com.dertyp7214.appstore"
        var userImageHashMap = HashMap<String, Drawable>()
        var appsList = HashMap<String, SearchItem>()
        var currentApp: SearchItem? = null
        private val icons = HashMap<String, Drawable>()
        private var rainbow: Thread? = null

        fun colorToString(@ColorInt intColor: Int): String {
            return String.format("#%06X", 0xFFFFFF and intColor)
        }

        fun addAlpha(originalColor: String, alpha: Double): String {
            var originalColor = originalColor
            val alphaFixed = Math.round(alpha * 255)
            var alphaHex = java.lang.Long.toHexString(alphaFixed)
            if (alphaHex.length == 1) {
                alphaHex = "0$alphaHex"
            }
            if (originalColor.replace("#", "").length > 6)
                originalColor = originalColor.replaceFirst("#..".toRegex(), "#")
            originalColor = originalColor.replace("#", "#$alphaHex")
            return originalColor
        }

        fun tintWidget(view: View, color: Int) {
            val wrappedDrawable = DrawableCompat.wrap(view.background)
            DrawableCompat.setTint(wrappedDrawable.mutate(), color)
            view.background = wrappedDrawable
        }

        fun setCursorColor(view: EditText, @ColorInt color: Int) {
            try {
                var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
                field.isAccessible = true
                val drawableResId = field.getInt(view)

                field = TextView::class.java.getDeclaredField("mEditor")
                field.isAccessible = true
                val editor = field.get(view)

                val drawable = ContextCompat.getDrawable(view.context, drawableResId)
                drawable!!.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                val drawables = arrayOf(drawable, drawable)

                field = editor.javaClass.getDeclaredField("mCursorDrawable")
                field.isAccessible = true
                field.set(editor, drawables)
            } catch (ignored: Exception) {
            }
        }

        fun removeMyApp(packageName: String, context: Context) {
            getWebContent(Config.API_URL + "/apps/myapps.php?uid=" + Config
                    .UID(context) + "&remove=" + packageName)
        }

        fun getInstalledApps(context: Context): List<ApplicationInfo> {
            val pm = context.packageManager
            return pm.getInstalledApplications(PackageManager.GET_META_DATA)
        }

        fun getApplicationInfo(context: Context, packageName: String): ApplicationInfo? {
            var info: ApplicationInfo? = null
            try {
                info = context.packageManager.getApplicationInfo(packageName, 0)
            } catch (ignored: Exception) {
            }
            return info
        }

        fun encodeToBase64(drawable: Drawable): String {
            val image = drawableToBitmap(drawable)
            val byteArrayOS = ByteArrayOutputStream()
            image!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)
            return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
        }

        fun decodeBase64(context: Context, input: String): Drawable {
            val decodedBytes = Base64.decode(input, 0)
            return BitmapDrawable(
                    context.resources, BitmapFactory
                    .decodeByteArray(decodedBytes, 0, decodedBytes.size))
        }

        fun getSettings(context: Context): SharedPreferences {
            return context.getSharedPreferences("settings_" + Config.UID(context), Context.MODE_PRIVATE)
        }

        fun setSettings(context: Context, settings: SharedPreferences) {
            val objectMap = settings.all
            val prefs = getSettings(context).edit()
            for (key in objectMap.keys) {
                val obj = objectMap[key]
                when (obj) {
                    is Float -> prefs.putFloat(key, obj)
                    is String -> prefs.putString(key, obj as String?)
                    is Boolean -> prefs.putBoolean(key, obj)
                    is Int -> prefs.putInt(key, obj)
                    is Long -> prefs.putLong(key, obj)
                    is Set<*> -> prefs.putStringSet(key, obj as Set<String>?)
                }
            }
            prefs.apply()
        }

        fun setNavigationBarColor(activity: Activity, view: View?, @ColorInt color: Int, duration: Int) {
            if ((getSettings(activity).getBoolean(COLORED_NAVIGATIONBAR,
                            false
                    ) || Build.VERSION.SDK_INT < Build.VERSION_CODES.P)
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view != null) {
                val window = activity.window
                val animator = ValueAnimator
                        .ofObject(ArgbEvaluator(), window.navigationBarColor, color)
                animator.duration = duration.toLong()
                animator.addUpdateListener { animation ->
                    val c = animation.animatedValue as Int
                    if (isColorBright(c) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    else
                        view.systemUiVisibility = View.VISIBLE
                    window.navigationBarColor = c
                }
                animator.start()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && view != null) {
                val window = activity.window
                val animator = ValueAnimator
                        .ofObject(ArgbEvaluator(), window.navigationBarColor, Color.BLACK)
                animator.duration = duration.toLong()
                animator.addUpdateListener { animation ->
                    val c = animation.animatedValue as Int
                    if (isColorBright(c) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    else
                        view.systemUiVisibility = View.VISIBLE
                    window.navigationBarColor = c
                }
                animator.start()
            }
        }

        fun setStatusBarColor(activity: Activity, view: View, @ColorInt color: Int, duration: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val window = activity.window
                val animator = ValueAnimator
                        .ofObject(ArgbEvaluator(), window.statusBarColor, color)
                animator.duration = duration.toLong()
                animator.addUpdateListener { animation ->
                    val c = animation.animatedValue as Int
                    if (isColorBright(c))
                        view.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    else
                        view.systemUiVisibility = View.VISIBLE
                    window.statusBarColor = c
                }
                animator.start()
            }
        }

        fun isColorBright(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color
                    .blue(color)) / 255
            return darkness < 0.5
        }

        fun getDominantColor(drawable: Drawable): Int {
            val newBitmap = Bitmap.createScaledBitmap(drawableToBitmap(drawable)!!, 1, 1, true)
            val color = newBitmap.getPixel(0, 0)
            newBitmap.recycle()
            return color
        }

        fun manipulateColor(color: Int, factor: Float): Int {
            val a = Color.alpha(color)
            val r = Math.round(Color.red(color) * factor)
            val g = Math.round(Color.green(color) * factor)
            val b = Math.round(Color.blue(color) * factor)
            return Color.argb(
                    a,
                    Math.min(r, 255),
                    Math.min(g, 255),
                    Math.min(b, 255)
            )
        }

        fun drawableToBitmap(drawable: Drawable?): Bitmap? {
            val bitmap = if (drawable!!.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Bitmap.createBitmap(1, 1,
                        Bitmap.Config.ARGB_8888
                )
            } else {
                Bitmap
                        .createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight,
                                Bitmap.Config.ARGB_8888
                        )
            }

            if (drawable is BitmapDrawable) {
                val bitmapDrawable = drawable as BitmapDrawable?
                if (bitmapDrawable!!.bitmap != null) {
                    return bitmapDrawable.bitmap
                }
            }

            val canvas = Canvas(bitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        }

        fun getWebContent(url: String): String? {
            return try {
                val web = URL(url)
                val reader = BufferedReader(InputStreamReader(web.openStream()))

                val ret = StringBuilder()
                var line: String? = null

                while ({line = reader.readLine(); line}() != null)
                    ret.append(line!!)

                reader.close()
                ret.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun getWebContent(url: String, path: File, id: Int, listener: Listener): File? {
            return try {
                val fileurl = URL(url)
                val urlConnection = fileurl.openConnection()
                urlConnection.connect()

                val inputStream = BufferedInputStream(urlConnection.getInputStream(), 8192)

                if (!path.exists())
                    path.mkdirs()

                val downloadedFile = File(path, "download_app_$id.apk")
                val outputStream = FileOutputStream(downloadedFile)
                val buffer = ByteArray(8192)

                var last = 0
                val fileSize = urlConnection.contentLength.toLong()
                var read: Long? = null
                var total: Long = 0

                listener.run(0)

                while ({ read = inputStream.read(buffer).toLong(); read }()!! >= 0) {
                    val tmp = (total * 100 / fileSize).toInt()
                    total += read!!
                    if (fileSize > 0 && tmp > last + 2) {
                        last = tmp
                        listener.run(tmp)
                    }
                    outputStream.write(buffer, 0, read!!.toInt())
                }

                listener.run(100)

                outputStream.flush()
                outputStream.close()
                inputStream.close()
                downloadedFile
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        fun executeCommand(activity: Activity, cmds: String) {
            val logs = Logs(activity)
            logs.info("COMMAND", cmds)
            try {
                val process = Runtime.getRuntime().exec("su")
                val os = DataOutputStream(process.outputStream)

                os.writeBytes(cmds + "\n")

                os.writeBytes("exit\n")
                os.flush()
                os.close()

                process.waitFor()
            } catch (ignored: Exception) {
            }
        }

        fun appInstalled(context: Activity, uri: String): Boolean {
            return applicationInstalled(context, uri) && !verifyInstallerId(context, uri)
        }

        fun verifyInstallerId(context: Activity, packageName: String): Boolean {
            return try {
                val validInstallers = ArrayList(
                        Arrays.asList("com.android.vending", "com.google.android.feedback"))

                val installer = context.packageManager.getInstallerPackageName(packageName)

                installer != null && validInstallers.contains(installer)
            } catch (e: Exception) {
                Logs.getInstance(context).error("verifyInstallerId", e.toString())
                false
            }
        }

        fun applicationInstalled(context: Context, uri: String): Boolean {
            val pm = context.packageManager
            return try {
                pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
                true
            } catch (ignored: PackageManager.NameNotFoundException) {
                false
            }
        }

        @JvmOverloads
        fun drawableFromUrl(context: Context, url: String, @DrawableRes def: Int = R.drawable.ic_person): Drawable {
            if (icons.containsKey(url))
                return icons[url] ?: context.resources.getDrawable(R.drawable.ic_launcher)
            return try {
                val bmp: Bitmap
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val input = connection.inputStream

                bmp = BitmapFactory.decodeStream(input)
                val drawable = BitmapDrawable(context.resources, bmp)
                icons[url] = drawable
                drawable
            } catch (e: Exception) {
                e.printStackTrace()
                val bitmap = drawableToBitmap(context.getDrawable(def))
                val color = ThemeStore.getInstance(context)!!.accentColor
                BitmapDrawable(
                        context.resources, overlay(createBitmap(bitmap!!, color), bitmap))
            }
        }

        private fun createBitmap(copy: Bitmap, @ColorInt color: Int): Bitmap {
            val bmp = Bitmap
                    .createBitmap(copy.width, copy.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(color)
            return bmp
        }

        private fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
            val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
            val canvas = Canvas(bmOverlay)
            canvas.drawBitmap(bmp1, Matrix(), null)
            canvas.drawBitmap(bmp2, Matrix(), null)
            return bmOverlay
        }

        fun installApk(context: Context, file: File?) {
            try {
                if (file!!.exists()) {
                    val fileNameArray = file.name.split(Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (fileNameArray[fileNameArray.size - 1] == "apk") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val downloadedApk = getFileUri(context, file)
                            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(
                                    downloadedApk,
                                    "application/vnd.android.package-archive"
                            )
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            context.startActivity(intent)
                        } else {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setDataAndType(
                                    Uri.fromFile(file),
                                    "application/vnd.android.package-archive"
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun getFileUri(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(context,
                    context.applicationContext
                            .packageName + ".GenericFileProvider", file
            )
        }

        fun n(o: Any) {
        }

        fun sleep(duration: Long) {
            try {
                Thread.sleep(duration)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }

        private fun rainbow(context: Activity) {
            rainbow = getRainbow(context)
            if (getSettings(context).getBoolean("rainbow_mode", false)) rainbow!!.start()
        }

        private fun getRainbow(context: Activity): Thread {
            return Thread {
                Looper.prepare()
                while (rainbow != null) {
                    for (component in ThemeManager.getInstance(context)
                            .components) {
                        sleep(500)
                        context.runOnUiThread {
                            val animator = ValueAnimator.ofInt(0, 360)
                            animator.duration = 3000
                            animator.addUpdateListener { animation ->
                                val color = animation.animatedValue as Int
                                component.changeColor(
                                        changeHue(ThemeStore.getInstance(context)!!.accentColor,
                                                color))
                            }
                            animator.start()
                        }
                    }
                    sleep(3000 + 500)
                }
            }
        }

        @ColorInt
        fun changeHue(@ColorInt color: Int, degree: Int): Int {
            val hsv = FloatArray(3)
            Color.colorToHSV(color, hsv)
            hsv[0] += degree.toFloat()
            while (hsv[0] > 360) hsv[0] -= 360f
            return Color.HSVToColor(hsv)
        }

        fun toggleRainBow(enabled: Boolean, context: Activity) {
            if (enabled && rainbow == null) {
                rainbow = getRainbow(context)
                rainbow!!.start()
            } else {
                rainbow = null
            }
        }

        fun getResId(resName: String, c: Class<*>): Int {
            return try {
                val idField = c.getDeclaredField(resName)
                idField.getInt(idField)
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }

        fun getStatusBarColor(activity: Activity): Int {
            return activity.window.statusBarColor
        }

        fun setStatusBarColor(activity: Activity, @ColorInt color: Int) {
            activity.window.statusBarColor = color
        }
    }
}
