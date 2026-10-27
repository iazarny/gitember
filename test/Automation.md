# Gitember Automated UI Test Plan

Companion to `Updated_plan.md` (146 manual test cases). This document proposes
a concrete, **100% free/open-source** automated UI testing stack for Gitember,
maps the manual test cases to automation feasibility, and gives runnable
example code that fits the project's existing test style
(`src/test/java/com/az/gitember/service/GitRepoServiceTest.java` — real
on-disk JGit repos, JUnit 5, no mocking of the git layer).

---

## 1. Why this stack

Gitember is a **Java 21 + Swing** desktop application (not web, not
JavaFX) invoked either as a full GUI (`MainFrame`) or as two small standalone
windows (`DiffViewerWindow`, `FolderCompareWindow`) via CLI args. That rules
out the two most commonly-reached-for free UI tools:

- **Selenium / Playwright / Cypress** — WebDriver/browser automation only. No path to a Swing `JFrame`.
- **TestFX** — JavaFX only; Gitember explicitly migrated *away* from JavaFX in v3.0 (see `CLAUDE.md`).

| Framework | License | Fit for Swing | Verdict |
|---|---|---|---|
| **AssertJ Swing** (`assertj-swing`, successor to FEST-Swing) | Apache 2.0 | Purpose-built: `java.awt.Robot`-driven, component fixtures for `JFrame`/`JDialog`/`JMenuItem`/`JTable`/`JTree`/`JList`/`JTextField` | **Recommended — primary driver** |
| Jemmy (NetBeans) | CDDL/GPLv2 | Similar capability, but maintenance has slowed and its API is heavier/older-style | Rejected — worse ergonomics, license friction (CDDL/GPL) vs Apache 2.0 |
| Marathon ITE | mixed OSS/commercial | Record/replay is nice for exploratory work but the OSS core lags modern Java Swing/FlatLaf and isn't JUnit5-native | Rejected — not a good CI citizen |
| Raw `java.awt.Robot` | JDK-bundled | Full control, zero dependency | Used **underneath** AssertJ Swing already; hand-rolling coordinate-based clicks directly is brittle and not recommended as the primary approach |

**AssertJ Swing** is the clear choice: free, Apache-2.0, actively usable on
Java 21, finds components by name/type/text (no pixel coordinates), and has
built-in polling/timeouts so tests don't need `Thread.sleep`.

To exercise the *real* git plumbing (clone/fetch/pull/push, LFS, SSH)
without depending on live GitHub/GitLab/etc., we pair AssertJ Swing with
tools already adjacent to the project's own dependency tree:

| Need | Tool | License | Notes |
|---|---|---|---|
| Local git server for clone/fetch/push/pull tests | **JGit test artifacts** (`org.eclipse.jgit.junit`, `org.eclipse.jgit.junit.http`) | EDL (BSD-style) | JGit ships an in-process Jetty-backed smart-HTTP server and a `git://` `Daemon` specifically for testing — same org as the project's main JGit dependency, zero new infra |
| SSH key auth (Ed25519/ECDSA, passphrase) without a real host | **Apache MINA SSHD** embeddable `SshServer` | Apache 2.0 | Already a transitive dependency via `org.eclipse.jgit.ssh.apache` |
| Stub GitHub/GitLab/Bitbucket/Gitea/Azure DevOps REST APIs, Gravatar, Ollama's local REST API | **WireMock** (`wiremock-standalone` or `wiremock-jre8`) | Apache 2.0 | Deterministic PR-list/avatar/LLM fixtures — no live-account flakiness, no rate limits |
| Fault injection (network disconnect mid-transfer, latency) | **Toxiproxy** (Shopify) | MIT | Optional — see §7; a hand-rolled blocking TCP proxy is a lighter-weight substitute if you want zero extra binaries |
| Headless CI display (Linux) | **Xvfb** | MIT/X11 | Windows/macOS runners have a real display already; only Linux CI needs this |
| Test runner / lifecycle | **JUnit 5** (already a dependency) | EPL 2.0 | Reuse `@Tag`, `@BeforeEach`/`@AfterEach`, no new runner needed |
| Separating slow UI tests from fast unit tests | **maven-failsafe-plugin** | Apache 2.0 (Maven core) | `mvn test` stays fast; `mvn verify` runs UI + integration tests |

