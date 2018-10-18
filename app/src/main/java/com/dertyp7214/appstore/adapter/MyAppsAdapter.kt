/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.fragments.FragmentMyApps
import com.dertyp7214.appstore.items.MyAppItem
import com.dertyp7214.appstore.items.SearchItem
import com.dertyp7214.appstore.screens.AppScreen
import java.util.*

class MyAppsAdapter(private val context: Fragment, private val appItemList: List<MyAppItem>) : RecyclerView.Adapter<MyAppsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAppsAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.app_small, parent, false))
    }

    override fun onBindViewHolder(holder: MyAppsAdapter.ViewHolder, position: Int) {
        val item = appItemList[position]
        val appInstalled = Utils.applicationInstalled(Objects.requireNonNull<FragmentActivity>(context.activity),
                item.packageName)

        holder.appSize.text = item.appSize
        holder.appTitle.text = item.appTitle
        holder.appIcon.setImageDrawable(item.appIcon)

        holder.clear.setOnClickListener {
            Thread {
                Utils.removeMyApp(item.packageName, context.activity!!)
                if (context is FragmentMyApps)
                    context.getMyApps(position)
            }.start()
        }

        if (!Utils.appsList.containsKey(item.packageName))
            Utils.appsList[item.packageName] = SearchItem(item.appTitle, item.packageName, item.appIcon)

        holder.view.setOnClickListener {
            val icon = Pair.create<View, String>(holder.appIcon, "icon")
            val options = ActivityOptions.makeSceneTransitionAnimation(context.activity, icon)
            context.startActivity(Intent(context.activity, AppScreen::class.java)
                    .putExtra("id", item.packageName), options.toBundle())
        }

        holder.openInstall.text = if (appInstalled)
            context
                    .getString(R.string.text_open)
        else
            context.getString(R.string.text_install)
        holder.openInstall.setOnClickListener {
            if (appInstalled)
                context.startActivity(context.activity!!.packageManager
                        .getLaunchIntentForPackage(item.packageName))
            else
                AppScreen.downloadApp(context.activity!!, item.appTitle,
                        item.packageName, holder.openInstall)
        }

        holder.play.setOnClickListener {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri
                        .parse("market://details?id=" + item.packageName)))
            } catch (e: android.content.ActivityNotFoundException) {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(
                        "https://play.google.com/store/apps/details?id=" + item
                                .packageName)))
            }
        }

        val color = ThemeStore.getInstance(context.activity!!)!!.accentColor
        val bg = context.resources.getDrawable(R.drawable.button_border) as GradientDrawable
        bg.setStroke(3, color)
        holder.openInstall.setBackgroundDrawable(bg)
        holder.openInstall.setTextColor(color)

        if (Utils.verifyInstallerId(Objects.requireNonNull<FragmentActivity>(context.activity),
                        item.packageName)) {
            holder.play.visibility = View.VISIBLE
            holder.openInstall.visibility = View.GONE
        } else {
            holder.play.visibility = View.GONE
            holder.openInstall.visibility = View.VISIBLE
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appTitle: TextView = itemView.findViewById(R.id.title)
        var appSize: TextView = itemView.findViewById(R.id.size)
        var view: View = itemView.findViewById(R.id.view)
        var clear: ImageButton = itemView.findViewById(R.id.btn_clear)
        var openInstall: Button = itemView.findViewById(R.id.btn_openInstall)
        var play: ImageView = itemView.findViewById(R.id.img_play)
        var appIcon: ImageView = itemView.findViewById(R.id.img)
    }

    override fun getItemCount(): Int {
        return appItemList.size
    }
}
