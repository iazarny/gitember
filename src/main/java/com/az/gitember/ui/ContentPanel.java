package com.az.gitember.ui;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel {

    private final JPanel mainContent;
    private final JLabel placeholderLabel;

    public ContentPanel() {
        setLayout(new BorderLayout());

        mainContent = new JPanel(new BorderLayout());
        placeholderLabel = new JLabel("Open a repository to get started", SwingConstants.CENTER);
        placeholderLabel.setFont(placeholderLabel.getFont().deriveFont(16f));
        placeholderLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        mainContent.add(placeholderLabel, BorderLayout.CENTER);
        add(mainContent, BorderLayout.CENTER);
    }

    public void setContent(JComponent component) {
        mainContent.removeAll();
        if (component != null) {
            mainContent.add(component, BorderLayout.CENTER);
        } else {
            mainContent.add(placeholderLabel, BorderLayout.CENTER);
        }
        mainContent.revalidate();
        mainContent.repaint();
    }
}
