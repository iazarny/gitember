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
- Git user.name
- Git user.email
- Global Git configuration
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

Record:

- Installer size
- Installed size
- Startup time
- License
- Open-source status
- Account requirements

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
