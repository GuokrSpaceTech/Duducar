package com.guokrspace.dududriver.util;

import android.text.TextUtils;

import com.guokrspace.dududriver.alipay.Base64;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by hyman on 17/3/25.
 */
public class AESUtil {

    // 算法/模式/补码方式
    private static final String ALGORITHM_MODEL_COMPLEMENT = "AES/CBC/PKCS5Padding";

    private static final String DEFAULT_KEY = "PiuMYaVQ6bB90eRj";

    private static final String IV_E = "2938513409340637";

    private AESUtil() {}

    public static String encrypt(String src) {
        return encrypt(DEFAULT_KEY, src);
    }


    public static String encrypt(String key, String src) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key is blank: key = " + key);
        }
        if (key.length() != 16) {
            throw new IllegalArgumentException("key length is invalid: key.length() = " + key.length());
        }
        byte[] raw = key.getBytes();
        SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(ALGORITHM_MODEL_COMPLEMENT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        IvParameterSpec iv = new IvParameterSpec(IV_E.getBytes());
        try {
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        }
        byte[] encrypted = null;
        try {
            encrypted = cipher.doFinal(src.getBytes());
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return null;
        }

        return Base64.encode(encrypted);
    }


    public static String decrypt(String src) {
        return decrypt(DEFAULT_KEY, src);
    }


    public static String decrypt(String key, String src) {
        if (TextUtils.isEmpty(key)) {
            throw new IllegalArgumentException("key is blank: key = " + key);
        }
        if (key.length() != 16) {
            throw new IllegalArgumentException("key length is invalid: key.length() = " + key.length());
        }

        try {
            byte[] raw = key.getBytes("ASCII");
            SecretKeySpec sKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM_MODEL_COMPLEMENT);
            IvParameterSpec iv = new IvParameterSpec(IV_E.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);
            byte[] encrypted = Base64.decode(src);
            byte[] original = cipher.doFinal(encrypted);
            String originalSrc = new String(original);
            return originalSrc;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return null;
        }

    }

}
