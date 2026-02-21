package com.az.gitember.service;

import org.apache.commons.codec.DecoderException;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;

import static org.junit.jupiter.api.Assertions.*;

class CipherServiceTest {

    @Test
    void decrypt() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, DecoderException {
        assertEquals("bla-bla" , CipherService.decrypt("bla-bla"));
        assertEquals("Bender" , CipherService.decrypt("//AES//3f37a7838b939b99df2c6a9e86892683"));
        assertEquals(CipherService.decrypt("//AES//3f37a7838b939b99df2c6a9e86892683") , CipherService.decrypt("//AES//3f37a7838b939b99df2c6a9e86892683"));
        assertEquals("" , CipherService.decrypt(""));
        assertEquals(null , CipherService.decrypt(null));
    }

    @Test
    void crypt() throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        assertEquals("//AES//bla-bla" , CipherService.crypt("//AES//bla-bla"));
        assertEquals("//AES2//bla-bla" , CipherService.crypt("//AES2//bla-bla"));
        assertTrue(CipherService.crypt("Hal9000").startsWith("//AES2//"));
        // Due to random IV, encrypting same value twice produces different ciphertext
        String encrypted1 = CipherService.crypt("Hal9000");
        String encrypted2 = CipherService.crypt("Hal9001");
        // Both should decrypt to original values
        assertEquals("Hal9000", CipherService.decrypt(encrypted1));
        assertEquals("Hal9001", CipherService.decrypt(encrypted2));
        assertEquals(null , CipherService.crypt(null));
        assertEquals("" , CipherService.crypt(""));

    }

}