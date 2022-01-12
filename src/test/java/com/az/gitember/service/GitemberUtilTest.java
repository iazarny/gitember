package com.az.gitember.service;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void getServer() throws MalformedURLException {
        assertEquals("https://gitlab.com", GitemberUtil.getServer("git@gitlab.com:azarny1/learn-gitlab.git"));
        assertEquals("http://www.gitgui.org", GitemberUtil.getServer("http://www.gitgui.org/ctx/project.git"));
    }

    @Test
    void lstContains() {
        List<String> lst = List.of("One", "Two");
        assertTrue(GitemberUtil.lstContains(lst, "oNe"));
        assertTrue(!GitemberUtil.lstContains(lst, "Three"));
    }
}