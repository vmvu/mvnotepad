package com.minhvu.proandroid.sqlite.database.Utils;

import android.util.Base64;
import android.util.Log;

import java.nio.charset.Charset;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by vomin on 8/29/2017.
 */

public class DeEncrypter {
    private static final String algorithm = "AES/CBC/PKCS5PADDING";
    private static final String algorithmCreateKey = "AES";


    private static String key;

    private static IvParameterSpec createIVs() {
        final byte[] iv = new byte[16];
        Arrays.fill(iv, (byte) 0x00);
        return new IvParameterSpec(iv);
    }


    public static String encryptString(String string) {
        try {
            SecretKey secretKey = KeyGenerator.getInstance(algorithmCreateKey).generateKey();

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, createIVs());

            byte[] uft8 = string.getBytes(Charset.forName("UTF-8"));
            byte[] enc = cipher.doFinal(uft8);

            key = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            return Base64.encodeToString(enc, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("encrypt", e.toString());
        }
        return null;
    }

    public static String getKey() {
        String key = DeEncrypter.key;
        DeEncrypter.key = "";
        return key;
    }

    public static String decryptString(String strEncrypt, String key) {
        try {
            byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
            SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, algorithmCreateKey);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, originalKey, createIVs());
            byte[] dec = cipher.doFinal(Base64.decode(strEncrypt, Base64.DEFAULT));
            return new String(dec, Charset.forName("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("decrypt", e.toString());
        }
        return null;
    }

    public static byte[] encryptFile(byte[] bytes) {
        try {
            SecretKey secretKey = KeyGenerator.getInstance(algorithmCreateKey).generateKey();

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, createIVs());

            byte[] enc = cipher.doFinal(bytes);

            key = Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            return Base64.encode(enc, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("encrypt", e.toString());
        }
        return null;
    }

    public static byte[] decryptFile(byte[] bytes, String key) {
        try {
            byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
            SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, algorithmCreateKey);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, originalKey, createIVs());
            return cipher.doFinal(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("decrypt", e.toString());
        }
        return null;
    }
}
