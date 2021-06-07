package com.az.gitember.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ExtensionMapTest {

    @Test
    void isTextExtension() {

        assertTrue(ExtensionMap.isTextExtension(Path.of(".txt")));
        assertTrue(ExtensionMap.isTextExtension(Path.of("abc.txt")));
        assertTrue(ExtensionMap.isTextExtension(Path.of("c:\\asdf\\asdf\\abc.txt")));
        assertTrue(ExtensionMap.isTextExtension(Path.of("asdf/asdf/abc.rb")));
    }
}