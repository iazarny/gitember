package com.az.gitember.service;

import com.az.gitember.controller.TextBrowserContentAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LangResolverTest {

    @Test
    void resolveLang() {
        LangResolver adapter = new LangResolver();
        assertTrue(adapter.resolveLang("jsp").isPresent());
    }
}