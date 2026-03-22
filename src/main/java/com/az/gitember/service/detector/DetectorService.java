package com.az.gitember.service.detector;

import com.az.gitember.service.detector.impl.ConnectionStringDetector;
import com.az.gitember.service.detector.impl.EntropyDetector;
import com.az.gitember.service.detector.impl.KeyBasedDetector;
import com.az.gitember.service.detector.impl.ValuePatternDetector;

import java.util.ArrayList;
import java.util.List;

public class DetectorService {

    private final List<Detector> detectors;

    public DetectorService() {
        this.detectors = new ArrayList<>();
        this.detectors.add(
                new ValuePatternDetector()
        );
        this.detectors.add(
                new KeyBasedDetector()
        );
        this.detectors.add(
                new EntropyDetector()
        );
        this.detectors.add(
                new ConnectionStringDetector()
        );
    }

    public List<Finding> detect(ScanContext context) {
        List<Finding> detections = new ArrayList<>();
        for (Detector detector : detectors) {
            detections.addAll(detector.detect(context));
        }
        return detections;
    }



}
