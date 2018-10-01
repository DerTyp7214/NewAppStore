/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.uiTools;

import android.annotation.SuppressLint;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;
import android.view.animation.TranslateAnimation;

import androidx.core.graphics.PathParser;

public class AnimationHelper {

    public static final String ACTIVITY_OPEN_ENTER = "activity_open_enter";
    public static final String ACTIVITY_OPEN_EXIT = "activity_open_exit";
    public static final String ACTIVITY_CLOSE_ENTER = "activity_close_enter";
    public static final String ACTIVITY_CLOSE_EXIT = "activity_close_exit";

    private static AnimationHelper instance;

    private AnimationHelper(){
        instance=this;
    }

    public static AnimationHelper getInstance(){
        if(instance==null)
            new AnimationHelper();
        return instance;
    }

    public AnimationSet getAnimation(String name){
        switch (name){
            case ACTIVITY_OPEN_ENTER:
                return getActivityOpenEnterAnim();
            case ACTIVITY_OPEN_EXIT:
                return getActivityOpenExitAnim();
            case ACTIVITY_CLOSE_ENTER:
                return getActivityCloseEnterAnim();
            case ACTIVITY_CLOSE_EXIT:
                return getActivityCloseExitAnim();
            default:
                return null;
        }
    }

    private AnimationSet getActivityOpenEnterAnim() {
        AnimationSet anim = new AnimationSet(false);
        anim.setZAdjustment(Animation.ZORDER_TOP);
        TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, 0.04100001f, 0f);
        translateAnimation.setDuration(425);
        translateAnimation.setInterpolator(fastOutSlowIn());
        anim.addAnimation(translateAnimation);
        ClipRectAnimation clipRectAnimation = new ClipRectAnimation(0f, 0.959f, 1f, 1f, 0f, 0f, 1f, 1f);
        clipRectAnimation.setDuration(425);
        clipRectAnimation.setInterpolator(fastOutExtraSlowIn());
        anim.addAnimation(clipRectAnimation);
        return anim;
    }

    private AnimationSet getActivityOpenExitAnim()  {
        AnimationSet anim = new AnimationSet(false);
        TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, 0f, -0.019999981f);
        translateAnimation.setDuration(425);
        translateAnimation.setInterpolator(fastOutSlowIn());
        anim.addAnimation(translateAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.9f);
        alphaAnimation.setDuration(117);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        anim.addAnimation(alphaAnimation);
        return anim;
    }

    private AnimationSet getActivityCloseEnterAnim() {
        AnimationSet anim = new AnimationSet(false);
        TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, -0.019999981f, 0f);
        translateAnimation.setDuration(425);
        translateAnimation.setInterpolator(fastOutSlowIn());
        anim.addAnimation(translateAnimation);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.9f, 1.0f);
        alphaAnimation.setDuration(425);
        alphaAnimation.setStartOffset(0);
        alphaAnimation.setInterpolator(activityCloseDim());
        anim.addAnimation(alphaAnimation);
        return anim;
    }

    private AnimationSet getActivityCloseExitAnim() {
        AnimationSet anim = new AnimationSet(false);
        TranslateAnimation translateAnimation = new TranslateAnimation(0f, 0f, 0f, 0.04100001f);
        translateAnimation.setDuration(425);
        translateAnimation.setInterpolator(fastOutSlowIn());
        anim.addAnimation(translateAnimation);
        ClipRectAnimation clipRectAnimation = new ClipRectAnimation(0f, 0f, 1f, 1f, 0f, 0.959f, 1f, 1f);
        clipRectAnimation.setDuration(425);
        clipRectAnimation.setInterpolator(fastOutExtraSlowIn());
        anim.addAnimation(clipRectAnimation);
        return anim;
    }

    private Interpolator fastOutSlowIn()  {
        return new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    }

    @SuppressLint("RestrictedApi")
    private Interpolator fastOutExtraSlowIn() {
        return new PathInterpolator(PathParser.createPathFromPathData("M 0,0 C 0.05, 0, 0.133333, 0.06, 0.166666, 0.4 C 0.208333, 0.82, 0.25, 1, 1, 1"));
    }

    private Interpolator activityCloseDim() {
        return new PathInterpolator(0.33f, 0.0f, 1.0f, 1.0f);
    }

    private Interpolator aggressiveEase() {
        return new PathInterpolator(0.2f, 0.0f, 0.0f, 1.0f);
    }

}
