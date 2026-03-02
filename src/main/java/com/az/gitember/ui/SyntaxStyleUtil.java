package com.az.gitember.ui;

import com.az.gitember.service.Context;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

/**
 * Maps file extensions to RSyntaxTextArea syntax styles.
 */
public final class SyntaxStyleUtil {

    private static final Map<String, String> EXT_TO_STYLE = Map.ofEntries(
            Map.entry("txt", SyntaxConstants.SYNTAX_STYLE_HTML),
            Map.entry("java", SyntaxConstants.SYNTAX_STYLE_JAVA),
            Map.entry("kt", SyntaxConstants.SYNTAX_STYLE_KOTLIN),
            Map.entry("scala", SyntaxConstants.SYNTAX_STYLE_SCALA),
            Map.entry("groovy", SyntaxConstants.SYNTAX_STYLE_GROOVY),
            Map.entry("py", SyntaxConstants.SYNTAX_STYLE_PYTHON),
            Map.entry("rb", SyntaxConstants.SYNTAX_STYLE_RUBY),
            Map.entry("js", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT),
            Map.entry("mjs", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT),
            Map.entry("ts", SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT),
            Map.entry("tsx", SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT),
            Map.entry("jsx", SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT),
            Map.entry("json", SyntaxConstants.SYNTAX_STYLE_JSON),
            Map.entry("xml", SyntaxConstants.SYNTAX_STYLE_XML),
            Map.entry("html", SyntaxConstants.SYNTAX_STYLE_HTML),
            Map.entry("htm", SyntaxConstants.SYNTAX_STYLE_HTML),
            Map.entry("css", SyntaxConstants.SYNTAX_STYLE_CSS),
            Map.entry("less", SyntaxConstants.SYNTAX_STYLE_LESS),
            Map.entry("sql", SyntaxConstants.SYNTAX_STYLE_SQL),
            Map.entry("sh", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL),
            Map.entry("bash", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL),
            Map.entry("zsh", SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL),
            Map.entry("bat", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH),
            Map.entry("cmd", SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH),
            Map.entry("c", SyntaxConstants.SYNTAX_STYLE_C),
            Map.entry("h", SyntaxConstants.SYNTAX_STYLE_C),
            Map.entry("cpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS),
            Map.entry("cxx", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS),
            Map.entry("hpp", SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS),
            Map.entry("cs", SyntaxConstants.SYNTAX_STYLE_CSHARP),
            Map.entry("go", SyntaxConstants.SYNTAX_STYLE_GO),
            Map.entry("rs", SyntaxConstants.SYNTAX_STYLE_RUST),
            Map.entry("php", SyntaxConstants.SYNTAX_STYLE_PHP),
            Map.entry("pl", SyntaxConstants.SYNTAX_STYLE_PERL),
            Map.entry("lua", SyntaxConstants.SYNTAX_STYLE_LUA),
            Map.entry("r", SyntaxConstants.SYNTAX_STYLE_NONE),
            Map.entry("yaml", SyntaxConstants.SYNTAX_STYLE_YAML),
            Map.entry("yml", SyntaxConstants.SYNTAX_STYLE_YAML),
            Map.entry("md", SyntaxConstants.SYNTAX_STYLE_MARKDOWN),
            Map.entry("markdown", SyntaxConstants.SYNTAX_STYLE_MARKDOWN),
            Map.entry("properties", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE),
            Map.entry("ini", SyntaxConstants.SYNTAX_STYLE_INI),
            Map.entry("cfg", SyntaxConstants.SYNTAX_STYLE_INI),
            Map.entry("toml", SyntaxConstants.SYNTAX_STYLE_NONE),
            Map.entry("dart", SyntaxConstants.SYNTAX_STYLE_DART),
            Map.entry("swift", SyntaxConstants.SYNTAX_STYLE_NONE),
            Map.entry("tex", SyntaxConstants.SYNTAX_STYLE_LATEX),
            Map.entry("latex", SyntaxConstants.SYNTAX_STYLE_LATEX),
            Map.entry("dockerfile", SyntaxConstants.SYNTAX_STYLE_DOCKERFILE),
            Map.entry("makefile", SyntaxConstants.SYNTAX_STYLE_MAKEFILE)
    );

    private static final String THEME_LIGHT = "/org/fife/ui/rsyntaxtextarea/themes/default.xml";
    private static final String THEME_DARK  = "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml";

    private SyntaxStyleUtil() {}

    /** Returns true when the active FlatLaf theme is dark. */
    public static boolean isDarkTheme() {
        Color bg = UIManager.getColor("Panel.background");
        return bg != null && bg.getRed() < 128;
    }

    /**
     * Applies the appropriate RSyntaxTextArea colour theme (light / dark)
     * based on the current Swing look-and-feel.
     */
    public static void applyTheme(RSyntaxTextArea area) {
        String path = isDarkTheme() ? THEME_DARK : THEME_LIGHT;
        try {
            Theme theme = Theme.load(SyntaxStyleUtil.class.getResourceAsStream(path));
            theme.apply(area);
        } catch (IOException | NullPointerException ignored) {
            // theme file missing — fall back to RSyntaxTextArea defaults
        }
    }

    public static String getSyntaxStyle(String fileName) {
        if (fileName == null) return SyntaxConstants.SYNTAX_STYLE_NONE;

        String lower = fileName.toLowerCase();

        // Special file names
        if (lower.endsWith("dockerfile")) return SyntaxConstants.SYNTAX_STYLE_DOCKERFILE;
        if (lower.endsWith("makefile")) return SyntaxConstants.SYNTAX_STYLE_MAKEFILE;
        if (lower.endsWith("pom.xml") || lower.endsWith(".xml")) return SyntaxConstants.SYNTAX_STYLE_XML;
        if (lower.endsWith(".fxml")) return SyntaxConstants.SYNTAX_STYLE_XML;

        int dot = lower.lastIndexOf('.');
        if (dot >= 0 && dot < lower.length() - 1) {
            String ext = lower.substring(dot + 1);
            return EXT_TO_STYLE.getOrDefault(ext, SyntaxConstants.SYNTAX_STYLE_NONE);
        }

        return SyntaxConstants.SYNTAX_STYLE_NONE;
    }

    /**
     * Returns a monospaced font at the size configured in Settings (falls back to 13).
     * Use this wherever an RSyntaxTextArea font is set so the user's preference is honoured.
     */
    public static Font monoFont() {
        var settings = Context.getSettings();
        int size = (settings != null && settings.getFontSize() > 0) ? settings.getFontSize() : 13;
        return new Font(Font.MONOSPACED, Font.PLAIN, size);
    }

    // Highlight colors — two palettes selected at paint time based on active theme
    public static Color addedBg() {
        return isDarkTheme() ? new Color(0, 70, 0) : new Color(200, 255, 200);
    }
    public static Color deletedBg() {
        return isDarkTheme() ? new Color(90, 20, 20) : new Color(255, 200, 200);
    }
    public static Color changedBg() {
        return isDarkTheme() ? new Color(20, 50, 100) : new Color(200, 230, 255);
    }

    /**
     * Returns a theme-aware foreground colour for a SCM file-status label
     * (as used in the changed-files table of the commit detail panel).
     *
     * <p>Returns {@code null} for unknown statuses so callers can fall back to
     * the default table foreground.</p>
     *
     * @param status upper-case status string, e.g. {@code "ADDED"}, {@code "MODIFIED"} …
     */
    public static Color statusColor(String status) {
        if (status == null) return null;
        boolean dark = isDarkTheme();
        return switch (status) {
            case "ADDED",    "ADD"             -> dark ? new Color( 80, 220,  80) : new Color(  0, 140,  0);
            case "MODIFIED", "MODIFY"          -> dark ? new Color( 80, 160, 255) : new Color(  0,  90, 200);
            case "REMOVED",  "DELETE"          -> dark ? new Color(255,  90,  90) : new Color(190,  0,   0);
            case "RENAMED",  "RENAME", "COPY"  -> dark ? new Color(220, 170,  60) : new Color(140,  80,  0);
            default                            -> null;
        };
    }

}
