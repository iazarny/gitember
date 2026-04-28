package com.az.gitember.service;

import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a human-readable AI description of a unified diff using a local
 * Ollama LLM.
 *
 * <p>The description includes:</p>
 * <ul>
 *   <li>A plain-English summary of what changed and why it likely matters</li>
 *   <li>A bullet-list of key technical changes (APIs, logic, config)</li>
 *   <li>A note on potential impact (breaking changes, performance, security)</li>
 * </ul>
 *
 * <p>Input diff is truncated to {@link #MAX_DIFF_CHARS} before sending to
 * avoid exceeding typical model context windows.</p>
 */
public final class LlmDiffDescriptionService {

    private static final Logger log = Logger.getLogger(LlmDiffDescriptionService.class.getName());

    /** Characters of unified diff fed to the model (≈ 10 k tokens at avg 4 chars/token). */
    public static final int MAX_DIFF_CHARS = 12_000;

    private LlmDiffDescriptionService() {}

    /**
     * Asks the locally-running Ollama model to describe the changes captured
     * in {@code diffText}.
     *
     * @param diffText  unified diff text (will be truncated if necessary)
     * @param labelA    human-friendly name of the base revision (e.g. branch name)
     * @param labelB    human-friendly name of the changed revision
     * @param ollamaUrl Ollama base URL (e.g. {@code http://localhost:11434})
     * @param modelName Ollama model name (e.g. {@code llama3.2})
     * @return formatted description, never {@code null}
     * @throws Exception if the model call fails
     */
    public static String describe(String diffText,
                                  String labelA,
                                  String labelB,
                                  String ollamaUrl,
                                  String modelName) throws Exception {
        String truncated = diffText != null && diffText.length() > MAX_DIFF_CHARS
                ? diffText.substring(0, MAX_DIFF_CHARS) + "\n... [diff truncated] ..."
                : diffText;

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(5))
                .build();

        String prompt = buildPrompt(labelA, labelB, truncated);
        log.log(Level.FINE, "Requesting diff description from {0} ({1})", new Object[]{ollamaUrl, modelName});

        return model.generate(prompt);
    }

    // ── prompt ────────────────────────────────────────────────────────────────

    private static String buildPrompt(String labelA, String labelB, String diff) {
        return """
                You are a senior software engineer reviewing a git diff.
                Analyse the unified diff shown below and write a concise, structured description.

                Base revision  : %s
                Changed revision: %s

                Requirements:
                1. **Summary** (2–4 sentences): What was changed at a high level?
                   What is the purpose of these changes?
                2. **Key technical changes** (bullet list, max 10 items):
                   Important logic, API, data-model, configuration or dependency changes.
                   Each bullet ≤ 2 sentences. Be specific — name files, methods, classes.
                3. **Potential impact** (bullet list, max 5 items):
                   Breaking changes, performance, security, test coverage, migration steps.
                   Omit this section if there is no notable impact.

                Rules:
                - Use plain, professional English — no filler phrases.
                - Do NOT repeat the raw diff lines — describe intent and effect instead.
                - If the diff is trivial (whitespace, comment-only), say so briefly.

                Diff:
                ```
                %s
                ```
                """.formatted(labelA, labelB, diff != null ? diff : "(empty diff)");
    }
}
