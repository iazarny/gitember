package com.az.gitember.service.detector.impl;

import java.util.regex.Pattern;

public class PasswordDetector {

    private static final Pattern PASSWORD_LIKE = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d|.*\\W).{12,}$"
    );

    private static final Pattern SENSITIVE_KEY = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|api[_-]?key)"
    );

    public static boolean isPassword(String key, String value) {
        if (value == null || value.length() < 8) return false;

        key = key == null ? "" : key;

        boolean keySuspicious = SENSITIVE_KEY.matcher(key).find();
        boolean structure = PASSWORD_LIKE.matcher(value).matches();
        double entropy = EntropyUtils.calculateEnhancedEntropy(value);
        int classes = charClasses(value);

        // 🔥 Decision logic (tuned)
        if (keySuspicious && value.length() >= 8) return true;

        if (structure && entropy > 0.6) return true;

        if (entropy > 0.75 && value.length() >= 16 && classes >= 3) return true;

        return false;
    }

    private static int charClasses(String s) {
        int score = 0;
        if (s.matches(".*[a-z].*")) score++;
        if (s.matches(".*[A-Z].*")) score++;
        if (s.matches(".*[0-9].*")) score++;
        if (s.matches(".*[^a-zA-Z0-9].*")) score++;
        return score;
    }
}