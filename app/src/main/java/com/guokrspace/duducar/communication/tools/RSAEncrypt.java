package com.guokrspace.duducar.communication.tools;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


public class RSAEncrypt {
    /**
     */
    private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static String PUBLIC_KEY = "";

    public static RSAPublicKey getPublicKey(){
        try {
            if(PUBLIC_KEY.length() < 1) {
                Log.e("RSAEncrypt","load public key failed");
            }
            return loadPublicKeyByStr(PUBLIC_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * ���������Կ��
     */
    public static void genKeyPair(String filePath) {
        // KeyPairGenerator���������ɹ�Կ��˽Կ�ԣ�����RSA�㷨���ɶ���
        KeyPairGenerator keyPairGen = null;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        keyPairGen.initialize(1024,new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        try {
            String publicKeyString = Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
            String privateKeyString = Base64.encodeToString(privateKey.getEncoded(), Base64.DEFAULT);
            FileWriter pubfw = new FileWriter(filePath + "/publicKey.keystore");
            FileWriter prifw = new FileWriter(filePath + "/privateKey.keystore");
            BufferedWriter pubbw = new BufferedWriter(pubfw);
            BufferedWriter pribw = new BufferedWriter(prifw);
            pubbw.write(publicKeyString);
            pribw.write(privateKeyString);
            pubbw.flush();
            pubbw.close();
            pubfw.close();
            pribw.flush();
            pribw.close();
            prifw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ���ļ����������м��ع�Կ
     *
     * @param
     *            ��Կ������
     * @throws Exception
     *             ���ع�Կʱ�������쳣
     */
    public static String loadPublicKeyByFile(InputStream stream) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if(readLine.indexOf("BEGIN")>=0 ||readLine.indexOf("END")>=0)
                    continue;
                sb.append(readLine);
            }
            br.close();
            PUBLIC_KEY = sb.toString();
            Log.e("TEST E", PUBLIC_KEY);
            return sb.toString();
        } catch (IOException e) {
            throw new Exception("系统文件缺失");
        } catch (NullPointerException e) {
            throw new Exception("系统文件缺失");
        }
    }

    /**
     * ���ַ����м��ع�Կ
     *
     * @param publicKeyStr
     *            ��Կ�����ַ���
     * @throws Exception
     *             ���ع�Կʱ�������쳣
     */
    public static RSAPublicKey loadPublicKeyByStr(String publicKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.decode(publicKeyStr, Base64.DEFAULT);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("�޴��㷨");
        } catch (InvalidKeySpecException e) {
            throw new Exception("��Կ�Ƿ�");
        } catch (NullPointerException e) {
            throw new Exception("��Կ����Ϊ��");
        }
    }

    /**
     * ���ļ��м���˽Կ
     *
     * @param
     *            ˽Կ�ļ���
     * @return �Ƿ�ɹ�
     * @throws Exception
     */
    public static String loadPrivateKeyByFile(String path) throws Exception {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path
                    + "/privateKey.keystore"));
            String readLine = null;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if(readLine.indexOf("BEGIN")>=0 ||readLine.indexOf("END")>=0)
                    continue;
                sb.append(readLine);
            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            throw new Exception("˽Կ���ݶ�ȡ����");
        } catch (NullPointerException e) {
            throw new Exception("˽Կ������Ϊ��");
        }
    }

    public static RSAPrivateKey loadPrivateKeyByStr(String privateKeyStr)
            throws Exception {
        try {
            byte[] buffer = Base64.decode(privateKeyStr, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("�޴��㷨");
        } catch (InvalidKeySpecException e) {
            throw new Exception("˽Կ�Ƿ�");
        } catch (NullPointerException e) {
            throw new Exception("˽Կ����Ϊ��");
        }
    }

    /**
     * ��Կ���ܹ���
     *
     * @param publicKey
     *            ��Կ
     * @param plainTextData
     *            ��������
     * @return
     * @throws Exception
     *             ���ܹ����е��쳣��Ϣ
     */
    public static byte[] encrypt(RSAPublicKey publicKey, byte[] plainTextData)
            throws Exception {
        if (publicKey == null) {
            throw new Exception("���ܹ�ԿΪ��, ������");
        }
        Cipher cipher = null;
        try {
            // ʹ��Ĭ��RSA
            cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("�޴˼����㷨");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("���ܹ�Կ�Ƿ�,����");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("���ĳ��ȷǷ�");
        } catch (BadPaddingException e) {
            throw new Exception("������������");
        }
    }

    /**
     * ˽Կ���ܹ���
     *
     * @param privateKey
     *            ˽Կ
     * @param plainTextData
     *            ��������
     * @return
     * @throws Exception
     *             ���ܹ����е��쳣��Ϣ
     */
    public static byte[] encrypt(RSAPrivateKey privateKey, byte[] plainTextData)
            throws Exception {
        if (privateKey == null) {
            throw new Exception("应用文件缺失!");
        }
        Cipher cipher = null;
        try {
            // ʹ��Ĭ��RSA
            cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(plainTextData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("�޴˼����㷨");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("����˽Կ�Ƿ�,����");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("���ĳ��ȷǷ�");
        } catch (BadPaddingException e) {
            throw new Exception("������������");
        }
    }

    /**
     * ˽Կ���ܹ���
     *
     * @param privateKey
     *            ˽Կ
     * @param cipherData
     *            ��������
     * @return ����
     * @throws Exception
     *             ���ܹ����е��쳣��Ϣ
     */
    public static byte[] decrypt(RSAPrivateKey privateKey, byte[] cipherData)
            throws Exception {
        if (privateKey == null) {
            throw new Exception("����˽ԿΪ��, ������");
        }
        Cipher cipher = null;
        try {
            // ʹ��Ĭ��RSA
            cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output = cipher.doFinal(cipherData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("�޴˽����㷨");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("����˽Կ�Ƿ�,����");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("���ĳ��ȷǷ�");
        } catch (BadPaddingException e) {
            throw new Exception("������������");
        }
    }

    /**
     * ��Կ���ܹ���
     *
     * @param publicKey
     *            ��Կ
     * @param cipherData
     *            ��������
     * @return ����
     * @throws Exception
     *             ���ܹ����е��쳣��Ϣ
     */
    public static byte[] decrypt(RSAPublicKey publicKey, byte[] cipherData)
            throws Exception {
        if (publicKey == null) {
            throw new Exception("���ܹ�ԿΪ��, ������");
        }
        Cipher cipher = null;
        try {
            // ʹ��Ĭ��RSA
            cipher = Cipher.getInstance("RSA");
            // cipher= Cipher.getInstance("RSA", new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] output = cipher.doFinal(cipherData);
            return output;
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("�޴˽����㷨");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeyException e) {
            throw new Exception("���ܹ�Կ�Ƿ�,����");
        } catch (IllegalBlockSizeException e) {
            throw new Exception("���ĳ��ȷǷ�");
        } catch (BadPaddingException e) {
            throw new Exception("������������");
        }
    }

    /**
     * �ֽ�����תʮ�������ַ���
     *
     * @param data
     *            ��������
     * @return ʮ����������
     */
    public static String byteArrayToString(byte[] data) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            // ȡ���ֽڵĸ���λ ��Ϊ�����õ���Ӧ��ʮ�����Ʊ�ʶ�� ע���޷�������
            stringBuilder.append(HEX_CHAR[(data[i] & 0xf0) >>> 4]);
            // ȡ���ֽڵĵ���λ ��Ϊ�����õ���Ӧ��ʮ�����Ʊ�ʶ��
            stringBuilder.append(HEX_CHAR[(data[i] & 0x0f)]);
            if (i < data.length - 1) {
                stringBuilder.append(' ');
            }
        }
        return stringBuilder.toString();
    }


}
  