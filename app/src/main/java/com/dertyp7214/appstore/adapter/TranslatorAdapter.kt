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

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R

class TranslatorAdapter(private val context: Context, private val translators: List<Translator>) : RecyclerView.Adapter<TranslatorAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslatorAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.translator_profile, parent, false))
    }

    override fun onBindViewHolder(holder: TranslatorAdapter.ViewHolder, position: Int) {
        val item = translators[position]

        holder.userName.text = item.name
        holder.userPicture.setImageDrawable(item.profilePicture)

        holder.view.setOnClickListener { openGitHubProfile(item.userName) }
    }

    private fun openGitHubProfile(userName: String) {
        openUrl("https://github.com/$userName")
    }

    private fun openUrl(url: String) {
        val gitIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(gitIntent)
    }

    override fun getItemCount(): Int {
        return translators.size
    }

    class Translator(val userName: String, val name: String, val profilePicture: Drawable)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var userPicture: ImageView = itemView.findViewById(R.id.profilePicture)
        var userName: TextView = itemView.findViewById(R.id.userName)
        var view: View = itemView.findViewById(R.id.view)
    }
}
