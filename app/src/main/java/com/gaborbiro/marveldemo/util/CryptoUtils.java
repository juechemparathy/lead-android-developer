package com.gaborbiro.marveldemo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {

    public static String md5(String text) {
        try {
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(text.getBytes());
            byte messageDigest[] = digest.digest();
            StringBuffer result = new StringBuffer();

            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2) h = "0" + h;
                result.append(h);
            }
            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
