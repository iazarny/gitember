# Gitember Manual Test Plan (Expanded)

This plan expands `Gitember_Detailed_Manual_Test_Plan.md` with full coverage of
Gitember's feature set, derived from the application source
(`com.az.gitember` — `ui/`, `dialog/`, `service/`, `handler/`).

## Conventions

Test root directory:
- Windows: `C:\Projects\tmp`
- Linux/macOS: `~/Projects/tmp`

Repository name used in examples: `gittestrepo1`

Test case ID ranges (by feature area), so new cases can be inserted later
without renumbering existing ones:

| Range | Area |
|---|---|
| TC-001 – TC-018 | Baseline (original plan): init, add, commit, branch, tag, merge, conflict, submodule, worktree, search, stats, clone/fetch/pull/push, LFS, workspace |
| TC-019 – TC-034 | Working copy operations & context menu |
| TC-035 – TC-046 | Branch, tag, stash context-menu operations |
| TC-047 – TC-056 | History operations: rebase, cherry-pick, revert, reset, blame |
| TC-057 – TC-062 | Submodules & worktrees (dialog-level) |
| TC-063 – TC-070 | Git LFS |
| TC-071 – TC-076 | Search & indexing |
| TC-077 – TC-082 | Statistics & activity charts |
| TC-083 – TC-090 | Pull Request panel (multi-forge) |
| TC-091 – TC-094 | Avatars |
| TC-095 – TC-102 | Secret-leak detection (empirical & LLM/Ollama) |
| TC-103 – TC-106 | AI features (commit message, branch compare description) |
| TC-107 – TC-114 | Settings & Credentials |
| TC-115 – TC-120 | Clone (auth modes) & SSH |
| TC-121 – TC-126 | Diff/compare tools & CLI standalone mode |
| TC-127 – TC-132 | Workspace mode (multi-repo) |
| TC-133 – TC-138 | Application chrome: menus, recent projects, terminal/explorer, DB compress |
| TC-139 – TC-146 | Resilience / negative / edge cases |

Expected Result section describes success criteria.
Verification section explains how a tester validates the result (usually a
plain `git` command run outside Gitember, treated as ground truth).

---

# TC-001 Initialize Local Repository

## Purpose
Verify a new Git repository can be created and opened in Gitember.

## Preconditions
- Git installed.
- Gitember installed.
- Directory Projects/tmp exists.

## Steps (Windows)

1. Open Command Prompt.
2. Execute:

```cmd
cd C:\Projects\tmp
mkdir gittestrepo1
cd gittestrepo1
```

3. Open Gitember.
4. Select:

```text
File -> Init Repository...
```

5. Choose:

```text
C:\Projects\tmp\gittestrepo1
```

## Steps (Linux/macOS)

```bash
mkdir -p ~/Projects/tmp/gittestrepo1
```

Open Gitember and initialize repository in this folder.

## Expected Result

- Repository initializes successfully.
- Current branch displayed.
- Working tree is clean.
- No errors shown.
- Repository is added to `File -> Open Recent`.

## Verification

Windows:

```cmd
dir /a
```

Linux/macOS:

```bash
ls -la
```

Verify `.git` directory exists.

---

# TC-002 Add New File

## Steps

Create file externally.

Windows:

```cmd
echo Hello Gitember>readme.txt
```

Linux/macOS:

```bash
echo "Hello Gitember" > readme.txt
```

Refresh Gitember if necessary (`Working copy -> Refresh`, F5).

## Expected Result

- `readme.txt` appears as untracked.
- Status color/icon indicates new file.
- `ProjectWatcher` picks up the change automatically without requiring manual refresh (verify both paths: auto-detect and manual F5).

## Verification

Open Working Tree view.

Confirm:

```text
readme.txt = Untracked
```

---

# TC-003 Stage And Commit File

## Steps

1. Stage `readme.txt` (checkbox, or right-click -> Stage).
2. Enter commit message:

```text
Initial commit
```

3. Press Commit.

## Expected Result

- Commit created.
- Working tree becomes clean.
- Commit visible in history.
- Commit message is saved into recent-messages history (`Settings.commitMsg`) and offered again via autocomplete on next commit.

## Verification

Git:

```bash
git log --oneline -1
```

Expected:

```text
<hash> Initial commit
```

---

# TC-004 Branch Creation

## Steps

1. Open Branches (`Branch -> Create...` or right-click in tree).
2. Create branch:

```text
feature/login
```

3. Checkout branch.

## Expected Result

- Branch created.
- HEAD switched to branch.

## Verification

```bash
git branch --show-current
```

Expected:

```text
feature/login
```

---

# TC-005 External File Modification Detection

## Windows

```cmd
echo Updated>>readme.txt
```

## Linux/macOS

```bash
echo "Updated" >> readme.txt
```

## Expected Result

- Gitember detects modified file.
- Refresh occurs automatically (`ProjectWatcher`) or manually (F5).

## Verification

```bash
git status
```

Expected:

```text
modified: readme.txt
```

---

# TC-006 Tag Creation

## Steps

Create annotated tag via `Create tag...` (from Branch context menu on a commit/branch):

```text
v1.0.0
```

Message:

```text
First release
```

## Expected Result

- Tag appears in Tags view.

## Verification

```bash
git tag
```

Verify:

```text
v1.0.0
```

---

# TC-007 Merge Branches

## Steps

1. Create branch `feature-a`.
2. Commit change.
3. Checkout main.
4. Right-click `feature-a` -> `Merge feature-a -> main...`.

## Expected Result

- Merge completes.
- History shows merge (or fast-forward, depending on ancestry).
- `MergeDialog` reports the merge status/message returned by `getMessage(mergeResult)`.

## Verification

```bash
git log --graph --oneline
```

Verify merge commit exists when applicable.

---

# TC-008 Conflict Resolution

## Setup

```bash
git checkout -b branchA
```

Modify first line of readme.
Commit.

```bash
git checkout main

git checkout -b branchB
```

Modify same line differently.
Commit.

Checkout main.

```bash
git merge branchA
```

Then:

```bash
git merge branchB
```

## Expected Result

- Gitember shows conflict in Working Copy panel (`ConflictedFilesDialog`).
- Context menu on conflicted file offers "Using mine (OURS)", "Using theirs (THEIRS)", "Open in Merge Tool...", "Mark resolved".
- "Open in Merge Tool..." opens `ThreeWayMergeWindow` showing base/ours/theirs panes.

## Verification

Check file contains conflict markers before resolution:

```text
<<<<<<<
=======
>>>>>>>
```

Resolve conflict using Gitember (try all three resolution paths across repeated setups: OURS, THEIRS, manual edit + Mark resolved).

Expected after save:

- No conflict markers.
- File no longer listed as conflicted.
- Merge commit possible (Commit button enabled once all conflicts resolved).

---

# TC-009 Submodule

## Steps

