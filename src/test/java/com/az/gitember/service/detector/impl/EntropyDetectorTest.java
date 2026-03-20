package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;
import com.az.gitember.service.detector.FileType;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntropyDetectorTest {

    // 20 unique chars → entropy = log2(20) ≈ 4.32 — well above threshold 3.5
    static final String HIGH_ENTROPY_20 = "K7x9mP2nQ8vR5tY3uW6s";
    // 22 unique chars → entropy ≈ 4.46
    static final String HIGH_ENTROPY_22 = "K7x9mP2nQ8vR5tY3uW6sZ1";
    // 20 chars, only 4 distinct → entropy = 2.0 — below threshold
    static final String LOW_ENTROPY_20  = "aaaabbbbccccddddeeee";
    // 20 identical chars → entropy = 0
    static final String ZERO_ENTROPY_20 = "aaaaaaaaaaaaaaaaaaaa";
    // 9 unique chars — well below the 20-char minimum length gate
    static final String HIGH_ENTROPY_9 = "K7x9mP2nQ";

    private EntropyDetector detector;
    private static final Path FILE = Paths.get("src/main/Secret.java");
    private static final String SHA  = "cafebabe";

    @BeforeEach
    void setUp() {
        detector = new EntropyDetector();
    }

    private ScanContext ctx(String... lines) {
        ScanContext c = new ScanContext(FILE, List.of(lines), new FileType());
        c.setSha(SHA);
        return c;
    }

    // --- interface ---

    @Test
    void name_returnsEntropyDetector() {
        assertEquals("EntropyDetector", detector.name());
    }

    @Test
    void setMultilingual_doesNotThrow() {
        assertDoesNotThrow(() -> detector.setMultilingual(true));
        assertDoesNotThrow(() -> detector.setMultilingual(false));
    }

    // --- no findings ---

    @Test
    void detect_emptyInput_returnsEmpty() {
        assertTrue(detector.detect(ctx()).isEmpty());
    }

    @Test
    void detect_shortTokensOnly_returnsEmpty() {
        // All tokens under 20 chars, even with high entropy
        assertTrue(detector.detect(ctx("hello world foo=bar")).isEmpty());
    }

    @Test
    void detect_lowEntropyToken_returnsEmpty() {
        assertTrue(detector.detect(ctx(LOW_ENTROPY_20)).isEmpty());
    }

    @Test
    void detect_zeroEntropyToken_returnsEmpty() {
        assertTrue(detector.detect(ctx(ZERO_ENTROPY_20)).isEmpty());
    }

    // --- length gate ---

    @Test
    void detect_token9Chars_skippedEvenIfHighEntropy() {
        // 9-char token with 9 unique chars → very high entropy but < 20 chars
        assertTrue(detector.detect(ctx(HIGH_ENTROPY_9)).isEmpty());
    }

    @Test
    void detect_token20Chars_passesLengthGate() {
        assertFalse(detector.detect(ctx(HIGH_ENTROPY_20)).isEmpty());
    }

    // --- high entropy detected ---

    @Test
    void detect_highEntropyToken_returnsOneFinding() {
        List<Finding> findings = detector.detect(ctx(HIGH_ENTROPY_20));
        assertEquals(1, findings.size());
    }

    @Test
    void detect_finding_hasCorrectFields() {
        List<Finding> findings = detector.detect(ctx(HIGH_ENTROPY_20));
        Finding f = findings.get(0);
        assertEquals(SHA, f.getSha());
        assertEquals(FILE, f.getFile());
        assertEquals(1, f.getLineNo());
        assertEquals("HIGH_ENTROPY", f.getType());
        assertEquals("High entropy string", f.getMessage());
        assertEquals(Confidence.LOW, f.getConfidence());
        assertEquals(HIGH_ENTROPY_20, f.getLine());
        assertEquals(HIGH_ENTROPY_20, f.getMatchedValue());
    }

    // --- token splitting ---

    @Test
    void detect_highEntropyTokenEmbeddedInLine_found() {
        String line = "config.secret=" + HIGH_ENTROPY_22 + " # set at runtime";
        List<Finding> findings = detector.detect(ctx(line));
        assertTrue(findings.stream().anyMatch(f -> f.getMatchedValue().equals("config.secret=" + HIGH_ENTROPY_22)),
                "Token including the key prefix should be matched as one whitespace-split token");
    }

    @Test
    void detect_twoHighEntropyTokensOnOneLine_twoFindings() {
        String line = HIGH_ENTROPY_20 + " " + HIGH_ENTROPY_22;
        List<Finding> findings = detector.detect(ctx(line));
        assertEquals(2, findings.size());
    }

    @Test
    void detect_tabSeparatedTokens_eachAssessedIndependently() {
        String line = LOW_ENTROPY_20 + "\t" + HIGH_ENTROPY_20;
        List<Finding> findings = detector.detect(ctx(line));
        assertEquals(1, findings.size());
        assertEquals(HIGH_ENTROPY_20, findings.get(0).getMatchedValue());
    }

    // --- multi-line ---

    @Test
    void detect_correctLineNumbers() {
        List<Finding> findings = detector.detect(ctx(
                "innocuous line",
                HIGH_ENTROPY_20,
                "another clean line",
                HIGH_ENTROPY_22
        ));
        assertEquals(2, findings.size());
        assertEquals(2, findings.get(0).getLineNo());
        assertEquals(4, findings.get(1).getLineNo());
    }

    @Test
    void detect_onlyMatchingLinesProduceFindings() {
        List<Finding> findings = detector.detect(ctx(
                LOW_ENTROPY_20,
                ZERO_ENTROPY_20,
                "short",
                HIGH_ENTROPY_20
        ));
        assertEquals(1, findings.size());
        assertEquals(4, findings.get(0).getLineNo());
    }
}
