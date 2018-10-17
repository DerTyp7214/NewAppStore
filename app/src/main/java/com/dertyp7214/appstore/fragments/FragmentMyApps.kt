/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.adapter.MyAppsAdapter
import com.dertyp7214.appstore.components.DividerItemDecorator
import com.dertyp7214.appstore.items.MyAppItem
import org.json.JSONObject
import java.util.*

class FragmentMyApps : TabFragment() {

    private var refreshLayout: SwipeRefreshLayout? = null
    private var activity: Activity? = null
    private val myAppList = ArrayList<MyAppItem>()
    private var recyclerView: RecyclerView? = null
    private var adapter: MyAppsAdapter? = null
    private var themeStore: ThemeStore? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_myapps, container, false)

        instance = this

        activity = getActivity()

        themeStore = ThemeStore.getInstance(activity!!)

        adapter = MyAppsAdapter(this, myAppList)

        recyclerView = view.findViewById(R.id.rv_my_apps)

        val layoutManager = LinearLayoutManager(activity)
        val dividerItemDecoration = DividerItemDecorator(ContextCompat.getDrawable(activity!!, R.drawable.divider)!!)
        recyclerView!!.addItemDecoration(dividerItemDecoration)
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.adapter = adapter

        view.findViewById<CardView>(R.id.card)
        getMyApps()

        refreshLayout = view.findViewById(R.id.refresh)
        refreshLayout!!.setColorSchemeColors(themeStore!!.primaryColor,
                themeStore!!.getPrimaryHue(100),
                themeStore!!.getPrimaryHue(200),
                themeStore!!.getPrimaryHue(300))
        refreshLayout!!.setDistanceToTriggerSync(80)
        refreshLayout!!.setSize(SwipeRefreshLayout.DEFAULT)
        refreshLayout!!.setOnRefreshListener {
            Timer().schedule(object : TimerTask() {
                override fun run() {
                    if (FragmentAppGroups.hasInstance()) {
                        val appGroups = FragmentAppGroups.instance
                        appGroups!!.getAppList(refreshLayout, true)
                    }
                }
            }, 1000)
        }

        return view
    }

    @JvmOverloads
    public fun getMyApps(id: Int = -1) {
        Thread {
            try {
                myAppList.clear()
                val myApps = Utils.getWebContent(
                        Config.API_URL + "/apps/myapps.php?uid=" + Config.UID(activity!!))
                val `object` = JSONObject(myApps)
                val array = `object`.getJSONArray("apps")
                for (i in 0 until array.length()) {
                    val app = array.getJSONObject(i)
                    if (app.getString("id") != "null")
                        myAppList.add(MyAppItem(app.getString("title"),
                                app.getString("size"),
                                app.getString("id"),
                                Utils.drawableFromUrl(activity!!, app.getString("img"))))
                }
                activity!!.runOnUiThread {
                    if (id >= 0)
                        adapter!!.notifyItemRemoved(id)
                    else
                        adapter!!.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun getName(context: Context): String {
        return context.getString(R.string.my_apps)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        var instance: FragmentMyApps? = null
            private set

        fun hasInstance(): Boolean {
            return instance != null
        }

        @JvmStatic
        internal fun getInstance(): FragmentMyApps {
            return instance!!
        }
    }
}
