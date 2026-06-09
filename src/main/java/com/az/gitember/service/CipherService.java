package com.az.gitember.service;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores credentials in the OS keychain (macOS Keychain, Windows Credential Store,
 * Linux Secret Service). Falls back to AES-CBC when the keychain is unavailable.
 * Decryption still handles the legacy AES/ECB (//AES//) and AES/CBC (//AES2//) formats
 * so existing settings files are migrated transparently on first save.
 */
public class CipherService {

    private final static Logger log = Logger.getLogger(CipherService.class.getName());

    private final static String aesPrefix      = "//AES//";
    private final static String aesCbcPrefix   = "//AES2//";
    private final static String keyringPrefix  = "//KEYRING//";
    private final static String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private final static int    IV_LENGTH        = 16;
    private final static String KEYRING_SERVICE  = "gitember";

    private static final SecretKeySpec keySpec;
    private static final Keyring keyring;

    /** Populated on decrypt so that the next crypt() reuses the same keychain entry. */
    private static final Map<String, String> plaintextToKeyId = new HashMap<>();

    static {
        SecretKeySpec ks = null;
        try {
            ks = new SecretKeySpec(deriveKey(), "AES");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot init AES key", e);
        }
        keySpec = ks;

        Keyring kr = null;
        try {
            kr = Keyring.create();
        } catch (BackendNotSupportedException e) {
            log.log(Level.WARNING, "OS keyring not available, falling back to AES encryption", e);
        }
        keyring = kr;
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
        }
        if (str.startsWith(keyringPrefix)) {
            String keyId = str.substring(keyringPrefix.length());
            if (keyring != null) {
                try {
                    String plaintext = keyring.getPassword(KEYRING_SERVICE, keyId);
                    if (plaintext != null) {
                        plaintextToKeyId.put(plaintext, keyId);
                        return plaintext;
                    }
                } catch (PasswordAccessException e) {
                    log.log(Level.WARNING, "Keyring entry not found for id=" + keyId, e);
                }
            }
            return "";
        }
        if (str.startsWith(aesCbcPrefix)) {
            try {
                byte[] combined = Hex.decodeHex(str.substring(aesCbcPrefix.length()));
                byte[] iv        = Arrays.copyOfRange(combined, 0, IV_LENGTH);
                byte[] encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);
                Cipher decipher  = Cipher.getInstance(CIPHER_ALGORITHM);
                decipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv));
                return new String(decipher.doFinal(encrypted), StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.log(Level.WARNING, "Cannot decrypt AES2 value", e);
                return str;
            }
        }
        if (str.startsWith(aesPrefix)) {
            try {
                SecretKeySpec legacyKeySpec = new SecretKeySpec(
                        "0123456789fast11".getBytes(StandardCharsets.UTF_8), "AES");
                Cipher decipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                decipher.init(Cipher.DECRYPT_MODE, legacyKeySpec);
                byte[] decrypted = decipher.doFinal(Hex.decodeHex(str.substring(aesPrefix.length())));
                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return str;
            }
        }
        return str;
    }

    public static String crypt(String str) {
        return crypt(str, aesCbcPrefix);
    }

    /**
     * Encrypts {@code str} using the OS keychain when available; otherwise falls back
     * to AES-CBC (the {@code withPrefix} format). Already-encrypted values are returned
     * unchanged.
     */
    public static synchronized String crypt(String str, String withPrefix) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return "";
        }
        if (str.startsWith(aesPrefix) || str.startsWith(aesCbcPrefix) || str.startsWith(keyringPrefix)) {
            return str;
        }

        if (keyring != null) {
            try {
                String existingKeyId = plaintextToKeyId.get(str);
                if (existingKeyId != null) {
                    return keyringPrefix + existingKeyId;
                }
                String keyId = UUID.randomUUID().toString();
                keyring.setPassword(KEYRING_SERVICE, keyId, str);
                plaintextToKeyId.put(str, keyId);
                return keyringPrefix + keyId;
            } catch (PasswordAccessException e) {
                log.log(Level.WARNING, "Cannot write to OS keyring, falling back to AES", e);
            }
        }

        return cryptAes(str);
    }

    private static String cryptAes(String str) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            byte[] combined  = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return aesCbcPrefix + Hex.encodeHexString(combined);
        } catch (Exception e) {
            log.log(Level.WARNING, "Cannot encrypt value", e);
            return str;
        }
    }
}
