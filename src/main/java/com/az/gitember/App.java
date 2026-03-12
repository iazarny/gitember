package com.az.gitember;

import com.az.gitember.dialog.SettingsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.DiffViewerWindow;
import com.az.gitember.ui.FolderCompareWindow;
import com.az.gitember.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
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

                if (args.length == 2) {
                    File left  = new File(args[0]);
                    File right = new File(args[1]);
                    if (left.exists() && right.exists()) {
                        if (left.isDirectory() && right.isDirectory()) {
                            FolderCompareWindow w = new FolderCompareWindow();
                            w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            w.setVisible(true);
                            w.compare(left.getAbsolutePath(), right.getAbsolutePath());
                            return;
                        } else if (left.isFile() && right.isFile()) {
                            String leftContent  = Files.readString(left.toPath());
                            String rightContent = Files.readString(right.toPath());
                            DiffViewerWindow w = new DiffViewerWindow(
                                    left.getName(),
                                    left.getAbsolutePath(),  leftContent,
                                    right.getAbsolutePath(), rightContent);
                            w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            w.setVisible(true);
                            return;
                        }
                    }
                }

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
