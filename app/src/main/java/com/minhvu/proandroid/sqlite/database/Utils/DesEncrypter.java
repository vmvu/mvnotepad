package com.minhvu.proandroid.sqlite.database.Utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static javax.crypto.Cipher.ENCRYPT_MODE;

/**
 * Created by vomin on 8/29/2017.
 */

public class DesEncrypter {
    private static final String algorithm = "AES/CBC/PKCS5PADDING";
    private static final String algorithmKey = "AES";


    String key;

    private IvParameterSpec creatIV(){
        final byte[] iv = new byte[16];
        Arrays.fill(iv,(byte)0x00);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        return ivParameterSpec;
    }


    public String encrypt(String string){
        try{
            SecretKey secretKey  = KeyGenerator.getInstance(algorithmKey).generateKey();

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, creatIV());

            byte[] uft8 = string.getBytes(StandardCharsets.UTF_8);
            byte[] enc = cipher.doFinal(uft8);

            key =  Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
            return Base64.encodeToString(enc, Base64.DEFAULT);
        }catch (Exception e){
            e.printStackTrace();
            Log.d("encrypt", e.toString());
        }
        return null;
    }
    public String getKey(){
        return key;
    }

    public String decrypt(String strEncrypt, String key){
        try {
            byte[] encodedKey = Base64.decode(key, Base64.DEFAULT);
            SecretKey originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, algorithmKey);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, originalKey, creatIV());
            byte[] dec = cipher.doFinal(Base64.decode(strEncrypt, Base64.DEFAULT));
            return new String(dec, StandardCharsets.UTF_8);
        }catch (Exception e){
            e.printStackTrace();
            Log.d("decrypt", e.toString());
        }
        return null;
    }
}
