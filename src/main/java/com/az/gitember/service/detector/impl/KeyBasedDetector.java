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

    private static final Pattern KEY_PATTERN =
            Pattern.compile("(?i)(cipher|pwd|pass|passwd|password|passcode|passphrase|secret|token|api[_-]?key|client[_-]?secret|auth[_-]?code|credential|access[_-]?key|watchword)");

    private static final Pattern KEY_PATTERN_DE =
            Pattern.compile("(?i)(passwort|kennwort|geheimnis|schluessel|token|zugangscode|berechtigungsnachweis)");

    private static final Pattern KEY_PATTERN_ES =
            Pattern.compile("(?i)(contrasena|clave|secreto|ficha|token|codigo[_-]?acceso|credencial)");

    private static final Pattern KEY_PATTERN_FR =
            Pattern.compile("(?i)(mot[_-]?de[_-]?passe|secret|jeton|token|code[_-]?authentification|identifiant)");

    private static final Pattern KEY_PATTERN_IT =
            Pattern.compile("(?i)(password|parola[_-]?chiave|segreto|gettone|token|codice[_-]?accesso|credenziali)");

    private static final Pattern KEY_PATTERN_PL =
            Pattern.compile("(?i)(haslo|s[e|i]kret|token|klucz|kod[_-]?dostepu|uwierzytelnienie|poswiadczenie)");

    private static final Pattern KEY_PATTERN_UA =
            Pattern.compile("(?i)(пароль|код[_-]?доступа|ключ|секрет|токен|посвідчення|облікові[_-]?дані|шифр|гасло)",
                    Pattern.UNICODE_CASE);

    private static final Pattern KEY_PATTERN_UA_TR =
            Pattern.compile("(?i)(parol|kod[_-]?dostupu|kliuch|klyuch|sekret|token|posvidchennia|oblikovi[_-]?dani|shyfr|haslo)");

    private boolean multilingual = false;

    @Override
    public List<Finding> detect(ScanContext context) {
        List<Finding> findings = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < context.getLines().size(); i++) {
            String line = context.getLines().get(i);
            for(Pattern pattern : getPatterns()) { //TODO several threads , per cpu
                Matcher matrcher = pattern.matcher(line);
                if (matrcher.find()) {
                    findings.add(new Finding(
                            context.getSha(),
                            context.getFile(),
                            i + 1,
                            "KEY_MATCH",
                            "Suspicious key detected",
                            Confidence.MEDIUM,
                            line,
                            matrcher.group(0)
                    ));
                }
            }
        }
        return findings;
    }

    private List<Pattern> getPatterns() {
        List<Pattern> rez = new ArrayList<>();
        rez.add(KEY_PATTERN);
        if (multilingual) {
            rez.add(KEY_PATTERN_DE);
            rez.add(KEY_PATTERN_ES);
            rez.add(KEY_PATTERN_IT);
            rez.add(KEY_PATTERN_FR);
            rez.add(KEY_PATTERN_PL);
            rez.add(KEY_PATTERN_UA);
            rez.add(KEY_PATTERN_UA_TR);
        }
        return rez;
    }

    @Override
    public String name() {
        return "KeyBasedDetector";
    }

    @Override
    public void setMultilingual(boolean multilingual) {
        this.multilingual = multilingual;
    }
}
