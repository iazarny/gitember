package com.az.gitember.service;

import dev.langchain4j.model.ollama.OllamaChatModel;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a concise git commit message for staged changes using a local Ollama LLM.
 * An optional user-supplied hint (partial message already typed) is forwarded to the
 * model so the suggestion can continue or complement what the developer started.
 */
public final class LlmCommitMessageService {

    private static final Logger log = Logger.getLogger(LlmCommitMessageService.class.getName());

    /** Characters of diff fed to the model (≈ 6 k tokens at avg 4 chars/token). */
    public static final int MAX_DIFF_CHARS = 8_000;

    private LlmCommitMessageService() {}

    /**
     * Asks the locally-running Ollama model to write a commit message for the given diff.
     *
     * @param diffText  staged diff text (truncated if needed)
     * @param userHint  optional partial commit message the developer has typed, may be blank
     * @param ollamaUrl Ollama base URL (e.g. {@code http://localhost:11434})
     * @param modelName Ollama model name (e.g. {@code llama3.2})
     * @return single-line commit message, never {@code null}
     * @throws Exception if the model call fails
     */
    public static String generate(String diffText,
                                  String userHint,
                                  String ollamaUrl,
                                  String modelName) throws Exception {
        String truncated = diffText != null && diffText.length() > MAX_DIFF_CHARS
                ? diffText.substring(0, MAX_DIFF_CHARS) + "\n... [diff truncated] ..."
                : diffText;

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(3))
                .build();

        String prompt = buildPrompt(truncated, userHint);
        log.log(Level.FINE, "Requesting commit message from {0} ({1})", new Object[]{ollamaUrl, modelName});
        return model.chat(prompt).trim();
    }

    private static String buildPrompt(String diff, String userHint) {
        String hintSection = (userHint != null && !userHint.isBlank())
                ? "The developer has started writing: \"" + userHint + "\"\n"
                  + "Continue or complete that message while keeping its intent.\n\n"
                : "";
        return """
                You are an expert software engineer. Write a concise git commit message for the staged changes shown below.

                Rules:
                - Single line, imperative mood (e.g. "Fix null pointer in login flow")
                - 50–72 characters maximum
                - No period at the end
                - No markdown, no conventional-commit prefixes, no quotes
                - Focus on WHAT changed and WHY, not HOW
                - Output ONLY the commit message text — nothing else

                %sDiff:
                ```
                %s
                ```
                """.formatted(hintSection, diff != null ? diff : "(no staged changes)");
    }
}
