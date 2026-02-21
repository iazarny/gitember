package com.az.gitember.service;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service to use AES-CBC to encrypt/decrypt passwords.
 * Uses a machine-specific derived key for better security.
 */
public class CipherService {

    private final static Logger log = Logger.getLogger(CipherService.class.getName());
    private final static String aesPrefix = "//AES//";
    private final static String aesCbcPrefix = "//AES2//";
    private final static String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private final static int IV_LENGTH = 16;

    private static SecretKeySpec keySpec;

    static {
        try {
            byte[] key = deriveKey();
            keySpec = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot init aes provider", e);
        }
    }

    private static byte[] deriveKey() throws Exception {
        String machineId = System.getProperty("user.name") + System.getProperty("user.home");
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(machineId.getBytes(StandardCharsets.UTF_8));
        return Arrays.copyOf(hash, 16);
    }

    public static synchronized String decrypt(String str) {
        if (str == null) {
            return null;
        } else if (str.startsWith(aesCbcPrefix)) {
            try {
                String encoded = str.substring(aesCbcPrefix.length());
                byte[] combined = Hex.decodeHex(encoded);
                byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
                byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

                Cipher decipher = Cipher.getInstance(CIPHER_ALGORITHM);
                decipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
                byte[] decryptedData = decipher.doFinal(encrypted);
                return new String(decryptedData, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.log(Level.WARNING, "Cannot decrypt value", e);
                return str;
            }
        } else if (str.startsWith(aesPrefix)) {
            try {
                byte[] legacyKey = "0123456789fast11".getBytes(StandardCharsets.UTF_8);
                SecretKeySpec legacyKeySpec = new SecretKeySpec(legacyKey, "AES");
                Cipher decipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                decipher.init(Cipher.DECRYPT_MODE, legacyKeySpec);
                byte[] decryptedData = decipher.doFinal(Hex.decodeHex(str.substring(aesPrefix.length())));
                return new String(decryptedData, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return str;
            }
        } else {
            return str;
        }
    }

    public static String crypt(String str) {
        return crypt(str, aesCbcPrefix);
    }

    public static synchronized String crypt(String str, String withPrefix) {
        if (str == null) {
            return null;
        } else if (str.startsWith(aesPrefix) || str.startsWith(aesCbcPrefix)) {
            return str;
        } else {
            try {
                byte[] toCrypt = str.getBytes(StandardCharsets.UTF_8);
                if (toCrypt.length == 0) {
                    return "";
                }

                byte[] iv = new byte[IV_LENGTH];
                new SecureRandom().nextBytes(iv);

                Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
                byte[] encryptedData = cipher.doFinal(toCrypt);

                byte[] combined = new byte[iv.length + encryptedData.length];
                System.arraycopy(iv, 0, combined, 0, iv.length);
                System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);

                return withPrefix + Hex.encodeHexString(combined);
            } catch (Exception e) {
                log.log(Level.WARNING, "Cannot encrypt value", e);
                return str;
            }
        }
    }

}
