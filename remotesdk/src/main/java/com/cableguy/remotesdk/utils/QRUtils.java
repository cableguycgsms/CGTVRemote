package com.cableguy.remotesdk.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRUtils {
//
//    public interface QRCallback {
//        void onGenerated(Bitmap bitmap);
//        void onFailure(Exception e);
//    }
//
//    /**
//     * Generates a QR Code bitmap by fetching the URL and encoding it.
//     * Calls the callback with the result asynchronously.
//     */
//    public static void generateQRCode(int width, int height, QRCallback callback, Context context) {
//        String deviceId = DeviceUtils.getDeviceId(context);
//        String apiUrl = "https://remotesetu.in:4679/get-socket/" + deviceId;
//        ApiUtils.fetchSocketUrl(apiUrl, new ApiUtils.UrlCallback() {
//            @Override
//            public void onSuccess(String url) {
//                try {
//                    QRCodeWriter writer = new QRCodeWriter();
//                    BitMatrix bitMatrix = writer.encode("https://remotesetu.in:4679" + url, BarcodeFormat.QR_CODE, width, height);
//                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//
//                    for (int x = 0; x < width; x++) {
//                        for (int y = 0; y < height; y++) {
//                            bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//
//                    callback.onGenerated(bitmap);
//
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                    callback.onFailure(e);
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                callback.onFailure(e);
//            }
//        });
//    }
}