```bash
git submodule add https://github.com/example/repo.git libs/repo
```

Refresh Gitember.

## Expected Result

- Submodule visible in `SubmodulePanel`.
- Commit pointer visible.

## Verification

```bash
git submodule status
```

Returns valid commit hash.

---

# TC-010 Working Tree (git worktree)

## Steps

```bash
git worktree add ../gittestrepo1-feature feature/login
```

Refresh, or add via `Working copy -> Worktrees...` (`WorktreesDialog`) instead of the CLI.

## Expected Result

- Worktree listed in Gitember's `WorktreesDialog`.
- Independent checkout available.

## Verification

```bash
git worktree list
```

Verify both paths displayed.

---

# TC-011 Search History

## Steps

Search for:

```text
Initial commit
```

using the `SearchBar` in `HistoryPanel`.

## Expected Result

- Matching commit returned.
- Works both with and without a built Lucene index (see TC-071..076).

## Verification

Search results contain commit hash and message.

---

# TC-012 Statistics

## Steps

Create multiple commits from different authors.

Open `Repository -> Statistics...` (`StatDialog`).

## Expected Result

- Statistics dashboard populated (author list, commit counts, average commits/month).

## Verification

Compare dashboard values with:

```bash
git shortlog -sn
```

---

# TC-013 Clone Remote Repository

## Steps

Use Gitember `File -> Clone Repository...` (`CloneDialog`).

URL example:

```text
https://github.com/user/test.git
```

Target:

```text
Projects/tmp/remote-test
```

## Expected Result

- Repository cloned.
- Branches downloaded.

## Verification

```bash
git remote -v
```

Remote origin exists.

---

# TC-014 Fetch

## Steps

Create new branch remotely.

In Gitember execute `Branch -> Fetch`.

## Expected Result

- New remote branch becomes visible.
- `PullResultDialog`/status bar reports fetch summary.

## Verification

```bash
git branch -r
```

Branch appears in list.

---

# TC-015 Pull

## Steps

Commit remotely.

Execute `Branch -> Pull` in Gitember.

## Expected Result

- New commit downloaded.
- Local branch updated.
- `PullResultDialog` shown with summary; if a merge/rebase-pull produces conflicts, `ConflictedFilesDialog` is shown instead.

## Verification

```bash
git log --oneline
```

Contains remote commit.

---

# TC-016 Push

## Steps

Create commit locally.

Push via `Branch -> Push`.

## Expected Result

- Push succeeds.
- No outgoing commits remain.
- `PushResultDialog` shows result summary.

## Verification

Check remote repository UI.

Commit visible remotely.

---

# TC-017 Git LFS

## Steps

```bash
git lfs track "*.zip"
```

Add 100MB+ zip file.
Commit.
Push.

## Expected Result

- LFS upload performed.
- Pointer file stored in Git.

## Verification

```bash
git lfs ls-files
```

File listed.

---

# TC-018 Workspace Mode

## Setup

Workspace contains:
- repo-local
- repo-remote-a
- repo-remote-b

## Steps

1. Create workspace (`File -> Init Workspace...` / `WorkspaceDialog`).
2. Add repositories (existing recent projects, or browse to a folder).
3. Save workspace (press "Open").
4. Restart Gitember.
5. Reopen workspace from `File -> Open Recent` workspace list.

## Expected Result

- Workspace restored correctly.
- All repositories visible.
- Repository state preserved.

## Verification

Confirm:

- branch names match
- repository count matches
- repository status matches pre-restart state

---

# Working Copy Operations & Context Menu (TC-019 – TC-034)

Source: `ui/WorkingCopyContextMenu.java`, `ui/WorkingCopyPanel.java`, `ui/WorkingCopyOps.java`.

---

# TC-019 Stage Single File

## Steps
1. Modify one tracked file.
2. Right-click it in Working Copy panel -> `Stage`.

## Expected Result
File moves from "Unstaged" to "Staged" section.

## Verification
```bash
git status
```
File shown under "Changes to be committed".

---

# TC-020 Unstage Single File

## Steps
1. With a staged file, right-click -> `Unstage`.

## Expected Result
File returns to unstaged list; index entry removed (`removeFileFromCommitStage`).

## Verification
```bash
git diff --cached
```
No longer lists the file.

---

# TC-021 Stage Multiple Selected Files

## Steps
1. Modify 3+ files.
2. Multi-select (Ctrl/Shift+click) in Working Copy panel.
3. Right-click -> `Stage selected (N)`.

## Expected Result
All N files staged in a single action; count in menu label matches selection size.

## Verification
```bash
git status
```

---

# TC-022 Unstage Multiple Selected Files

## Steps
Same as TC-021 but starting from staged files, using `Unstage selected (N)`.

## Expected Result
All selected files unstaged together.

---

# TC-023 Diff With Repository (Unstaged File)

## Steps
1. Modify a tracked file.
2. Right-click -> `Diff with repository`.

## Expected Result
`DiffViewerWindow` (or inline diff panel) opens showing working-tree vs HEAD/index diff with syntax highlighting.

## Verification
Diff content matches:
```bash
git diff -- <file>
```

---

# TC-024 Revert Single File (Discard Changes)

## Steps
1. Modify a tracked file.
2. Right-click -> `Revert...`.
3. Confirm.

## Expected Result
File content restored to last committed/staged state (`checkoutFile`); working tree becomes clean for that file.

## Verification
```bash
git status
git diff
```
No pending changes for the file.

---

# TC-025 Revert Selected Files (Multi)

## Steps
Modify multiple files, multi-select, right-click -> `Revert selected...`, confirm.

## Expected Result
All selected files reverted in one action.

---

# TC-026 File History From Working Copy

## Steps
Right-click a tracked file -> `History`.

## Expected Result
`ScmRevisionInformation` list opens showing commit history for that specific file (`getFileHistory`).

## Verification
```bash
git log --follow -- <file>
```
Commit list/order matches.

---

# TC-027 Open File Externally

## Steps
Right-click a file -> `Open`.

## Expected Result
File opens in the OS-default application.

---

# TC-028 Physical Delete (Single File)

## Steps
Right-click an untracked or tracked file -> `Physical delete...`, confirm.

## Expected Result
File is removed from disk (not just unstaged). Confirmation dialog required before delete (destructive action).

## Verification
File absent from filesystem;
```bash
git status
```
shows it as deleted (if it was tracked) or gone (if untracked).

---

# TC-029 Physical Delete (Multi-select)

## Steps
Select multiple files -> `Physical delete selected...`, confirm.

## Expected Result
All selected files removed from disk in one action.

---

# TC-030 Conflict File — Mark Resolved

## Preconditions
Merge conflict exists (see TC-008).

## Steps
1. Manually edit the conflicted file removing conflict markers.
2. Right-click -> `Mark resolved`.

## Expected Result
File is added to index (`addFileToCommitStage`) and no longer flagged as conflicted; Commit becomes possible once all conflicts cleared.

