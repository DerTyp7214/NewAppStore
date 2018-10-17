/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.dertyp7214.appstore.Config
import com.dertyp7214.appstore.R
import com.dertyp7214.appstore.ThemeStore
import com.dertyp7214.appstore.Utils
import com.dertyp7214.appstore.Utils.Companion.manipulateColor
import com.dertyp7214.appstore.items.SearchItem
import java.util.*

@Suppress("NAME_SHADOWING")
class FragmentChangeLogs : Fragment() {

    private var changes: TextView? = null
    private var themeStore: ThemeStore? = null
    private var activity: Activity? = null
    private var changeLog: String? = null
    private var version: String? = null
    internal var view: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_change_logs, container, false)

        activity = getActivity()

        themeStore = ThemeStore.getInstance(getActivity()!!)

        changes = view!!.findViewById(R.id.txt_change)

        setColor(themeStore!!.primaryColor)

        return view
    }

    fun setColor(@ColorInt color: Int) {
        if (changes!!.visibility == View.VISIBLE) {
            changes!!.setTextColor(manipulateColor(color, 0.6f))
            view!!.setBackgroundColor(ColorUtils.setAlphaComponent(color, 0x37))
        }
    }

    fun getChangeLogs(searchItem: SearchItem, callback: Callback) {
        if (changeLog == null)
            changeLog = Utils.getWebContent(Config.API_URL + "/apps/list.php?changes=" + searchItem.id)
        if (version == null)
            version = Utils.getWebContent(Config.API_URL + "/apps/list.php?version=" + searchItem.id)
        callback.run(changes, setText(changeLog, version!!, false))
    }

    private fun setText(text: String?, version: String, big: Boolean): Spanned? {
        var text = text
        if (text == null || text.isEmpty() || Objects.requireNonNull(text).startsWith("{")) {
            return null
        }
        if (!big)
            text = text.split("</new>".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0] + "</body></html>"
        text = "<br/>" + text.replace("%ver%", version)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(text)
        }
    }

    interface Callback {
        fun run(textView: TextView?, text: Spanned?)
    }
}
