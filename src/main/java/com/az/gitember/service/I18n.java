package com.az.gitember.service;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Localized UI messages.
 *
 * <p>Messages live in {@code /i18n/messages*.properties} (UTF-8). {@code messages.properties} is
 * the English base bundle; every other language overrides it key by key.
 *
 * <p>Language resolution, highest priority first:
 * <ol>
 *     <li>the language forced in Settings → UI ({@link #init(String)} argument);</li>
 *     <li>the operating-system language ({@link Locale#getDefault()} at startup);</li>
 *     <li>if that language has no bundle: Russian and Belarusian fall back to Ukrainian,
 *         everything else falls back to English.</li>
 * </ol>
 * A key missing from a partially translated bundle resolves the same way, so a {@code ru}
 * bundle would read {@code ru → uk → en}.
 *
 * <p>{@link #t(String, Object...)} runs the message through {@link MessageFormat} only when
 * arguments are passed, so apostrophes must be doubled ({@code ''}) only in messages that take
 * {@code {0}}-style arguments.
 */
public final class I18n {

    private static final Logger log = Logger.getLogger(I18n.class.getName());

    public static final String BUNDLE = "i18n.messages";

    /** Value stored in settings when the language follows the operating system. */
    public static final String SYSTEM_LANGUAGE = "";

    /** Languages that have a bundle, in the order the Settings combo lists them. */
    public static final List<String> SUPPORTED_LANGUAGES = List.of("en", "uk");

    private static final String UKRAINIAN = "uk";
    private static final Set<String> UKRAINIAN_FALLBACK = Set.of("ru", "be");
    private static final String SWING_PREFIX = "swing.";

    /** OS locale captured before {@link #init} changes the display default. */
    private static final Locale OS_LOCALE = Locale.getDefault();

    private static final ResourceBundle.Control CONTROL = new ResourceBundle.Control() {
        @Override
        public List<String> getFormats(String baseName) {
            return ResourceBundle.Control.FORMAT_PROPERTIES;
        }

        @Override
        public Locale getFallbackLocale(String baseName, Locale locale) {
            return null; // never fall back to the JVM default locale; ROOT (English) is the base
        }

        @Override
        public List<Locale> getCandidateLocales(String baseName, Locale locale) {
            List<Locale> candidates = new ArrayList<>(super.getCandidateLocales(baseName, locale));
            if (UKRAINIAN_FALLBACK.contains(locale.getLanguage())) {
                candidates.add(candidates.size() - 1, Locale.forLanguageTag(UKRAINIAN));
            }
            return candidates;
        }
    };

    private static volatile Locale locale;
    private static volatile ResourceBundle bundle;

    private I18n() {
    }

    /**
     * Selects the UI language. Call once at startup, before any UI is built.
     *
     * @param uiLanguage language forced in settings, or blank to follow the OS
     */
    public static synchronized void init(String uiLanguage) {
        locale = resolveLocale(uiLanguage, OS_LOCALE);
        bundle = ResourceBundle.getBundle(BUNDLE, locale, CONTROL);
        Locale.setDefault(Locale.Category.DISPLAY, locale);
        JComponent.setDefaultLocale(locale);
        log.log(Level.INFO, "UI language {0} (requested {1}, OS {2})",
                new Object[]{locale, StringUtils.defaultIfBlank(uiLanguage, "system"), OS_LOCALE});
    }

    /**
     * Picks the bundle locale: the forced language when set, otherwise the OS language; a
     * language without a bundle becomes Ukrainian for ru/be and English for anything else.
     */
    static Locale resolveLocale(String uiLanguage, Locale osLocale) {
        Locale requested = StringUtils.isNotBlank(uiLanguage)
                ? Locale.forLanguageTag(uiLanguage.trim())
                : osLocale;
        String lang = requested.getLanguage();
        if (isAvailable(lang)) {
            return Locale.forLanguageTag(lang);
        }
        if (UKRAINIAN_FALLBACK.contains(lang) && isAvailable(UKRAINIAN)) {
            // Keep the requested language so a later partial ru/be bundle is still preferred.
            return Locale.forLanguageTag(lang);
        }
        return Locale.ENGLISH;
    }

    /** True when the language is English (the base bundle) or has its own bundle file. */
    static boolean isAvailable(String lang) {
        if (StringUtils.isBlank(lang)) {
            return false;
        }
        if ("en".equals(lang)) {
            return true;
        }
        return I18n.class.getResource("/" + BUNDLE.replace('.', '/') + "_" + lang + ".properties") != null;
    }

    public static Locale getLocale() {
        ensureInit();
        return locale;
    }

    /**
     * Returns the message for {@code key}. With arguments the message is a {@link MessageFormat}
     * pattern; arguments are inserted as strings (no locale number grouping).
     */
    public static String t(String key, Object... args) {
        String pattern;
        try {
            pattern = bundle().getString(key);
        } catch (MissingResourceException e) {
            log.log(Level.WARNING, "Missing message key {0}", key);
            return key;
        }
        if (args == null || args.length == 0) {
            return pattern;
        }
        Object[] text = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            text[i] = String.valueOf(args[i]);
        }
        return new MessageFormat(pattern, locale).format(text);
    }

    /**
     * Count-dependent message. Looks up {@code key.<form>} where form is {@code one}/{@code other}
     * for English and {@code one}/{@code few}/{@code many} for Ukrainian, Russian and Belarusian;
     * a missing form falls back to {@code key.other}, then {@code key.many}.
     *
     * @param n    the count that selects the form
     * @param args message arguments ({@code n} itself is usually {@code {0}})
     */
    public static String plural(String key, long n, Object... args) {
        ResourceBundle b = bundle();
        String k = key + "." + pluralForm(locale.getLanguage(), n);
        if (!b.containsKey(k)) {
            k = key + ".other";
        }
        if (!b.containsKey(k)) {
            k = key + ".many";
        }
        return t(k, args);
    }

    /** CLDR cardinal plural category for the languages Gitember ships. */
    static String pluralForm(String lang, long n) {
        if ("uk".equals(lang) || UKRAINIAN_FALLBACK.contains(lang)) {
            long mod10 = Math.abs(n) % 10;
            long mod100 = Math.abs(n) % 100;
            if (mod10 == 1 && mod100 != 11) {
                return "one";
            }
            if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14)) {
                return "few";
            }
            return "many";
        }
        return n == 1 ? "one" : "other";
    }

    /** Display text for a {@code ScmItem.Status} value; unknown values are returned unchanged. */
    public static String status(String status) {
        if (status == null || status.isEmpty()) {
            return status;
        }
        String key = "status." + status;
        ResourceBundle b = bundle();
        return b.containsKey(key) ? b.getString(key) : status;
    }

    /** Native name of a supported language, e.g. "English", "Українська". */
    public static String languageName(String lang) {
        Locale l = Locale.forLanguageTag(lang);
        return StringUtils.capitalize(l.getDisplayLanguage(l));
    }

    /**
     * Puts the bundle's {@code swing.*} keys into {@link UIManager}, translating JDK-provided
     * texts (option-pane and file-chooser buttons). Developer defaults survive look-and-feel
     * switches, so this only has to run once.
     */
    public static void installSwingDefaults() {
        ResourceBundle b = bundle();
        for (String key : b.keySet()) {
            if (key.startsWith(SWING_PREFIX)) {
                UIManager.put(key.substring(SWING_PREFIX.length()), b.getString(key));
            }
        }
    }

    private static ResourceBundle bundle() {
        ensureInit();
        return bundle;
    }

    private static void ensureInit() {
        if (bundle == null) {
            init(SYSTEM_LANGUAGE);
        }
    }
}
