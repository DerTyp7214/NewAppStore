/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.items.SearchItem
import com.dertyp7214.appstore.screens.AppScreen

class AppAdapter(private val context: Activity, private val appItemList: List<SearchItem>) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_item_vertical, parent, false))
    }

    override fun onBindViewHolder(holder: AppAdapter.ViewHolder, position: Int) {
        val item = appItemList[position]

        holder.appTitle.text = item.appTitle
        holder.appIcon.setImageDrawable(item.appIcon)

        for (searchItem in appItemList)
            if (!Utils.appsList.containsKey(searchItem.id))
                Utils.appsList[searchItem.id] = searchItem

        holder.view.setOnClickListener {
            val icon = Pair.create<View, String>(holder.appIcon, "icon")
            val options = ActivityOptions.makeSceneTransitionAnimation(context, icon)
            context.startActivity(Intent(context, AppScreen::class.java).putExtra("id", item.id), options.toBundle())
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        var appTitle: TextView = itemView.findViewById(R.id.appTitle)
        var view: View = itemView.findViewById(R.id.view)
    }

    override fun getItemCount(): Int {
        return appItemList.size
    }
}
