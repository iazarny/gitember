package com.az.gitember.ui.workingcopy;

import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.ui.support.GitFixtures;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.core.MouseButton;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.fixture.JPopupMenuFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.eclipse.jgit.api.MergeResult;
import org.junit.jupiter.api.Test;

import javax.swing.JPopupMenu;
import java.nio.file.Files;
import java.util.List;

import static com.az.gitember.service.GitemberUtil.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TC-008-range — merge two branches with conflicting changes to the same file, then resolve
 * the conflict from the working-copy context menu ("Resolve conflict" > "Using theirs
 * (THEIRS)") and verify the file is staged and no longer conflicted.
 */
class CommitAndConflictUITest extends SwingUiTestBase {

    @Test
    void resolveConflict_usingTheirs_stagesFileAndClearsConflict() throws Exception {

        GitFixtures.commitFile(repoDir, "readme.txt", "Lorem ipsum\n", "Initial commit");

        String masterBranchName = GitFixtures.getBranchName(repoDir);
        System.out.println(">>>> " + repoDir);

        GitFixtures.createBranch(repoDir, "branch1");
        GitFixtures.checkoutBranch(repoDir, "branch1");

        Files.writeString(repoDir.resolve("readme.txt"), "Lorem ipsum dolor sit amet, consectetur adipiscing elit\n");
        GitFixtures.commitFile(repoDir, "readme.txt",  "commit to the branch1");

        GitFixtures.checkoutBranch(repoDir, masterBranchName);

        GitFixtures.createBranch(repoDir, "branch2");
        GitFixtures.checkoutBranch(repoDir, "branch2");

        Files.writeString(repoDir.resolve("readme.txt"), "Lorem ipsum two beer or not two beers, consectetur adipiscing elit\n");
        GitFixtures.commitFile(repoDir, "readme.txt",  "commit to the branch2");


        MergeResult result = GitFixtures.mergeBranch(repoDir, "branch1", masterBranchName);
        assertEquals(MergeResult.MergeStatus.FAST_FORWARD, result.getMergeStatus());

        result = GitFixtures.mergeBranch(repoDir, "branch2", masterBranchName);
        assertEquals(MergeResult.MergeStatus.CONFLICTING, result.getMergeStatus());


        window.menuItemWithPath("Working copy", "Refresh").click();
        Pause.pause(new Condition("readme.txt to appear in working copy") {
            @Override
            public boolean test() {
                List<ScmItem> items = Context.getStatusList();
                ScmItem item = items.get(0);
                return is(item.getAttribute().getStatus()).oneOf( ScmItem.Status.CONFLICT) && "readme.txt".equals(item.getShortName());
            }
        }, Timeout.timeout(10_000));

        int row = window.table("workingCopyTable").cell("readme.txt").row();
        window.table("workingCopyTable").click(TableCell.row(row).column(1), MouseButton.RIGHT_BUTTON);

        JPopupMenu popup = robot.findActivePopupMenu();
        JPopupMenuFixture popupFixture = new JPopupMenuFixture(robot, popup);
        popupFixture.menuItemWithPath("Resolve conflict", "Using theirs (THEIRS)").click();

        Pause.pause(new Condition("conflict on readme.txt to be resolved") {
            @Override
            public boolean test() {
                List<ScmItem> items = Context.getStatusList();
                return items != null && items.stream().anyMatch(i ->
                        "readme.txt".equals(i.getShortName()) && i.isStaged())
                        && items.stream().noneMatch(i ->
                        "readme.txt".equals(i.getShortName())
                                && ScmItem.Status.CONFLICT.equals(i.getAttribute().getStatus()));
            }
        }, Timeout.timeout(10_000));
    }
}
