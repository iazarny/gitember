package com.az.gitember.service;

import dev.langchain4j.model.ollama.OllamaChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
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
     * @param diffText    unified diff text (will be truncated if necessary)
     * @param labelA      human-friendly name of the base revision (e.g. branch name)
     * @param labelB      human-friendly name of the changed revision
     * @param projectPath root folder of the project (used for language detection)
     * @param ollamaUrl   Ollama base URL (e.g. {@code http://localhost:11434})
     * @param modelName   Ollama model name (e.g. {@code llama3.2})
     * @return formatted description, never {@code null}
     * @throws Exception if the model call fails
     */
    public static String describe(String diffText,
                                  String labelA,
                                  String labelB,
                                  String projectPath,
                                  String ollamaUrl,
                                  String modelName) throws Exception {
        String truncated = diffText != null && diffText.length() > MAX_DIFF_CHARS
                ? diffText.substring(0, MAX_DIFF_CHARS) + "\n... [diff truncated] ..."
                : diffText;

        Set<String> languages = detectLanguages(projectPath);

        OllamaChatModel model = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName(modelName)
                .timeout(Duration.ofMinutes(5))
                .build();

        String prompt = buildPrompt(labelA, labelB, languages, truncated);
        log.log(Level.FINE, "Requesting diff description from {0} ({1}), languages={2}",
                new Object[]{ollamaUrl, modelName, languages});

        return model.generate(prompt);
    }

    private static Set<String> detectLanguages(String projectPath) {
        if (projectPath == null || projectPath.isBlank()) return Set.of();
        try {
            ProjectLanguageDetectorService detector =
                    new ProjectLanguageDetectorService(ProjectLanguageDetectorService.defaultMappings());
            return detector.detectLanguages(Paths.get(projectPath));
        } catch (Exception e) {
            log.log(Level.FINE, "Language detection failed: {0}", e.getMessage());
            return Set.of();
        }
    }

    // ── prompt ────────────────────────────────────────────────────────────────

    private static String buildPrompt(String labelA, String labelB, Set<String> languages, String diff) {
        String langLine = languages.isEmpty()
                ? ""
                : "Project language(s): " + String.join(", ", languages) + "\n";
        String langFocus = languageFocusHint(languages);

        return """
                You are a senior software engineer reviewing a git diff.
                Analyse the unified diff shown below and write a concise, structured description.

                Base revision   : %s
                Changed revision: %s
                %s
                Requirements:
                1. Create **Summary** for overall changes (2–10 sentences): What was changed at a high level?
                   What is the purpose of these changes?
                2. **Key technical changes** (bullet list, max 10 items):
                   Important logic, API, data-model, configuration or dependency changes.
                   Each bullet ≤ 2 sentences. Be specific — name files, methods, classes.%s
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
                """.formatted(labelA, labelB, langLine, langFocus, diff != null ? diff : "(empty diff)");
    }

    private static String languageFocusHint(Set<String> languages) {
        if (languages.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (String lang : languages) {
            String hint = switch (lang) {
                case "Java", "Kotlin", "Scala" ->
                        "\n   For JVM languages pay special attention to: class/interface changes, " +
                        "method signatures, annotations, generics, dependency updates (pom.xml / Gradle), " +
                        "API compatibility, checked exceptions.";
                case "JavaScript", "TypeScript" ->
                        "\n   For JS/TS pay special attention to: exported API shape, " +
                        "type definitions, async/promise patterns, npm dependency changes, " +
                        "bundler / tsconfig configuration.";
                case "Python" ->
                        "\n   For Python pay special attention to: public function/class signatures, " +
                        "type hints, dependency changes (requirements.txt / pyproject.toml), " +
                        "breaking import changes.";
                case "Go" ->
                        "\n   For Go pay special attention to: exported function/type changes, " +
                        "interface satisfaction, goroutine/concurrency patterns, module dependency updates.";
                case "Rust" ->
                        "\n   For Rust pay special attention to: public API (pub items), " +
                        "trait implementations, lifetime annotations, Cargo.toml dependency changes, " +
                        "unsafe blocks.";
                case "C#", "F#" ->
                        "\n   For .NET pay special attention to: public API surface, " +
                        "NuGet dependency changes, async/await patterns, interface and attribute changes.";
                case "C++", "C/C++" ->
                        "\n   For C/C++ pay special attention to: header changes, " +
                        "ABI-breaking modifications, memory management, CMake/Makefile build changes.";
                case "PHP" ->
                        "\n   For PHP pay special attention to: public class/method signatures, " +
                        "Composer dependency changes, namespace changes.";
                case "Ruby" ->
                        "\n   For Ruby pay special attention to: public method signatures, " +
                        "Gemfile dependency changes, module/class hierarchy.";
                case "Swift" ->
                        "\n   For Swift pay special attention to: public API, protocol conformances, " +
                        "Swift Package Manager dependency changes.";
                case "Dart" ->
                        "\n   For Dart/Flutter pay special attention to: widget tree changes, " +
                        "pubspec.yaml dependency updates, null-safety annotations.";
                case "Terraform" ->
                        "\n   For Terraform pay special attention to: resource additions/removals, " +
                        "variable/output changes, provider version changes, state-affecting modifications.";
                default -> "";
            };
            if (!hint.isEmpty()) { sb.append(hint); break; }
        }
        return sb.toString();
    }
}