Everything above is free and open source; nothing requires a paid license,
account, or SaaS subscription.

---

## 2. Project setup

### 2.1 New test dependencies (`pom.xml`)

```xml
<dependencies>
    <!-- ... existing dependencies ... -->

    <!-- AssertJ Swing — Swing UI driving/assertions -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-swing-junit</artifactId>
        <version>3.17.1</version>
        <scope>test</scope>
    </dependency>

    <!-- JGit test infrastructure — in-process smart-HTTP / git:// server -->
    <dependency>
        <groupId>org.eclipse.jgit</groupId>
        <artifactId>org.eclipse.jgit.junit</artifactId>
        <version>${jgit.version}</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.eclipse.jgit</groupId>
        <artifactId>org.eclipse.jgit.junit.http</artifactId>
        <version>${jgit.version}</version>
        <scope>test</scope>
    </dependency>

    <!-- Apache MINA SSHD embeddable server — SSH auth tests -->
    <dependency>
        <groupId>org.apache.sshd</groupId>
        <artifactId>sshd-scp</artifactId>
        <version>2.14.0</version>
        <scope>test</scope>
    </dependency>

    <!-- WireMock — stub GitHub/GitLab/Bitbucket/Gitea/Azure DevOps/Gravatar/Ollama HTTP APIs -->
    <dependency>
        <groupId>org.wiremock</groupId>
        <artifactId>wiremock-standalone</artifactId>
        <version>3.9.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 2.2 Directory layout

```
src/test/java/com/az/gitember/
  service/                     (existing — headless service-layer tests, unchanged)
  ui/
    support/
      SwingUiTestBase.java      base class: launches MainFrame against a temp repo, screenshot-on-failure
      GitFixtures.java          shared temp-repo / temp-remote builders (extends patterns from GitRepoServiceTest)
      LocalGitServer.java       wraps JGit junit.http test server for clone/fetch/push/pull tests
      LocalSshServer.java       wraps embeddable MINA SshServer for SSH auth tests
      WireMockForges.java       WireMock stub builders per forge (GitHub/GitLab/Bitbucket/Gitea/AzureDevOps) + Gravatar + Ollama
    workingcopy/
      StageUnstageUiTest.java
      ConflictResolutionUiTest.java
      RevertDeleteUiTest.java
    branchtag/
      BranchLifecycleUiTest.java
      TagLifecycleUiTest.java
      StashUiTest.java
    history/
      InteractiveRebaseUiTest.java
      CherryPickRevertResetUiTest.java
      BlameUiTest.java
    remote/
      CloneFetchPullPushUiTest.java
      SshAuthUiTest.java
      LfsUiTest.java
    dialogs/
      SettingsUiTest.java
      CredentialsUiTest.java
      WorkspaceUiTest.java
    integrations/
      PullRequestPanelUiTest.java
      AvatarUiTest.java
      SecretDetectionUiTest.java
    standalone/
      CliDiffViewerUiTest.java
      CliFolderCompareUiTest.java
```

`ui/` tests are tagged `@Tag("ui")`; the service-layer tests remain
untagged/`@Tag("fast")` and continue to run under `mvn test`.

### 2.3 Maven build wiring

```xml
<build>
    <plugins>
        <!-- keep mvn test fast: unit + service-layer tests only -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
            <configuration>
                <excludedGroups>ui</excludedGroups>
            </configuration>
        </plugin>

        <!-- mvn verify additionally runs the UI suite -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.2.5</version>
            <configuration>
                <groups>ui</groups>
                <forkCount>1</forkCount>
                <reuseForks>false</reuseForks> <!-- each UI test class gets a clean AWT/EDT state -->
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Commands:

```bash
mvn test              # fast unit/service tests only (unchanged CI gate)
mvn verify            # adds the AssertJ-Swing UI suite
mvn verify -Dgroups=ui -Dtest=BranchLifecycleUiTest   # a single UI test class
```

