---
title: Submodules
sidebar_position: 10
---

# Submodules

Git submodules allow you to embed one Git repository inside another as a subdirectory while keeping their histories separate. They are commonly used to include shared libraries or third-party dependencies directly in a project.

See also [Git Submodules](https://git-scm.com/book/en/v2/Git-Tools-Submodules) in Git documentation.

## Viewing Submodules

When you open a repository that contains submodules, they are listed in the **Submodules** section of the Branches panel.

![Submodules panel](submodules-panel.png)

Each entry shows:

| Column | Description |
|--------|-------------|
| **Name** | The path of the submodule inside the parent repository. |
| **URL** | The remote URL the submodule points to. |
| **SHA** | The commit SHA the parent repository currently has checked out for the submodule. |

## Updating Submodules

After cloning a repository or after a pull that changed the recorded submodule commit, the submodule directories may be empty or out of date. To initialise and update all submodules:

1. Open the **Repository** menu.
2. Select **Update Submodules**.

![Update submodules menu](submodules-update-menu.png)

Gitember runs `git submodule update --init --recursive` in the background. Progress is shown in the status bar.

## Synchronising Submodule URLs

If the remote URL of a submodule has changed in `.gitmodules`, you need to synchronise the recorded URL before updating:

1. Open the **Repository** menu.
2. Select **Sync Submodules**.

This propagates the new URL from `.gitmodules` into each submodule's local configuration.

## Opening a Submodule

To work inside a submodule as if it were a standalone repository:

1. Double-click the submodule entry in the **Submodules** panel.
2. Gitember opens the submodule in a new tab, allowing you to browse its history, create branches, commit, and push independently.

![Submodule opened as tab](submodules-open-tab.png)

## Summary

| Action | How to trigger |
|--------|---------------|
| View submodules | Branches panel → **Submodules** section |
| Initialise / update submodules | Repository menu → **Update Submodules** |
| Sync changed remote URLs | Repository menu → **Sync Submodules** |
| Open submodule as standalone repo | Double-click submodule entry |
