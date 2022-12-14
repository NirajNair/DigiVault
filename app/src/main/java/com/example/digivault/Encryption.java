package com.example.digivault;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption  {
    private static final String salt = "SaltySalt";

    private static final int IV_LENGTH = 16;

    private static byte[] getSaltBytes() throws Exception {
        return salt.getBytes("UTF-8");
    }

    private static char[] getMasterPassword() {
        return "SuperSecretPassword".toCharArray();
    }

    public byte[] encrypt(byte[] input) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(getMasterPassword(), getSaltBytes(), 65536,256);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] ivBytes = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
        byte[] encryptedTextBytes = cipher.doFinal(input);
        byte[] finalByteArray = new byte[ivBytes.length + encryptedTextBytes.length];
        System.arraycopy(ivBytes, 0, finalByteArray, 0, ivBytes.length);
        System.arraycopy(encryptedTextBytes, 0, finalByteArray, ivBytes.length, encryptedTextBytes.length);
        return finalByteArray;
    }



    public byte[] decrypt(String input) throws Exception {
        if (input.length() <= IV_LENGTH) {
            throw new Exception("The input string is not long enough to contain the initialisation bytes and data.");
        }
        byte[] byteArray = Base64.decode(input, Base64.DEFAULT);
        byte[] ivBytes = new byte[IV_LENGTH];
        System.arraycopy(byteArray, 0, ivBytes, 0, 16);
        byte[] encryptedTextBytes = new byte[byteArray.length - ivBytes.length];
        System.arraycopy(byteArray, IV_LENGTH, encryptedTextBytes, 0, encryptedTextBytes.length);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        PBEKeySpec spec = new PBEKeySpec(getMasterPassword(), getSaltBytes(), 65536, 256);
        SecretKey secretKey = factory.generateSecret(spec);
        SecretKeySpec secret = new SecretKeySpec(secretKey.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(ivBytes));
        byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
        return decryptedTextBytes;
    }

















//    private static final String SECRET_KEY
//            = "dont_look_its_the_secret_key";
//
//    private static final String SALT = "oooooffffff";
//
//    // This method use to encrypt to string
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public String encrypt(String strToEncrypt)
//    {
//        try {
//
//            // Create default byte array
//            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0,
//                    0, 0, 0, 0, 0, 0, 0, 0 };
//            IvParameterSpec ivspec
//                    = new IvParameterSpec(iv);
//
//            // Create SecretKeyFactory object
//            SecretKeyFactory factory
//                    = SecretKeyFactory.getInstance(
//                    "PBKDF2WithHmacSHA256");
//
//            // Create KeySpec object and assign with
//            // constructor
//            KeySpec spec = new PBEKeySpec(
//                    SECRET_KEY.toCharArray(), SALT.getBytes(),
//                    65536, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKeySpec secretKey = new SecretKeySpec(
//                    tmp.getEncoded(), "AES");
//
//            Cipher cipher = Cipher.getInstance(
//                    "AES/CBC/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey,
//                    ivspec);
//            // Return encrypted string
//            return Base64.getEncoder().encodeToString(
//                    cipher.doFinal(strToEncrypt.getBytes(
//                            StandardCharsets.UTF_8)));
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    // This method use to decrypt to string
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public String decrypt(String strToDecrypt)
//    {
//        try {
//
//            // Default byte array
//            byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0,
//                    0, 0, 0, 0, 0, 0, 0, 0 };
//            // Create IvParameterSpec object and assign with
//            // constructor
//            IvParameterSpec ivspec
//                    = new IvParameterSpec(iv);
//
//            // Create SecretKeyFactory Object
//            SecretKeyFactory factory
//                    = SecretKeyFactory.getInstance(
//                    "PBKDF2WithHmacSHA256");
//
//            // Create KeySpec object and assign with
//            // constructor
//            KeySpec spec = new PBEKeySpec(
//                    SECRET_KEY.toCharArray(), SALT.getBytes(),
//                    65536, 256);
//            SecretKey tmp = factory.generateSecret(spec);
//            SecretKeySpec secretKey = new SecretKeySpec(
//                    tmp.getEncoded(), "AES");
//
//            Cipher cipher = Cipher.getInstance(
//                    "AES/CBC/PKCS5PADDING");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey,
//                    ivspec);
//            // Return decrypted string
//            return new String(cipher.doFinal(
//                    Base64.getDecoder().decode(strToDecrypt)));
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
