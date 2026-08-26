package com.az.gitember.ui.integrations;

import com.az.gitember.service.Context;
import com.az.gitember.ui.support.GitFixtures;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.core.MouseButton;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

/**
 * TC-095 — empirical-mode secret detection: staging an AWS-key-shaped string surfaces the
 * findings panel in {@code CommitDialog} before the user can commit. No Ollama/LLM dependency
 * (see {@code DetectorService}'s empirical fallback when no local Ollama is running).
 *
 * <p>Starts from a repo with one existing commit — see {@code StageUnstageUiTest}'s javadoc for
 * why a totally empty (zero-commit) repo currently NPEs when {@code CommitDialog} opens.
 */
class SecretDetectionUiTest extends SwingUiTestBase {

    @Test
    void stagingAwsKeyShapedString_flagsFindingBeforeCommit() throws Exception {
        GitFixtures.commitFile(repoDir, "a.txt", "1\n", "Initial commit");
        Context.getSettings().setEnableLeakDetector(true);

        Files.writeString(repoDir.resolve("config.txt"), "AWS_KEY=AKIAIOSFODNN7EXAMPLE\n");

        window.menuItemWithPath("Working copy", "Refresh").click();
        Pause.pause(new Condition("config.txt to appear in working copy") {
            @Override
            public boolean test() {
                return Context.getStatusList() != null && Context.getStatusList().stream()
                        .anyMatch(i -> "config.txt".equals(i.getShortName()));
            }
        }, Timeout.timeout(10_000));

        int row = window.table("workingCopyTable").cell("config.txt").row();
        window.table("workingCopyTable").click(TableCell.row(row).column(1), MouseButton.LEFT_BUTTON);

        Pause.pause(new Condition("config.txt to be staged") {
            @Override
            public boolean test() {
                return Context.getStatusList().stream()
                        .filter(i -> "config.txt".equals(i.getShortName()))
                        .anyMatch(i -> i.isStaged());
            }
        }, Timeout.timeout(10_000));

        window.menuItemWithPath("Branch", "Commit...").click();
        DialogFixture commitDialog = window.dialog();

        Pause.pause(new Condition("secret-detector findings to appear") {
            @Override
            public boolean test() {
                return findingsLabelShowing(commitDialog);
            }
        }, Timeout.timeout(20_000));

        commitDialog.label("findingsLabel").requireVisible();
        commitDialog.button("cancelButton").click();
    }

    private static boolean findingsLabelShowing(DialogFixture dialog) {
        try {
            return dialog.label("findingsLabel").target().isShowing();
        } catch (Exception notFoundYet) {
            return false;
        }
    }
}