---

# TC-031 Conflict File — Using Mine (OURS)

## Steps
On a conflicted file, right-click -> `Using mine (OURS)`.

## Expected Result
File content set to the "ours" side (`checkoutFile(..., Stage.OURS)`); file marked resolved automatically or after explicit stage.

## Verification
```bash
git show :2:<file>
```
matches resulting file content.

---

# TC-032 Conflict File — Using Theirs (THEIRS)

## Steps
On a conflicted file, right-click -> `Using theirs (THEIRS)`.

## Expected Result
File content set to the "theirs" side (`checkoutFile(..., Stage.THEIRS)`).

## Verification
```bash
git show :3:<file>
```
matches resulting file content.

---

# TC-033 Open In Merge Tool (Three-Way Merge Window)

## Steps
On a conflicted file, right-click -> `Open in Merge Tool...`.

## Expected Result
`ThreeWayMergeWindow` opens with base/ours/theirs/result panes; editing and saving the result pane resolves the conflict and stages the file.

---

# TC-034 Rename Tracked File

## Steps
Rename a tracked file via OS file manager (or a Gitember rename action if exposed) while repo is open.

## Expected Result
Gitember detects the rename (`renameFile`); status shows as a rename (old path deleted, new path added) rather than delete+add when similarity is high enough for Git to detect it.

## Verification
```bash
git status
```
Shows `renamed:` entry.

---

# Branch / Tag / Stash Context-Menu Operations (TC-035 – TC-046)

Source: `ui/BranchContextMenuFactory.java`.

---

# TC-035 Checkout Branch (Context Menu)

## Steps
Right-click a local branch -> `Checkout`.

## Expected Result
HEAD switches to branch; blocked with a clear message if there are uncommitted changes that would be overwritten.

---

# TC-036 Create Local Branch From Remote

## Steps
Right-click a remote-tracking branch -> `Create local branch...`.

## Expected Result
New local branch created tracking the remote; checked out.

## Verification
```bash
git branch -vv
```
Shows tracking relationship.

---

# TC-037 Merge From Branch Context Menu

## Steps
Right-click branch `feature-a` while on `main` -> `Merge feature-a -> main...`.

## Expected Result
Same as TC-007 but triggered via context menu; label reflects source/target names.

---

# TC-038 Rebase From Branch Context Menu

## Steps
Right-click branch `feature-a` while on `main` -> `Rebase feature-a -> main...`.

## Expected Result
`rebaseBranch` runs; on success, `main`'s commits are replayed onto `feature-a` (or per JGit semantics used); on conflict, conflict UI appears with Continue/Abort/Skip options (`InteractiveContinueAbortDialog`).

## Verification
```bash
git log --graph --oneline --all
```

---

# TC-039 Pull Specific Branch

## Steps
Right-click a branch with an upstream -> `Pull <branch>`.

## Expected Result
Pull runs scoped to that branch; menu item is disabled/labeled differently when no upstream is configured (`disablePull`).

---

# TC-040 Push Specific Branch

## Steps
Right-click a local branch -> `Push`.

## Expected Result
Push runs for that branch; label reflects branch/remote.

---

# TC-041 Push Tag To Remote

## Preconditions
Local tag exists (TC-006).

## Steps
Right-click the tag -> `Push tag to remote...`.

## Expected Result
Tag pushed (`pushTag`); visible on remote.

## Verification
```bash
git ls-remote --tags origin
```

---

# TC-042 Delete Local Tag

## Steps
Right-click tag -> `Delete tag "<name>"...`, confirm.

## Expected Result
Tag removed locally (`deleteLocalTag`); remote copy (if any) untouched.

## Verification
```bash
git tag
```
Tag absent.

---

# TC-043 Delete Remote Tag

## Steps
After TC-041, right-click the tag -> delete-remote-tag action (if exposed) or via project settings; confirm.

## Expected Result
`deleteRemoteTag` removes tag ref from remote.

## Verification
```bash
git ls-remote --tags origin
```
Tag absent.

---

# TC-044 Delete Local / Remote-Tracking Branch

## Steps
Right-click a local branch not currently checked out -> `Delete <name>...`, confirm.
Repeat for a remote-tracking branch.

## Expected Result
- Local branch: `deleteLocalBranch` removes it; blocked with an error if it's the current branch or unmerged (unless force-confirmed).
- Remote-tracking branch: `deleteRemoteTrackingBranch` removes the local ref only, does not touch the actual remote branch.

## Verification
```bash
git branch
git branch -r
```

---

# TC-045 Branch Diff ("Diff with" Submenu)

## Steps
Right-click a branch -> `Diff with` -> pick another branch from submenu.

## Expected Result
`BranchDiffWindow` opens listing changed files between the two branches (`branchDiff` / `getBranchDiff`), each openable in a diff viewer.

## Verification
```bash
git diff branchA..branchB --stat
```
File list matches.

---

# TC-046 Stash Context Menu — Apply / Edit Message / Drop

## Steps
1. Create a stash (`Working copy -> Stash...`) with a message.
2. Right-click stash -> `Apply Stash`.
3. Create another stash; right-click -> `Edit Message…`, change text.
4. Right-click a stash -> `Drop Stash`, confirm.

## Expected Result
- Apply: changes reapplied to working tree (stash entry retained per JGit apply semantics).
- Edit Message: `renameStash` updates the message shown in the stash list.
- Drop: `deleteStash` removes the entry.

## Verification
```bash
git stash list
```
Reflects each state change.

---

# History Operations: Rebase, Cherry-Pick, Revert, Reset, Blame (TC-047 – TC-056)

---

# TC-047 Interactive Rebase — Reorder Commits

## Preconditions
Branch with 4+ local commits ahead of a base.

## Steps
1. `Branch -> Interactive Rebase…` (`InteractiveRebaseDialog`).
2. Drag-and-drop (or Up/Down buttons) to reorder two commits.
3. Confirm.

## Expected Result
Commits applied in the new order (`interactiveRebase`); dialog lists commits newest-first but applies oldest-first internally.

## Verification
```bash
git log --oneline
```
Order matches the reordering.

---

# TC-048 Interactive Rebase — Reword

## Steps
In `InteractiveRebaseDialog`, set a commit's action to `reword`, edit its message inline, confirm.

## Expected Result
Target commit's message updated; SHA changes (and all descendant SHAs).

## Verification
```bash
git log --oneline
```

---

# TC-049 Interactive Rebase — Squash / Fixup

## Steps
Mark one commit `squash` (or `fixup`) onto the commit above it; confirm.

## Expected Result
Two commits combine into one; for `squash` the user is prompted/able to edit the combined message, for `fixup` the squashed commit's message is discarded.

## Verification
```bash
git log --oneline
```
Commit count reduced by one.

---

# TC-050 Interactive Rebase — Drop

## Steps
Mark a commit `drop`; confirm.

