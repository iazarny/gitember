package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;
import com.az.gitember.service.detector.Detector;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public class EntropyDetector implements Detector {

    private final double threshold = 3.5;

    //public EntropyDetector(double threshold) {
   //     this.threshold = threshold;
   // }

    @Override
    public List<Finding> detect(ScanContext context) {
        List<Finding> findings = new ArrayList<>();

        for (int i = 0; i < context.getLines().size(); i++) {
            String line = context.getLines().get(i);

            for (String token : line.split("\\s+")) {
                if (token.length() < 8) continue;

                double entropy = calculateEntropy(token);

                if (entropy > threshold) {
                    findings.add(new Finding(
                            context.getSha(),
                            context.getFile(),
                            i + 1,
                            "HIGH_ENTROPY",
                            "High entropy string",
                            Confidence.LOW,
                            line,
                            token
                    ));
                }
            }
        }

        return findings;
    }

    private double calculateEntropy(String s) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char c : s.toCharArray()) {
            frequencies.put(c, frequencies.getOrDefault(c, 0) + 1);
        }
        double entropy = 0;
        for (int count : frequencies.values()) {
            double p = (double) count / s.length();
            entropy -= p * (Math.log(p) / Math.log(2));
        }
        return entropy;
    }

    @Override
    public String name() {
        return "EntropyDetector";
    }

    @Override
    public void setMultilingual(boolean multilingual) {

    }
}