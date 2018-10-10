/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dertyp7214.appstore.Utils;
import com.dertyp7214.themeablecomponents.components.ThemeableToolbar;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nullable;

import androidx.annotation.ColorInt;

public class CustomToolbar extends ThemeableToolbar {

    public CustomToolbar(Context context) {
        super(context);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setToolbarIconColor(@ColorInt int toolbarColor){
        int tintColor = Utils.isColorBright(toolbarColor) ? Color.BLACK : Color.WHITE;
        for(ImageView imageButton : findChildrenByClass(ImageView.class, this)) {
            Drawable drawable = imageButton.getDrawable();
            drawable.setColorFilter(tintColor, PorterDuff.Mode.SRC_ATOP);
            imageButton.setImageDrawable(drawable);
        }
        for (TextView textView : findChildrenByClass(TextView.class, this)) {
            textView.setTextColor(tintColor);
            textView.setHintTextColor(tintColor);
        }
    }

    private <V extends View> Collection<V> findChildrenByClass(Class<V> clazz, ViewGroup... viewGroups) {
        Collection<V> collection = new ArrayList<>();
        for(ViewGroup viewGroup : viewGroups)
            collection.addAll(gatherChildrenByClass(viewGroup, clazz, new ArrayList<>()));
        return collection;
    }

    private <V extends View> Collection<V> gatherChildrenByClass(ViewGroup viewGroup, Class<V> clazz, Collection<V> childrenFound) {

        for (int i = 0; i < viewGroup.getChildCount(); i++)
        {
            final View child = viewGroup.getChildAt(i);
            if (clazz.isAssignableFrom(child.getClass())) {
                childrenFound.add((V)child);
            }
            if (child instanceof ViewGroup) {
                gatherChildrenByClass((ViewGroup) child, clazz, childrenFound);
            }
        }

        return childrenFound;
    }
}