## Expected Result
Commit removed entirely from history; its changes are gone unless they reappear via later commits.

## Verification
```bash
git log --oneline
```
Commit absent.

---

# TC-051 Interactive/Regular Rebase — Conflict, Continue/Abort/Skip

## Steps
1. Force a conflicting interactive rebase.
2. When conflict UI (`InteractiveContinueAbortDialog`) appears, test each path in separate runs:
   - Resolve conflict, click Continue (`rebaseContinue`).
   - Click Abort (`rebaseAbort`).
   - Click Skip (`rebaseSkip`) to drop the conflicting commit.

## Expected Result
- Continue: rebase proceeds to next step or completes.
- Abort: repository restored to pre-rebase state exactly.
- Skip: conflicting commit is skipped, rebase continues.

## Verification
```bash
git status
git log --oneline
```

---

# TC-052 Cherry-Pick Commit

## Steps
1. On branch B, select a commit from branch A's history (or from a merged-in view).
2. Trigger cherry-pick action (history panel context menu).

## Expected Result
`cherryPick` applies the commit's changes onto the current branch as a new commit; conflicts surface the same conflict-resolution UI as merge.

## Verification
```bash
git log --oneline
```
New commit present with same message/content, different SHA.

---

# TC-053 Revert Commit

## Steps
Select a commit in history -> `Revert`.

## Expected Result
`revertCommit` creates a new commit that undoes the selected commit's changes; conflicts (if the revert doesn't apply cleanly) surface resolution UI.

## Verification
```bash
git log --oneline
git diff <original>~1 <original> -- | git apply -R --check
```
(or simpler: diff the working tree against the pre-commit-under-test state)

---

# TC-054 Reset Branch — Soft / Mixed / Hard

## Steps
Repeat for each mode via history context menu (or dedicated reset dialog):
1. Reset to an earlier commit with `ResetType.SOFT`.
2. Reset to an earlier commit with `ResetType.MIXED`.
3. Reset to an earlier commit with `ResetType.HARD` (on a disposable branch/commit — destructive).

## Expected Result
- SOFT: HEAD moves, index & working tree unchanged (changes appear staged).
- MIXED: HEAD & index move, working tree unchanged (changes appear unstaged).
- HARD: HEAD, index, and working tree all reset — uncommitted changes lost. Gitember should warn before a HARD reset given data loss risk.

## Verification
```bash
git status
git diff --cached
```
Matches expected mode semantics.

---

# TC-055 Blame — Per File

## Steps
Open a tracked file with multi-author history; trigger Blame (`blame(ScmItem)`).

## Expected Result
Each line annotated with commit SHA, author, and date; matches `getBlameAnnotations`.

## Verification
```bash
git blame <file>
```

---

# TC-056 Blame — Aggregate / Per-Month Stats

## Steps
Trigger the aggregate blame/statistics view backed by `blame(PlotCommitList, ...)` / `getLastCommitPerMonth` (via Statistics dialog).

## Expected Result
Per-month "last commit" and ownership stats computed without error on a repo spanning multiple months.

---

# Submodules & Worktrees — Dialog Level (TC-057 – TC-062)

---

# TC-057 Update Submodules

## Preconditions
Repo has a submodule (TC-009) pointing to an older commit than upstream.

## Steps
`Repository -> Submodules -> Update Submodules`.

## Expected Result
`updateSubmodules` fetches/checks out the recorded submodule commit; progress shown.

## Verification
```bash
git submodule status
```
No longer shows `+` (out of sync) if it matched already, or updates content to match recorded SHA.

---

# TC-058 Sync Submodule URLs

## Steps
Change a submodule's URL in `.gitmodules`, then `Repository -> Submodules -> Sync Submodule URLs`.

## Expected Result
`syncSubmodules` rewrites the submodule's local remote URL to match `.gitmodules`.

## Verification
```bash
git config -f .git/config --get submodule.<name>.url
```

---

# TC-059 Remove Submodule

## Steps
Right-click submodule in `SubmodulePanel` -> `Remove…`, confirm.

## Expected Result
Submodule de-registered and removed from working tree when no local modifications exist.

---

# TC-060 Remove Submodule (Force)

## Steps
Introduce a local modification inside the submodule, then right-click -> `Remove (force)…`, confirm.

## Expected Result
Submodule removed despite local changes (destructive — warn user).

---

# TC-061 Open Submodule

## Steps
Right-click submodule -> `Open`.

## Expected Result
Gitember opens the submodule as its own repository (new view/tab or window).

---

# TC-062 Worktree Add / Remove / Prune

## Steps
1. `Working copy -> Worktrees...` -> Add, specify path + branch (existing or new).
2. Remove a worktree from the list.
3. Manually delete a worktree folder outside Gitember, then click Prune.

## Expected Result
- Add: `addWorktree` creates the worktree; new-branch checkbox creates and checks out a new branch there.
- Remove: `removeWorktree` unregisters it (with force option if it has changes).
- Prune: `pruneWorktrees` clears stale administrative entries for the manually-deleted worktree.

## Verification
```bash
git worktree list
```

---

# Git LFS (TC-063 – TC-070)

Source: `dialog/LfsManageDialog.java`, `service/GitLfsUtil.java`, `GitRepoService` LFS methods.

---

# TC-063 Enable LFS On Existing Repo (No LFS Yet)

## Preconditions
Repo without LFS configured; `Context.isLfsRepo()` returns false.

## Steps
`Repository -> Git LFS -> Manage LFS…` -> `Enable LFS`.

## Expected Result
`enableLfsOnExistingRepo` runs `git lfs install`-equivalent setup; status header updates to "LFS enabled".

## Verification
```bash
git lfs env
```

---

# TC-064 Track New Pattern

## Steps
In `LfsManageDialog`, add pattern `*.psd`.

## Expected Result
`lfsTrack` appends the pattern to `.gitattributes`; pattern appears in the tracked-patterns list.

## Verification
```bash
cat .gitattributes
```
Contains `*.psd filter=lfs ...`.

---

# TC-065 Untrack Pattern

## Steps
Remove a previously tracked pattern from the list.

## Expected Result
`lfsUntrack` removes the corresponding `.gitattributes` line.

## Verification
```bash
cat .gitattributes
```
Line removed.

---

# TC-066 View LFS-Tracked Files

## Steps
With at least one committed LFS file, open `LfsManageDialog`'s file table.

## Expected Result
`getLfsFiles` lists LFS objects with size/pointer info matching reality.

## Verification
```bash
git lfs ls-files
```

---

# TC-067 Fetch LFS Objects

## Steps
Clone a repo with LFS pointers but skip smudge (or set `GIT_LFS_SKIP_SMUDGE=1`), then `Repository -> Git LFS -> Fetch LFS Objects` (or the button in `LfsManageDialog`).

## Expected Result
`fetchLfsObjects` downloads actual LFS content, replacing pointer stubs on disk.

