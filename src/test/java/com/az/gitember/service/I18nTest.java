package com.az.gitember.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class I18nTest {

    private static final Locale UK = Locale.forLanguageTag("uk");
    private final Locale displayLocale = Locale.getDefault(Locale.Category.DISPLAY);

    @AfterEach
    void restore() {
        I18n.init("en");
        Locale.setDefault(Locale.Category.DISPLAY, displayLocale);
    }

    @Test
    void forcedLanguageWinsOverOs() {
        assertEquals(UK, I18n.resolveLocale("uk", Locale.ENGLISH));
        assertEquals(Locale.ENGLISH, I18n.resolveLocale("en", UK));
    }

    @Test
    void blankLanguageFollowsOs() {
        assertEquals(UK, I18n.resolveLocale(null, Locale.forLanguageTag("uk-UA")));
        assertEquals(UK, I18n.resolveLocale("  ", UK));
        assertEquals(Locale.ENGLISH, I18n.resolveLocale("", Locale.US));
    }

    @Test
    void languageWithoutBundleFallsBackToEnglish() {
        assertEquals(Locale.ENGLISH, I18n.resolveLocale(null, Locale.GERMANY));
        assertEquals(Locale.ENGLISH, I18n.resolveLocale("fr", UK));
    }

    @Test
    void russianAndBelarusianUseUkrainianMessages() {
        for (String lang : List.of("ru", "be")) {
            I18n.init(lang);
            assertEquals("Скасувати", I18n.t("cancel"), lang);
        }
        I18n.init(null);
        I18n.init("ru-RU");
        assertEquals("Скасувати", I18n.t("cancel"));
    }

    @Test
    void englishAndUkrainianMessages() {
        I18n.init("en");
        assertEquals("Cancel", I18n.t("cancel"));
        I18n.init("uk");
        assertEquals("Скасувати", I18n.t("cancel"));
    }

    @Test
    void argumentsAreFormattedAsPlainStrings() {
        I18n.init("en");
        assertEquals("Commit failed: boom", I18n.t("commitFailed", "boom"));
        // no locale grouping for numbers, apostrophes from '' escapes
        assertEquals("Cannot checkout 'feature/x'.", I18n.t("cannotCheckoutBranchNamed", "feature/x"));
        assertEquals("Indexed commit 12345 of 20000…", I18n.t("indexedCommitOf", 12345, 20000));
    }

    @Test
    void missingKeyReturnsKey() {
        assertEquals("no.such.key", I18n.t("no.such.key"));
    }

    @Test
    void pluralForms() {
        I18n.init("en");
        assertEquals("1 match", I18n.plural("matchesCount", 1, 1));
        assertEquals("3 matches", I18n.plural("matchesCount", 3, 3));
        I18n.init("uk");
        assertEquals("1 збіг", I18n.plural("matchesCount", 1, 1));
        assertEquals("21 збіг", I18n.plural("matchesCount", 21, 21));
        assertEquals("3 збіги", I18n.plural("matchesCount", 3, 3));
        assertEquals("11 збігів", I18n.plural("matchesCount", 11, 11));
        assertEquals("25 збігів", I18n.plural("matchesCount", 25, 25));
    }

    @Test
    void statusIsTranslatedForDisplayOnly() {
        I18n.init("uk");
        assertEquals("Змінено", I18n.status("Modified"));
        assertEquals("LFS:lfs_file", I18n.status("LFS:lfs_file"));
    }

    // ── bundle consistency ───────────────────────────────────────────────────

    private static final Pattern ARG = Pattern.compile("\\{\\d+}");

    private static Properties load(String name) throws IOException {
        Properties p = new Properties();
        try (InputStream in = I18n.class.getResourceAsStream("/i18n/" + name)) {
            assertNotNull(in, name);
            p.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return p;
    }

    private static Set<String> args(String s) {
        Set<String> out = new TreeSet<>();
        Matcher m = ARG.matcher(s);
        while (m.find()) out.add(m.group());
        return out;
    }

    @Test
    void ukrainianBundleMatchesEnglish() throws IOException {
        Properties en = load("messages.properties");
        Properties uk = load("messages_uk.properties");
        for (String key : en.stringPropertyNames()) {
            boolean englishOnlyPluralForm = key.endsWith(".other");
            if (!englishOnlyPluralForm) {
                assertTrue(uk.containsKey(key), "missing Ukrainian key " + key);
                assertEquals(args(en.getProperty(key)), args(uk.getProperty(key)), "placeholders of " + key);
            }
        }
    }

    @Test
    void everyKeyUsedInCodeExists() throws IOException {
        Properties en = load("messages.properties");
        Pattern call = Pattern.compile("I18n\\.(t|plural)\\(\"([^\"]+)\"\\s*[,)]");
        List<String> missing = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path f : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                Matcher m = call.matcher(Files.readString(f));
                while (m.find()) {
                    String key = m.group(2);
                    boolean found = "plural".equals(m.group(1))
                            ? en.containsKey(key + ".other")
                            : en.containsKey(key);
                    if (!found) missing.add(f.getFileName() + ": " + key);
                }
            }
        }
        assertEquals(List.of(), missing);
    }
}
