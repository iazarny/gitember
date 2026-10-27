package com.az.gitember.service.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects tracker keys such as {@code PAY-123} in commit messages and branch names.
 */
public final class IssueKeyDetector {

    private static final Pattern KEY = Pattern.compile("\\b([A-Z][A-Z0-9]{1,9}-\\d+)\\b");

    private IssueKeyDetector() {
    }

    public static String findFirst(String text) {
        String key = null;
        List<String> keys = findAll(text);
        if (!keys.isEmpty()) {
            key = keys.get(0);
        }
        return key;
    }

    public static List<String> findAll(String text) {
        List<String> keys = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            Matcher matcher = KEY.matcher(text);
            while (matcher.find()) {
                String key = matcher.group(1);
                if (!keys.contains(key)) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    public static boolean contains(String text, String key) {
        boolean found = false;
        if (text != null && key != null && !key.isBlank()) {
            found = text.contains(key);
        }
        return found;
    }
}
