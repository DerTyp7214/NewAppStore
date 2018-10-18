/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

@file:Suppress("DEPRECATION")

package com.dertyp7214.appstore.fragments

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import androidx.cardview.widget.CardView
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.dev.Logs

@Suppress("DEPRECATION")
class FragmentUserCard : Fragment() {

    private var relativeLayout: RelativeLayout? = null
    private var container: ViewGroup? = null
    private var inflater: LayoutInflater? = null
    private var cardView: CardView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_fragment_user_card, container, false)

        this.inflater = inflater
        this.container = container

        cardView = view.findViewById(R.id.card)
        relativeLayout = view.findViewById(R.id.content)

        return view
    }

    fun setContentView(@LayoutRes layout: Int, onAttachListener: OnAttachListener) {
        try {
            val view = inflater!!.inflate(layout, container, false)
            onAttachListener.onAttach(view)
            relativeLayout!!.addView(view)
        } catch (e: Exception) {
            Logs.getInstance(activity).error("setContentView", e.toString())
        }
    }

    fun setBorderRadius(i: Float) {
        if (cardView != null)
            cardView!!.radius = i
    }

    interface OnAttachListener {
        fun onAttach(view: View)
    }
}