### 2.4 CI (GitHub Actions example — all free runners/tools)

```yaml
jobs:
  unit-tests:
    strategy:
      matrix: { os: [ubuntu-latest, windows-latest, macos-latest] }
    runs-on: ${{ matrix.os }}
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - run: mvn -B test

  ui-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - uses: coactions/setup-xvfb@v1   # free, MIT-licensed action wrapping Xvfb
        with:
          run: mvn -B verify -Dgroups=ui
      - uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: ui-failure-screenshots
          path: target/ui-test-screenshots/**
```

Windows/macOS have a real display in GitHub-hosted runners, so the UI job
could also be run there without Xvfb if you want native-L&F coverage
(FlatLaf renders slightly differently per OS) — recommended as a periodic
(nightly) job rather than on every PR, to keep PR feedback fast.

---

## 3. Base test harness (example code)

```java
// src/test/java/com/az/gitember/ui/support/SwingUiTestBase.java
package com.az.gitember.ui.support;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.swing.edt.GuiActionRunner.execute;

@Tag("ui")
public abstract class SwingUiTestBase {

    protected Path repoDir;
    protected Robot robot;
    protected FrameFixture window;

    @BeforeAll
    static void setUpOnce() {
        FailOnThreadViolationRepaintManager.install(); // catches EDT-safety bugs, not just test bugs
    }

    @BeforeEach
    void setUpRobotAndRepo(TestInfo info) throws Exception {
        repoDir = GitFixtures.newInitializedRepo();
        robot = BasicRobot.robotWithNewAwtHierarchy();

        JFrame frame = execute(() -> {
            com.az.gitember.ui.MainFrame f = com.az.gitember.ui.MainFrame.getInstance();
            f.init();
            return f;
        });
        window = new FrameFixture(robot, frame);
        window.show();
        openRepo(repoDir); // drives File -> Open Repository... with repoDir
    }

    @AfterEach
    void tearDown(TestInfo info, org.junit.jupiter.api.TestReporter reporter) throws Exception {
        try {
            if (Boolean.getBoolean("ui.test.failed")) {
                captureScreenshot(info.getDisplayName());
            }
        } finally {
            window.cleanUp();     // disposes frame, stops robot
            GitFixtures.delete(repoDir);
        }
    }

    private void captureScreenshot(String name) throws Exception {
        BufferedImage img = new java.awt.Robot().createScreenCapture(
                new java.awt.Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize()));
        File out = new File("target/ui-test-screenshots", name.replaceAll("\\W+", "_") + ".png");
        out.getParentFile().mkdirs();
        javax.imageio.ImageIO.write(img, "png", out);
    }

    protected void openRepo(Path dir) {
        window.menuItemWithPath("File", "Open Repository...").click();
        // JFileChooser fixture: AssertJ Swing finds the file-chooser dialog automatically
        org.assertj.swing.fixture.JFileChooserFixture chooser =
                org.assertj.swing.fixture.JFileChooserFixture.findFileChooser().using(robot);
        chooser.setCurrentDirectory(dir.toFile());
        chooser.approve();
    }
}
```

```java
// src/test/java/com/az/gitember/ui/support/GitFixtures.java
package com.az.gitember.ui.support;

import org.eclipse.jgit.api.Git;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

public final class GitFixtures {
    private GitFixtures() {}

    public static Path newInitializedRepo() throws Exception {
        Path dir = Files.createTempDirectory("gitember-ui-");
        try (Git git = Git.init().setDirectory(dir.toFile()).call()) {
            git.getRepository().getConfig().setString("user", null, "name", "UI Test");
            git.getRepository().getConfig().setString("user", null, "email", "ui-test@example.com");
            git.getRepository().getConfig().save();
        }
        return dir;
    }

    public static void delete(Path dir) throws IOException {
        if (dir == null || !Files.exists(dir)) return;
        Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(java.io.File::delete);
    }
}
```

