package com.az.gitember.ui;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

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

    private SyntaxStyleUtil() {}

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
}
