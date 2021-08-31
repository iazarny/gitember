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
        assertTrue(CipherService.crypt("Hal9000").startsWith("//AES//"));
        assertEquals(CipherService.crypt("Hal9000"), CipherService.crypt("Hal9000"));
        assertNotEquals(CipherService.crypt("Hal9000"), CipherService.crypt("Bender"));
        assertEquals(null , CipherService.crypt(null));
        assertEquals("" , CipherService.crypt(""));

    }

}