package io.github.theaester.pcremotecontrol.comms;
// Encryptor.java
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Base64;

public class Encryptor {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final String SALT = "1234567812345678"; // Fixed salt for key generation
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 128;
    private SecretKey secretKey;
    private IvParameterSpec iv;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public Encryptor(String password) throws Exception {
        // Generate key from password
        //SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        //KeySpec spec = new PBEKeySpec(password.toCharArray(), SALT.getBytes(), ITERATION_COUNT, KEY_LENGTH);
        //SecretKey tmp = factory.generateSecret(spec);
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(password.getBytes(StandardCharsets.UTF_8));
        secretKey = new SecretKeySpec(keyBytes, 0, 16, ALGORITHM);
        Log.d("Encryptor", new String(secretKey.getEncoded()));

        // Use a fixed IV (initialization vector)
        iv = new IvParameterSpec(SALT.getBytes());

        // Initialize ciphers
        encryptCipher = Cipher.getInstance(TRANSFORMATION);
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        decryptCipher = Cipher.getInstance(TRANSFORMATION);
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
    }

    public String encrypt(String data) throws Exception {
        byte[] encrypted = encryptCipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return new String(Base64.getEncoder().encode(encrypted),StandardCharsets.UTF_8);
    }

    public String decrypt(String data) throws Exception {
        byte[] decodedBytes = Base64.getDecoder().decode(data);
        byte[] decrypted = decryptCipher.doFinal(decodedBytes);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}