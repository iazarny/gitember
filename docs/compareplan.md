# Git GUI Client Comparison & Benchmark Plan

## 1. Goal

Create a repeatable benchmark for comparing Git GUI clients based on real Git workflows rather than feature checklists.

Each client should be tested against the same Git repositories, history, conflicts, branches, submodules, worktrees, and LFS objects.

The benchmark should evaluate:

- Feature availability
- Feature completeness
- Ease of use
- Number of steps/clicks
- Performance
- Error handling
- Recovery from failed operations
- Safety around destructive operations
- Quality of visualization
- Advanced Git functionality

---

# 2. Clients to Compare

Primary candidates:

1. GitEmber
2. GitHub Desktop
3. Sourcetree
4. Fork
5. GitKraken
6. SmartGit

Optional:

7. Lazygit

Lazygit can be included as a reference point even though it is primarily a terminal UI rather than a traditional desktop GUI.

---

# 3. Rating System

Use the following status indicators:

- 🟢 Fully supported and convenient
- 🟡 Supported but limited or awkward
- 🔴 Not supported
- 🐞 Works but has bugs/problems
- ⚡ Particularly good UX
- 🖥️ Requires external tool or terminal

Important:

A feature should only receive 🟢 after actually testing it.

"Supports rebase" on a website is not enough. Test the complete operation, including conflicts and recovery.

---

# 4. Installation & Configuration

Test:

- Installation experience
- Portable version
- First startup
- Git detection
- Git version selection
- SSH configuration
- HTTPS authentication
- Credential manager/keychain
- GPG signing
- Git user.name / user.email
- Proxy configuration
- External editor
- External diff tool
- External merge tool
- Light/dark theme
- UI customization
- Keyboard shortcuts
- Startup time
- Memory usage
- Internet requirement
- Account requirement
- Installer size
- Installed size
- Startup time


---

# 5. Repository Creation & Cloning

## Local repository

Test:

- Create new repository
- Initialize existing directory
- Add README
- Add .gitignore
- Initial commit
- Change default branch
- Open existing repository

## Clone

Test:

- Clone HTTPS repository
- Clone SSH repository
- Clone private repository
- Clone large repository
- Shallow clone
- Clone specific branch
- Clone into existing directory
- Clone with submodules
- Clone with Git LFS

Evaluate:

- Number of clicks
- Dialog clarity
- Progress reporting
- Error messages
- Recovery from failed clone

---

# 6. Working Tree & Staging

Test:

- Detect modified files
- Detect untracked files
- Stage file
- Unstage file
- Stage individual hunk
- Stage individual line
- Discard file changes
- Discard hunk
- Discard line
- Rename file
- Delete file
- Add binary file
- Add large file
- Ignore file
- Edit .gitignore

## Diff viewer

Test:

- Unified diff
- Side-by-side diff
- Syntax highlighting
- Word-level diff
- Line-level diff
- Binary file handling
- Image diff
- Large-file performance
- File history
- Compare arbitrary files
- Compare directories

---

# 7. Commits

Test:

- Create commit
- Amend commit
- Commit selected files
- Commit selected hunks
- Commit selected lines
- Empty commit
- Edit commit message
- Change author
- Change committer
- Co-authors
- Signed commit
- Verify signature
- Commit template
- Conventional commit support
- Commit message generation

## Commit manipulation

Test:

- Undo last commit
- Soft reset
- Mixed reset
- Hard reset
- Revert commit
- Cherry-pick commit

---

# 8. Branch Management

Create several branches:

```text
main
├── feature-A
├── feature-B
├── bugfix
└── experiment
```

Test:

- Create branch
- Create branch from selected commit
- Create branch from remote branch
- Rename branch
- Delete local branch
- Delete remote branch
- Checkout branch
- Checkout detached HEAD
- Switch branch with uncommitted changes
- Branch comparison
- Track upstream branch
- Set upstream
- Change upstream
- View ahead/behind count
- Stale branch detection
- Prune remote branches

Evaluate:

- Graph visualization
- Branch discoverability
- Safety warnings
- Recovery options

---

# 9. Merge Operations

Test:

- Fast-forward merge
- No-ff merge
- Squash merge
- Merge commit creation
- Merge conflict handling
- Abort merge
- Continue merge
- Resolve text conflicts
- Resolve binary conflicts
- Resolve rename conflicts
- Resolve delete/modify conflicts
- Merge preview

Evaluate:

- Conflict UI quality
- Merge wizard quality
- Number of clicks
- Recovery

---

# 10. Rebase

Test:

- Rebase branch
- Rebase onto specific branch
- Interactive rebase
- Reorder commits
- Edit commit
- Reword commit
- Squash commits
- Fixup commits
- Drop commit
- Split commit
- Rebase conflict resolution
- Abort rebase
- Continue rebase
- Recover failed rebase

