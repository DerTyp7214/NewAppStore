/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.components.StartSnapHelper
import com.dertyp7214.appstore.items.AppGroupItem
import com.dertyp7214.appstore.items.NoConnection

class AppGroupAdapter(private val context: Activity, private val appGroupItemList: List<AppGroupItem>) : RecyclerView.Adapter<AppGroupAdapter.ViewHolderNoConnection>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderNoConnection {
        return when (viewType) {
            0 -> ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.app_group, parent, false))
            else -> ViewHolderNoConnection(LayoutInflater.from(parent.context)
                    .inflate(R.layout.no_connection, parent, false))
        }
    }

    override fun onBindViewHolder(holder: ViewHolderNoConnection, position: Int) {
        try {
            when (holder.itemViewType) {
                0 -> {
                    val viewHolder = holder as ViewHolder
                    val appGroupItem = appGroupItemList[position]
                    val appAdapter = AppAdapter(context, appGroupItem.appList)
                    viewHolder.recyclerView.adapter = appAdapter
                    viewHolder.title.text = appGroupItem.title
                }
                1 -> {
                    val item = appGroupItemList[position] as NoConnection
                    holder.title.text = item.title
                }
            }
        } catch (e: Exception) {
            Log.d("AppGroupAdapter", e.message)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (appGroupItemList[position] is NoConnection) 1 else 0
    }

    override fun getItemCount(): Int {
        return appGroupItemList.size
    }

    internal inner class ViewHolder(itemView: View) : ViewHolderNoConnection(itemView) {

        var recyclerView: RecyclerView
        override lateinit var title: TextView

        init {
            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.HORIZONTAL

            recyclerView = itemView.findViewById(R.id.app_list_vertical)
            recyclerView.layoutManager = layoutManager

            val startSnapHelper = StartSnapHelper()
            startSnapHelper.attachToRecyclerView(recyclerView)

            title = itemView.findViewById(R.id.title)
        }
    }

    open inner class ViewHolderNoConnection(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open var title: TextView = itemView.findViewById(R.id.title)
    }
}
