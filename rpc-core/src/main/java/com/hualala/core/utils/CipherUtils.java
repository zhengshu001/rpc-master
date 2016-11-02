package com.hualala.core.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by xiangbin on 2016/11/1.
 */
public class CipherUtils {

    private static String CHARSET = "UTF-8";
    /**
     * MD5 加密
     */
    public static String getMD5Str(String str) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(str.getBytes(CHARSET));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        byte[] byteArray = messageDigest.digest();

        StringBuffer md5StrBuff = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return md5StrBuff.toString();
    }
    /**
     * MD5 加密
     */
    public static String getMD5Str(String str, String charsetName) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");

            messageDigest.reset();

            messageDigest.update(str.getBytes(charsetName));
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        return byte2String(messageDigest.digest());
    }
    /**
     * 取DES的加密
     * @param secrityKey
     * @param str
     * @param charsetName
     * @return
     */
    public static String getDESStr(String algorithm, byte[] secrityKey, String str, String charsetName) {
        try {
            byte[] data = str.getBytes(charsetName);
            SecretKey deskey = new SecretKeySpec(secrityKey, algorithm);

            Cipher cipher = Cipher.getInstance(algorithm);

            cipher.init(Cipher.ENCRYPT_MODE, deskey);
            return byte2String(cipher.doFinal(data));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 取3DES的加密
     * @param secrityKey
     * @param str
     * @param charsetName
     * @return
     */
    public static String get3DESStr(byte[] secrityKey, String str, String charsetName) {
        return getDESStr("DESede", secrityKey, str, charsetName);
    }
    /**
     * 取DES的加密
     * @param secrityKey
     * @param str
     * @param charsetName
     * @return
     */
    public static String getDESStr(byte[] secrityKey, String str, String charsetName) {
        return getDESStr("DES", secrityKey, str, charsetName);
    }
    /**
     * byte数据组字符串
     * @param byteArray
     * @return
     */
    public static String byte2String(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {
            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
                sb.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            else
                sb.append(Integer.toHexString(0xFF & byteArray[i]));
        }

        return sb.toString();
    }

    public static String getSHA1Str(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return byte2String(md.digest(byte2String(str.getBytes("UTF-8")).getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
