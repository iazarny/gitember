# Gitember Manual Test Plan (Detailed)

## Conventions

Test root directory:
- Windows: `C:\Projects\tmp`
- Linux/macOS: `~/Projects/tmp`

Repository name used in examples:
- `gittestrepo1`

Expected Result section describes success criteria.
Verification section explains how a tester validates the result.

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
Repository -> Initialize Repository
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

Refresh Gitember if necessary.

## Expected Result

- `readme.txt` appears as untracked.
- Status color indicates new file.

## Verification

Open Working Tree view.

Confirm:

```text
readme.txt = Untracked
```

---

# TC-003 Stage And Commit File

## Steps

1. Stage `readme.txt`.
2. Enter commit message:

```text
Initial commit
```

3. Press Commit.

## Expected Result

- Commit created.
- Working tree becomes clean.
- Commit visible in history.

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

1. Open Branches.
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
- Refresh occurs automatically or manually.

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

Create annotated tag:

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
4. Merge feature-a.

## Expected Result

- Merge completes.
- History shows merge.

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

- Gitember shows conflict.
- Conflict editor opens.

## Verification

Check file contains conflict markers before resolution:

```text
<<<<<<<
=======
>>>>>>>
```

Resolve conflict using Gitember.

Expected after save:

- No conflict markers.
- Merge commit possible.

---

# TC-009 Submodule

## Steps

```bash
git submodule add https://github.com/example/repo.git libs/repo
```

Refresh Gitember.

## Expected Result

- Submodule visible.
- Commit pointer visible.

## Verification

```bash
git submodule status
```

Returns valid commit hash.

---

# TC-010 Working Tree

## Steps

```bash
git worktree add ../gittestrepo1-feature feature/login
```

## Expected Result

- Worktree listed in Gitember.
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

## Expected Result

- Matching commit returned.

## Verification

Search results contain commit hash and message.

---

# TC-012 Statistics

## Steps

Create multiple commits from different authors.

## Expected Result

- Statistics dashboard populated.
- Totals are correct.

## Verification

Compare dashboard values with:

```bash
git shortlog -sn
```

---

# TC-013 Clone Remote Repository

## Steps

Use Gitember clone.

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

In Gitember execute Fetch.

## Expected Result

- New remote branch becomes visible.

## Verification

```bash
git branch -r
```

Branch appears in list.

---

# TC-015 Pull

## Steps

Commit remotely.

Execute Pull in Gitember.

## Expected Result

- New commit downloaded.
- Local branch updated.

## Verification

```bash
git log --oneline
```

Contains remote commit.

---

# TC-016 Push

## Steps

Create commit locally.

Push via Gitember.

## Expected Result

- Push succeeds.
- No outgoing commits remain.

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

1. Create workspace.
2. Add repositories.
3. Save workspace.
4. Restart Gitember.
5. Reopen workspace.

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

# Edge Cases To Execute For Every Feature

For every test case above repeat using:

- Path with spaces
- Unicode path
- Empty repository
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
