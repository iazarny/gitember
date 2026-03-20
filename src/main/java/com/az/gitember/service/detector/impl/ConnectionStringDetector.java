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
public class ConnectionStringDetector implements Detector {

    private static final Pattern CONN =
            Pattern.compile("://[^\\s:]+:[^\\s@]+@");

    @Override
    public List<Finding> detect(ScanContext context) {
        List<Finding> findings = new ArrayList<>();

        for (int i = 0; i < context.getLines().size(); i++) {
            String line = context.getLines().get(i);

            Matcher matcher = CONN.matcher(line);

            if (matcher.find()) {
                findings.add(new Finding(
                        context.getSha(),
                        context.getFile(),
                        i + 1,
                        "CONNECTION_STRING",
                        "Credentials inside connection string",
                        Confidence.HIGH,
                        line,
                        matcher.group(0)
                ));
            }
        }

        return findings;
    }

    @Override
    public String name() {
        return "ConnectionStringDetector";
    }

    @Override
    public void setMultilingual(boolean multilingual) {

    }
}