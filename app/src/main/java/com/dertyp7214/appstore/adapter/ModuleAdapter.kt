/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.adapter.ModuleAdapter.ViewHolder
import com.dertyp7214.appstore.items.ModuleItem

class ModuleAdapter(private val appItemList: List<ModuleItem>) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.module_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = appItemList[position]

        holder.appTitle.text = item.title
        holder.appIcon.setImageDrawable(item.icon)
        holder.appPackage.text = item.packageName

        holder.view.setOnClickListener {
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var appIcon: ImageView = itemView.findViewById(R.id.img_icon)
        var appTitle: TextView = itemView.findViewById(R.id.txt_label)
        var appPackage: TextView = itemView.findViewById(R.id.txt_pkg)
        var view: View = itemView.findViewById(R.id.view)
    }

    override fun getItemCount(): Int {
        return appItemList.size
    }
}
