package com.cableguy.remotesdk.utils;
import android.util.Base64;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class AESUtils {
    private static final String SECRET_KEY_BASE64 = "D7A9mbpVbwTDN46I8zFICCT1i0xErZBDYZ9E7uEGD9Y=";
    private static final String IV_BASE64 = "O3V9xH4DNdrZVZ0qzWJIXw==";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String encrypt(String data) {
        try {
            byte[] keyBytes = Base64.decode(SECRET_KEY_BASE64, Base64.DEFAULT);
            byte[] ivBytes = Base64.decode(IV_BASE64, Base64.DEFAULT);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);

            byte[] encryptedVal = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedVal, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String decrypt(String encryptedData) {
        try {
            byte[] keyBytes = Base64.decode(SECRET_KEY_BASE64, Base64.DEFAULT);
            byte[] ivBytes = Base64.decode(IV_BASE64, Base64.DEFAULT);

            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            IvParameterSpec iv = new IvParameterSpec(ivBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);

            byte[] decodedVal = Base64.decode(encryptedData, Base64.NO_WRAP);
            byte[] decryptedVal = cipher.doFinal(decodedVal);
            return new String(decryptedVal, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
