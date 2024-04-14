package com.az.gitember.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GitemberUtilTest {



    @Test
    void adapt() {
        String [] s = GitLfsUtil.parseLsfListLine("12345   -  what ever");
        assertEquals("12345", s[0]);
        assertEquals("-", s[1]);
        assertEquals("what ever", s[2]);

        s = GitLfsUtil.parseLsfListLine("1   *  2");
        assertEquals("1", s[0]);
        assertEquals("*", s[1]);
        assertEquals("2", s[2]);

        assertNull(GitLfsUtil.parseLsfListLine("1   dqw qwd   2"));
        assertNull(GitLfsUtil.parseLsfListLine("dqw qwd - asd"));
    }

    @Test
    void countWhiteCharFromStart() {
        assertEquals(0, GitemberUtil.countWhiteCharFromStart(null));
        assertEquals(0, GitemberUtil.countWhiteCharFromStart(""));
        assertEquals(0, GitemberUtil.countWhiteCharFromStart("aaa"));
        assertEquals(1, GitemberUtil.countWhiteCharFromStart(" -"));
        assertEquals(10, GitemberUtil.countWhiteCharFromStart("          1"));
    }

    @Test
    void getFolderName() {
        assertEquals("", GitemberUtil.getFolderName(null));
        assertEquals("abc", GitemberUtil.getFolderName("abc"));
        assertEquals("abc", GitemberUtil.getFolderName("/abc"));
        assertEquals("abc", GitemberUtil.getFolderName("/abc/"));
        assertEquals("abc", GitemberUtil.getFolderName("/abc/.git"));
        assertEquals("abc", GitemberUtil.getFolderName("abc/.git"));
    }
}