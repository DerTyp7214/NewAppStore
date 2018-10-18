/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R

class UserAdapter(private val context: Activity, private val users: List<User>, private val click: OnClick) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.user_view, parent, false))
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val item = users[position]

        holder.img.setImageDrawable(item.profileImage)
        holder.img.setOnClickListener(click.onClick(item.uid))
    }

    interface OnClick {
        fun onClick(uid: String): View.OnClickListener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var img: ImageView = itemView.findViewById(R.id.user_img)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    class User(val uid: String, val name: String, val email: String, val createdAt: String, val profileImage: Drawable)
}