This mirrors `GitRepoServiceTest`'s existing temp-directory-and-real-repo
pattern exactly — the UI layer of the test suite reuses the same fixtures
the service layer already trusts, so behavior differences surface as UI
bugs, not fixture drift.

---

## 4. Example UI tests (representative, not exhaustive)

### 4.1 TC-003 — Stage and commit a file

```java
class StageUnstageUiTest extends SwingUiTestBase {

    @Test
    void stageAndCommit_createsCommitVisibleInHistory() throws Exception {
        Files.writeString(repoDir.resolve("readme.txt"), "hello\n");
        window.menuItemWithPath("Working copy", "Refresh").click();

        window.table("workingCopyTable")           // component name set via setName() — see §5
              .cell("readme.txt").rightClick();
        robot.click(window.robot().finder().findByName("popup.stage", JMenuItem.class));

        window.menuItemWithPath("Branch", "Commit...").click();
        DialogFixture commitDialog = window.dialog();
        commitDialog.textBox("commitMessageArea").setText("Initial commit");
        commitDialog.button("commitButton").click();

        window.menuItemWithPath("Repository", "Statistics…"); // sanity nav still works post-commit

        try (Git git = Git.open(repoDir.toFile())) {
            assertEquals("Initial commit",
                    git.log().setMaxCount(1).call().iterator().next().getShortMessage());
        }
    }
}
```

### 4.2 TC-008 — Conflict resolution via "Using theirs (THEIRS)"

```java
class ConflictResolutionUiTest extends SwingUiTestBase {

    @Test
    void conflictedFile_usingTheirs_resolvesAndStages() throws Exception {
        // Arrange conflict directly via JGit (fast, deterministic) — UI test focuses on the
        // resolution UX, not on re-deriving merge mechanics already covered by GitRepoServiceTest.
        ConflictFixtures.createConflictedMerge(repoDir, "readme.txt", "mine\n", "theirs\n");
        window.menuItemWithPath("Working copy", "Refresh").click();

        window.table("workingCopyTable").cell("readme.txt").rightClick();
        robot.click(window.robot().finder().findByName("popup.usingTheirs", JMenuItem.class));

        assertEquals("theirs\n", Files.readString(repoDir.resolve("readme.txt")));
        window.table("workingCopyTable").cell("readme.txt")
              .requireValue("Resolved"); // status column, per WorkingCopyTableModel
    }
}
```

### 4.3 TC-047 — Interactive rebase reorder

```java
class InteractiveRebaseUiTest extends SwingUiTestBase {

    @Test
    void interactiveRebase_moveCommitUp_changesOrder() throws Exception {
        GitFixtures.commitFile(repoDir, "a.txt", "1", "commit A");
        GitFixtures.commitFile(repoDir, "b.txt", "2", "commit B");
        window.menuItemWithPath("Working copy", "Refresh").click();

        window.menuItemWithPath("Branch", "Interactive Rebase…").click();
        DialogFixture rebase = window.dialog();
        rebase.table("rebaseStepsTable").selectRows(1);   // "commit B" (newest-first display)
        rebase.button("moveDownButton").click();           // pushes B below A -> applies before A
        rebase.button("startRebaseButton").click();

        try (Git git = Git.open(repoDir.toFile())) {
            var log = git.log().call().iterator();
            assertEquals("commit A", log.next().getShortMessage()); // now HEAD
            assertEquals("commit B", log.next().getShortMessage());
        }
    }
}
```

### 4.4 TC-013/014/015/016 — Clone/Fetch/Pull/Push against a local JGit test server (no live GitHub)

```java
class CloneFetchPullPushUiTest extends SwingUiTestBase {

    LocalGitServer server; // wraps org.eclipse.jgit.junit.http test infra

    @BeforeEach
    void startServer() throws Exception {
        server = LocalGitServer.startWithBareRepo(); // http://localhost:<port>/repo.git
    }

    @AfterEach
    void stopServer() throws Exception { server.stop(); }

    @Test
    void clone_fromLocalServer_populatesBranchesAndHistory() throws Exception {
        window.menuItemWithPath("File", "Clone Repository...").click();
        DialogFixture clone = window.dialog();
        clone.textBox("urlField").setText(server.httpUrl());
        clone.textBox("destField").setText(Files.createTempDirectory("clone-dest-").toString());
        clone.button("cloneButton").click();

        window.tree("branchTree").requireVisible();
        // assert against Context/GitRepoService state or the tree model, same as GitRepoServiceTest would
    }
}
```

