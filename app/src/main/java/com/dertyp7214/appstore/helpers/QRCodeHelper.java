/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Base64;

import com.dertyp7214.appstore.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class QRCodeHelper {

    private final static int QRCodeDimension = 1000;

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap generateQRCode(Context context, String Value) {
        BitMatrix bitMatrix;

        try {
            Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    QRCodeDimension, QRCodeDimension, hintMap
            );
        } catch (Exception e) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ?
                        context.getResources().getColor(R.color.QRCodeBlackColor) : context
                        .getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }

        Bitmap bitmap =
                Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, QRCodeDimension, 0, 0, bitMatrixWidth, bitMatrixHeight);

        Bitmap overlay =
                BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

        Bitmap whitePane = Bitmap.createBitmap(overlay.getWidth(), overlay.getHeight(),
                Bitmap.Config.ARGB_8888);
        whitePane.eraseColor(Color.WHITE);

        return mergeBitmaps(mergeBitmaps(overlay, whitePane, overlay.getWidth(), 1F), bitmap,
                QRCodeDimension, 3);
    }

    private static Bitmap mergeBitmaps(Bitmap overlay, Bitmap bitmap, int bigDim, float overlayMetric) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        overlay = Bitmap.createScaledBitmap(overlay, (int) (bigDim / overlayMetric),
                (int) (bigDim / overlayMetric),
                false);

        Bitmap combined = Bitmap.createBitmap(width, height, bitmap.getConfig());
        Canvas canvas = new Canvas(combined);
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        canvas.drawBitmap(bitmap, new Matrix(), null);

        int centreX = (canvasWidth - overlay.getWidth()) / 2;
        int centreY = (canvasHeight - overlay.getHeight()) / 2;
        canvas.drawBitmap(overlay, centreX, centreY, null);

        return combined;
    }
}
