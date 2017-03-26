package com.guokrspace.duducar.communication.tools;

import android.util.Base64;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Security {
    private static SecretKey SECURITY_KEY = null;
    private static String SECURITY_KEY_MD5 = null;

    public static void initKey(){
        if(SECURITY_KEY == null)
            SECURITY_KEY = restoreSecretKey(generateAESSecretKey());
        if(SECURITY_KEY_MD5 != null) return;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(SECURITY_KEY.getEncoded());
            SECURITY_KEY_MD5 = new BigInteger(1, md.digest()).toString(16).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    public static String getSecurityKey(){
        initKey();
        return SECURITY_KEY_MD5;
    }
    public static String encrypt(String input, String skey){
        byte[] crypted = null;  
        try{  
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey key = new SecretKeySpec(skey.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            crypted = cipher.doFinal(input.getBytes());  
        }catch(Exception e){  
        System.out.println(e.toString());  
    }  
    return new String(Base64.encode(crypted, Base64.DEFAULT));
}  

    public static String decrypt(String input, String skey){
        byte[] output = null;  
        try{  
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKey key = new SecretKeySpec(skey.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            output = cipher.doFinal(Base64.decode(input, Base64.DEFAULT));
        }catch(Exception e){
            System.out.println(e.toString());  
        }  
        return new String(output);  
    }

    public static byte[] generateAESSecretKey() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            return keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
              // TODO Auto-generated catch block
            e.printStackTrace();
        }
            return null;
    }

    public static SecretKey restoreSecretKey(byte[] secretBytes) {
        SecretKey secretKey = new SecretKeySpec(secretBytes, "AES");
        return secretKey;
    }


}  