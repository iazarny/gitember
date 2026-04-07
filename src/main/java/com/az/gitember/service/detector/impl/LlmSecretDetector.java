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

    /** Maximum lines fed to the model per file (~6 k tokens at ~20 chars/line). */
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
            response = model.generate(prompt);
        } catch (Exception e) {
            log.log(Level.WARNING, "LLM secret scan failed for " + context.getFile(), e);
            return Collections.emptyList();
        }

        return parseResponse(response, context);
    }

    // -------------------------------------------------------------------------

    private static String buildPrompt(String fileName, List<String> lines) {
        StringBuilder sb = new StringBuilder(512 + lines.size() * 80);
        sb.append("""
           You are a security code scanner.
           Analyze the source file below for hardcoded secrets.\n\n""");
        sb.append("File: ").append(fileName).append("\n```\n");
        for (int i = 0; i < lines.size(); i++) {
            sb.append(i + 1).append(": ").append(lines.get(i)).append('\n');
        }
        sb.append("```\n\n");
        sb.append("""
                Return ONLY a JSON array of findings. Each element must have these fields:
                                                                              "lineNo"       : integer (1-based)
                                                                              "type"         : one of SECRET, TOKEN, API_KEY, PASSWORD, CREDENTIAL, CONNECTION_STRING
                                                                              "confidence"   : one of LOW, MEDIUM, HIGH, CRITICAL
                                                                              "description"  : short description (max 80 chars)
                                                                              "matchedValue" : suspicious value partially redacted (first 4 chars + ***)
                
                                                                            Detection rules:
                
                                                                            Flag ONLY if at least one of the following is true:
                                                                            - A hardcoded secret is assigned to a variable or field with sensitive name:
                                                                              (password, passwd, pwd, secret, token, api_key, apikey, access_key, private_key, client_secret)
                                                                            - A connection string contains embedded credentials (user:pass@, password=, pwd=)
                                                                            - Private key or certificate material is present (e.g. "BEGIN PRIVATE KEY")
                                                                            - OAuth client secrets or bearer tokens explicitly assigned
                                                                            - High-entropy string (length >= 20, mixed case, digits, symbols) AND:
                                                                                - appears in assignment (e.g. key = "...") OR
                                                                                - appears in HTTP headers (Authorization, X-API-KEY)
                
                                                                            Additional constraints for HIGH/CRITICAL:
                                                                            - CRITICAL: clear credential or private key exposure
                                                                            - HIGH: strong indication of real secret with proper context
                                                                            - MEDIUM/LOW: weaker signals
                
                                                                            Do NOT flag:
                                                                            - Placeholder patterns: ${...}, <...>, %..%, example_*, *_here, changeme, YOUR_*
                                                                            - Test/mock values: "test", "dummy", "sample", "fake", "123456", "password"
                                                                            - UUIDs, hashes, or IDs (hex strings, GUIDs, SHA1/256, etc.)
                                                                            - Environment variable references (process.env, System.getenv, $VAR)
                                                                            - Constants without sensitive naming (e.g. VERSION_ID, REQUEST_ID)
                                                                            - Comments or documentation lines
                                                                            - Reserved keywords or syntax-only lines
                                                                            - Strings shorter than 8 characters
                                                                            - Base64 strings without sensitive context (e.g. not tied to key/token)
                                                                            - empty lines
                                                                            - lines which are have reserved programming languages words like "import", "include", etc
                
                                                                            Heuristics:
                                                                            - Prefer variable name + assignment context over standalone strings
                                                                            - Prefer structured secrets over random-looking values
                                                                            - Reduce confidence if no clear sensitive context
                
                                                                            If nothing suspicious found, return exactly: []
                                                                            Output valid JSON only — no explanation, no markdown fences.
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
                if (!"CRITICAL".equals(type)) {
                    continue;
                }
                String description  = str(item.get("description"), "Potential secret");
                String matchedValue = str(item.get("matchedValue"), "");

                Confidence confidence;
                try { confidence = Confidence.valueOf(confStr.toUpperCase()); }
                catch (IllegalArgumentException e) { confidence = Confidence.MEDIUM; }

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
