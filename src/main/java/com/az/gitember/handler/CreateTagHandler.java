package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;

public class CreateTagHandler extends AbstractAsyncHandler<String> {

    private final String tagName;
    private final String commitSha;

    public CreateTagHandler(Component parent, StatusBar statusBar, String tagName) {
        this(parent, statusBar, tagName, null);
    }

    public CreateTagHandler(Component parent, StatusBar statusBar, String tagName, String commitSha) {
        super(parent, statusBar);
        this.tagName = tagName;
        this.commitSha = commitSha;
    }

    @Override
    protected String getOperationName() {
        return "Create tag " + tagName;
    }

    @Override
    protected String doInBackground() throws Exception {
        if (commitSha != null) {
            Context.getGitRepoService().createTag(tagName, commitSha);
        } else {
            Context.getGitRepoService().createTag(tagName);
        }
        Context.updateTags();
        return tagName;
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus("Tag created: " + result);
    }

    /**
     * Prompts user for a tag name and executes if confirmed.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar) {
        showAndExecute(parent, statusBar, null);
    }

    /**
     * Prompts user for a tag name and creates a tag at {@code commitSha} (or HEAD if null).
     */
    public static void showAndExecute(Component parent, StatusBar statusBar, String commitSha) {
        String name = JOptionPane.showInputDialog(parent,
                "Tag name:", "Create Tag", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.isBlank()) {
            new CreateTagHandler(parent, statusBar, name.trim(), commitSha).execute();
        }
    }
}
