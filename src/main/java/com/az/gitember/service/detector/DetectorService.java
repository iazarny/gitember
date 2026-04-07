package com.az.gitember.service.detector;

import com.az.gitember.service.detector.impl.ConnectionStringDetector;
import com.az.gitember.service.detector.impl.EntropyDetector;
import com.az.gitember.service.detector.impl.KeyBasedDetector;
import com.az.gitember.service.detector.impl.LlmSecretDetector;
import com.az.gitember.service.detector.impl.ValuePatternDetector;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates secret-detection across staged files.
 *
 * <p>Two modes:</p>
 * <ul>
 *   <li><b>Empirical</b> (default constructor) – uses regex / entropy heuristics only.
 *       Fast, offline, but higher false-positive rate.</li>
 *   <li><b>LLM-based</b> ({@link #DetectorService(String, String)}) – {@link ValuePatternDetector}
 *       for well-known token patterns plus {@link LlmSecretDetector} which sends file content
 *       to a local Ollama model. More accurate; requires Ollama to be running.</li>
 * </ul>
 */
public class DetectorService {

    private final List<Detector> detectors;

    /** Empirical-only mode — no Ollama required. */
    public DetectorService() {
        this.detectors = new ArrayList<>();
        this.detectors.add(new ValuePatternDetector());
        this.detectors.add(new KeyBasedDetector());
        this.detectors.add(new EntropyDetector());
        this.detectors.add(new ConnectionStringDetector());
    }

    /**
     * LLM-based mode — uses {@link ValuePatternDetector} for known token formats
     * plus an {@link LlmSecretDetector} that queries the Ollama model for everything else.
     *
     * @param ollamaBaseUrl base URL of the Ollama server (e.g. {@code "http://localhost:11434"})
     * @param modelName     model to use (e.g. {@code "llama3.2"})
     */
    public DetectorService(String ollamaBaseUrl, String modelName) {
        this.detectors = new ArrayList<>();
        this.detectors.add(new ValuePatternDetector());
        this.detectors.add(new LlmSecretDetector(ollamaBaseUrl, modelName));
    }

    public List<Finding> detect(ScanContext context) {
        List<Finding> results = new ArrayList<>();
        for (Detector detector : detectors) {
            results.addAll(detector.detect(context));
        }
        return results;
    }
}
