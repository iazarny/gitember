package com.az.gitember.ui.misc;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

/**
 * FlatLaf themes selectable in Settings. {@link #getId()} is the value stored in
 * {@code Settings.theme}; the macOS themes keep the legacy "light"/"dark" ids so
 * existing settings files map onto them unchanged.
 */
public enum LafTheme {

    MAC_LIGHT("light", "macOS Light"),
    MAC_DARK("dark", "macOS Dark"),
    FLAT_LIGHT("flatlight", "Flat Light"),
    FLAT_DARK("flatdark", "Flat Dark"),
    INTELLIJ("intellij", "IntelliJ"),
    DARCULA("darcula", "Darcula");

    private final String id;
    private final String displayName;

    LafTheme(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Installs this theme as the current look and feel. */
    public void setup() {
        switch (this) {
            case MAC_LIGHT -> FlatMacLightLaf.setup();
            case MAC_DARK -> FlatMacDarkLaf.setup();
            case FLAT_LIGHT -> FlatLightLaf.setup();
            case FLAT_DARK -> FlatDarkLaf.setup();
            case INTELLIJ -> FlatIntelliJLaf.setup();
            case DARCULA -> FlatDarculaLaf.setup();
        }
    }

    /** Resolves a stored settings id; unknown or empty ids fall back to {@link #MAC_LIGHT}. */
    public static LafTheme fromId(String id) {
        for (LafTheme theme : values()) {
            if (theme.id.equalsIgnoreCase(id)) {
                return theme;
            }
        }
        return MAC_LIGHT;
    }
}
