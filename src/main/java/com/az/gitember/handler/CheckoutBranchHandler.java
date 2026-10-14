package com.az.gitember.handler;

import com.az.gitember.service.I18n;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.service.Context;
import org.eclipse.jgit.api.errors.CheckoutConflictException;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CheckoutBranchHandler extends AbstractAsyncHandler<String> {

    private final ScmBranch branch;

    public CheckoutBranchHandler(Component parent,  ScmBranch branch) {
        super(parent);
        this.branch = branch;
    }

    @Override
    protected String getOperationName() {
        return I18n.t("checkout", branch.getShortName());
    }

    @Override
    protected String doInBackground() throws Exception {
        Context.getGitRepoService().checkoutBranch(branch.getFullName(), progressMonitor);
        Context.updateBranches();
        Context.updateWorkingBranch();
        Context.updateStatus(null);
        return branch.getShortName();
    }

    @Override
    protected void onSuccess(String result) {
        statusBar.setStatus(I18n.t("checkedOut", result));
    }

    @Override
    protected void onError(Exception e) {
        CheckoutConflictException conflict = findConflictException(e);
        if (conflict == null) {
            super.onError(e);
            return;
        }

        List<String> files = conflict.getConflictingPaths();
        statusBar.clearProgress();
        statusBar.setStatus(I18n.t("checkoutFailedLocalChangesWould"));

        StringBuilder sb = new StringBuilder();
        sb.append(I18n.t("cannotCheckoutBranchNamed", branch.getShortName())).append("\n\n");
        sb.append(I18n.plural("filesWouldBeOverwritten", files.size()));
        for (String f : files) {
            sb.append("  \u2022 ").append(f).append("\n");
        }
        sb.append(I18n.t("commitStashYourChangesBefore"));

        JOptionPane.showMessageDialog(parent, sb.toString(),
                I18n.t("cannotCheckoutBranch"), JOptionPane.WARNING_MESSAGE);
    }

    /** Walks the cause chain to find a {@link CheckoutConflictException}. */
    private static CheckoutConflictException findConflictException(Throwable t) {
        while (t != null) {
            if (t instanceof CheckoutConflictException cce) {
                return cce;
            }
            t = t.getCause();
        }
        return null;
    }
}
