/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.appstore.*
import com.dertyp7214.appstore.Config.API_URL
import com.dertyp7214.appstore.Utils.Companion.drawableFromUrl
import com.dertyp7214.appstore.Utils.Companion.getSettings
import com.dertyp7214.appstore.Utils.Companion.getWebContent
import com.dertyp7214.appstore.adapter.AppGroupAdapter
import com.dertyp7214.appstore.items.AppGroupItem
import com.dertyp7214.appstore.items.NoConnection
import com.dertyp7214.appstore.items.SearchItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

@SuppressLint("ValidFragment")
class FragmentAppGroups : TabFragment() {
    lateinit var refreshLayout: SwipeRefreshLayout
    private var recyclerViewAppGroup: RecyclerView? = null
    private var adapter: AppGroupAdapter? = null
    private var context: Activity? = null
    private val appList = ArrayList<AppGroupItem>()
    private var UID: String? = null
    private var version: String? = null
    private var t: Thread? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_app_groups, container, false)

        context = activity
        UID = Config.UID(context!!)

        val themeStore = ThemeStore.getInstance(context!!)
        adapter = AppGroupAdapter(context!!, appList)

        recyclerViewAppGroup = view.findViewById(R.id.recyclerViewAppGroup)
        recyclerViewAppGroup!!.layoutManager = LinearLayoutManager(context)
        recyclerViewAppGroup!!.adapter = adapter

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout.setColorSchemeColors(themeStore!!.primaryColor,
                themeStore.getPrimaryHue(100),
                themeStore.getPrimaryHue(200),
                themeStore.getPrimaryHue(300))
        refreshLayout.setDistanceToTriggerSync(80)
        refreshLayout.setSize(SwipeRefreshLayout.DEFAULT)
        refreshLayout.setOnRefreshListener {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    getAppList(refreshLayout, true)
                }
            }, 1000)
        }

        refreshLayout.isRefreshing = true
        getAppList(refreshLayout, false)

        instance = this

        return view
    }

    fun getAppList(layout: SwipeRefreshLayout?, refresh: Boolean) {
        if (t != null)
            t!!.interrupt()
        t = Thread {
            if (haveConnection()) {
                if (serverOnline()) {
                    try {

                        appList.clear()

                        if (JSONObject(LocalJSON.getJSON(context!!)).getBoolean("error")
                                || refresh
                                || !getSettings(context!!).getString("last_refresh", "000000")!!
                                        .contentEquals(DateFormat.format("yyyyMMdd", Date()))) {
                            getSettings(context!!).edit().putString("last_refresh",
                                    DateFormat.format("yyyyMMdd", Date()).toString())
                                    .apply()
                            LocalJSON.setJSON(context!!,
                                    getWebContent(
                                            Config.API_URL + "/apps/list.php?user=" + Config
                                                    .UID(context!!))!!)
                        }

                        val jsonObject = JSONObject(LocalJSON.getJSON(context!!))
                        val array = jsonObject.getJSONArray("apps")

                        if (refresh) {
                            if (FragmentMyApps.hasInstance())
                                FragmentMyApps.getInstance().getMyApps()
                            val installedApps = JSONArray()
                            for (i in 0 until array.length() - 1) {
                                if (Utils.appInstalled(context!!,
                                                array.getJSONObject(i).getString("ID"))) {
                                    installedApps.put(array.getJSONObject(i).getString("ID"))
                                }
                            }
                            val url = Config.API_URL + Config.APK_PATH
                                    .replace("{uid}", UID!!)
                                    .replace("{id}", installedApps.toString()
                                            .replace("&", ""))
                            Log.d("FETCH", getWebContent(url))
                        }

                        val appsList = ArrayList<SearchItem>()

                        for (i in 0 until array.length() - 1) {
                            val obj = array.getJSONObject(i)
                            if (Utils.appInstalled(context!!, obj.getString("ID")))
                                appsList.add(
                                        SearchItem(obj.getString("title"), obj.getString("ID"),
                                                drawableFromUrl(context!!,
                                                        obj.getString("image")),
                                                obj.getString("version"),
                                                getUpdate(obj)))
                        }

                        for (item in appsList)
                            Utils.appsList[item.id] = item

                        val updateList = ArrayList<SearchItem>()

                        for (item in appsList) {
                            if (item.version != getLocalVersion(item) && item
                                            .version != "0" && item.isUpdate)
                                updateList.add(item)
                        }

                        if (updateList.size > 0)
                            appList.add(
                                    AppGroupItem(getString(R.string.text_update), updateList))

                        appList.add(AppGroupItem(getString(R.string.text_installed_apps),
                                appsList))

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    context!!.runOnUiThread {
                        adapter!!.notifyDataSetChanged()
                        recyclerViewAppGroup!!.scrollBy(1, 1)
                        recyclerViewAppGroup!!.scrollBy(-1, -1)
                        if (layout != null)
                            layout.isRefreshing = false
                    }
                } else {
                    appList.clear()
                    appList.add(NoConnection(getString(R.string.server_offline)))
                    context!!.runOnUiThread {
                        adapter!!.notifyDataSetChanged()
                        if (layout != null)
                            layout.isRefreshing = false
                    }
                }
            } else {
                appList.clear()
                appList.add(NoConnection(getString(R.string.no_connection)))
                context!!.runOnUiThread {
                    adapter!!.notifyDataSetChanged()
                    if (layout != null)
                        layout.isRefreshing = false
                }
            }
        }
        t!!.start()
    }

    private fun getUpdate(`object`: JSONObject): Boolean {
        try {
            return `object`.getBoolean("update")
        } catch (e: JSONException) {
            e.printStackTrace()
            return true
        }

    }

    private fun getLocalVersion(item: SearchItem): String {
        try {
            val pinfo = context!!.packageManager.getPackageInfo(item.id, 0)
            return pinfo.versionName
        } catch (e: Exception) {
            return getServerVersion(item)
        }

    }

    private fun getServerVersion(item: SearchItem): String {
        if (version == null)
            version = getWebContent(API_URL + "/apps/list.php?version=" + item.id)
        return version!!
    }

    override fun getName(context: Context): String {
        return context.getString(R.string.home)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        var instance: FragmentAppGroups? = null
            private set

        fun hasInstance(): Boolean {
            return instance != null
        }

        internal fun getInstance(): FragmentAppGroups {
            return instance!!
        }
    }
}
