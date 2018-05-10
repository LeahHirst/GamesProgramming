package com.ahirst.doodaddash.util;

import android.graphics.Bitmap;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class CameraUtil {

    public static String encodeBitmapToBase64JPEG(Bitmap image) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOS);

        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

}
