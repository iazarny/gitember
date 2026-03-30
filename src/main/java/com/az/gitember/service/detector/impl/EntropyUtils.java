package com.az.gitember.service.detector.impl;

public class EntropyUtils {

    public static double calculateEnhancedEntropy(String s) {
        if (s == null || s.isEmpty()) return 0.0;

        double shannon = shannonEntropy(s);
        double normalized = normalize(shannon, s);
        double diversity = charClassScore(s);
        double repetitionPenalty = repetitionPenalty(s);

        // weighted score
        return (normalized * 0.5)
                + (diversity * 0.3)
                - (repetitionPenalty * 0.2);
    }

    /**
     * Original Shannon entropy (unchanged, but optimized)
     */
    private static double shannonEntropy(String s) {
        int len = s.length();
        int[] freq = new int[128]; // ASCII fast path

        for (char c : s.toCharArray()) {
            if (c < 128) {
                freq[c]++;
            }
        }

        double entropy = 0.0;

        for (int count : freq) {
            if (count == 0) continue;

            double p = (double) count / len;
            entropy -= p * (Math.log(p) / Math.log(2));
        }

        return entropy;
    }

    /**
     * Normalize entropy to [0..1] range
     */
    private static double normalize(double entropy, String s) {
        int uniqueChars = (int) s.chars().distinct().count();

        if (uniqueChars <= 1) return 0;

        double maxEntropy = Math.log(uniqueChars) / Math.log(2);
        return entropy / maxEntropy;
    }

    /**
     * Character class diversity (0..1)
     */
    private static double charClassScore(String s) {
        int classes = 0;

        if (s.matches(".*[a-z].*")) classes++;
        if (s.matches(".*[A-Z].*")) classes++;
        if (s.matches(".*[0-9].*")) classes++;
        if (s.matches(".*[^a-zA-Z0-9].*")) classes++;

        return classes / 4.0;
    }

    /**
     * Penalize repetition patterns like "aaaa", "abcabc"
     */
    private static double repetitionPenalty(String s) {
        int repeats = 0;

        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) {
                repeats++;
            }
        }

        return (double) repeats / s.length();
    }
}