## Verification
File size matches original binary, not pointer-file size (~130 bytes).

---

# TC-068 Commit + Push Large LFS File (>100MB)

Covered in original TC-017 — retain as-is.

---

# TC-069 LFS With No Remote / Offline

## Steps
In an LFS repo with no configured remote, attempt Fetch LFS Objects.

## Expected Result
Graceful error message, no crash.

---

# TC-070 addLFSSupport On Init/Clone

## Steps
Init a fresh repo and immediately check LFS filter config (`addLFSSupport` is invoked during repo creation).

## Expected Result
Git LFS smudge/clean filters are pre-registered even before any file is tracked, so the first `lfs track` works without a separate `git lfs install`.

## Verification
```bash
git config --get-regexp filter.lfs
```

---

# Search & Indexing (TC-071 – TC-076)

Source: `service/SearchService.java`, `GitRepoService.search/indexHistory`, `dialog/IndexHistoryDialog.java`.

---

# TC-071 Unindexed (Linear) Commit Search

## Steps
On a repo that has never been indexed, search history for a term present in a commit message or file content.

## Expected Result
`search(commits, term, luceneIndexed=false)` performs a linear scan and returns correct matches; may be slower on large repos but must still be correct.

---

# TC-072 Build Lucene Index

## Steps
`Repository -> Index History…` (`IndexHistoryDialog`) -> start indexing.

## Expected Result
`indexHistory` builds a Lucene index over commit content (using Tika for binary/text extraction where applicable); progress shown; completes without error.

## Verification
Index files created under Gitember's data directory (e.g. `~/.gitember/<repo>/index` or similar — confirm actual path from `SearchService`).

---

# TC-073 Indexed Search Matches Linear Search

## Steps
After TC-072, repeat the TC-071 search with `luceneIndexed=true`.

## Expected Result
Same result set as the linear search (modulo Lucene tokenization differences called out if any); noticeably faster on a large repo.

---

# TC-074 Search With Special Characters / Regex-Looking Terms

## Steps
Search for a term containing `*`, `"`, `(`, or other Lucene special syntax characters.

## Expected Result
No exception; either literal match or documented Lucene query-syntax behavior — should not crash the UI.

---

# TC-075 Re-Index After New Commits

## Steps
1. Build index (TC-072).
2. Add new commits.
3. Search for a term only in the new commits.

## Expected Result
Either auto re-index or an explicit "re-index" action is required and documented; verify the actual behavior and that stale results aren't silently missing without indication.

---

# TC-076 Search Term History / Autocomplete

## Steps
Perform several distinct searches; reopen the search bar.

## Expected Result
Previous terms are offered (`Settings.searchTerms`) for quick re-search.

---

# Statistics & Activity Charts (TC-077 – TC-082)

Source: `dialog/StatDialog.java`, `service/GitRepoStatService.java`, `service/ActivityChartService.java`, `service/WorkspaceChartService.java`.

---

# TC-077 Per-Author Commit Counts

## Steps
Open Statistics with commits from 3+ distinct author identities (including one with the same name but different email).

## Expected Result
Counts grouped correctly; matches `git shortlog -sn` (decide/verify whether grouping is by name, email, or both).

---

# TC-078 Average Commits Per Month

## Steps
Create commits spread across several months (adjust system clock or use `--date`/`GIT_AUTHOR_DATE` for backdating in setup only — outside Gitember).

## Expected Result
`calculateAverageperMonth` produces per-month averages consistent with manual calculation from `git log --date=short`.

---

# TC-079 Branch-Scoped vs All-Branches Statistics

## Steps
Toggle "all branches" option (`getCommitsByTree(..., all, ...)`) with multiple branches containing distinct commits.

## Expected Result
Counts change correctly when scope changes from current branch to all branches.

---

# TC-080 Statistics On Empty Repository

## Steps
Open Statistics immediately after `git init` with zero commits.

## Expected Result
Dashboard shows empty/zero state without exceptions.

---

# TC-081 Workspace-Level Activity Chart

## Steps
In a multi-repo workspace, open the workspace-level activity/statistics view.

## Expected Result
`WorkspaceChartService` aggregates activity across all repos in the workspace correctly (per-repo breakdown, combined timeline).

---

# TC-082 Statistics On Large Repository

## Steps
Run Statistics against a repo with >10k commits (see Edge Cases section for a truly large repo).

## Expected Result
Completes in reasonable time with a progress indicator; no OOM.

---

# Pull Request Panel — Multi-Forge (TC-083 – TC-090)

Source: `service/PullRequestService.java`, `ui/PullRequestPanel.java`.

---

# TC-083 List Open Pull Requests — GitHub

## Preconditions
Repo remote points to a GitHub repository with at least one open PR.

## Steps
Open `PullRequestPanel`; ensure "include closed" is off.

## Expected Result
`fetchGitHub` returns open PRs; list matches GitHub UI.

---

# TC-084 List Open Pull Requests — GitLab / Bitbucket / Gitea / Azure DevOps

## Steps
Repeat TC-083 against a repo hosted on each of GitLab, Bitbucket, Gitea, and Azure DevOps.

## Expected Result
Each forge is correctly detected from remote URL and its respective `fetchX` method returns accurate results, including Azure DevOps URL variants (`dev.azure.com/...` and legacy `*.visualstudio.com/...`).

---

# TC-085 Include Closed / Merged PRs

## Steps
Toggle "include closed" on; verify against a repo with both open and closed/merged PRs.

## Expected Result
GitHub: closed PRs surfaced with correct "merged" vs "closed" state distinction (via `merged_at`). Other forges: closed/merged states included per `includeClosed` handling.

---

# TC-086 View Changed Files For A PR

## Steps
Select a PR (GitHub, GitLab, or Azure DevOps — the forges with `fetchPrFiles` support) and view its file list.

## Expected Result
`fetchGitHubPrFiles` / `fetchGitLabMrFiles` / `fetchAzureDevOpsPrFiles` returns the correct changed-file list, each diffable.

## Verification
Compare file list/count against the forge's web UI for that PR.

---

# TC-087 PR Pagination (>30 Files Or >1 Page Of PRs)

## Steps
Test against a PR with more than the forge's default page size of changed files (e.g., >30 for GitHub).

## Expected Result
Pagination logic (`fetch up to 100 pages of 100`) retrieves the complete file list, not just the first page.

---

# TC-088 Access Token Authentication Per Forge

## Steps
Configure a PAT per project (`Project Settings` / `CredentialsDialog`) for each forge, including the GitHub token variants ("user:token", "x-access-token:token", "token:x-oauth-basic").

## Expected Result
Authenticated requests succeed and can see private repos / higher rate limits; each token form parses correctly.

---

# TC-089 No Token — Public Repo

## Steps
Clear access token for a project pointing to a public repo.

## Expected Result
PRs still fetch successfully (unauthenticated), subject to forge rate limits; a clear rate-limit error is shown if hit rather than a crash.

