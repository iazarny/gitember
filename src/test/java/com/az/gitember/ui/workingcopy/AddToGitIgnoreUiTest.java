package com.az.gitember.ui.workingcopy;

import com.az.gitember.service.Context;
import com.az.gitember.ui.support.GitFixtures;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.core.MouseButton;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.fixture.JPopupMenuFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.jupiter.api.Test;

import javax.swing.JPopupMenu;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Adds an untracked working-copy file to {@code .gitignore} from the context menu and
 * verifies the file leaves the status list while remaining on disk.
 */
class AddToGitIgnoreUiTest extends SwingUiTestBase {

    @Test
    void addToGitIgnore_untrackedFile_leavesWorkingCopyAndWritesIgnore() throws Exception {
        GitFixtures.commitFile(repoDir, "a.txt", "1\n", "Initial commit");
        Files.writeString(repoDir.resolve("secret.env"), "TOKEN=x\n");

        window.menuItemWithPath("Working copy", "Refresh").click();
        Pause.pause(new Condition("secret.env to appear in working copy") {
            @Override
            public boolean test() {
                return Context.getStatusList() != null && Context.getStatusList().stream()
                        .anyMatch(i -> "secret.env".equals(i.getShortName()));
            }
        }, Timeout.timeout(10_000));

        int row = window.table("workingCopyTable").cell("secret.env").row();
        window.table("workingCopyTable").click(TableCell.row(row).column(1), MouseButton.RIGHT_BUTTON);

        JPopupMenu popup = robot.findActivePopupMenu();
        JPopupMenuFixture popupFixture = new JPopupMenuFixture(robot, popup);
        popupFixture.menuItemWithPath("Add to .gitignore").click();

        Pause.pause(new Condition("secret.env to leave working copy") {
            @Override
            public boolean test() {
                return Context.getStatusList() != null && Context.getStatusList().stream()
                        .noneMatch(i -> "secret.env".equals(i.getShortName()));
            }
        }, Timeout.timeout(10_000));

        assertTrue(Files.exists(repoDir.resolve("secret.env")));
        assertTrue(Files.readString(repoDir.resolve(".gitignore")).contains("/secret.env"));
    }
}
