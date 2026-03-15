package com.az.gitember.handler;

import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import com.az.gitember.ui.StatusBar;

import javax.swing.*;
import java.awt.*;

public class DeleteTagHandler extends AbstractAsyncHandler<String> {

    private final ScmBranch tag;
    private final boolean deleteRemote;

    public DeleteTagHandler(Component parent, StatusBar statusBar, ScmBranch tag, boolean deleteRemote) {
        super(parent, statusBar);
        this.tag = tag;
        this.deleteRemote = deleteRemote;
    }

    @Override
    protected String getOperationName() {
        return "Delete tag " + tag.getShortName();
    }

    @Override
    protected String doInBackground() throws Exception {
        Context.getGitRepoService().deleteLocalTag(tag.getFullName());
        if (deleteRemote) {
            Context.getGitRepoService().deleteRemoteTag(tag.getFullName());
        }
        Context.updateTags();
        String fullName = tag.getFullName();
        return fullName.startsWith("refs/tags/") ? fullName.substring("refs/tags/".length()) : fullName;
    }

    @Override
    protected void onSuccess(String result) {
        String where = deleteRemote ? " locally and from remote" : " locally";
        statusBar.setStatus("Tag \"" + result + "\" deleted" + where);
    }

    /**
     * Shows a dialog asking whether to delete the tag locally only or also from the remote,
     * then executes the chosen operation.
     */
    public static void showAndExecute(Component parent, StatusBar statusBar, ScmBranch tag) {
        String fullName = tag.getFullName();
        String shortName = fullName.startsWith("refs/tags/")
                ? fullName.substring("refs/tags/".length()) : tag.getShortName();

        String[] options = {"Local only", "Local + Remote", "Cancel"};
        int choice = JOptionPane.showOptionDialog(
                parent,
                "Delete tag \"" + shortName + "\"?",
                "Delete Tag",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, options, options[0]);

        if (choice == 0) {
            new DeleteTagHandler(parent, statusBar, tag, false).execute();
        } else if (choice == 1) {
            new DeleteTagHandler(parent, statusBar, tag, true).execute();
        }
    }
}
