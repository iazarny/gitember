---
title: Git Notes
sidebar_position: 4
---

# Git Notes

Git notes are extra comments stored **next to** a commit, not inside it. Adding, editing, or
removing a note does not change the commit SHA, so history, signatures, and already-pushed
commits stay the same.

See also [git notes](https://git-scm.com/docs/git-notes) in the Git documentation.

Gitember uses the default notes namespace `refs/notes/commits`.

## Viewing a note

Select a commit in **History**. When that commit has a note, the details pane shows a
**Notes** section under the author, SHA, and signature fields.

Commits without a note keep the details pane unchanged.

## Adding or editing a note

1. Right-click a commit in History.
2. Choose **Add note…** (or **Edit note…** when a note already exists). The items sit
   directly under **Reset to commit…**.
3. Enter the note text and click **OK**.

An existing note is replaced. Clearing the text in the editor and confirming removes the
note.

## Removing a note

1. Right-click a commit that already has a note.
2. Choose **Remove note**.
3. Confirm.

**Remove note** is disabled when the commit has no note.

## What Gitember stores

JGit exposes `NoteMap` for the notes tree. Gitember adds, shows, and removes notes through
JGit’s `notesAdd` / `notesShow` / `notesRemove` commands, which write a notes commit on
`refs/notes/commits`.

| Action | Git equivalent |
|--------|----------------|
| Add / edit | `git notes add -f -m "…" <commit>` |
| Show | `git notes show <commit>` |
| Remove | `git notes remove <commit>` |

Notes are local until you push `refs/notes/commits`. A regular **Push** of the branch does
not publish them.

## Summary

| Action | How to trigger |
|--------|----------------|
| View note | Select the commit — **Notes** appears in details when present |
| Add note | Right-click → **Add note…** |
| Edit note | Right-click → **Edit note…** |
| Remove note | Right-click → **Remove note** |
