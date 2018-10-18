/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.uiTools

import android.graphics.Rect
import android.graphics.RectF
import android.view.animation.Animation
import android.view.animation.Transformation

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class ClipRectAnimation : Animation {
    private val mFromRect = RectF()
    private val mToRect = RectF()

    private val mResolvedFrom = Rect()
    private val mResolvedTo = Rect()
    private val mSetClipRect: Method? = null

    constructor(fromClip: RectF?, toClip: RectF?) {
        if (fromClip == null || toClip == null) {
            throw RuntimeException("Expected non-null animation clip rects")
        }
        mFromRect.set(fromClip)
        mToRect.set(toClip)
    }

    constructor(fromL: Float, fromT: Float, fromR: Float, fromB: Float,
                toL: Float, toT: Float, toR: Float, toB: Float) {
        mFromRect.set(fromL, fromT, fromR, fromB)
        mToRect.set(toL, toT, toR, toB)
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        mResolvedFrom.left = resolveSize(Animation.RELATIVE_TO_SELF, mFromRect.left, width, parentWidth).toInt()
        mResolvedFrom.top = resolveSize(Animation.RELATIVE_TO_SELF, mFromRect.top, height, parentHeight).toInt()
        mResolvedFrom.right = resolveSize(Animation.RELATIVE_TO_SELF, mFromRect.right, width, parentWidth).toInt()
        mResolvedFrom.bottom = resolveSize(Animation.RELATIVE_TO_SELF, mFromRect.bottom, height, parentHeight).toInt()
        mResolvedTo.left = resolveSize(Animation.RELATIVE_TO_SELF, mToRect.left, width, parentWidth).toInt()
        mResolvedTo.top = resolveSize(Animation.RELATIVE_TO_SELF, mToRect.top, height, parentHeight).toInt()
        mResolvedTo.right = resolveSize(Animation.RELATIVE_TO_SELF, mToRect.right, width, parentWidth).toInt()
        mResolvedTo.bottom = resolveSize(Animation.RELATIVE_TO_SELF, mToRect.bottom, height, parentHeight).toInt()
    }

    override fun applyTransformation(it: Float, tr: Transformation) {
        val l = mResolvedFrom.left + ((mResolvedTo.left - mResolvedFrom.left) * it).toInt()
        val t = mResolvedFrom.top + ((mResolvedTo.top - mResolvedFrom.top) * it).toInt()
        val r = mResolvedFrom.right + ((mResolvedTo.right - mResolvedFrom.right) * it).toInt()
        val b = mResolvedFrom.bottom + ((mResolvedTo.bottom - mResolvedFrom.bottom) * it).toInt()

        try {
            mSetClipRect!!.invoke(tr, l, t, r, b)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    override fun willChangeTransformationMatrix(): Boolean {
        return false
    }
}