### 4.5 TC-107 — Theme switch persists across restart

```java
class SettingsUiTest extends SwingUiTestBase {

    @Test
    void switchToDarkTheme_persistsAcrossRestart() throws Exception {
        window.menuItemWithPath("File", "Settings...").click();
        DialogFixture settings = window.dialog();
        settings.comboBox("themeCombo").selectItem("Dark");
        settings.button("okButton").click();

        // simulate restart: re-read settings the same way App.main() does
        Context.readSettings();
        assertEquals("dark", Context.getSettings().getTheme());
    }
}
```

### 4.6 TC-123 — CLI standalone diff viewer

```java
class CliDiffViewerUiTest {

    @Test
    void twoFileArgs_opensStandaloneDiffViewer_noMainFrame() throws Exception {
        Path a = Files.writeString(Files.createTempFile("a", ".txt"), "one\n");
        Path b = Files.writeString(Files.createTempFile("b", ".txt"), "two\n");

        Thread appThread = new Thread(() ->
                com.az.gitember.App.main(new String[]{ a.toString(), b.toString() }));
        appThread.start();

        FrameFixture diffWindow = WindowFinder.findFrame(DiffViewerWindow.class)
                .withTimeout(5000).using(BasicRobot.robotWithNewAwtHierarchy());
        diffWindow.requireVisible();
        diffWindow.cleanUp();
    }
}
```

### 4.7 TC-095 — Secret-leak detection (empirical mode, deterministic, no Ollama)

```java
class SecretDetectionUiTest extends SwingUiTestBase {

    @Test
    void stagingAwsKeyShapedString_flagsFindingBeforeCommit() throws Exception {
        Context.getSettings().setEnableLeakDetector(true); // empirical mode: no Ollama dependency
        Files.writeString(repoDir.resolve("config.txt"), "AWS_KEY=AKIAABCDEFGHIJKLMNOP\n");
        window.menuItemWithPath("Working copy", "Refresh").click();
        stage("config.txt");

        window.menuItemWithPath("Branch", "Commit...").click();
        DialogFixture commit = window.dialog();
        commit.label("findingsLabel").requireVisible();
        commit.button("cancelButton").click(); // TC-100 path in the same test's teardown assertion
    }
}
```

---

## 5. One prerequisite change to the production code: component names

AssertJ Swing can find components by type, text, or **name**
(`setName(...)`). Gitember's Swing components mostly aren't named today,
which forces brittler lookups (`finder().findByType(...)` + index, or text
matching on button labels that might be localized later). Before writing
the bulk of the UI suite, add `setName("...")` calls to the ~40 components
actually exercised by tests — this is a small, low-risk, additive change:

```java
// dialog/CommitDialog.java
commitBtn.setName("commitButton");
cancelBtn.setName("cancelButton");
findingsLabel.setName("findingsLabel");

// ui/WorkingCopyPanel.java
table.setName("workingCopyTable");

// dialog/SettingsDialog.java
themeCombo.setName("themeCombo");
```

This is the single highest-leverage prerequisite: without it, every test
above becomes noticeably more fragile (index-based lookups break the
moment a panel gains one more button).

---

## 6. Feasibility map — manual TC ranges → automation approach

