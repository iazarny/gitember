package com.az.gitember.handler;

import com.az.gitember.service.I18n;
import com.az.gitember.dialog.InteractiveContinueAbortDialog;
import com.az.gitember.service.Context;
import com.az.gitember.ui.HistoryPanel;
import com.az.gitember.ui.MainFrame;
import com.az.gitember.ui.StatusBar;
import org.eclipse.jgit.api.RebaseResult;

import javax.swing.*;
import java.awt.*;

public class RebaseBranchHandler extends AbstractAsyncHandler<RebaseResult> {

    private final String branchFullName;

    public RebaseBranchHandler(Component parent, String branchFullName) {
        super(parent);
        this.branchFullName = branchFullName;
    }

    @Override
    protected String getOperationName() {
        return I18n.t("rebase");
    }

    @Override
    protected RebaseResult doInBackground() throws Exception {
        RebaseResult result = Context.getGitRepoService().rebaseBranch(branchFullName);
        Context.updateAll();
        return result;
    }

    @Override
    protected void onSuccess(RebaseResult result) {
        String status = result.getStatus().toString();
        statusBar.setStatus(I18n.t("rebaseBranchHandler.rebase", status));
        JOptionPane.showMessageDialog(parent,
                I18n.t("rebaseResult", status),
                I18n.t("rebase"), JOptionPane.INFORMATION_MESSAGE);
        InteractiveContinueAbortDialog.showIfRebaseInProgress(
                MainFrame.getInstance(), MainFrame.getInstance().getStatusBar(),
                () -> {
                    HistoryPanel historyPanel = MainFrame.getInstance().getHistoryPanel();
                    if (historyPanel != null) {
                        historyPanel.loadHistory(null, true);
                    }
                });
    }

    /**
     * Shows confirmation and executes.
     */
    public static void showAndExecute(Component parent,
                                      String branchFullName, String branchShortName) {
        String workingBranchName = Context.getWorkingBranch() != null
                ? Context.getWorkingBranch().getShortName() : "current";

        int result = JOptionPane.showConfirmDialog(parent,
                I18n.t("rebaseOnto", branchShortName, workingBranchName),
                I18n.t("rebaseBranch"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            new RebaseBranchHandler(parent,  branchFullName).execute();
        }
    }
}
