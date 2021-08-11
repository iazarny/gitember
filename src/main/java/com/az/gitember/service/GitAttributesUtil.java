package com.az.gitember.service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Working with git attributes to support large files.
 * See https://git-scm.com/docs/gitattributes
 */
public class GitAttributesUtil {


    public static List<String> parseGitAttributes(String str, boolean lfsOnly) {
        List<String> lines = GitemberUtil.getLines(str).stream()
                .filter(l -> !lfsOnly || l.toLowerCase(Locale.ROOT).contains("lfs"))
                .collect(Collectors.toList());
        return lines;
    }

    public static List<String> getLsfPatters(String str) {
        List<String> lfsPatterns = parseGitAttributes(str, true).stream()
                .map( l -> l.substring(0, l.indexOf(" ")))
                .collect(Collectors.toList());
        return  lfsPatterns;
    }

    public static String addLfsPattern(String attrs, String pattern) {

        List<String> allPatterns = getLsfPatters(attrs);
        if (!allPatterns.contains(pattern)) {
            List<String> lines = GitemberUtil.getLines(attrs);
            lines.add(pattern + " filter=lfs diff=lfs merge=lfs -text");
            return lines.stream().collect(Collectors.joining("\n"));
        }
        return attrs;

    }

    public static String removeLfsPattern(String attrs, String pattern) {
        List<String> lines = parseGitAttributes(attrs, false);
        lines.removeIf( s -> s.startsWith(pattern) );
        return lines.stream().collect(Collectors.joining("\n"));
    }

}
