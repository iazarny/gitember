package com.az.gitember.service;

import com.az.gitember.data.LangDefinition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GitemberUtilTest {

    String definition = "[\n" +
            "  {\n" +
            "    \"name\": \"test\",\n" +
            "    \"extension\" : [\"test\", \"tst\"],\n" +
            "    \"keywords\" : [\"ababa\", \"galamaga\"]\n" +
            "  }\n" +
            "]";

    @Test
    void getKeywordsDefinition() {

        LangDefinition[] ldef = GitemberUtil.getKeywordsDefinition(definition);
        assertEquals(1, ldef.length);

    }

    @Test
    void topValues() {
        Map<String, Integer> mp = new HashMap<>() {{
           put("A", 101);
           put("B", 20);
           put("C", 30);
           put("D", 40);
           put("E", 50);
           put("F", 60);
           put("G", 70);
           put("H", 80);
           put("I", 90);
           put("K", 100);
        }};

        Map<String, Integer> rez = GitemberUtil.topValues(mp, 2);
        assertEquals(2, rez.size());
        assertEquals(100, rez.get("K"));
        assertEquals(101, rez.get("A"));


    }
}