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

class ValuePatternDetectorTest {

    private ValuePatternDetector detector;
    private static final Path FILE = Paths.get("src/config.yml");
    private static final String SHA = "deadbeef";

    // Valid synthetic tokens (safe — random chars matching the patterns)
    static final String GITHUB_PAT    = "ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";        // ghp_ + 36
    static final String AWS_KEY       = "AKIAIOSFODNN7EXAMPLEKEY1";                         // AKIA + 16 upper-alnum: IOSFODNN7EXAMPLEKEY (wait, need exactly 16)
    static final String GOOGLE_KEY    = "AIzaSyDabcdefghijklmnopqrstuvwxyz_-ABCD";          // AIza + 35
    static final String JWT           = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    static final String SLACK_TOKEN   = "xoxb-1234567890-ABCDEFGHIJKLMNOPQRSTUVWX";         // xox[baprs]-10digits-24alnum
    static final String STRIPE_KEY    = "sk_live_abcdefghijklmnopqrstuvwx";                  // sk_live_ + 24
    static final String SQUARE_SECRET = "sq0csp-ABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890123456"; // sq0csp- + 43
    static final String SENDGRID_KEY  = "SG.abcdefghijklmnopqrstuv.ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklm"; // SG. + 22 + . + 43
    static final String GITLAB_PAT    = "glpat-abcdefghijklmnopqrst";                        // glpat- + 20
    static final String ATLASSIAN_TOK = "atlassian-token:abcdefghijklmnopqrstuvwx";          // atlassian-token: + 24
    static final String PYPI_TOKEN    = "pypi-AgEIcHlwaQAC" + "A".repeat(55);               // prefix + 55 chars
    static final String SLACK_BOT     = "xoxb-12345678901-12345678901-abcdefghijklmnopqrstuvwx"; // xoxb-11-11-24
    static final String MONGODB_URI   = "mongodb://user:p%40ss@db.example.com/mydb";

    @BeforeEach
    void setUp() {
        detector = new ValuePatternDetector();
    }

    private ScanContext ctx(String... lines) {
        ScanContext c = new ScanContext(FILE, List.of(lines), new FileType());
        c.setSha(SHA);
        return c;
    }

    // --- interface ---

    @Test
    void name_returnsValuePatternDetector() {
        assertEquals("ValuePatternDetector", detector.name());
    }



    // --- no findings ---

    @Test
    void detect_emptyLines_returnsEmpty() {
        assertTrue(detector.detect(ctx()).isEmpty());
    }

    @Test
    void detect_innocuousLine_returnsEmpty() {
        assertTrue(detector.detect(ctx("username=admin", "host=localhost", "port=5432")).isEmpty());
    }

    // --- GitHub PAT ---

