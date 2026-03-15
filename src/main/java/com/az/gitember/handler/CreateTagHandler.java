package com.az.gitember.handler;

import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;

public class CreateTagHandler extends AbstractAsyncHandler<String> {

    private final String tagName;

    public CreateTagHandler(Component parent, StatusBar statusBar, String tagName) {
        super(parent, statusBar);
        this.tagName = tagName;
    }

    @Override
    protected String getOperationName() {
        return "Create tag " + tagName;
    }

    @Override
    protected String doInBackground() throws Exception {
        Context.getGitRepoService().createTag(tagName);
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
        String name = JOptionPane.showInputDialog(parent,
                "Tag name:", "Create Tag", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.isBlank()) {
            new CreateTagHandler(parent, statusBar, name.trim()).execute();
        }
    }
}
