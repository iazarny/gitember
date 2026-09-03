package com.az.gitember.ui.workingcopy;

import com.az.gitember.service.Context;
import com.az.gitember.ui.support.GitFixtures;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.core.MouseButton;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Amends the last commit from {@code CommitDialog}: extra staged file is folded into HEAD
 * and the commit message is replaced, without adding a second commit.
 */
class AmendCommitUiTest extends SwingUiTestBase {

    @Test
    void amendLastCommit_updatesMessageAndIncludesStagedFile() throws Exception {
        GitFixtures.commitFile(repoDir, "a.txt", "1\n", "Initial commit");
        Files.writeString(repoDir.resolve("b.txt"), "2\n");

        window.menuItemWithPath("Working copy", "Refresh").click();
        Pause.pause(new Condition("b.txt to appear in working copy") {
            @Override
            public boolean test() {
                return Context.getStatusList() != null && Context.getStatusList().stream()
                        .anyMatch(i -> "b.txt".equals(i.getShortName()));
            }
        }, Timeout.timeout(10_000));

        int row = window.table("workingCopyTable").cell("b.txt").row();
        window.table("workingCopyTable").click(TableCell.row(row).column(1), MouseButton.LEFT_BUTTON);

        Pause.pause(new Condition("b.txt to be staged") {
            @Override
            public boolean test() {
                return Context.getStatusList().stream()
                        .filter(i -> "b.txt".equals(i.getShortName()))
                        .anyMatch(i -> i.isStaged());
            }
        }, Timeout.timeout(10_000));

        window.menuItemWithPath("Branch", "Commit ...").click();
        DialogFixture commitDialog = window.dialog();
        commitDialog.checkBox("amendCheckBox").requireEnabled().check();
        commitDialog.textBox("commitMessageArea").requireText("Initial commit");
        commitDialog.textBox("commitMessageArea").setText("Amended commit");
        commitDialog.button("commitButton").click();

        Pause.pause(new Condition("commit dialog to close") {
            @Override
            public boolean test() {
                return !commitDialog.target().isShowing();
            }
        }, Timeout.timeout(10_000));

        try (Git git = Git.open(repoDir.toFile())) {
            List<RevCommit> commits = new ArrayList<>();
            git.log().call().forEach(commits::add);
            assertEquals(1, commits.size(), "Amend must not add a second commit");
            assertEquals("Amended commit", commits.get(0).getShortMessage());
        }
        assertTrue(GitFixtures.filesInHead(repoDir).contains("a.txt"));
        assertTrue(GitFixtures.filesInHead(repoDir).contains("b.txt"));
    }
}