Evaluate:

- Visual clarity
- Safety
- Recovery paths

---

# 11. Conflict Resolution

Prepare repositories with intentional conflicts.

Test:

- Text conflict
- Multiple file conflicts
- Binary conflict
- Rename conflict
- Delete/delete conflict
- Modify/delete conflict
- Submodule conflict
- Three-way merge view
- External merge integration

Evaluate:

- Conflict visualization
- Auto-resolution support
- Ease of recovery
- Error handling

---

# 12. History Exploration

Test:

- Commit graph
- Author filtering
- Branch filtering
- Date filtering
- Search by message
- Search by SHA
- Search by file
- Search by author
- Blame view
- File history
- Folder history
- History performance

Evaluate:

- Graph readability
- Search speed
- Navigation UX

---

# 13. Remote Operations

Test:

- Add remote
- Rename remote
- Remove remote
- Fetch
- Pull
- Push
- Push new branch
- Push tags
- Force push
- Force push with lease
- Push selected ref
- Fetch specific branch
- Fetch prune
- Remote URL edit

Evaluate:

- Safety around force push
- Progress reporting
- Error diagnostics

---

# 14. Tags & Releases

Test:

- Lightweight tag
- Annotated tag
- Signed tag
- Edit tag message
- Delete local tag
- Delete remote tag
- Push tag
- Push all tags
- Checkout tag

Evaluate:

- Ease of creation
- Visualization
- Signature support

---

# 15. Stash Management

Test:

- Create stash
- Named stash
- Stash selected files
- Stash untracked files
- Apply stash
- Pop stash
- Delete stash
- Drop stash
- Stash conflict handling
- Partial stash

Evaluate:

- Discoverability
- Recovery

---

# 16. Submodules

Create test repository containing nested submodules.

Test:

- Clone with submodules
- Initialize submodule
- Update submodule
- Add submodule
- Remove submodule
- Commit submodule change
- Recursive update
- Submodule diff
- Submodule status

Evaluate:

- Visualization
- Ease of use
- Error handling

---

# 17. Git Worktrees

Test:

- Create worktree
- Open worktree
- Remove worktree
- Switch worktree
- Worktree list
- Branch-worktree association
- Worktree conflicts

Evaluate:

- Discoverability
- Workflow efficiency
- Error handling

---

# 18. Git LFS

Use a repository containing large binaries.

Test:

- Detect LFS repository
- Clone LFS repository
- Download LFS files
- Upload LFS files
- Lock file
- Unlock file
- LFS diff handling
- LFS error handling

Evaluate:

- Performance
- Progress visibility

---

# 19. Advanced Git Features

Test:

- Reflog view
- Recover deleted branch
- Recover hard reset
- Recover deleted commit
- Recover stash
- Bisect support
- Notes support
- Sparse checkout
- Partial clone
- Shallow repository management
- Alternate object databases

Evaluate:

- Availability
- Usability
- Recovery support

---

# 20. Automation & Integrations

Test:

- Git hooks support
- Pre-commit hook handling
- Post-commit hook handling
- GitFlow support
- GitHub integration
- GitLab integration
- Bitbucket integration
- Azure DevOps integration
- Jira integration
- Issue linking
- Pull request creation
- Pull request checkout
- Pull request review

Evaluate:

- Integration depth
- Workflow smoothness
- Authentication experience

---

# 21. Performance Benchmarks

Use:

- Small repository (~100 commits)
- Medium repository (~10k commits)
- Large repository (~100k+ commits)
- Large monorepo
- LFS repository
- Submodule repository

Measure:

- Startup time
- Repository open time
- Clone time
- Fetch time
- Push time
- History render time
- Diff render time
- Memory usage
- CPU usage
- Large-file responsiveness

---

# 22. Safety & Recovery

Test intentionally destructive operations.

Test:

- Hard reset warning
- Force push warning
- Delete branch warning
- Delete tag warning
- Discard changes warning
- Rebase recovery
- Merge recovery
- Abort operations
- Undo support
- Reflog integration

Evaluate:

- Safety mechanisms
- Recoverability
- User guidance

---

# 23. UI/UX Evaluation

Score 1-10:

- Discoverability
- Learnability
- Visual clarity
- Keyboard-first workflow
- Mouse efficiency
- Screen space usage
- Commit graph quality
- Dark theme quality
- Power-user friendliness
- Beginner friendliness

---

# 24. Enterprise & Team Workflows

Test:

- Multiple accounts
- Multiple SSH keys
- Corporate proxy
- Smartcard support
- Hardware security keys
- Commit signing policies
- Branch protection awareness
- Large repository handling
- Offline operation

Evaluate:

- Enterprise readiness
- Security support
- Team collaboration experience

