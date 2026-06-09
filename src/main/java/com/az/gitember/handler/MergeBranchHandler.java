package com.az.gitember.handler;

import com.az.gitember.data.MergeDialogResult;
import com.az.gitember.service.Context;
import com.az.gitember.ui.MergeDialog;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.revwalk.RevCommit;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MergeBranchHandler extends AbstractAsyncHandler<MergeResult> {

    private final String branchFullName;
    private final MergeDialogResult dialogResult;

    public MergeBranchHandler(Component parent, StatusBar statusBar,
                              String branchFullName, MergeDialogResult dialogResult) {
        super(parent, statusBar);
        this.branchFullName = branchFullName;
        this.dialogResult   = dialogResult;
    }

    @Override
    protected String getOperationName() {
        return "Merge";
    }

    @Override
    protected MergeResult doInBackground() throws Exception {
        MergeResult result = Context.getGitRepoService().mergeBranch(
                branchFullName,
                dialogResult.getCommitMessage(),
                dialogResult.isSquash(),
                dialogResult.getFastForwardMode());
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(MergeResult result) {
        String status = result.getMergeStatus().toString();
        statusBar.setStatus("Merge: " + status);
        JOptionPane.showMessageDialog(parent,
                "Merge result: " + status,
                "Merge", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the merge options dialog and executes if confirmed.
     */
    public static void showAndExecute(Frame parent, StatusBar statusBar,
                                      String branchFullName, String branchShortName) {
        String workingBranchName = Context.getWorkingBranch() != null
                ? Context.getWorkingBranch().getShortName() : "current";

        List<String> commitMessages;
        try {
            List<RevCommit> commits = Context.getGitRepoService().getCommitsToMerge(branchFullName);
            commitMessages = commits.stream()
                    .map(RevCommit::getShortMessage)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            commitMessages = Collections.emptyList();
        }

        MergeDialog dialog = new MergeDialog(parent, branchShortName, workingBranchName, commitMessages);
        dialog.setVisible(true);

        MergeDialogResult result = dialog.getResult();
        if (result != null) {
            new MergeBranchHandler(parent, statusBar, branchFullName, result).execute();
        }
    }
}
