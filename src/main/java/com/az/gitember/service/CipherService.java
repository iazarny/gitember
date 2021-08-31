package com.az.gitember.service;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple service to use aes to crypt password.
 */
public class CipherService {

    private final static Logger log = Logger.getLogger(CipherService.class.getName());
    private final static String aesPrefix = "//AES//";

    private final static  byte[] key = "0123456789fast11".getBytes();
    private static Cipher cipher;
    private static Cipher decipher;
    private static SecretKeySpec keySpec;

    static {
        try {
            cipher = Cipher.getInstance("AES");
            decipher = Cipher.getInstance("AES");
            keySpec = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot init aes provider" , e);
        }
    }

    public static String decrypt(String str) {
        if (str == null) {
            return null;
        } else if (str.startsWith(aesPrefix)) {
            try {
                decipher.init(Cipher.DECRYPT_MODE, keySpec);
                byte[] decryptedData = decipher.doFinal(Hex.decodeHex(str.substring(aesPrefix.length())));
                return new String(decryptedData);
            } catch (Exception e) {
                return str;
            }
        } else {
            return str;
        }
    }

    public static String crypt(String str) {
        if (str == null) {
            return null;
        } else if (str.startsWith(aesPrefix)) {
            return str;
        } else {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, keySpec);
                byte [] toCrypt = str.getBytes();
                if (toCrypt.length == 0) {
                    return "";

                } else {
                    byte[] encryptedData = cipher.doFinal(toCrypt);
                    return aesPrefix + Hex.encodeHexString(encryptedData);

                }
            } catch ( Exception e) {
                return str;
            }
        }
    }

}
