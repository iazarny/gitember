package com.az.gitember.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GitAttributesUtilTest {

    String attrs = "*.bmp filter=lfs diff=lfs merge=lfs -text\n" +
            "*.psd filter=lfs diff=lfs merge=lfs -text\n" +
            "*.git filter=movie diff=movie merge=movie -text\n" +
            "aaa.dfx filter=lfs diff=lfs merge=lfs -text";


    @Test
    void parseGitAttributes() {

        List<String> strs = GitAttributesUtil.parseGitAttributes(attrs, false);
        assertEquals(4, strs.size());

        strs = GitAttributesUtil.parseGitAttributes(attrs, true);
        assertEquals(3, strs.size());

    }

    @Test
    void getLsfPatters() {
        List<String> patterns = GitAttributesUtil.getLsfPatters(attrs);
        assertEquals(3, patterns.size());
        assertEquals("*.bmp", patterns.get(0));
        assertEquals("*.psd", patterns.get(1));
        assertEquals("aaa.dfx", patterns.get(2));
    }

    @Test
    void removeLfsPattern() {

        String newAttrs = GitAttributesUtil.removeLfsPattern(attrs, "*.psd");
        List<String> strs = GitAttributesUtil.parseGitAttributes(newAttrs, false);
        assertEquals(3, strs.size());

        strs = GitAttributesUtil.parseGitAttributes(newAttrs, true);
        assertEquals(2, strs.size());
    }

    @Test
    void addLsfPatters() {
        String rez = GitAttributesUtil.addLfsPattern(attrs,"*.jpg");
        List<String> strs = GitAttributesUtil.parseGitAttributes(rez, false);
        assertEquals(5, strs.size());
        assertEquals("*.jpg",  GitAttributesUtil.getLsfPatters(rez).get(3));


        rez = GitAttributesUtil.addLfsPattern(rez,"*.jpg");
        strs = GitAttributesUtil.parseGitAttributes(rez, false);
        assertEquals(5, strs.size());

    }

}