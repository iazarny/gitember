package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;
import com.az.gitember.service.detector.Detector;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;

import java.util.*;

/**
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public class EntropyDetector implements Detector {

    private static final int MIN_LENGTH = 16;
    private static final double ENTROPY_THRESHOLD = 0.65;

    @Override
    public List<Finding> detect(ScanContext context) {
        List<Finding> findings = new ArrayList<>();

        /*List<String> lines = context.getLines();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!isLooksLikeCode(line)) {
                for (String token : tokenize(line)) {

                    if (!isCandidate(token)) continue;


                    double score = EntropyUtils.calculateEnhancedEntropy(token);

                    if (score < ENTROPY_THRESHOLD) continue;

                    findings.add(new Finding(
                            context.getSha(),
                            context.getFile(),
                            i + 1,
                            "HIGH_ENTROPY",
                            "High entropy string detected",
                            Confidence.MEDIUM,
                            line,
                            mask(token)
                    ));
                }
            }

        }*/
        return findings;
    }

    /**
     * Split line into meaningful tokens
     */
    private List<String> tokenize(String line) {
        return Arrays.asList(line.split("[\\s\"'=,:;(){}\\[\\]]+"));
    }

    /**
     * Pre-filter candidates
     */
    private boolean isCandidate(String token) {
        if (token.length() < MIN_LENGTH) return false;

        // must have at least 3 character classes
        if (charClasses(token) < 3) return false;

        // ignore obvious non-secrets
        String lower = token.toLowerCase();
        if (lower.contains("http") || lower.contains("https") || lower.contains("localhost")) return false;
        if (lower.contains("example") || lower.contains("dummy") || lower.contains("test")) return false;

        return true;
    }

    public  boolean isLooksLikeCode(String s) {
        if (s == null) {
            return false;
        }

        // Too short to be typical method chain + arguments
        if (s.length() < 15) {
            return false;
        }

        // Must contain at least one method call pattern: .something(
        if (!s.matches(".*\\.[a-zA-Z_][a-zA-Z0-9_]*\\s*\\(.*")) {
            return false;
        }

        // Java statements very often end with semicolon
        if (!s.trim().endsWith(";")) {
            return false;
        }

        // Count typical Java punctuation characters that appear a lot in code
        int punctCount = 0;
        for (char c : s.toCharArray()) {
            if (".()[];,".indexOf(c) != -1) {
                punctCount++;
            }
        }

        // Usually need quite a few of these in real method chains
        if (punctCount < 6) {
            return false;
        }

        // Should be printable (no weird control chars)
        if (!s.chars().allMatch(c -> c >= 32 && c <= 126)) {
            return false;
        }

        return true;
    }

    /**
     * Character diversity
     */
    private int charClasses(String s) {
        int score = 0;
        if (s.matches(".*[a-z].*")) score++;
        if (s.matches(".*[A-Z].*")) score++;
        if (s.matches(".*[0-9].*")) score++;
        if (s.matches(".*[^a-zA-Z0-9].*")) score++;
        return score;
    }

    /**
     * Mask sensitive value for output
     */
    private String mask(String s) {
        if (s.length() <= 8) return "****";
        return s.substring(0, 4) + "****" + s.substring(s.length() - 4);
    }

    @Override
    public String name() {
        return "HighEntropyDetector";
    }
}