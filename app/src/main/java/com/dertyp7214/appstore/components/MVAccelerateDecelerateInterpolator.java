/*
 * Copyright (c) 2018.
 * Created by Josua Lengwenath
 */

package com.dertyp7214.appstore.components;

import android.view.animation.Interpolator;

public class MVAccelerateDecelerateInterpolator implements Interpolator {

    // easeInOutQuint
    public float getInterpolation(float t) {
        float x;
        if (t<0.5f)
        {
            x = t*2.0f;
            return 0.5f*x*x*x*x*x;
        }
        x = (t-0.5f)*2-1;
        return 0.5f*x*x*x*x*x+1;
    }
}