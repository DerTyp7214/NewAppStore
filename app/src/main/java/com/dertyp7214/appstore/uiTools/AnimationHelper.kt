/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.uiTools

import android.annotation.SuppressLint
import android.view.animation.*
import androidx.core.graphics.PathParser

class AnimationHelper private constructor() {

    private val activityOpenEnterAnim: AnimationSet
        get() {
            val anim = AnimationSet(false)
            anim.zAdjustment = Animation.ZORDER_TOP
            val translateAnimation = TranslateAnimation(0f, 0f, 0.04100001f, 0f)
            translateAnimation.duration = 425
            translateAnimation.interpolator = fastOutSlowIn()
            anim.addAnimation(translateAnimation)
            val clipRectAnimation = ClipRectAnimation(0f, 0.959f, 1f, 1f, 0f, 0f, 1f, 1f)
            clipRectAnimation.duration = 425
            clipRectAnimation.interpolator = fastOutExtraSlowIn()
            anim.addAnimation(clipRectAnimation)
            return anim
        }

    private val activityOpenExitAnim: AnimationSet
        get() {
            val anim = AnimationSet(false)
            val translateAnimation = TranslateAnimation(0f, 0f, 0f, -0.019999981f)
            translateAnimation.duration = 425
            translateAnimation.interpolator = fastOutSlowIn()
            anim.addAnimation(translateAnimation)
            val alphaAnimation = AlphaAnimation(1.0f, 0.9f)
            alphaAnimation.duration = 117
            alphaAnimation.interpolator = LinearInterpolator()
            anim.addAnimation(alphaAnimation)
            return anim
        }

    private val activityCloseEnterAnim: AnimationSet
        get() {
            val anim = AnimationSet(false)
            val translateAnimation = TranslateAnimation(0f, 0f, -0.019999981f, 0f)
            translateAnimation.duration = 425
            translateAnimation.interpolator = fastOutSlowIn()
            anim.addAnimation(translateAnimation)
            val alphaAnimation = AlphaAnimation(0.9f, 1.0f)
            alphaAnimation.duration = 425
            alphaAnimation.startOffset = 0
            alphaAnimation.interpolator = activityCloseDim()
            anim.addAnimation(alphaAnimation)
            return anim
        }

    private val activityCloseExitAnim: AnimationSet
        get() {
            val anim = AnimationSet(false)
            val translateAnimation = TranslateAnimation(0f, 0f, 0f, 0.04100001f)
            translateAnimation.duration = 425
            translateAnimation.interpolator = fastOutSlowIn()
            anim.addAnimation(translateAnimation)
            val clipRectAnimation = ClipRectAnimation(0f, 0f, 1f, 1f, 0f, 0.959f, 1f, 1f)
            clipRectAnimation.duration = 425
            clipRectAnimation.interpolator = fastOutExtraSlowIn()
            anim.addAnimation(clipRectAnimation)
            return anim
        }

    init {
        instance = this
    }

    fun getAnimation(name: String): AnimationSet? {
        return when (name) {
            ACTIVITY_OPEN_ENTER -> activityOpenEnterAnim
            ACTIVITY_OPEN_EXIT -> activityOpenExitAnim
            ACTIVITY_CLOSE_ENTER -> activityCloseEnterAnim
            ACTIVITY_CLOSE_EXIT -> activityCloseExitAnim
            else -> null
        }
    }

    private fun fastOutSlowIn(): Interpolator {
        return PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f)
    }

    @SuppressLint("RestrictedApi")
    private fun fastOutExtraSlowIn(): Interpolator {
        return PathInterpolator(PathParser.createPathFromPathData("M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1"))
    }

    private fun activityCloseDim(): Interpolator {
        return PathInterpolator(0.33f, 0.0f, 1.0f, 1.0f)
    }

    private fun aggressiveEase(): Interpolator {
        return PathInterpolator(0.2f, 0.0f, 0.0f, 1.0f)
    }

    companion object {

        const val ACTIVITY_OPEN_ENTER = "activity_open_enter"
        const val ACTIVITY_OPEN_EXIT = "activity_open_exit"
        const val ACTIVITY_CLOSE_ENTER = "activity_close_enter"
        const val ACTIVITY_CLOSE_EXIT = "activity_close_exit"

        private var instance: AnimationHelper? = null

        fun getInstance(): AnimationHelper? {
            if (instance == null)
                AnimationHelper()
            return instance
        }
    }
}
