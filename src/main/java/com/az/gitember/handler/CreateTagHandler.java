package com.az.gitember.handler;

import com.az.gitember.data.Settings;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;
import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;

import javax.swing.*;
import java.awt.*;

public class CreateTagHandler extends AbstractAsyncHandler<String> {

    private final String tagName;
    private final String commitSha;

    public CreateTagHandler(Component parent,  String tagName) {
        this(parent, tagName, null);
    }

    public CreateTagHandler(Component parent,  String tagName, String commitSha) {
        super(parent);
        this.tagName = tagName;
        this.commitSha = commitSha;
    }

    @Override
    protected String getOperationName() {
        return "Create tag " + tagName;
    }

    @Override
    protected String doInBackground() throws Exception {

        Settings settings = Context.getSettings();
        boolean signTag = !Settings.SignOption.NONE.getOption().equalsIgnoreCase(settings.getSignOption())
                && BooleanUtils.toBoolean(settings.getSignTag());
        String pathToKey = settings.getSignKey();

        if (commitSha != null) {
            Context.getGitRepoService().createTag(tagName, commitSha, settings.getSignOption(), signTag, pathToKey);
        } else {
            Context.getGitRepoService().createTag(tagName, settings.getSignOption(), signTag, pathToKey);
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
            new CreateTagHandler(parent,  name.trim(), commitSha).execute();
        }
    }
}
