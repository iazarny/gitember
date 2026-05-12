# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Gitember is a free, open-source Git GUI client built with **Java 21 + Swing + FlatLaf**. It replaced an older JavaFX UI in v3.0. The app can also be invoked from the command line as a standalone diff/folder-compare tool by passing two file or directory paths as arguments.

## Build & Run Commands

```bash
# Build (skip tests)
mvn package -DskipTests

# Build with tests
mvn package

# Run directly via Maven
mvn exec:java

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=GitRepoServiceTest

# Run a single test method
mvn test -Dtest=GitRepoServiceTest#testCommitAndLog

# Produce the fat/runnable jar (Spring Boot repackage, classifier "boot")
mvn package -DskipTests
# Output: target/gitember-3.2-SNAPSHOT-boot.jar
java -jar target/gitember-3.2-SNAPSHOT-boot.jar
```

JGit releases are pulled from the Eclipse repository (`https://repo.eclipse.org/...`), not Maven Central — this is already configured in `pom.xml`.

## Architecture

### Global state bus: `Context`

`service/Context.java` is the application's single source of truth. It holds all runtime state as static fields (current repo path, working branch, branch lists, status list, plot commit list, pull requests, etc.) and exposes it through a **JavaBeans `PropertyChangeSupport`** bus. UI panels register listeners on named properties (`PROP_*` constants) so they react to state changes without direct coupling.

The two key services held by `Context` are:
- `GitRepoService` — all JGit operations (branches, commits, diff, blame, stash, rebase, LFS, submodules, …)
- `SettingService` — reads/writes `~/.gitember/gitember2.json` via Jackson

### Layer map

| Package | Role |
|---|---|
| `com.az.gitember` | `App` entry point; sets up L&F, SSH factory, GPU acceleration |
| `service/` | Business logic; no Swing imports except `SwingUtilities.invokeLater` |
| `handler/` | Async Swing operations; each extends `AbstractAsyncHandler<T>` which wraps `SwingWorker` |
| `ui/` | Swing panels and windows wired to `Context` property changes |
| `dialog/` | Modal dialogs (commit, clone, init, settings, …) |
| `data/` | POJOs / DTOs; `Settings`, `Project`, `ScmItem`, `ScmBranch`, `ScmRevisionInformation`, etc. |
| `service/detector/` | Secret-leak detection pipeline |
| `service/avatar/` | Avatar fetching for GitHub / GitLab / Bitbucket / Azure DevOps / Gravatar |

### Async pattern

Long-running git operations (push, pull, fetch, clone, rebase) follow a single pattern:
1. Subclass `AbstractAsyncHandler<T>`, implement `getOperationName()`, `doInBackground()`, `onSuccess()`.
2. Call `.execute()` from the EDT — it creates a `SwingWorker`, shows progress in `StatusBar`, and calls back on the EDT.
3. `AbstractAsyncHandler` also provides shared auth-retry logic (`isAuthError()`, `promptAndSaveCredentials()`).

### UI structure

`MainFrame` owns a `CardLayout` that switches between a `WelcomePanel` (no repo open) and a split-pane repo view. The split pane has `MainTreePanel` (left sidebar: branches, tags, stash, submodules) and `ContentPanel` (right: `HistoryPanel`, `WorkingCopyPanel`, `PullRequestPanel`, `SubmodulePanel`). Switching between them is done by firing `PROP_MAIN_PANE_NAME` on `Context`.

### Secret-leak detection

`DetectorService` orchestrates a pipeline of `Detector` implementations:
- **Empirical mode** (default): `ValuePatternDetector` (regex for known token formats) + `KeyBasedDetector` + `EntropyDetector` + `ConnectionStringDetector`
- **LLM mode**: `ValuePatternDetector` + `LlmSecretDetector` (sends content to a local Ollama model)

Ollama lifecycle (download, start, model pull) is managed by `OllamaManager`. The model and feature flags are stored in `Settings` (`enableLeakDetector`, `llmDetectorModel`).

### Pull request integration

`PullRequestService` detects the forge (GitHub, GitLab, Bitbucket, Gitea, Azure DevOps) from the remote URL and calls the appropriate REST API. The access token is stored per-project in `Settings.projects` (serialized to `~/.gitember/gitember2.json`). Token values are masked via `MaskStringValueSerializer` / `MaskStringValueDeSerializer` in Jackson.

### Settings persistence

`SettingService` stores everything in `~/.gitember/gitember2.json`. The `Settings` object contains the project list (each with credentials, home folder, access token) and global preferences (theme, font size, AI feature flags). On first run, `Settings.DEFAULT_IGNORE_COMPARE_FILES` seeds the folder-compare ignore list.

## Key Dependencies

- **JGit 7.6** — all git operations; uses `SshdSessionFactory` (Apache MINA sshd) for SSH, supporting Ed25519/ECDSA keys
- **FlatLaf 3.7** — Swing L&F (light/dark)
- **Ikonli + FontAwesome 5** — icon rendering in Swing
- **RSyntaxTextArea** — syntax-highlighted file/diff viewer
- **Lucene 9.9 + Apache Tika** — full-text search across commit content and binary files
- **LangChain4j 0.36.2 (Ollama)** — local LLM integration for AI features
- **Jackson 2.16** — settings serialization
- **JUnit 5** — tests use real on-disk git repos in temp directories (no mocking of the git layer)
