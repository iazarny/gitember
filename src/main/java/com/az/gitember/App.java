package com.az.gitember;

import com.az.gitember.dialog.SettingsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final Logger log = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        // Use native macOS screen menu bar
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", "Gitember");

        SwingUtilities.invokeLater(() -> {
            try {
                Context.readSettings();
                setupLookAndFeel();
                applyFontSize();
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Failed to start application", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to start: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void setupLookAndFeel() {
        try {
            boolean darkMode = false;
            if (Context.getSettings() != null && "dark".equalsIgnoreCase(Context.getSettings().getTheme())) {
                darkMode = true;
            }
            if (darkMode) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to set FlatLaf, using system default", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
        }
    }

    private static void applyFontSize() {
        if (Context.getSettings() != null) {
            int size = Context.getSettings().getFontSize();
            if (size > 0) {
                SettingsDialog.applyFontSize(size);
            }
        }
    }
}
