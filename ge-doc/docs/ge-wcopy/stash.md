---
title: Stash Changes
sidebar_position: 10
---

# Stash Changes

Stashing lets you temporarily set aside uncommitted changes so you can work on something else and come back to your changes later. It is useful when you need to switch context quickly without creating a work-in-progress commit.

See also [Git Stash](https://git-scm.com/docs/git-stash) in Git documentation.

## Creating a Stash

To stash your current changes:

1. Open the **Working Copy** page.
2. Right-click anywhere in the file list, or use the toolbar button, and choose **Stash Changes**.
3. Optionally enter a description for the stash in the dialog that appears.
4. Click **OK**.

TODO stash-create-dialog.png

Gitember runs `git stash` in the background. Your working directory is restored to the last committed state, and all staged and unstaged tracked changes are saved.

:::tip
Give each stash a meaningful description so you can identify it in the stash list later.
:::

## Viewing the Stash List

To see all saved stashes:

1. Open the **Branches** panel.
2. Expand the **Stashes** section, or select **Stash → Show Stash List** from the menu.

TODO stash-list.png

Each stash entry shows:

| Column | Description |
|--------|-------------|
| Index  | `stash@{0}` is the most recent entry. |
| Description | The message provided when the stash was created, or the default auto-generated message. |
| Date   | When the stash was saved. |

## Applying a Stash

To restore stashed changes back to the working directory:

1. Right-click the desired stash entry and select **Apply Stash**.

TODO stash-apply-menu.png

Gitember applies the stash without removing it from the stash list, so it remains available if needed again.

:::note
If the applied stash conflicts with the current working tree, conflicts will be marked in the Working Copy file list just like merge conflicts.
:::

## Dropping a Stash

To permanently remove a stash entry:

1. Right-click the stash entry.
2. Select **Delete Stash**.

TODO stash-delete-menu.png

The entry is removed from the stash list. This operation cannot be undone.

## Summary

| Action | How to trigger |
|--------|---------------|
| Save changes to stash | Working Copy → context menu → **Stash Changes** |
| View all stashes | Branches panel → **Stashes** section |
| Restore a stash | Stash list → right-click → **Apply Stash** |
| Remove a stash entry | Stash list → right-click → **Delete Stash** |
