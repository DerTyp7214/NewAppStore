/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.screens

import android.accounts.AccountManager
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.util.Pair
import android.util.TypedValue
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.dertyp7214.appstore.*
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.adapter.SearchAdapter
import com.dertyp7214.appstore.adapter.UserAdapter
import com.dertyp7214.appstore.components.MaterialSearchBar
import com.dertyp7214.appstore.dev.Logs
import com.dertyp7214.appstore.fragments.FragmentAbout
import com.dertyp7214.appstore.fragments.FragmentAppGroups
import com.dertyp7214.appstore.fragments.FragmentMyApps
import com.dertyp7214.appstore.fragments.TabFragment
import com.dertyp7214.appstore.helpers.SQLiteHandler
import com.dertyp7214.appstore.helpers.SessionManager
import com.dertyp7214.appstore.items.SearchItem
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder
import java.util.*

@Suppress("DEPRECATION", "REDUNDANT_LABEL_WARNING")
class MainActivity : Utils(), NavigationView.OnNavigationItemSelectedListener, MaterialSearchBar.OnSearchActionListener {

    private var adapter: ViewPagerAdapter? = null
    private var searchAdapter: SearchAdapter? = null
    private var id = 0
    private var random: Random? = null
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var navView: NavigationView? = null
    private var profilePic: Drawable? = null
    private var searchBar: MaterialSearchBar? = null
    private var drawer: DrawerLayout? = null
    private var app_bar: View? = null
    private var fragmentAbout: FragmentAbout? = null
    private val appItems = ArrayList<SearchItem>()

    private val margin: Int
        get() = dpToPx(6) + dpToPx(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main, false)

        random = Random()
        themeStore = ThemeStore.resetInstance(this)
        logs = Logs(this)

        Utils.getChangeLogs(this).showDialogOnVersionChange()

        checkAppDir()
        checkForOldAppStore()
        if (Build.VERSION.SDK_INT < 28)
            Utils.setNavigationBarColor(this, window.decorView, themeStore!!.primaryColor,
                    300)
        else
            Utils.setNavigationBarColor(this, window.decorView, Color.WHITE, 300)