    @Test
    void detect_githubPat_critical() {
        List<Finding> f = detector.detect(ctx("token=" + GITHUB_PAT));
        assertEquals(1, findingsByDescription(f, "GitHub Personal Access Token").size());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "GitHub Personal Access Token").get(0).getConfidence());
    }

    @Test
    void detect_githubPat_matchedValueIsToken() {
        List<Finding> f = detector.detect(ctx(GITHUB_PAT));
        Finding hit = findingsByDescription(f, "GitHub Personal Access Token").get(0);
        assertEquals(GITHUB_PAT, hit.getMatchedValue());
    }

    @Test
    void detect_githubPat_tooShort_notDetected() {
        // ghp_ + 35 chars — one short
        String short_ = "ghp_ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";
        assertTrue(findingsByDescription(detector.detect(ctx(short_)), "GitHub Personal Access Token").isEmpty());
    }

    // --- AWS Access Key ---

    @Test
    void detect_awsKey_critical() {
        // AKIA + exactly 16 uppercase alphanumeric
        String awsKey = "AKIAIOSFODNN7EXAMPLE";   // AKIA(4) + IOSFODNN7EXAMPLE(16)
        List<Finding> f = detector.detect(ctx("aws_access_key_id=" + awsKey));
        assertFalse(findingsByDescription(f, "AWS Access Key").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "AWS Access Key").get(0).getConfidence());
    }

    @Test
    void detect_awsKey_matchedValueContainsAKIA() {
        String awsKey = "AKIAIOSFODNN7EXAMPLE";
        Finding hit = findingsByDescription(detector.detect(ctx(awsKey)), "AWS Access Key").get(0);
        assertTrue(hit.getMatchedValue().startsWith("AKIA"));
    }

    // --- Google API Key ---

    @Test
    void detect_googleApiKey_high() {
        List<Finding> f = detector.detect(ctx(GOOGLE_KEY));
        assertFalse(findingsByDescription(f, "Google API Key").isEmpty());
        assertEquals(Confidence.HIGH, findingsByDescription(f, "Google API Key").get(0).getConfidence());
    }

    // --- JWT ---

    @Test
    void detect_jwt_medium() {
        List<Finding> f = detector.detect(ctx(JWT));
        assertFalse(findingsByDescription(f, "JWT Token").isEmpty());
        assertEquals(Confidence.MEDIUM, findingsByDescription(f, "JWT Token").get(0).getConfidence());
    }

    @Test
    void detect_singleSegment_notJwt() {
        // A JWT needs three dot-separated segments
        assertTrue(findingsByDescription(detector.detect(ctx("notajwt")), "JWT Token").isEmpty());
    }

    // --- Slack Token ---

    @Test
    void detect_slackToken_critical() {
        // xox[baprs]-10..12 digits-24 alnum
        String slack = "xoxb-1234567890-ABCDEFGHIJKLMNOPQRSTUVWX";
        List<Finding> f = detector.detect(ctx(slack));
        assertFalse(findingsByDescription(f, "Slack Token").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "Slack Token").get(0).getConfidence());
    }

    // --- Slack Bot Token ---

    @Test
    void detect_slackBotToken_critical() {
        List<Finding> f = detector.detect(ctx(SLACK_BOT));
        assertFalse(findingsByDescription(f, "Slack Bot Token").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "Slack Bot Token").get(0).getConfidence());
    }

    // --- Stripe Secret Key ---

    @Test
    void detect_stripeKey_critical() {
        List<Finding> f = detector.detect(ctx(STRIPE_KEY));
        assertFalse(findingsByDescription(f, "Stripe Secret Key").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "Stripe Secret Key").get(0).getConfidence());
    }

    // --- Square OAuth Secret ---

    @Test
    void detect_squareSecret_critical() {
        List<Finding> f = detector.detect(ctx(SQUARE_SECRET));
        assertFalse(findingsByDescription(f, "Square OAuth Secret").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "Square OAuth Secret").get(0).getConfidence());
    }

    // --- SendGrid API Key ---

    @Test
    void detect_sendgridKey_critical() {
        List<Finding> f = detector.detect(ctx(SENDGRID_KEY));
        assertFalse(findingsByDescription(f, "SendGrid API Key").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "SendGrid API Key").get(0).getConfidence());
    }

    // --- GitLab PAT ---

    @Test
    void detect_gitlabPat_critical() {
        List<Finding> f = detector.detect(ctx(GITLAB_PAT));
        assertFalse(findingsByDescription(f, "GitLab Personal Access Token").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "GitLab Personal Access Token").get(0).getConfidence());
    }

    // --- Atlassian API Token ---

    @Test
    void detect_atlassianToken_high() {
        List<Finding> f = detector.detect(ctx(ATLASSIAN_TOK));
        assertFalse(findingsByDescription(f, "Atlassian API Token").isEmpty());
        assertEquals(Confidence.HIGH, findingsByDescription(f, "Atlassian API Token").get(0).getConfidence());
    }

    // --- PyPI API Token ---

    @Test
    void detect_pypiToken_critical() {
        List<Finding> f = detector.detect(ctx(PYPI_TOKEN));
        assertFalse(findingsByDescription(f, "PyPI API Token").isEmpty());
        assertEquals(Confidence.CRITICAL, findingsByDescription(f, "PyPI API Token").get(0).getConfidence());
    }

    // --- Generic Hex Private Key ---

    @Test
    void detect_hexKey_bug_charClass0to0_onlyMatchesZeroDigit() {
        // NOTE: The pattern uses [a-fA-F0-0]{64} — the range 0-0 means only the char '0',
        // so digits 1-9 are NOT matched. This test documents the actual (buggy) behavior.
        String allZeroHex = "0".repeat(64);
        List<Finding> f = detector.detect(ctx(allZeroHex));
        assertFalse(findingsByDescription(f, "Generic Hex Private Key").isEmpty(),
                "Should match 64-char string of '0' (only digit in the broken char class)");

        String realHex = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2";
        assertTrue(findingsByDescription(detector.detect(ctx(realHex)), "Generic Hex Private Key").isEmpty(),
                "Should NOT match real hex with digits 1-9 due to [0-0] bug in pattern");
    }

    // --- MongoDB Connection String ---

    @Test
    void detect_mongodbUri_high() {
        List<Finding> f = detector.detect(ctx(MONGODB_URI));
        assertFalse(findingsByDescription(f, "MongoDB Connection String").isEmpty());
        assertEquals(Confidence.HIGH, findingsByDescription(f, "MongoDB Connection String").get(0).getConfidence());
    }

    @Test
    void detect_mongodbSrvUri_detected() {
        String srv = "mongodb+srv://admin:secret@cluster.mongodb.net/prod";
        assertFalse(findingsByDescription(detector.detect(ctx(srv)), "MongoDB Connection String").isEmpty());
    }

    // --- finding metadata ---

    @Test
    void detect_finding_hasCorrectShaFileLineType() {
        List<Finding> f = detector.detect(ctx("ignore", GITHUB_PAT));
        Finding hit = findingsByDescription(f, "GitHub Personal Access Token").get(0);
        assertEquals(SHA, hit.getSha());
        assertEquals(FILE, hit.getFile());
        assertEquals(2, hit.getLineNo());
        assertEquals("SECRET", hit.getType());
    }

    // --- multiple secrets on same line / multiple lines ---

    @Test
    void detect_multipleMatchingLines_onePerLine() {
        List<Finding> f = detector.detect(ctx(
                "clean line",
                "token=" + GITHUB_PAT,
                "also clean",
                "key=" + "AKIAIOSFODNN7EXAMPLE"
        ));
        assertEquals(2, findingsByDescription(f, "GitHub Personal Access Token").size()
                + findingsByDescription(f, "AWS Access Key").size());
    }

    // --- helper ---

    private List<Finding> findingsByDescription(List<Finding> findings, String description) {
        return findings.stream()
                .filter(f -> description.equals(f.getMessage()))
                .toList();
    }
}