---

# TC-090 Unsupported / Unrecognized Remote

## Steps
Point a project at a self-hosted or unrecognized git host URL.

## Expected Result
`PullRequestPanel` shows a graceful "not supported" state rather than an exception.

---

# Avatars (TC-091 – TC-094)

Source: `service/avatar/*`.

---

# TC-091 GitHub / GitLab / Bitbucket / Azure DevOps Avatar Resolution

## Steps
View commit history / commit detail for commits authored by users with known accounts on each forge.

## Expected Result
Correct avatar image fetched and cached per author (`GitHubAvatarClient`, `GitLabAvatarClient`, `BitbucketAvatarClient`, `AzureDevOpsAvatarClient`).

---

# TC-092 Gravatar Fallback

## Steps
View a commit from an author whose email has no forge-account mapping but has a Gravatar.

## Expected Result
`GravatarClient` supplies the avatar as fallback.

---

# TC-093 No Avatar Available

## Steps
View a commit from an author with no forge account and no Gravatar.

## Expected Result
A generic/default placeholder avatar is shown; no broken image or exception.

---

# TC-094 Avatar Caching / Offline

## Steps
Load avatars once (online), then disconnect network and reopen the same history.

## Expected Result
Previously fetched avatars are shown from cache without new network calls; no hang waiting on unreachable network.

---

# Secret-Leak Detection (TC-095 – TC-102)

Source: `service/detector/*`, `dialog/CommitDialog.java`, `dialog/SettingsDialog.java` (Ollama lifecycle), `service/OllamaManager.java`.

---

# TC-095 Empirical Mode — Known Token Pattern (ValuePatternDetector)

## Preconditions
`enableLeakDetector = true`, LLM mode not configured (default empirical `DetectorService()`).

## Steps
1. Stage a file containing a recognizable secret pattern (e.g., a fake AWS access key `AKIAABCDEFGHIJKLMNOP` or a GitHub PAT-shaped string).
2. Open Commit dialog.

## Expected Result
`ValuePatternDetector` flags the finding; Commit dialog shows "⚠ Potential secrets / sensitive data detected" with the finding listed; user can still choose to Commit or Cancel.

---

# TC-096 Empirical Mode — Key-Based Detector

## Steps
Stage a file containing a PEM-formatted private key block (`-----BEGIN PRIVATE KEY-----`).

## Expected Result
`KeyBasedDetector` flags it.

---

# TC-097 Empirical Mode — High-Entropy String (EntropyDetector)

## Steps
Stage a file containing a long random-looking base64/hex string assigned to a variable (e.g., `apiSecret = "kX9..."`), not matching a known pattern.

## Expected Result
`EntropyDetector` flags it based on entropy threshold; verify low-entropy strings of similar length are NOT flagged (false-positive check).

---

# TC-098 Empirical Mode — Connection String Detector

## Steps
Stage a file containing a DB connection string with embedded credentials (e.g., `postgres://user:pass@host:5432/db`).

## Expected Result
`ConnectionStringDetector` flags it.

---

# TC-099 Commit Despite Warning

## Steps
On the findings screen from TC-095, click `Commit` anyway.

## Expected Result
Commit proceeds (the detector is advisory, not a hard block) — confirm this is actually the intended UX and not a blocking gate.

---

# TC-100 Cancel Commit On Warning

## Steps
On the findings screen, click `Cancel`.

## Expected Result
Commit aborted; staged changes remain staged for correction.

---

# TC-101 LLM Mode — Ollama Lifecycle

## Preconditions
`Settings.enableLeakDetector = true` with LLM mode selected (requires Ollama).

## Steps
1. In `SettingsDialog`, enable the secret detector checkbox with Ollama not yet installed/running.
2. Observe `ensureOllamaOrRevert` flow: download prompt, install, start Ollama, pull `llmDetectorModel` (default `qwen2.5-coder`).
3. Stage a file with a secret not matching any regex/entropy heuristic but semantically identifiable as sensitive (e.g., a plausible-looking password assigned with a descriptive variable name).
4. Commit.

## Expected Result
- If the user declines the Ollama download/setup, the checkbox reverts to unchecked (`ensureOllamaOrRevert`) instead of leaving an inconsistent enabled-but-broken state.
- Once Ollama is running with the model pulled, `LlmSecretDetector` (combined with `ValuePatternDetector`) flags the semantic secret that pure regex/entropy would miss.

---

# TC-102 LLM Mode — Ollama Unreachable Mid-Session

## Steps
Enable LLM detector, then stop the Ollama process externally, then attempt a commit.

## Expected Result
Graceful error/fallback (e.g., warn detector unavailable) rather than hanging indefinitely or crashing the Commit dialog.

---

# AI Features — Commit Message & Branch Compare Description (TC-103 – TC-106)

Source: `service/LlmCommitMessageService.java`, `service/LlmDiffDescriptionService.java`.

---

# TC-103 AI Commit Message Generation — Enabled

## Preconditions
`Settings.enableCommitMessageGeneration = true`, Ollama running with a valid model.

## Steps
1. Stage a meaningful code change.
2. Open Commit dialog; trigger "Generate message" (AI) action.

## Expected Result
A commit message is generated reflecting the diff content; user can edit before committing.

---

# TC-104 AI Commit Message Generation — Ollama Unavailable

## Steps
Enable the feature but stop Ollama; trigger generation.

## Expected Result
Clear error, commit message field left editable manually — no crash.

---

# TC-105 Branch Compare AI Description — Enabled

## Preconditions
`Settings.enableBranchCompareDescription = true`.

## Steps
Open `BranchDiffWindow` (TC-045) between two branches with meaningfully different content.

## Expected Result
An AI-generated natural-language summary of the diff is shown alongside the file list.

---

# TC-106 AI Feature Flags Independence

## Steps
Enable only one of the three AI/experimental flags (leak detector, commit-msg generation, branch-compare description) at a time; verify the others remain off and their UI affordances (buttons/panels) are hidden/disabled.

## Expected Result
Flags are independent; enabling one doesn't silently enable another.

---

# Settings & Credentials (TC-107 – TC-114)

Source: `dialog/SettingsDialog.java`, `dialog/CredentialsDialog.java`, `dialog/ProjectSettingsDialog.java`, `service/CipherService.java`.

---

# TC-107 Theme Switch — Light / Dark

## Steps
`File -> Settings...` -> change Theme combo between Light and Dark, apply.

## Expected Result
UI switches FlatLaf theme live (or after restart, per implementation) without visual glitches; persisted in `~/.gitember/gitember2.json` (`theme` field) and restored on next launch.

---

# TC-108 Font Size Change

## Steps
Change font size value; apply.

## Expected Result
`applyFontSize` scales UI fonts; setting persists across restarts.

---

# TC-109 Folder-Compare Ignore Extensions List