        drawer = findViewById(R.id.drawer_layout)
        searchBar = findViewById(R.id.searchBar)
        searchBar!!.setOnSearchActionListener(this)
        searchBar!!.setRoundedSearchBarEnabled(true)
        searchBar!!.setupRoundedSearchBarEnabled(
                Utils.getSettings(this).getInt("search_bar_radius", 20))
        searchBar!!.setCardViewElevation(10)
        searchBar!!.addTextChangeListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {

            }

        })

        app_bar = findViewById(R.id.app_bar)
        app_bar!!.setBackgroundColor(ThemeStore.getInstance(this)!!.primaryColor)
        app_bar!!.setPadding(0, statusBarHeight, 0, 0)

        val margin = margin
        setMargins(searchBar!!, margin, margin, margin, margin)

        navView = findViewById(R.id.nav_view)
        navView!!.setCheckedItem(R.id.nav_home)
        navView!!.setPadding(0, 0, 0, navigationBarHeight)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navView!!.setNavigationItemSelectedListener(this)

        viewPager = findViewById(R.id.pager)
        adapter = ViewPagerAdapter(supportFragmentManager)
        addFragment(FragmentAppGroups())
        addFragment(FragmentMyApps())
        fragmentAbout = FragmentAbout(this)
        adapter!!.addFragment(fragmentAbout!!, getString(R.string.mal_title_about))

        viewPager!!.adapter = adapter

        tabLayout = findViewById(R.id.tabBar)
        tabLayout!!.setupWithViewPager(viewPager)
        tabLayout!!.setSelectedTabIndicatorColor(themeStore!!.primaryTextColor)
        tabLayout!!.setTabTextColors(themeStore!!.primaryTextColor,
                themeStore!!.primaryTextColor)

        searchAdapter = SearchAdapter(this, appItems)

        val searchRecView = findViewById<RecyclerView>(R.id.search_view)
        searchRecView.layoutManager = LinearLayoutManager(this)
        searchRecView.setPadding(0, 0, 0, navigationBarHeight)
        searchRecView.adapter = searchAdapter

        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                app_bar!!.setBackgroundColor(themeStore!!
                        .getPrimaryHue(((position.toFloat() + positionOffset) * 36).toInt()))
                if (Build.VERSION.SDK_INT < 28)
                    window.navigationBarColor = themeStore!!
                            .getPrimaryHue(((position.toFloat() + positionOffset) * 36).toInt())
                color = themeStore!!
                        .getPrimaryHue(((position.toFloat() + positionOffset) * 36).toInt())
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> navView!!.setCheckedItem(R.id.nav_home)
                    1 -> navView!!.setCheckedItem(R.id.nav_myapps)
                    2 -> navView!!.setCheckedItem(R.id.nav_about)
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        setUpNavView()
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
                resources.displayMetrics).toInt()
    }

    override fun onResume() {
        if (settingsChanged) {
            navView!!.setCheckedItem(R.id.nav_home)
            scrollTo(0)
            if (FragmentMyApps.hasInstance()) FragmentMyApps.getInstance().getMyApps()
            val animator = ValueAnimator
                    .ofObject(ArgbEvaluator(), color, themeStore!!.primaryColor)
            animator.duration = 300
            animator.addUpdateListener { animation ->
                val color = animation.animatedValue as Int
                themeStore = ThemeStore.resetInstance(this)
                app_bar!!.setBackgroundColor(color)
                if (Build.VERSION.SDK_INT < 28)
                    window.navigationBarColor = color
            }
            animator.start()
            color = themeStore!!.primaryColor
            settingsChanged = false
        }
        super.onResume()
    }

    override fun onRestart() {
        themeStore = ThemeStore.resetInstance(this)
        super.onRestart()
    }

    override fun onStart() {
        themeStore = ThemeStore.resetInstance(this)
        super.onStart()
    }

    private fun setUpNavView() {
        Thread {
            val db = SQLiteHandler(applicationContext)
            val user = db.userDetails
            val userID = user["uid"]
            val userName = user["name"]
            val userEmail = user["email"]
            val bg = findViewById<View>(R.id.nav_bg)
            val img = findViewById<ImageView>(R.id.user_image)
            val name = findViewById<TextView>(R.id.txt_name)
            val email = findViewById<TextView>(R.id.txt_email)
            profilePic = getDrawable(R.mipmap.ic_launcher_round)
            runOnUiThread {
                try {
                    name.text = userName
                    email.text = userEmail
                    setAccounts()
                } catch (e: Exception) {
                    e.printStackTrace()
                    logs.error("setUpNavView - runOnUiThread",
                            e.toString() + "\n" + e.message)
                    Utils.sleep(100)
                    setUpNavView()
                }
            }
            try {
                val file = File(filesDir, userID!! + ".png")
                when {
                    Utils.userImageHashMap.containsKey(userID) -> profilePic = Utils.userImageHashMap[userID]
                    file.exists() -> {
                        val options = BitmapFactory.Options()
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888
                        profilePic = BitmapDrawable(resources,
                                BitmapFactory.decodeFile(file.absolutePath, options))
                    }
                    else -> {
                        val url = "$API_URL/apps/pic/" + URLEncoder.encode(userID, "UTF-8")
                                .replace("+", "_") + ".png"
                        profilePic = Utils.drawableFromUrl(this, url)
                        val fileOutputStream = FileOutputStream(file)
                        Utils.drawableToBitmap(profilePic)!!
                                .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                logs.error("setUpNavView", e.toString() + "\n" + e.message)
            }

            runOnUiThread {
                try {
                    val color = Palette.from(
                            Utils.drawableToBitmap(if (Utils.userImageHashMap
                                            .containsKey(userID!! + "_bg"))
                                Utils
                                        .userImageHashMap[userID + "_bg"]
                            else
                                resources
                                        .getDrawable(R.drawable.demo))!!)
                            .generate()
                            .getDominantColor(ThemeStore.getInstance(this)!!.primaryColor)
                    if (Utils.isColorBright(color)) {
                        name.setTextColor(Color.BLACK)
                        email.setTextColor(Color.BLACK)
                    } else {
                        name.setTextColor(Color.WHITE)
                        email.setTextColor(Color.WHITE)
                    }
                    if (Utils.userImageHashMap.containsKey(userID + "_bg"))
                        bg.background = Utils.userImageHashMap[userID + "_bg"]
                    img.setImageDrawable(profilePic)
                    img.setOnClickListener {
                        val intent = Intent(this@MainActivity, UserProfile::class.java)
                        intent.putExtra("uid", userID)
                        val icon = Pair.create<View, String>(img, "profilePic")
                        val nameTransition = Pair.create<View, String>(name, "name")
                        val emailTransition = Pair.create<View, String>(email, "email")
                        val options = ActivityOptions
                                .makeSceneTransitionAnimation(this, icon, nameTransition,
                                        emailTransition)
                        startActivity(intent, options.toBundle())
                        drawer!!.closeDrawers()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    logs.error("setUpNavView - runOnUiThread",
                            e.toString() + "\n" + e.message)
                    Utils.sleep(100)
                    setUpNavView()
                }
            }
        }.start()
    }

    private fun setAccounts() {
        val db = SQLiteHandler(applicationContext)
        val user = db.userDetails
        val userID = user["uid"]
        val users = ArrayList<UserAdapter.User>()
        val am = AccountManager.get(this)
        val userAdapter = UserAdapter(this, users, object : UserAdapter.OnClick {
            override fun onClick(uid: String): View.OnClickListener {
                return View.OnClickListener {
                    var u: UserAdapter.User? = null
                    for (us in users)
                        if (us.uid == uid)
                            u = us
                    if (u != null) {
                        val session = SessionManager(applicationContext)
                        session.setLogin(true)
                        db.deleteUsers()
                        db.addUser(u.name, u.email, uid, u.createdAt)
                        startActivity(Intent(this@MainActivity, Splashscreen::class.java))
                        drawer!!.closeDrawers()
                        finish()
                    }
                }
            }
        })
        Thread {
            for (account in am.accounts) {
                try {
                    val uid = am.getUserData(account, "uid")
                    if (uid != userID)
                        users.add(UserAdapter.User(uid, am.getUserData(account, "name"),
                                account.name,
                                am.getUserData(account, "created_at"),
                                getUserImage(uid)))
                } catch (e: Exception) {
                    logs.error("setAccounts", e.toString())
                }

            }
            runOnUiThread { userAdapter.notifyDataSetChanged() }
        }.start()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        layoutManager.reverseLayout = true
        val userRecyclerView = findViewById<RecyclerView>(R.id.rv_users)
        userRecyclerView.layoutManager = layoutManager
        userRecyclerView.adapter = userAdapter
    }

    private fun getUserImage(uid: String): Drawable {
        var profilePic: Drawable
        try {
            val url = "$API_URL/apps/pic/" + URLEncoder.encode(uid, "UTF-8")
                    .replace("+", "_") + ".png"
            val imgFile = File(filesDir, "$uid.png")
            if (!imgFile.exists()) {
                if (Config.SERVER_ONLINE) {
                    profilePic = Utils.drawableFromUrl(this, url)
                    val fileOutputStream = FileOutputStream(imgFile)
                    Utils.drawableToBitmap(profilePic)!!
                            .compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

                } else
                    profilePic = resources.getDrawable(R.mipmap.ic_launcher, null)
            } else {
                val options = BitmapFactory.Options()
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                profilePic = BitmapDrawable(resources,
                        BitmapFactory.decodeFile(imgFile.absolutePath, options))
            }
        } catch (e: Exception) {
            profilePic = resources.getDrawable(R.mipmap.ic_launcher, null)
        }

        return profilePic
    }

    private fun search(query: String) {
        id = random!!.nextInt()
        val thread = Thread {
            try {

                val localId = id
                appItems.clear()

                if (JSONObject(LocalJSON.getJSON(this@MainActivity)).getBoolean("error"))
                    LocalJSON.setJSON(this@MainActivity, Utils.getWebContent(
                            API_URL + "/apps/list.php?user=" + Config.UID(this))!!)

                val jsonObject = JSONObject(LocalJSON.getJSON(this@MainActivity))

                val array = jsonObject.getJSONArray("apps")

                val appItemList = ArrayList<SearchItem>()

                for (i in 0 until array.length() - 1) {
                    val obj = array.getJSONObject(i)
                    if (obj.getString("title").toLowerCase().contains(query.toLowerCase()) && query != "")
                        appItemList.add(SearchItem(obj.getString("title"), obj.getString("ID"),
                                Utils.drawableFromUrl(this@MainActivity, obj.getString("image"))))
                }

                if (id == localId)
                    appItems.addAll(appItemList)

                this@MainActivity.runOnUiThread { searchAdapter!!.notifyDataSetChanged() }

            } catch (ignored: Exception) {
            }
        }
        if (thread.isAlive)
            thread.interrupt()
        thread.start()
    }

    private fun addFragment(fragment: TabFragment) {
        adapter!!.addFragment(fragment, fragment.getName(this))
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val searchView = findViewById<View>(R.id.searchLayout)
        when {
            drawer.isDrawerOpen(GravityCompat.START) -> drawer.closeDrawer(GravityCompat.START)
            searchView.visibility == View.VISIBLE -> {
                val content = findViewById<View>(R.id.content)
                content.visibility = View.VISIBLE
                changeOpacity(searchView, true)
                tabLayout!!.visibility = View.VISIBLE
            }
            fragmentAbout!!.bottomSheetBehavior.state == STATE_EXPANDED -> fragmentAbout!!.bottomSheetBehavior.setState(STATE_HIDDEN)
            fragmentAbout!!.bottomSheetBehaviorTranslators.state == STATE_EXPANDED -> fragmentAbout!!.bottomSheetBehaviorTranslators.setState(STATE_HIDDEN)
            else -> super.onBackPressed()
        }
    }

    private fun changeOpacity(view: View, hide: Boolean) {
        val from = if (hide) 1f else 0f
        val to = if (hide) 0f else 1f
        val animation1 = AlphaAnimation(from, to)
        animation1.duration = 300
        animation1.fillAfter = true
        view.startAnimation(animation1)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.nav_home -> scrollTo(0)
            R.id.nav_myapps -> scrollTo(1)
            R.id.nav_about -> scrollTo(2)
            R.id.nav_modules -> startActivity(this, ModulesScreen::class.java)
            R.id.nav_settings -> Utils.startActivityAsync(this, SettingsScreen::class.java).setTime(250)
                    .start(object : Async {
                        override fun run(activity: Activity, aClass: Class<*>, options: Bundle?) {
                            return startActivity(this@MainActivity, aClass, options)
                        }
                    })
            R.id.nav_logout -> startActivity(this, LogOut::class.java)
        }

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun scrollTo(index: Int) {
        try {
            viewPager!!.setCurrentItem(index, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSearchStateChanged(enabled: Boolean) {
        if (enabled && !Config.SERVER_ONLINE) searchBar!!.disableSearch()
        if (!enabled) {
            val searchLayout = findViewById<View>(R.id.searchLayout)
            val content = findViewById<View>(R.id.content)
            changeOpacity(searchLayout, true)
            tabLayout!!.visibility = View.VISIBLE
            content.visibility = View.VISIBLE
            searchBar!!.clearSuggestions()
        }
    }

    override fun onSearchConfirmed(text: CharSequence) {
        submit(text)
    }

    private fun submit(text: CharSequence) {
        search(text.toString())
        val searchLayout = findViewById<View>(R.id.searchLayout)
        val content = findViewById<View>(R.id.content)
        changeOpacity(searchLayout, false)
        tabLayout!!.visibility = View.GONE
        content.visibility = View.INVISIBLE
    }

    @SuppressLint("WrongConstant")
    override fun onButtonClicked(buttonCode: Int) {
        when (buttonCode) {
            MaterialSearchBar.BUTTON_NAVIGATION -> drawer!!.openDrawer(Gravity.START)
            MaterialSearchBar.BUTTON_SPEECH -> openVoiceRecognizer()
            MaterialSearchBar.BUTTON_BACK -> searchBar!!.disableSearch()
        }
    }

    private fun openVoiceRecognizer() {
        if (!searchBar!!.isFocused)
            searchBar!!.enableSearch()
        val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, getString(R.string.language_model))
        startActivityForResult(intent, 99)
        searchBar!!.text = ""
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            99 -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    searchBar!!.text = text[0]
                    submit(text[0])
                }
            }
        }
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
        private val mFragmentList = ArrayList<Fragment>()
        private val mFragmentTitleList = ArrayList<String>()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return mFragmentTitleList[position]
        }
    }

    companion object {

        var color = Color.BLACK
        var settingsChanged = false
    }
}
