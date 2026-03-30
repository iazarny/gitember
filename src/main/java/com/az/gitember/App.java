package com.az.gitember;

import com.az.gitember.dialog.SettingsDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.DiffViewerWindow;
import com.az.gitember.ui.FolderCompareWindow;
import com.az.gitember.ui.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.util.FS;

import javax.swing.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    private static final Logger log = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        // GPU acceleration — must be set before Java2D initialises (before invokeLater)
        setupAcceleration();

        // Register apache sshd as the global default SSH factory.
        // This ensures Ed25519 / ECDSA / OpenSSH-format keys work even when a transport
        // callback is not explicitly set (e.g. when the remote URL is unavailable).
        SshSessionFactory.setInstance(new SshdSessionFactoryBuilder()
                .setHomeDirectory(FS.DETECTED.userHome())
                .setSshDirectory(new File(FS.DETECTED.userHome(), ".ssh"))
                .build(null));

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

    /**
     * Enables Java2D GPU acceleration before the Java2D/AWT subsystem starts.
     * These properties have no effect if set after invokeLater fires.
     *
     * Windows: Direct3D pipeline (sun.java2d.d3d) — faster than the default GDI
     *          pipeline; VRAM caching for images (ddforcevram).
     * Linux:   OpenGL pipeline.
     * macOS:   OpenGL is deprecated; the JVM picks Metal automatically on recent JDKs.
     *
     * A property already set on the command line (-Dsun.java2d.d3d=false) takes
     * precedence — setProperty is a no-op when the value is already present.
     */
    private static void setupAcceleration() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            setIfAbsent("sun.java2d.d3d",         "true");   // Direct3D pipeline
            setIfAbsent("sun.java2d.ddforcevram", "true");   // keep images in VRAM
            setIfAbsent("sun.java2d.translaccel", "true");   // hw-accelerate translucency
            setIfAbsent("sun.java2d.noddraw",     "false");  // keep DirectDraw active
        } else if (os.contains("linux") || os.contains("nix") || os.contains("nux")) {
            setIfAbsent("sun.java2d.opengl", "true");
        }
        // macOS: no property needed — the JDK uses the Metal pipeline automatically
    }

    /** Sets a system property only when it has not already been provided (e.g. via -D). */
    private static void setIfAbsent(String key, String value) {
        if (System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
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
