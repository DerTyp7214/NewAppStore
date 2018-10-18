/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.Utils.Companion.getWebContent
import com.dertyp7214.appstore.items.SearchItem

class FragmentAppInfo : Fragment() {

    private var version: TextView? = null
    private var size: TextView? = null
    private var activity: Activity? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        activity = getActivity()

        version = view.findViewById(R.id.txt_verison)
        size = view.findViewById(R.id.txt_size)

        return view
    }

    fun getAppInfo(searchItem: SearchItem) {
        Thread {
            val ver = getWebContent(Config.API_URL + "/apps/list.php?version=" + searchItem.id)
            val si = getWebContent(Config.API_URL + "/apps/list.php?size=" + searchItem.id)
            activity!!.runOnUiThread { setText(arrayOf(version!!, size!!), arrayOf(ver!!, si!!)) }
        }.start()
    }

    private fun setText(textView: Array<TextView>, text: Array<String>) {
        if (textView.size != text.size)
            return
        for (i in textView.indices)
            textView[i].text = text[i]
    }
}
