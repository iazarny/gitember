package com.az.gitember.ui.workingcopy;

import com.az.gitember.service.Context;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.core.MouseButton;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for a bug where {@code CommitDialog.getDialogTitle()} NPEs on the very first
 * commit in a brand-new repo: {@code Context.getWorkingBranch()} used to return null on an
 * unborn HEAD (no {@code refs/heads/*} yet), which the title logic dereferenced unconditionally.
 * Fixed in {@code Project.updateWorkingBranch()} by falling back to
 * {@code GitRepoService.getCurrentScmBranch()}, which reads the symbolic HEAD target directly.
 */
class FirstCommitUiTest extends SwingUiTestBase {

    @Test
    void commitDialog_opensAndCommits_onUnbornHead() throws Exception {
        Files.writeString(repoDir.resolve("readme.txt"), "hello\n");

        window.menuItemWithPath("Working copy", "Refresh").click();
        Pause.pause(new Condition("readme.txt to appear in working copy") {
            @Override
            public boolean test() {
                return Context.getStatusList() != null && Context.getStatusList().stream()
                        .anyMatch(i -> "readme.txt".equals(i.getShortName()));
            }
        }, Timeout.timeout(10_000));

        int row = window.table("workingCopyTable").cell("readme.txt").row();
        window.table("workingCopyTable").click(TableCell.row(row).column(1), MouseButton.LEFT_BUTTON);

        Pause.pause(new Condition("readme.txt to be staged") {
            @Override
            public boolean test() {
                return Context.getStatusList().stream()
                        .filter(i -> "readme.txt".equals(i.getShortName()))
                        .anyMatch(i -> i.isStaged());
            }
        }, Timeout.timeout(10_000));

        window.menuItemWithPath("Branch", "Commit...").click();
        DialogFixture commitDialog = window.dialog();
        commitDialog.textBox("commitMessageArea").enterText("Initial commit");
        commitDialog.button("commitButton").click();

        Pause.pause(new Condition("commit dialog to close") {
            @Override
            public boolean test() {
                return !commitDialog.target().isShowing();
            }
        }, Timeout.timeout(10_000));

        try (Git git = Git.open(repoDir.toFile())) {
            assertEquals("Initial commit",
                    git.log().setMaxCount(1).call().iterator().next().getShortMessage());
        }
    }
}
