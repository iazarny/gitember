package com.az.gitember.ui.workingcopy;

import com.az.gitember.ui.support.GitFixtures;
import com.az.gitember.ui.support.SwingUiTestBase;
import org.assertj.swing.finder.WindowFinder;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Opens Project Settings, edits {@code .gitignore} in the file-viewer window, saves, and
 * verifies the pattern is written to disk.
 */
class EditGitIgnoreUiTest extends SwingUiTestBase {

    @Test
    void projectSettings_editGitIgnore_writesFile() throws Exception {
        GitFixtures.commitFile(repoDir, "a.txt", "1\n", "Initial commit");

        window.menuItemWithPath("Repository", "Project Settings…").click();
        DialogFixture settings = window.dialog();
        settings.button("editGitIgnoreButton").click();

        FrameFixture editor = WindowFinder.findFrame("fileViewerWindow")
                .withTimeout(10_000)
                .using(robot);
        editor.textBox("fileContentArea").setText("/secret.env\n");
        editor.button("saveFileButton").click();
        editor.button("closeFileButton").click();
        settings.button("Cancel").click();

        assertTrue(Files.readString(repoDir.resolve(".gitignore")).contains("/secret.env"));
    }
}
