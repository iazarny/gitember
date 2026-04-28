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
        progressBar.setPreferredSize(new Dimension(300, 4));
        progressBar.setVisible(false);

        // Wrapper keeps a fixed height in BorderLayout.SOUTH so the StatusBar
        // does not resize when the progress bar is shown or hidden.
        int barHeight = progressBar.getPreferredSize().height;
        JPanel progressWrapper = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, barHeight);
            }
            @Override public Dimension getMinimumSize() {
                return new Dimension(0, barHeight);
            }
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, barHeight);
            }
        };
        progressWrapper.setOpaque(false);
        progressWrapper.add(progressBar, BorderLayout.CENTER);

        add(statusLabel, BorderLayout.WEST);
        add(progressWrapper, BorderLayout.SOUTH);
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
