package com.az.gitember.ui.branchtag;

import com.az.gitember.service.Context;
import com.az.gitember.ui.support.GitFixtures;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TC-004-range — Branch > Create... prompts via a plain {@link javax.swing.JOptionPane}
 * input dialog (see {@code CreateBranchHandler}); verify the new branch is created from HEAD
 * and checked out, both on disk and in {@code Context}'s in-memory working branch.
 */
class BranchLifecycleUiTest extends SwingUiTestBase {

    @Test
    void createBranch_fromCurrentHead_createsAndChecksOutBranch() throws Exception {
        GitFixtures.commitFile(repoDir, "a.txt", "1\n", "Initial commit");

        window.menuItemWithPath("Branch", "Branch ...").click();

        JOptionPaneFixture inputDialog = JOptionPaneFinder.findOptionPane().using(robot);
        inputDialog.textBox().enterText("feature/test-branch");
        inputDialog.okButton().click();

        Pause.pause(new Condition("new branch to be checked out on disk") {
            @Override
            public boolean test() {
                try (Git git = Git.open(repoDir.toFile())) {
                    return "refs/heads/feature/test-branch".equals(git.getRepository().getFullBranch());
                } catch (Exception e) {
                    return false;
                }
            }
        }, Timeout.timeout(10_000));

        try (Git git = Git.open(repoDir.toFile())) {
            assertEquals("refs/heads/feature/test-branch", git.getRepository().getFullBranch());
        }

        Pause.pause(new Condition("Context working branch to reflect the checkout") {
            @Override
            public boolean test() {
                return Context.getWorkingBranch() != null
                        && "feature/test-branch".equals(Context.getWorkingBranch().getShortName());
            }
        }, Timeout.timeout(10_000));
    }
}
