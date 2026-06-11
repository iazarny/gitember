package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;
import com.az.gitember.service.detector.Detector;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Secret detector that uses a locally-running Ollama LLM to identify
 * hardcoded credentials, API keys, passwords and other sensitive data.
 *
 * <p>This detector replaces the empirical heuristic detectors (KeyBased,
 * Entropy, ConnectionString) with a language-model approach that understands
 * context and is less prone to false positives from variable-naming alone.</p>
 *
 * <p>The model is prompted to return a JSON array of findings; the response
 * is extracted and parsed with Jackson. Malformed or empty responses
 * silently yield an empty finding list.</p>
 */
public class LlmSecretDetector implements Detector {

    private static final Logger log = Logger.getLogger(LlmSecretDetector.class.getName());

    /** Maximum lines fed to the model per file (~6 k tokens at ~20 chars/line).
     *  Only HIGH and CRITICAL confidence findings are surfaced; LOW/MEDIUM are discarded. */
    private static final int MAX_LINES = 300;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OllamaChatModel model;

    public LlmSecretDetector(String baseUrl, String modelName) {
        this.model = OllamaChatModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(3))
                .build();
    }

    @Override
    public String name() {
        return "LLM Secret Detector";
    }

    @Override
    public List<Finding> detect(ScanContext context) {
        List<String> lines = context.getLines();
        if (lines == null || lines.isEmpty()) return Collections.emptyList();

        List<String> input = lines.size() > MAX_LINES ? lines.subList(0, MAX_LINES) : lines;
        String prompt = buildPrompt(context.getFile().getFileName().toString(), input);

        String response;
        try {
            response = model.chat(prompt);
        } catch (Exception e) {
            log.log(Level.WARNING, "LLM secret scan failed for " + context.getFile(), e);
            return Collections.emptyList();
        }

        return parseResponse(response, context);
    }

    // -------------------------------------------------------------------------

    private static String buildPrompt(String fileName, List<String> lines) {
        StringBuilder sb = new StringBuilder(512 + lines.size() * 80);
        sb.append("You are a security code scanner. Analyze the source file below for hardcoded secrets.\n\n");
        sb.append("File: ").append(fileName).append("\n```\n");
        for (int i = 0; i < lines.size(); i++) {
            sb.append(i + 1).append(": ").append(lines.get(i)).append('\n');
        }
        sb.append("```\n\n");
        sb.append("""
Return ONLY a JSON array. Each element must have exactly these fields:
  "lineNo"       : integer (1-based line number)
  "type"         : one of SECRET, TOKEN, API_KEY, PASSWORD, CREDENTIAL, CONNECTION_STRING
  "confidence"   : one of HIGH, CRITICAL
  "description"  : short description (max 80 chars)
  "matchedValue" : suspicious value with middle redacted (first 4 chars + *** + last 2 chars)

Flag ONLY when ALL of the following are true:
  1. A literal string value is present (not a variable reference, not a template expression)
  2. The value is assigned in a sensitive context — one of:
       - Variable/field name contains: password, passwd, pwd, secret, token, api_key, apikey,
         access_key, private_key, client_secret, auth_token, bearer, credentials
       - Connection string with embedded credentials (user:pass@host, password=, pwd=)
       - Private key / certificate block (BEGIN PRIVATE KEY, BEGIN RSA PRIVATE KEY, etc.)
       - HTTP header assignment with Authorization or X-API-Key as the header name
  3. The value itself looks like a real secret:
       - Length >= 8 characters
       - Not a well-known placeholder (see exclusion list below)

Confidence levels:
  CRITICAL — clear credential: private key block, known token format (AWS AKIA*, GitHub ghp_*, etc.)
  HIGH     — strong context: sensitive variable name + non-trivial literal value

NEVER flag:
  - Placeholder values: ${...}, {{...}}, <...>, %VAR%, changeme, replace_me, YOUR_*, *_HERE, TODO
  - Obvious test/example values: "test", "dummy", "sample", "fake", "example", "demo", "mock",
    "password", "secret", "12345678", "qwerty", "admin", "user", "pass"
  - Dynamic references: System.getenv(...), process.env.*, getProperty(...), @Value("${...}")
  - Method calls as values: anything containing () on the right-hand side of the assignment
  - UUIDs and hex digests: standard 8-4-4-4-12 UUIDs, 40-char SHA-1, 64-char SHA-256
  - URLs without credentials: http/https/ftp links with no embedded user:password
  - File paths, version strings, class names, log messages
  - SQL queries and regex patterns
  - Lines that are comments (// ... or # ... or /* ... */)
  - Import / include / require / package statements
  - Strings shorter than 8 characters

If nothing meets the above criteria, return exactly: []
Output valid JSON only — no explanation, no markdown fences, no trailing text.
""");
        return sb.toString();
    }

    private static List<Finding> parseResponse(String response, ScanContext context) {
        String json = extractJsonArray(response);
        if (json == null || json.isBlank() || json.equals("[]")) return Collections.emptyList();

        try {
            List<Map<String, Object>> items = MAPPER.readValue(json, new TypeReference<>() {});
            List<String> lines = context.getLines();
            List<Finding> findings = new ArrayList<>(items.size());

            for (Map<String, Object> item : items) {
                int    lineNo       = toInt(item.get("lineNo"), 0);
                String type         = str(item.get("type"),        "SECRET");
                String confStr      = str(item.get("confidence"),  "MEDIUM");
                String description  = str(item.get("description"), "Potential secret");
                String matchedValue = str(item.get("matchedValue"), "");

                Confidence confidence;
                try { confidence = Confidence.valueOf(confStr.toUpperCase()); }
                catch (IllegalArgumentException e) { confidence = Confidence.MEDIUM; }

                if (confidence != Confidence.HIGH && confidence != Confidence.CRITICAL) continue;

                String lineContent = (lineNo >= 1 && lineNo <= lines.size())
                        ? lines.get(lineNo - 1) : "";

                findings.add(new Finding(
                        context.getSha(),
                        context.getFile(),
                        lineNo,
                        type,
                        description,
                        confidence,
                        lineContent,
                        matchedValue
                ));
            }
            return findings;

        } catch (Exception e) {
            log.log(Level.FINE, "Cannot parse LLM response as JSON: " + response, e);
            return Collections.emptyList();
        }
    }

    /** Extracts the first [...] block from a response that may contain extra prose. */
    private static String extractJsonArray(String response) {
        if (response == null) return null;
        int start = response.indexOf('[');
        int end   = response.lastIndexOf(']');
        if (start >= 0 && end > start) return response.substring(start, end + 1);
        return null;
    }

    private static int toInt(Object o, int def) {
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(String.valueOf(o)); }
        catch (Exception e) { return def; }
    }

    private static String str(Object o, String def) {
        return o != null ? o.toString().trim() : def;
    }
}
