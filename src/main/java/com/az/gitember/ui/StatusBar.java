package com.az.gitember.ui;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {

    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    public StatusBar() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        statusLabel = new JLabel("Ready");
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(300, 16));
        progressBar.setVisible(false);

        add(statusLabel, BorderLayout.CENTER);
        add(progressBar, BorderLayout.EAST);
    }

    public void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    public void showProgress(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(visible);
            progressBar.setIndeterminate(visible);
        });
    }

    public void setProgress(int value, int max) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(max);
            progressBar.setValue(value);
        });
    }

    public void clearProgress() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            progressBar.setValue(0);
            statusLabel.setText("Ready");
        });
    }
}
