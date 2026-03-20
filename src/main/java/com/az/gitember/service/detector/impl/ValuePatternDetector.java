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
public class ValuePatternDetector implements Detector {

    private final static List<PatternRule> rules = List.of(

            new PatternRule(
                    Pattern.compile("ghp_[A-Za-z0-9]{36}"),
                    "GitHub Personal Access Token",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("AKIA[0-9A-Z]{16}"),
                    "AWS Access Key",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("AIza[0-9A-Za-z\\-_]{35}"),
                    "Google API Key",
                    Confidence.HIGH
            ),

            new PatternRule(
                    Pattern.compile("[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+"),
                    "JWT Token",
                    Confidence.MEDIUM
            ),

            new PatternRule(
                    Pattern.compile("xox[baprs]-[0-9]{10,12}-[a-zA-Z0-9]{24}"),
                    "Slack Token",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("sk_live_[0-9a-zA-Z]{24}"),
                    "Stripe Secret Key",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("sq0csp-[0-9A-Za-z\\-_]{43}"),
                    "Square OAuth Secret",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("SG\\.[a-zA-Z0-9_-]{22}\\.[a-zA-Z0-9_-]{43}"),
                    "SendGrid API Key",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("glpat-[a-zA-Z0-9\\-_]{20}"),
                    "GitLab Personal Access Token",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("atlassian-token:[a-zA-Z0-9]{24}"),
                    "Atlassian API Token",
                    Confidence.HIGH
            ),

            new PatternRule(
                    Pattern.compile("pypi-AgEIcHlwaQAC[A-Za-z0-9-_]{50,100}"),
                    "PyPI API Token",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    Pattern.compile("(?i)xoxb-[0-9]{11}-[0-9]{11}-[a-z0-9]{24}"),
                    "Slack Bot Token",
                    Confidence.CRITICAL
            ),

            new PatternRule(
                    // Catches standard 64-character hex strings often used for private keys
                    Pattern.compile("\\b[a-fA-F0-0]{64}\\b"),
                    "Generic Hex Private Key",
                    Confidence.LOW // High false-positive rate, use with variable name checks
            ),

            new PatternRule(
                    // Look for URI encoded connection strings
                    Pattern.compile("mongodb(?:\\+srv)?://[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+:[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}/"),
                    "MongoDB Connection String",
                    Confidence.HIGH
            )
    );



    @Override
    public List<Finding> detect(ScanContext context) {
        List<Finding> findings = new ArrayList<>();
        for (int i = 0; i < context.getLines().size(); i++) {
            String line = context.getLines().get(i);
            for (PatternRule rule : rules) {
                Matcher m = rule.pattern().matcher(line);
                while (m.find()) {
                    findings.add(new Finding(
                            context.getSha(),
                            context.getFile(),
                            i + 1,
                            "SECRET",
                            rule.description(),
                            rule.confidence(),
                            line,
                            m.group()
                    ));
                }
            }
        }
        return findings;
    }

    @Override
    public String name() {
        return "ValuePatternDetector";
    }

    @Override
    public void setMultilingual(boolean multilingual) {

    }

}
