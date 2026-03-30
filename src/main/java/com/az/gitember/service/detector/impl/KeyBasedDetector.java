package com.az.gitember.service.detector.impl;

import com.az.gitember.service.detector.Confidence;
import com.az.gitember.service.detector.Detector;
import com.az.gitember.service.detector.Finding;
import com.az.gitember.service.detector.ScanContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by Igor Azarny iazarny@yahoo.com on March 21  2026.
 */
public class KeyBasedDetector implements Detector {

    private static final Pattern KEY_PATTERN = Pattern.compile(
            "(?iu)(cipher|pwd|pass|passwd|password|passcode|passphrase|secret|token|api[_-]?key|client[_-]?secret|auth[_-]?code|credential|access[_-]?key|watchword|" +
                    "passwort|kennwort|geheimnis|schluessel|zugangscode|berechtigungsnachweis|" +
                    "contrasena|clave|secreto|ficha|codigo[_-]?acceso|credencial|" +
                    "mot[_-]?de[_-]?passe|jeton|code[_-]?authentification|identifiant|" +
                    "parola[_-]?chiave|segreto|gettone|codice[_-]?accesso|credenziali|" +
                    "haslo|s[ei]kret|klucz|kod[_-]?dostepu|uwierzytelnienie|poswiadczenie|" +
                    "пароль|код[_-]?доступа|ключ|секрет|токен|посвідчення|облікові[_-]?дані|шифр|гасло|" +
                    "parol|kod[_-]?dostupu|kliuch|klyuch|posvidchennia|oblikovi[_-]?dani|shyfr)"
    );

    // key = value OR key: value
    private static final Pattern ASSIGNMENT_PATTERN = Pattern.compile(
            "(?i)([a-zA-Z0-9._-]+)\\s*[:=]\\s*(.+)"
    );

    @Override
    public List<Finding> detect(ScanContext context) {
        List<Finding> findings = new ArrayList<>();

        List<String> lines = context.getLines();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            Matcher keyMatcher = KEY_PATTERN.matcher(line);
            if (!keyMatcher.find()) continue;

            Matcher assignMatcher = ASSIGNMENT_PATTERN.matcher(line);
            if (!assignMatcher.find()) continue;

            String key = assignMatcher.group(1);
            String rawValue = assignMatcher.group(2).trim();

            String value = extractValue(rawValue);

            if (value == null || !isHardcoded(value)) continue;

            findings.add(new Finding(
                    context.getSha(),
                    context.getFile(),
                    i + 1,
                    "HARDCODED_SECRET",
                    "Hardcoded secret assigned",
                    Confidence.HIGH,
                    line,
                    value
            ));
        }

        return findings;
    }

    /**
     * Extract clean value from quotes or raw
     */
    private String extractValue(String raw) {
        raw = raw.trim();

        // remove trailing comments
        raw = raw.replaceAll("#.*$", "").replaceAll("//.*$", "").trim();

        // quoted string
        if ((raw.startsWith("\"") && raw.endsWith("\"")) ||
                (raw.startsWith("'") && raw.endsWith("'"))) {
            return raw.substring(1, raw.length() - 1);
        }

        // unquoted value (env files, yaml)
        return raw.split("\\s")[0];
    }

    /**
     * Detect if value is hardcoded (NOT dynamic)
     */
    private boolean isHardcoded(String value) {

        if (value.length() < 4) return false;

        // ignore placeholders
        if (value.contains("${") || value.contains("}")) return false;

        // ignore env references
        if (value.startsWith("env(") || value.startsWith("process.env")) return false;

        // ignore method calls
        if (value.contains("(") && value.contains(")")) return false;

        // ignore obvious test values
        String v = value.toLowerCase();
        if (v.contains("test") || v.contains("dummy") || v.contains("example")) return false;

        return true;
    }

    @Override
    public String name() {
        return "KeyBasedDetector";
    }
}
