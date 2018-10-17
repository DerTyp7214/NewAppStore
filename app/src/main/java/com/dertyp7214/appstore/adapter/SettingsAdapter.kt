/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.adapter

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.settings.*

class SettingsAdapter(private val itemList: List<Settings>, private val context: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    open inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        open var title: TextView = view.findViewById(R.id.text)
        var subTitle: TextView? = view.findViewById(R.id.subTitle)
        open var box: View = view.findViewById(R.id.box)
        internal var imageRight: ProgressBar? = view.findViewById(R.id.progressBar)
    }

    inner class ViewHolderCheckBox internal constructor(view: View) : ViewHolder(view) {
        override var title: TextView = view.findViewById(R.id.text)
        override var box: View = view.findViewById(R.id.box)
    }

    inner class ViewHolderSwitch internal constructor(view: View) : ViewHolder(view) {
        override var title: TextView = view.findViewById(R.id.text)
        override var box: View = view.findViewById(R.id.box)
    }

    inner class ViewHolderColor internal constructor(view: View) : ViewHolder(view) {
        override var title: TextView = view.findViewById(R.id.text)
        override var box: View = view.findViewById(R.id.box)
        var colorView: View = view.findViewById(R.id.colorViewPlate)
    }

    inner class ViewHolderPlaceHolder internal constructor(view: View) : ViewHolder(view) {
        override var title: TextView = view.findViewById(R.id.text)
        override var box: View = view.findViewById(R.id.box)
    }

    inner class ViewHolderSlider internal constructor(view: View) : ViewHolder(view) {
        override var title: TextView = view.findViewById(R.id.text)
        var progress: TextView = view.findViewById(R.id.txt_prog)
        override var box: View = view.findViewById(R.id.box)
        var seekBar: SeekBar = view.findViewById(R.id.seekBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_normal, parent, false))
            1 -> return ViewHolderCheckBox(LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_checkbox, parent, false))
            2 -> return ViewHolderSwitch(LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_togglebutton, parent, false))
            3 -> return ViewHolderColor(LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_color, parent, false))
            4 -> return ViewHolderPlaceHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_placeholder, parent, false))
            5 -> return ViewHolderSlider(LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_slider, parent, false))
            else -> return ViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.settings_normal, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            0 -> {
                val viewHolder = holder as ViewHolder
                val setting = itemList[position]
                viewHolder.title.text = setting.text
                viewHolder.subTitle!!.text = setting.subTitle
                viewHolder.box.setOnClickListener { setting.onClick(viewHolder.subTitle!!, viewHolder.imageRight!!) }
            }
            1 -> {
                val viewHolderCheckBox = holder as ViewHolderCheckBox
                val settingsCheckBox = itemList[position] as SettingsCheckBox
                val checkBox: CheckBox = viewHolderCheckBox.title as CheckBox
                checkBox.text = settingsCheckBox.text
                checkBox.isChecked = settingsCheckBox.isChecked
                checkBox.setOnCheckedChangeListener { _, isChecked -> settingsCheckBox.isChecked = isChecked }
            }
            2 -> {
                val viewHolderSwitch = holder as ViewHolderSwitch
                val settingsSwitch = itemList[position] as SettingsSwitch
                val aSwitch: Switch = viewHolderSwitch.title as Switch
                aSwitch.text = settingsSwitch.text
                aSwitch.isChecked = settingsSwitch.isChecked
                aSwitch.setOnCheckedChangeListener { _, isChecked -> settingsSwitch.onCheckedChanged(isChecked) }
            }
            3 -> {
                val viewHolderColor = holder as ViewHolderColor
                val settingsColor = itemList[position] as SettingsColor
                viewHolderColor.title.text = settingsColor.text
                val bgDrawable = viewHolderColor.colorView.background as LayerDrawable
                val shape = bgDrawable.findDrawableByLayerId(R.id.plate_color) as GradientDrawable
                shape.setColor(settingsColor.colorInt)
                viewHolderColor.box
                        .setOnClickListener { settingsColor.onClick(viewHolderColor.colorView) }
            }
            4 -> {
                val viewHolderPlaceHolder = holder as ViewHolderPlaceHolder
                val settingsPlaceholder = itemList[position] as SettingsPlaceholder
                viewHolderPlaceHolder.title.text = settingsPlaceholder.text
            }
            5 -> {
                val viewHolderSlider = holder as ViewHolderSlider
                val settingsSlider = itemList[position] as SettingsSlider
                viewHolderSlider.title.text = settingsSlider.text
                viewHolderSlider.seekBar.progress = settingsSlider.progress
                viewHolderSlider.progress.text = viewHolderSlider.seekBar.progress.toString()
                viewHolderSlider.seekBar.setOnSeekBarChangeListener(
                        object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                                settingsSlider.onUpdate(progress)
                                viewHolderSlider.progress.text = progress.toString()
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar) {

                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar) {
                                settingsSlider.saveSetting()
                            }
                        })
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            itemList[position] is SettingsCheckBox -> 1
            itemList[position] is SettingsSwitch -> 2
            itemList[position] is SettingsColor -> 3
            itemList[position] is SettingsPlaceholder -> 4
            itemList[position] is SettingsSlider -> 5
            else -> 0
        }
    }
}