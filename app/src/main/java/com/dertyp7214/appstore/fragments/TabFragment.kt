/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.fragments

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dertyp7214.appstore.Config
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL

open class TabFragment : Fragment() {

    val navigationBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

    open fun getName(context: Context): String {
        return ""
    }

    fun serverOnline(): Boolean {
        return try {
            val url = URL(Config.API_URL)
            val sockaddr = InetSocketAddress(InetAddress.getByName(url.host), 80)
            val sock = Socket()
            val timeoutMs = 2000
            sock.connect(sockaddr, timeoutMs)
            true
        } catch (ignored: IOException) {
            false
        }
    }

    fun haveConnection(): Boolean {
        return try {
            val url = URL("http://www.google.de")
            val sockaddr = InetSocketAddress(InetAddress.getByName(url.host), 80)
            val sock = Socket()
            val timeoutMs = 2000
            sock.connect(sockaddr, timeoutMs)
            true
        } catch (ignored: IOException) {
            false
        }
    }

    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        var l = l
        var t = t
        var r = r
        var b = b
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = v.layoutParams as ViewGroup.MarginLayoutParams
            if (l == -1) l = p.leftMargin
            if (t == -1) t = p.topMargin
            if (r == -1) r = p.rightMargin
            if (b == -1) b = p.bottomMargin
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }
}
