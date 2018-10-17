/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import com.dertyp7214.appstore.Utils
import com.dertyp7214.themeablecomponents.components.ThemeableToolbar
import java.util.*

@Suppress("UNCHECKED_CAST")
class CustomToolbar : ThemeableToolbar {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setToolbarIconColor(@ColorInt toolbarColor: Int) {
        val tintColor = if (Utils.isColorBright(toolbarColor)) Color.BLACK else Color.WHITE
        for (imageButton in findChildrenByClass(ImageView::class.java, this)) {
            val drawable = imageButton.drawable
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP)
            imageButton.setImageDrawable(drawable)
        }
        for (textView in findChildrenByClass(TextView::class.java, this)) {
            textView.setTextColor(tintColor)
            textView.setHintTextColor(tintColor)
        }
    }

    private fun <V : View> findChildrenByClass(clazz: Class<V>, vararg viewGroups: ViewGroup): Collection<V> {
        val collection = ArrayList<V>()
        for (viewGroup in viewGroups)
            collection.addAll(gatherChildrenByClass(viewGroup, clazz, ArrayList()))
        return collection
    }

    private fun <V : View> gatherChildrenByClass(viewGroup: ViewGroup, clazz: Class<V>, childrenFound: MutableCollection<V>): Collection<V> {

        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (clazz.isAssignableFrom(child.javaClass)) {
                childrenFound.add(child as V)
            }
            if (child is ViewGroup) {
                gatherChildrenByClass(child, clazz, childrenFound)
            }
        }

        return childrenFound
    }
}
