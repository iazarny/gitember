package com.az.gitember.handler;

import com.az.gitember.data.Project;
import com.az.gitember.data.Submodule;
import com.az.gitember.service.Context;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Stages the gitlink and commits the submodule pointer change.
 */
public class CommitSubmoduleHandler extends AbstractAsyncHandler<Void> {

    private final String path;
    private final String message;

    public CommitSubmoduleHandler(Component parent, String path, String message) {
        super(parent);
        this.path = path;
        this.message = message;
    }

    @Override
    protected String getOperationName() {
        return "Commit submodule " + path;
    }

    @Override
    protected Void doInBackground() throws Exception {
        String authorName = "";
        String authorEmail = "";
        Project project = Context.getCurrentProject().orElse(null);
        if (project != null) {
            authorName = StringUtils.defaultString(project.getUserCommitName());
            authorEmail = StringUtils.defaultString(project.getUserCommitEmail());
        }
        Context.getGitRepoService().commitSubmoduleChange(path, message, authorName, authorEmail);
        return null;
    }

    @Override
    protected void onSuccess(Void result) {
        Context.updateSubmodules();
        Context.refreshWorkingCopy();
    }

    public static void showAndExecute(Component parent, Submodule submodule) {
        if (submodule != null) {
            promptAndCommit(parent, submodule);
        }
    }

    private static void promptAndCommit(Component parent, Submodule submodule) {
        String message = (String) JOptionPane.showInputDialog(
                parent,
                "Commit message for submodule " + submodule.getPath() + ":",
                "Commit Submodule Change",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "Update submodule " + submodule.getPath());
        if (StringUtils.isNotBlank(message)) {
            new CommitSubmoduleHandler(parent, submodule.getPath(), message.trim()).execute();
        }
    }
}
