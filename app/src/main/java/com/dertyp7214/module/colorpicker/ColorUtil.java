/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.module.colorpicker;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class ColorUtil {
    public ColorUtil() {
    }

    public static int getColor(String hexCode) {
        return getIntFromColor((float) Color.red(Color.parseColor(hexCode)), (float)Color.green(Color.parseColor(hexCode)), (float)Color.blue(Color.parseColor(hexCode)));
    }

    private static int getIntFromColor(float Red, float Green, float Blue) {
        int R = Math.round(255.0F * (256.0F - Red));
        int G = Math.round(255.0F * (256.0F - Green));
        int B = Math.round(255.0F * (256.0F - Blue));
        R = R << 16 & 16711680;
        G = G << 8 & '\uff00';
        B &= 255;
        return -16777216 | R | G | B;
    }

    public static int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() > 0 && drawable.getIntrinsicHeight() > 0) {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static int calculateColor(int color1, int color2, int max, int current) {
        String strC1 = Integer.toHexString(color1);
        String strC2 = Integer.toHexString(color2);
        String retColor = "#";

        for(int i = 2; i < strC1.length(); ++i) {
            String tmp1 = strC1.charAt(i) + "" + strC1.charAt(i + 1);
            String tmp2 = strC2.charAt(i) + "" + strC2.charAt(i + 1);
            int tmp1Color = (int)Long.parseLong(tmp1, 16);
            int tmp2Color = (int)Long.parseLong(tmp2, 16);
            int dif = tmp2Color - tmp1Color;
            double difCalc = (double)dif / (double)max;
            int colorMerge = (int)(difCalc * (double)current);
            if (tmp1Color + colorMerge > 255) {
                colorMerge = 0;
            }

            String add = Integer.toHexString(tmp1Color + colorMerge);
            if (add.length() < 2) {
                add = "0" + add;
            }

            retColor = retColor + add;
            ++i;
        }

        return Color.parseColor(retColor);
    }
}