| TC range | Area | Automation approach | Priority |
|---|---|---|---|
| TC-001–005, 019–034 | Init/add/commit, working-copy context menu | AssertJ Swing + real temp repo (no mocks) | **P0 — Phase 1** |
| TC-004, 035–046 | Branch/tag/stash | AssertJ Swing + real temp repo | **P0 — Phase 1** |
| TC-006–008, 037–038, 051 | Merge/conflict/rebase-conflict UX | AssertJ Swing; conflict state prepared via direct JGit calls (fast, deterministic), UI test covers only the resolution widgets | **P0 — Phase 1** |
| TC-047–056 | Interactive rebase, cherry-pick, revert, reset, blame | AssertJ Swing + real temp repo | **P1 — Phase 2** |
| TC-057–062 | Submodules, worktrees | AssertJ Swing + a second temp repo added as submodule/worktree | **P1 — Phase 2** |
| TC-013–017, 063–070 | Clone/fetch/pull/push, LFS | AssertJ Swing + **JGit `junit.http` local test server** (no live remote) | **P1 — Phase 2** |
| TC-115–120 | Clone auth modes, SSH | AssertJ Swing (dialog field visibility) + **embeddable MINA `SshServer`** for real key-based auth round-trips | **P2 — Phase 3** |
| TC-071–082 | Search/indexing, statistics | AssertJ Swing + real temp repo with scripted commit history | **P1 — Phase 2** |
| TC-083–090 | Pull Request panel (5 forges) | AssertJ Swing + **WireMock** stubs per forge (deterministic PR fixtures, no rate limits, tests all 5 forges without 5 real accounts) | **P2 — Phase 3** |
| TC-091–094 | Avatars | AssertJ Swing + WireMock (forge avatar endpoints + Gravatar) | **P2 — Phase 3** |
| TC-095–100 | Secret detection, empirical mode | AssertJ Swing, fully deterministic (regex/entropy detectors have no network dependency) | **P0 — Phase 1** (cheap, high value) |
| TC-101–106 | LLM mode / AI features (Ollama) | AssertJ Swing + WireMock stubbing Ollama's local REST API — **do not** assert exact generated text (non-deterministic even mocked-through); assert on flow/state (checkbox reverts, findings table populated, generate button enables field) | **P2 — Phase 3** |
| TC-107–114 | Settings & credentials | AssertJ Swing; TC-111 (masking) is better as a **service-layer** JSON-round-trip test (`ProjectJsonRoundTripTest.java` already exists — extend it) than a UI test | **P0 — Phase 1** |
| TC-121–126 | Diff/folder compare + CLI standalone | AssertJ Swing, incl. launching `App.main()` directly for the two standalone-window cases | **P1 — Phase 2** |
| TC-127–132 | Workspace mode | AssertJ Swing + N temp repos | **P2 — Phase 3** |
| TC-133, 135–138 | Recent projects, compress DB, about/help, shortcuts | AssertJ Swing | **P2 — Phase 3** |
| TC-134 | Open Terminal / Open Explorer | **Not recommended to automate** — spawns real OS chrome (Explorer/Terminal/Finder) outside the JVM; assert only that the menu item exists and doesn't throw, via a fake `Desktop`/process-launch seam if one is introduced, otherwise leave manual | Manual only |
| TC-139 | Network disconnect mid-transfer | **WireMock fault injection** (`Fault.CONNECTION_RESET_BY_PEER`) for HTTP-based ops (PR/avatar); for git-protocol clone/push, a small blocking TCP proxy killed mid-stream, or **Toxiproxy** if you want a reusable tool | **P3 — Phase 4 (resilience)** |
| TC-140 | Kill process mid-operation | Hard to automate faithfully in-JVM (you'd be killing your own test JVM); approximate by interrupting the worker thread / injecting an `IOException` mid-`SwingWorker.doInBackground()` and asserting recovery-on-reopen instead | **P3 — Phase 4**, partial coverage only |
| TC-141–144 | Uncommitted changes / conflict-on-open / detached HEAD / read-only files | AssertJ Swing + real temp repo prepared into the exact state via direct JGit/filesystem calls | **P2 — Phase 3** |
| TC-145 | Unicode/space paths | AssertJ Swing, parametrized `@ValueSource` over path variants | **P1 — Phase 2** (cheap, high regression value on Windows) |
| TC-146 | Large repo (>100k commits) / >5GB binary | **Not a CI-per-PR test** — belongs in a separate, manually-triggered or nightly performance lane (generate the large repo once, cache it, measure load time budget) | Nightly/perf lane, not PR gate |
| TC-012, 077–082 vs GitHub UI comparisons | Statistics correctness vs `git shortlog` | Assert against **JGit-computed** expected values in the test (same approach `GitRepoServiceTest` already uses for correctness), not against a human eyeballing the GitHub web UI | **P1 — Phase 2** |

---

## 7. Fault-injection detail for TC-139/140 (network disconnect)

Two free options, in order of recommendation:

1. **WireMock `Fault` responses** — for anything going over `PullRequestService`/`AvatarService`/`LlmSecretDetector`'s HTTP calls, stub the endpoint to return `Fault.CONNECTION_RESET_BY_PEER` or `Fault.EMPTY_RESPONSE` mid-test. Zero extra infrastructure since WireMock is already in the stack for TC-083–094/101.
2. **Toxiproxy** (MIT) — for genuine git-protocol-level disconnects during clone/fetch/push against the JGit local test server, run the smart-HTTP test server behind a Toxiproxy instance and call `toxiproxy.toxics().bandwidth(...)`/`.timeout(...)` or simply `proxy.disable()` mid-transfer. Requires running the Toxiproxy binary (Docker image or a downloaded executable) alongside the JVM — heavier than WireMock, so reserve it for the dedicated Phase 4 resilience suite rather than every PR.

A same-JVM alternative that avoids any extra binary: wrap the local JGit
test server's `ServerSocket`/Jetty connector in a small pass-through proxy
thread you write yourself (~40 lines), and have the test call
`proxy.close()` partway through a large clone. This is the lowest-dependency
option if the team prefers not to add Toxiproxy as a build/CI dependency.

---

## 8. Flakiness mitigation checklist

- **Never `Thread.sleep()`** in a UI test. Use AssertJ Swing's built-in
  polling (`Pause.pause(condition, timeout)`, fixture `require*` methods,
  which already retry) or `org.awaitility:awaitility` (Apache 2.0, free) for
  waiting on `Context` property-change events fired off the EDT.
- Run UI tests with `reuseForks=false` per class (see §2.3) — Swing's static
  `UIManager`/L&F state and `MainFrame.getInstance()` singleton leak across
  tests in the same JVM otherwise.
- `FailOnThreadViolationRepaintManager.install()` (see §3) turns "worked on
  my machine" EDT-safety bugs into hard test failures instead of
  intermittent CI flakes.
- Screenshot-on-failure (see §3 `tearDown`) turns a flaky CI failure into a
  20-second diagnosis instead of an unreproducible mystery.
- Keep LLM/Ollama-backed assertions (TC-101–106) behavioral (did the flow
  complete / did the field populate) rather than content-based (exact
  generated text) — this is true even against a WireMock-stubbed Ollama,
  since the point of stubbing there is determinism of the *stub*, but you
  still shouldn't over-couple tests to prompt-engineering details that will
  change.

---

## 9. Rollout plan

| Phase | Scope | Rough size |
|---|---|---|
| **0 — Foundations** | Add AssertJ Swing dep, `SwingUiTestBase`, `GitFixtures`, component `setName()` pass (§5), wire Failsafe + `@Tag("ui")` split, CI job with Xvfb | 1 module, no new test cases yet |
| **1 — Core CRUD (P0)** | TC-001–046 minus network/AI: init, add, commit, branch, tag, stash, working-copy context menu, conflict-resolution widgets, empirical secret detection, settings persistence | ~50 tests |
| **2 — History & local remotes (P1)** | TC-047–082, 013–017/063–070 (via JGit local server), 121–126, 145 | ~55 tests |
| **3 — Integrations & multi-repo (P2)** | TC-083–120 (via WireMock + MINA SshServer), 127–138, 141–144 | ~55 tests |
| **4 — Resilience (P3, separate CI lane)** | TC-139–140 fault injection, TC-146 large-repo/perf lane (nightly, not PR-gating) | ~10 tests + 1 perf job |

This mirrors the manual plan's own TC-range grouping, so manual and
automated coverage can be tracked side by side and gaps are visible at a
glance.
