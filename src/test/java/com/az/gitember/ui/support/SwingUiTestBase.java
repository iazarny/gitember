package com.az.gitember.ui.support;

import com.az.gitember.service.Context;
import com.az.gitember.ui.MainFrame;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.assertj.swing.timing.Pause;
import org.assertj.swing.timing.Timeout;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

import static org.assertj.swing.edt.GuiActionRunner.execute;

/**
 * Base class for AssertJ-Swing UI tests: launches a fresh {@link MainFrame} against a real
 * temp on-disk repo (via {@link GitFixtures}) and a sandboxed {@code user.home} so the suite
 * never touches the developer's real {@code ~/.gitember} settings.
 *
 * <p>One {@code @Test} method per subclass is strongly recommended: {@code MainFrame} and
 * {@code Context} hold static/singleton state that is only guaranteed clean per forked JVM
 * (see the {@code reuseForks=false} failsafe config), not per test method.
 */
@Tag("ui")
@ExtendWith(SwingUiTestBase.ScreenshotOnFailure.class)
public abstract class SwingUiTestBase {

    protected Path repoDir;
    protected Path fakeHome;
    protected Robot robot;
    protected FrameFixture window;

    @BeforeAll
    static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install();
    }

    @BeforeEach
    void setUpRobotAndRepo() throws Exception {
        // Sandbox settings persistence so the UI suite never reads/writes the developer's
        // real ~/.gitember/gitember2.json.
        fakeHome = java.nio.file.Files.createTempDirectory("gitember-ui-home-");
        System.setProperty("user.home", fakeHome.toString());

        if (openDefaultRepo()) {
            repoDir = GitFixtures.newInitializedRepo();
        }
        robot = BasicRobot.robotWithNewAwtHierarchy();

        JFrame frame = execute(() -> {
            Context.readSettings();
            MainFrame f = MainFrame.getInstance();
            f.init();
            return f;
        });
        window = new FrameFixture(robot, frame);
        window.show();
        if (openDefaultRepo()) {
            openRepo(repoDir);
        }
    }

    /**
     * Whether {@link #setUpRobotAndRepo()} should create {@link #repoDir} and open it as the
     * active repository before the test body runs — the right starting point for the single
     * repository tests that make up most of the suite.
     *
     * <p>Tests that drive several repositories at once (workspace tests) override this to
     * {@code false} and start from the welcome screen, creating and registering their own
     * repositories instead.
     */
    protected boolean openDefaultRepo() {
        return true;
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            window.cleanUp();
        } finally {
            GitFixtures.delete(repoDir);
            GitFixtures.delete(fakeHome);
        }
    }

    /**
     * Opens {@code dir} as the active repository by calling {@link Context#init(String)}
     * directly on this (background) thread — exactly what {@code OpenRepoHandler} does from
     * its {@code SwingWorker.doInBackground()} in the real app.
     *
     * <p>Driving this through the actual File -> Open Repository... menu + {@code JFileChooser}
     * was tried and dropped: a directories-only {@code JFileChooser} needs a real filesystem
     * click/selection to populate {@code getSelectedFile()} (typing a path into the filename
     * field and clicking Approve isn't enough), which makes it a UI-robustness problem of its
     * own and isn't what these tests are about. Opening a repo isn't the behavior under test
     * here — staging, committing, branching, etc. are — so we skip straight to the state those
     * tests need, the same way {@code ConflictFixtures}-style direct-JGit setup is used
     * elsewhere for prerequisite state that isn't the focus of a given test.
     */
    protected void openRepo(Path dir) throws Exception {
        Context.init(dir.toFile().getAbsolutePath());

        // Context.getRepositoryPath() returns the active project's .git dir, not the working
        // tree path we passed in above, so just confirm a project actually got activated.
        Pause.pause(new Condition("repository to open") {
            @Override
            public boolean test() {
                return Context.getRepositoryPath() != null;
            }
        }, Timeout.timeout(10_000));

        // ContentPanel swaps its single child in/out on view switches, so the working-copy
        // table/tree/etc. only exist in the component hierarchy once this view is active.
        execute(() -> {
            MainFrame.getInstance().activateProjectWorkingCopy();
            return null;
        });
    }

    static final class ScreenshotOnFailure implements TestWatcher {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            try {
                BufferedImage img = new java.awt.Robot().createScreenCapture(
                        new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                File out = new File("target/ui-test-screenshots",
                        context.getDisplayName().replaceAll("\\W+", "_") + ".png");
                out.getParentFile().mkdirs();
                ImageIO.write(img, "png", out);
            } catch (Exception ignored) {
                // best-effort diagnostics only
            }
        }
    }
}
