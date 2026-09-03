package com.az.gitember.dialog;

import javax.swing.*;
import java.awt.*;

/**
 * Commit message input for {@link CommitDialog}. In single-repository mode it's just the common
 * message area. In workspace mode it offers a common message plus one message per project; a
 * project whose own message is blank falls back to the common one at commit time.
 */
public class CommitMessagePanel extends JPanel {

    private final CommitMessageArea commonMessageArea;
    private final CommitMessageArea[] projectMessageAreas;

    /** @param projectNames {@code null} for single-repository mode, one entry per workspace project otherwise. */
    public CommitMessagePanel(String[] projectNames) {
        setLayout(new BorderLayout());

        commonMessageArea = new CommitMessageArea("Common", 5);
        commonMessageArea.getMessageArea().setName("commitMessageArea");

        if (projectNames == null) {
            projectMessageAreas = null;
            add(commonMessageArea, BorderLayout.CENTER);
        } else {
            projectMessageAreas = new CommitMessageArea[projectNames.length];

            Box perProjectBox = Box.createVerticalBox();
            perProjectBox.setBorder(BorderFactory.createEmptyBorder());
            for (int i = 0; i < projectNames.length; i++) {
                projectMessageAreas[i] = new CommitMessageArea(
                        projectNames[i] + " (blank = use common message):", 2);
                perProjectBox.add(projectMessageAreas[i]);
            }

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("Commit message", commonMessageArea);
            JScrollPane projeMessages = new JScrollPane(perProjectBox);
            projeMessages.setBorder(BorderFactory.createEmptyBorder());
            tabbedPane.addTab("Per project", projeMessages);
            add(tabbedPane, BorderLayout.CENTER);
        }
    }

    /** The common message, used as-is in single-repository mode. */
    public String getMessage() {
        return commonMessageArea.getText();
    }

    public void setMessage(String text) {
        commonMessageArea.setText(text);
    }

    /** Workspace project {@code index}'s own message, or the common message when it's blank. */
    public String getEffectiveMessage(int index) {
        String own = getOwnProjectMessage(index);
        return own.isEmpty() ? getMessage().trim() : own;
    }

    /** Per-project text as typed, without falling back to the common message. */
    public String getOwnProjectMessage(int index) {
        String text = "";
        if (projectMessageAreas != null && index >= 0 && index < projectMessageAreas.length) {
            text = projectMessageAreas[index].getText().trim();
        }
        return text;
    }

    public void setProjectMessage(int index, String text) {
        if (projectMessageAreas != null && index >= 0 && index < projectMessageAreas.length) {
            projectMessageAreas[index].setText(text != null ? text : "");
        }
    }
}
