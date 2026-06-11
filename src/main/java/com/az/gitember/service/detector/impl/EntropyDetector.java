package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;
import com.az.gitember.service.detector.Detector;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public class EntropyDetector implements Detector {

    private static final int    MIN_LENGTH        = 12;
    private static final double ENTROPY_THRESHOLD = 0.65;

    /**
     * Extracts the content of double- or single-quoted string literals of at least MIN_LENGTH chars.
     * Group 1 = double-quoted content, group 2 = single-quoted content.
     */
    private static final Pattern QUOTED = Pattern.compile(
            "\"([^\"]{" + MIN_LENGTH + ",})\"|'([^']{" + MIN_LENGTH + ",})'"
    );

    /**
     * Matches config/property-style lines: key = value  or  key: value.
     * Only considered for lines without parentheses or braces (i.e. not code).
     * Group 1 = the value part (must be at least MIN_LENGTH chars long).
     */
    private static final Pattern CONFIG_VALUE = Pattern.compile(
            "^[a-zA-Z0-9._\\-]+\\s*[:=]\\s*([^\\s#\"'$][^\\s#]{" + (MIN_LENGTH - 1) + ",})"
    );

    @Override
    public List<Finding> detect(ScanContext context) {
        List<Finding> findings = new ArrayList<>();
        List<String> lines = context.getLines();

        for (int i = 0; i < lines.size(); i++) {
            String line    = lines.get(i);
            String trimmed = line.trim();

            if (trimmed.isEmpty()
                    || trimmed.startsWith("//")
                    || trimmed.startsWith("#")
                    || trimmed.startsWith("*")
                    || trimmed.startsWith("/*")) continue;

            // Only look inside quoted string literals — code identifiers are never quoted.
            Matcher m = QUOTED.matcher(line);
            while (m.find()) {
                String literal = m.group(1) != null ? m.group(1) : m.group(2);
                checkValue(literal, i + 1, line, context, findings);
            }

            // For config-style lines (no parens/braces → not code), also check unquoted values.
            if (!trimmed.contains("(") && !trimmed.contains("{")) {
                Matcher cm = CONFIG_VALUE.matcher(trimmed);
                if (cm.find()) {
                    checkValue(cm.group(1), i + 1, line, context, findings);
                }
            }
        }
        return findings;
    }

    private void checkValue(String value, int lineNo, String line,
                             ScanContext context, List<Finding> findings) {
        if (!isCandidate(value)) return;
        if (EntropyUtils.calculateEnhancedEntropy(value) < ENTROPY_THRESHOLD) return;
        findings.add(new Finding(
                context.getSha(),
                context.getFile(),
                lineNo,
                "HIGH_ENTROPY",
                "High entropy string detected",
                Confidence.MEDIUM,
                line,
                mask(value)
        ));
    }

    private boolean isCandidate(String token) {
        if (token.length() < MIN_LENGTH) return false;
        if (charClasses(token) < 3) return false;

        String lower = token.toLowerCase();
        if (lower.contains("http") || lower.contains("localhost")) return false;
        if (lower.contains("example") || lower.contains("dummy")
                || lower.contains("test") || lower.contains("changeme")
                || lower.contains("placeholder") || lower.contains("your_")) return false;

        // UUIDs: 8-4-4-4-12
        if (token.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}"
                + "-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) return false;

        // Fully-qualified class / package names (multiple dot-separated segments)
        if (token.matches("[a-z][a-z0-9]*(\\.[A-Za-z][a-zA-Z0-9]*){2,}")) return false;

        // File paths
        if (token.startsWith("/") || token.startsWith("\\")
                || (token.length() > 2 && token.charAt(1) == ':')) return false;

        return true;
    }

    private int charClasses(String s) {
        int score = 0;
        if (s.matches(".*[a-z].*")) score++;
        if (s.matches(".*[A-Z].*")) score++;
        if (s.matches(".*[0-9].*")) score++;
        if (s.matches(".*[^a-zA-Z0-9].*")) score++;
        return score;
    }

    private String mask(String s) {
        if (s.length() <= 8) return "****";
        return s.substring(0, 4) + "****" + s.substring(s.length() - 4);
    }

    @Override
    public String name() {
        return "HighEntropyDetector";
    }
}