## Steps
Add/remove an extension in the ignore-extensions list (e.g., add `tmp`); use Compare Folders (TC-121) on a tree containing `.tmp` files.

## Expected Result
Files with ignored extensions are excluded from folder comparison; default set (`class, jar, dll, exe, pyc`, etc.) is present out of the box.

---

# TC-110 Access Token Takes Priority Over Username/Password

## Preconditions
`CredentialsDialog` for a project has both an access token and username/password filled in.

## Steps
Perform a push/pull/fetch requiring auth.

## Expected Result
Token is used for the transport, not username/password (per the dialog's documented priority).

---

# TC-111 Credential Masking In Settings File

## Steps
Set an access token and password for a project; inspect `~/.gitember/gitember2.json`.

## Expected Result
Token/password values are masked/obscured (`MaskStringValueSerializer`) in the stored JSON, not stored in plaintext (verify actual encryption/masking mechanism via `CipherService`).

---

# TC-112 Blank Token Falls Back To Username/Password

## Steps
Clear the token field, keep username/password filled; perform an authenticated operation.

## Expected Result
Username/password used successfully.

---

# TC-113 Per-Project Settings Isolation

## Steps
Open two different projects, each with distinct credentials/home folder in `ProjectSettingsDialog`.

## Expected Result
Settings for one project do not leak into or override the other's.

---

# TC-114 Settings File Corruption / Missing

## Steps
Delete or corrupt `~/.gitember/gitember2.json`, then launch Gitember.

## Expected Result
Application starts with sane defaults instead of crashing; does not silently wipe/overwrite user data without warning if the file is merely malformed vs. missing.

---

# Clone (Auth Modes) & SSH (TC-115 – TC-120)

Source: `dialog/CloneDialog.java`, `App.java` SSH setup (`SshdSessionFactoryBuilder`).

---

# TC-115 Clone Over HTTPS — Username & Password

## Steps
In `CloneDialog`, select "Username & Password" radio, fill in valid creds for a private repo, clone.

## Expected Result
Clone succeeds; username/password fields visible only for this combination (HTTPS + Password auth).

---

# TC-116 Clone Over HTTPS — Access Token

## Steps
Select "Access Token" radio, provide a valid PAT, clone a private repo.

## Expected Result
Clone succeeds; only the token field is shown (username/password hidden).

---

# TC-117 Clone Over SSH — Credential Fields Hidden

## Steps
Enter an SSH-style URL (`git@host:org/repo.git`) in `CloneDialog`.

## Expected Result
Username/password/token rows are hidden entirely (auth handled by SSH agent/keys), per `isHttp` check.

---

# TC-118 Shallow Clone

## Steps
Check "Clone last commits only (shallow clone)", clone a repo with long history.

## Expected Result
Resulting repo has a truncated history (`git log` shows fewer commits than origin, `.git/shallow` file present).

## Verification
```bash
git log --oneline | wc -l
cat .git/shallow
```

---

# TC-119 SSH Key Types — Ed25519 / ECDSA

## Preconditions
`~/.ssh` contains an Ed25519 key and, separately in another run, an ECDSA key, each registered with the remote.

## Steps
Clone/fetch/push over SSH with each key type active.

## Expected Result
`SshdSessionFactoryBuilder` (Apache MINA sshd) successfully authenticates with both key types without needing external `ssh-agent` config.

---

# TC-120 SSH — Passphrase-Protected Key

## Steps
Use an SSH key protected by a passphrase; perform a fetch.

## Expected Result
User is prompted for the passphrase (or a cached/keyring mechanism is used); wrong passphrase yields a clear auth-failure message, not a generic crash.

---

# Diff/Compare Tools & CLI Standalone Mode (TC-121 – TC-126)

Source: `ui/CompareFilesDialog.java`, `ui/FolderCompareWindow.java`, `ui/DiffViewerWindow.java`, `App.java` args handling.

---

# TC-121 Compare Two Files (Tools Menu)

## Steps
`Tools -> Compare Files…` (F7), select two files with differences.

## Expected Result
`DiffViewerWindow`/`CompareFilesDialog` shows side-by-side diff with syntax highlighting appropriate to file extension (`SyntaxStyleUtil`).

---

# TC-122 Compare Two Folders (Tools Menu)

## Steps
`Tools -> Compare Folders…` (Shift+F7), select two directory trees with some identical, some differing, some unique-to-one-side files.

## Expected Result
`FolderCompareWindow` correctly classifies each file as identical / modified / left-only / right-only; respects the ignore-extensions list (TC-109).

---

# TC-123 CLI Standalone — Compare Two Files

## Steps
Launch the jar directly with two file paths:
```bash
java -jar target/gitember-3.2-SNAPSHOT-boot.jar fileA.txt fileB.txt
```

## Expected Result
Per `App.main`, since both args are existing files, a standalone `DiffViewerWindow` opens directly (no `MainFrame`), and the app exits when the window closes (`EXIT_ON_CLOSE`).

---

# TC-124 CLI Standalone — Compare Two Folders

## Steps
```bash
java -jar target/gitember-3.2-SNAPSHOT-boot.jar dirA dirB
```

## Expected Result
`FolderCompareWindow` opens standalone and immediately runs `compare(dirA, dirB)`.

---

# TC-125 CLI Args — Invalid / Mixed Types

## Steps
Launch with:
- One file + one directory
- Paths that don't exist
- Only one argument
- More than two arguments

## Expected Result
Falls through to normal `MainFrame` startup (per the `if (left.exists() && right.exists())` guard and the `args.length == 2` check) rather than throwing an unhandled exception.

---

# TC-126 Create Diff / Apply Diff (Patch Files)

## Steps
1. `Working copy -> Create diff` with some staged/unstaged changes, save the patch file.
2. On a clean copy of the same repo state prior to those changes, `Working copy -> Apply diff...`, select the patch.

## Expected Result
`createDiff()` produces a valid unified diff; applying it via `Apply diff...` reproduces the original changes in the working tree.

## Verification
```bash
git apply --check patch.diff
```
succeeds independently of Gitember.

---

# Workspace Mode — Multi-Repo (TC-127 – TC-132)

Source: `dialog/WorkspaceDialog.java`, `MainMenuBar` Workspace menu.

---

# TC-127 Add / Remove Project In Workspace

## Steps
In `WorkspaceDialog`, add an existing recent project and a brand-new folder-browsed repo; remove one afterward.

## Expected Result
Workspace's project list updates in-memory; changes only persist on pressing "Open" (Cancel discards edits per class doc).

---

# TC-128 Workspace-Level Pull / Push / Fetch

## Steps
With a workspace containing 2+ repos each with pending remote changes, use `Workspace -> Pull` (and separately Push, Fetch).

## Expected Result
Operation runs across all repos in the workspace; per-repo results/errors are reported individually (one repo's failure shouldn't silently swallow others' results).

---

# TC-129 Workspace-Level Commit

## Steps
Make changes in 2+ repos within a workspace; use `Workspace -> Commit...`.

## Expected Result
User can commit to each dirty repo (either a combined dialog iterating repos, or per-repo prompts — verify actual UX); each repo gets its own commit with its own message.

---

# TC-130 Workspace-Level Create Branch

## Steps
`Workspace -> Create Branch...` with a name, across all repos in the workspace.

## Expected Result
Branch created in every repo where applicable (or where selected); errors on individual repos (e.g., name collision) are surfaced clearly.

---

# TC-131 Rename Workspace

## Steps
Change the Name field in `WorkspaceDialog` for an existing workspace, save.

## Expected Result
`createNewWorkspaceName`-style uniqueness isn't violated; rename reflected in `File -> Open Recent` workspace list.

---

# TC-132 Duplicate Workspace Names

## Steps
Create two workspaces and attempt to give them the same name.

## Expected Result
Either blocked with a validation message, or auto-suffixed (matching `createNewWorkspaceName`'s "New workspace 2" pattern) — verify actual behavior is not silently ambiguous in the recent list.

---

# Application Chrome (TC-133 – TC-138)

Source: `ui/MainMenuBar.java`.

---

# TC-133 Open Recent Projects List

## Steps
Open 3+ different repositories over time; check `File -> Open Recent`.

## Expected Result
List shows them ordered (likely most-recent-first) and each entry reopens the correct repo.

---

# TC-134 Open Terminal / Open In Explorer

## Steps
`Repository -> Terminal` and `Repository -> Open Explorer` (label varies by OS — "Reveal in Finder" on macOS, "Show in Explorer" on Windows).

## Expected Result
Opens the OS terminal / file manager rooted at the repository's working directory.

---

# TC-135 Compress Database (git gc)

## Steps
`Repository -> Compress Database` on a repo with loose objects (many small commits, no prior gc).

## Expected Result
`compressDatabase` (via `CompressDatabaseHandler`) runs without blocking the UI thread (progress shown), reduces `.git/objects` footprint.

## Verification
```bash
git count-objects -v
```
before/after shows fewer loose objects.

---

# TC-136 Project Settings Dialog

## Steps
`Repository -> Project Settings…`.

## Expected Result
Shows/edits per-project settings (home folder, credentials) distinct from global `Settings` (TC-107..114).

---

# TC-137 About / Help Contents

## Steps
`Help -> About`; `Help -> Help Contents` (F1).

## Expected Result
About shows version/author info; Help Contents opens documentation (local or web) without error.

---

# TC-138 Keyboard Accelerators

## Steps
Exercise each documented accelerator without using the menu: `Ctrl+O` (Open), `Ctrl+C` (Clone) `*`, `Ctrl+I` (Init), `Ctrl+S` (Settings), `F5` (Refresh), `F7` (Compare Files), `Shift+F7` (Compare Folders), `F1` (Help).

`*` Verify `Ctrl+C` doesn't conflict with standard copy-to-clipboard behavior inside text fields/panels — test focus context carefully.

## Expected Result
Each shortcut triggers the same action as its menu item; no conflicts with text-field copy/paste or OS-level shortcuts.

---

# Resilience / Negative / Edge Cases (TC-139 – TC-146)

These generalize the "Edge Cases To Execute For Every Feature" section below into concrete, independently runnable cases for the highest-risk operations.

---

# TC-139 Network Disconnect Mid-Push/Pull/Fetch/Clone

## Steps
Start a push/pull/fetch/clone against a remote, then disable networking mid-transfer.

## Expected Result
Operation fails with a clear, specific error (not a hang or silent failure); repository is left in a consistent state (no partial/corrupt index or half-applied merge) — verify with `git status` / `git fsck` afterward.

---

# TC-140 Application Restart During Long-Running Operation

## Steps
Start Compress Database, a large Clone, or an Interactive Rebase, then force-kill the Gitember process mid-operation.

## Expected Result
On relaunch, the repository is either automatically recovered/cleaned (e.g., stale rebase state detected and surfaced) or the user is clearly told a rebase/merge is in progress and given Continue/Abort options (`InteractiveContinueAbortDialog` / `getRepositoryState`).

---

# TC-141 Existing Uncommitted Changes Block Destructive Ops

## Steps
With uncommitted changes present, attempt: branch checkout, pull (merge strategy), reset --hard, worktree removal.

## Expected Result
Each destructive-to-working-tree operation either warns and requires confirmation, or is blocked outright with a clear message — verify per-operation, since some (hard reset) are inherently destructive by design and should have the strongest warning.

---

# TC-142 Existing Merge Conflict / Rebase-In-Progress On Repo Open

## Steps
Externally (via CLI) start a merge or rebase that results in conflicts, leave it unresolved, then open that repo fresh in Gitember.

## Expected Result
`getRepositoryState` is detected on load; UI surfaces the in-progress state (conflicted files list, Continue/Abort options) rather than presenting a falsely "clean" working copy.

---

# TC-143 Detached HEAD

## Steps
Checkout a specific commit SHA (detached HEAD) via history panel (`checkoutRevCommit`).

## Expected Result
UI clearly indicates detached HEAD state (not a branch name); creating a new branch from this state (`checkoutRevCommit(..., newBranchName, ...)`) works correctly; committing while detached warns about the eventual need to create a branch to keep the work.

---

# TC-144 Read-Only Files In Working Tree

## Steps
Make a tracked file read-only at the OS level, then attempt to modify it via a checkout/revert/apply-diff operation that needs to write to it.

## Expected Result
Clear permission-denied error surfaced to the user; no partial/corrupted write.

---

# TC-145 Path With Spaces / Unicode Characters

## Steps
Repeat repository init, clone, and a file add+commit inside a path containing spaces (`C:\Projects\tmp\git test repo`) and, separately, Unicode characters (`C:\Projects\tmp\гит-репо-测试`).

## Expected Result
All operations succeed identically to an ASCII no-space path; no mangled encoding in file names, commit messages, or diffs.

---

# TC-146 Very Large Repository / Binary File

## Steps
Open a repository with >100k commits; separately, add and commit a binary file >5GB (LFS-tracked, to avoid blowing up the plain git object store).

## Expected Result
History panel loads incrementally/paginated rather than blocking the UI indefinitely; large LFS file uploads/downloads with progress feedback and without OOM.

---

# Edge Cases To Execute For Every Feature

For every test case above, additionally repeat using the following conditions where applicable to that feature (cross-reference the dedicated cases in TC-139–TC-146 for the ones already made concrete):

- Path with spaces
- Unicode path
- Empty repository (zero commits)
- Large repository (>100k commits)
- Repository with submodules
- Repository with Git LFS
- Repository with working trees
- Detached HEAD
- Read-only files
- Large binary files (>5GB)
- Network disconnect during operation
- Application restart during operation
- Existing uncommitted changes
- Existing merge conflict
- Existing rebase in progress
