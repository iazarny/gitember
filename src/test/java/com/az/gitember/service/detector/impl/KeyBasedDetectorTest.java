package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;
import com.az.gitember.service.detector.FileType;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeyBasedDetectorTest {

    private KeyBasedDetector detector;
    private static final Path FILE = Paths.get("config.properties");
    private static final String SHA = "abc123";

    @BeforeEach
    void setUp() {
        detector = new KeyBasedDetector();
    }

    private ScanContext context(String... lines) {
        ScanContext ctx = new ScanContext(FILE, List.of(lines), new FileType());
        ctx.setSha(SHA);
        return ctx;
    }

    // --- name / interface ---

    @Test
    void name_returnsKeyBasedDetector() {
        assertEquals("KeyBasedDetector", detector.name());
    }

    // --- no findings ---

    @Test
    void detect_emptyLines_returnsEmpty() {
        assertTrue(detector.detect(context()).isEmpty());
    }

    @Test
    void detect_innocuousLine_returnsEmpty() {
        assertTrue(detector.detect(context("username=admin", "host=localhost")).isEmpty());
    }

    // --- English keywords ---

    @ParameterizedTest
    @ValueSource(strings = {
        "password=secret123",
        "PASSWORD=secret123",
        "passwd=hunter2",
        "passcode=1234",
        "passphrase=my long phrase",
        "secret=abc",
        "token=eyJhbGciOiJIUzI1NiJ9",
        "api_key=abc123",
        "api-key=abc123",
        "apikey=abc123",
        "client_secret=xyz",
        "client-secret=xyz",
        "auth_code=111",
        "auth-code=111",
        "credential=mypass",
        "access_key=AKIAIOSFODNN7",
        "access-key=AKIAIOSFODNN7",
        "watchword=abc",
        "cipher=aes256",
        "pwd=secret",
    })
    void detect_englishKeyword_returnsOneFinding(String line) {
        List<Finding> findings = detector.detect(context(line));
        assertEquals(1, findings.size(), "Expected finding for: " + line);
        Finding f = findings.get(0);
        assertEquals(SHA, f.getSha());
        assertEquals(FILE, f.getFile());
        assertEquals(1, f.getLineNo());
        assertEquals("KEY_MATCH", f.getType());
        Assertions.assertEquals(Confidence.MEDIUM, f.getConfidence());
        assertEquals(line, f.getLine());
        assertNotNull(f.getMatchedValue());
        assertFalse(f.getMatchedValue().isBlank());
    }

    @Test
    void detect_multipleMatchingLines_returnsOnePerLine() {
        List<Finding> findings = detector.detect(context(
                "username=admin",
                "password=secret",
                "host=localhost",
                "token=abc"
        ));
        assertEquals(2, findings.size());
        assertEquals(2, findings.get(0).getLineNo());
        assertEquals(4, findings.get(1).getLineNo());
    }

    @Test
    void detect_matchedValueCapturesKeyword() {
        List<Finding> findings = detector.detect(context("my_token=abc123"));
        assertEquals(1, findings.size());
        assertEquals("token", findings.get(0).getMatchedValue().toLowerCase());
    }

    // --- case insensitivity ---

    @Test
    void detect_uppercaseKeyword_isDetected() {
        assertFalse(detector.detect(context("TOKEN=abc")).isEmpty());
    }

    @Test
    void detect_mixedCaseKeyword_isDetected() {
        assertFalse(detector.detect(context("Secret=abc")).isEmpty());
    }

    // --- multilingual OFF by default ---

    @Test
    void detect_germanKeyword_notDetectedByDefault() {
        // "kennwort" has no overlap with English keywords
        assertTrue(detector.detect(context("kennwort=geheim")).isEmpty());
    }

    @Test
    void detect_ukrainianKeyword_notDetectedByDefault() {
        assertTrue(detector.detect(context("пароль=geheim")).isEmpty());
    }

    // --- multilingual ON ---

    @ParameterizedTest
    @ValueSource(strings = {
        "passwort=geheim",
        "kennwort=geheim",
        "geheimnis=abc",
        "schluessel=abc",
        "zugangscode=abc",
        "berechtigungsnachweis=abc",
    })
    void detect_germanKeyword_detectedWhenMultilingual(String line) {
        assertFalse(detector.detect(context(line)).isEmpty(), "Expected finding for: " + line);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "contrasena=abc",
        "clave=abc",
        "secreto=abc",
        "ficha=abc",
        "credencial=abc",
    })
    void detect_spanishKeyword_detectedWhenMultilingual(String line) {
        assertFalse(detector.detect(context(line)).isEmpty(), "Expected finding for: " + line);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "mot_de_passe=abc",
        "jeton=abc",
        "identifiant=abc",
    })
    void detect_frenchKeyword_detectedWhenMultilingual(String line) {
        assertFalse(detector.detect(context(line)).isEmpty(), "Expected finding for: " + line);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "parola_chiave=abc",
        "segreto=abc",
        "gettone=abc",
        "credenziali=abc",
    })
    void detect_italianKeyword_detectedWhenMultilingual(String line) {
        assertFalse(detector.detect(context(line)).isEmpty(), "Expected finding for: " + line);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "haslo=abc",
        "sekret=abc",
        "klucz=abc",
        "uwierzytelnienie=abc",
        "poswiadczenie=abc",
    })
    void detect_polishKeyword_detectedWhenMultilingual(String line) {
        assertFalse(detector.detect(context(line)).isEmpty(), "Expected finding for: " + line);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "пароль=abc",
        "ключ=abc",
        "секрет=abc",
        "токен=abc",
        "шифр=abc",
        "гасло=abc",
    })
    void detect_ukrainianKeyword_detectedWhenMultilingual(String line) {
        assertFalse(detector.detect(context(line)).isEmpty(), "Expected finding for: " + line);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "parol=abc",
        "kliuch=abc",
        "klyuch=abc",
        "sekret=abc",
        "shyfr=abc",
        "haslo=abc",
    })
    void detect_ukrainianTranslitKeyword_detectedWhenMultilingual(String line) {
        assertFalse(detector.detect(context(line)).isEmpty(), "Expected finding for: " + line);
    }

    // --- token is shared across multiple patterns, should not duplicate on EN-only ---

    @Test
    void detect_tokenKeyword_monolingual_exactlyOneFinding() {
        List<Finding> findings = detector.detect(context("token=abc"));
        assertEquals(1, findings.size());
    }

    // --- finding fields ---

    @Test
    void detect_findingHasCorrectMessage() {
        List<Finding> findings = detector.detect(context("password=abc"));
        assertEquals("Suspicious key detected", findings.get(0).getMessage());
    }
}
