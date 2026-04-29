# Reddit Posts — Gitember 3.2

---

## r/git

**Title:** Gitember Git GUI v3.2 just dropped

**What's new:**

**Worktrees** — full UI support for creating, switching, and removing worktrees.  
If you juggle hotfix branches while keeping a long-running feature branch alive, 
this is the workflow improvement you've been waiting for. No terminal commands needed.

**3-way merge conflict resolver** — BASE / OURS / THEIRS side-by-side. Pick a side, edit inline, stage with one click. No separate merge tool to install.

**AI-assisted writing** :
- generate PR descriptions from branch diff + commit history
- explain what changed between two branches in plain language

The rest of the feature set:t
 - 
 - full-text search across commits/code/docs (including Office and CAD files)
 - Git LFS 
 - GitHub/GitLab/Bitbucket/Gitea integration
 - interactive rebase,
 - statistics


Source and downloads: https://gitember.org | https://github.com/iazarny/gitember

What Git GUI features do you wish existed? Genuinely curious.

---

## r/programming

**Title:** Why I rewrote a JavaFX Git GUI in Swing — and what I added in 3.2

In 2016 I started Gitember as an experiment: a Git desktop client in Java. I chose JavaFX because it looked modern and complete.

By version 2, the cracks showed. JavaFX was removed from the JDK in Java 11, 
adding packaging complexity. Startup time was noticeable. 
The CSS engine added overhead that made no sense for a utility you open 20 times a day.

So for version 3 I rewrote the entire UI layer in Swing.

**Why Swing in 2024:**
- Ships in every JDK, zero extra runtime
- Fast startup — open → do the job → close
- Low memory overhead
- Predictable behaviour across platforms

The rewrite didn't remove functionality; it made everything lighter.

**Version 3.2 adds three things I'm proud of:**

1. **Git worktrees** — create and manage worktrees from the UI. Each opens in its own window. Status and diffs work correctly per worktree.

2. **3-way merge editor** — BASE / OURS / THEIRS in one view, with inline editing and one-click staging. Resolving conflicts without leaving the app finally works well.

3. **AI features** — optional, OpenAI-compatible (local models supported). Generates commit messages, PR descriptions, and branch diff explanations. Useful when you've been in a rabbit hole for 4 hours and need to explain what you did.

The rest: full-text search (commits, code, Office/CAD files), LFS, interactive rebase, statistics, GitHub/GitLab/Bitbucket/Gitea.

Stack: Java 17, Swing + FlatLaf, JGit, Lucene.

Repo: https://github.com/iazarny/gitember | https://gitember.org

Happy to answer questions about the JavaFX → Swing migration if anyone is considering something similar.

---

## r/java

**Title:** Gitember 3.2 — open-source Swing Git GUI: worktrees, 3-way merge, AI integration (JGit + FlatLaf + Lucene)

Just released version 3.2 of Gitember, a Git desktop client I've been building since 2016. Sharing here because the stack might be of interest.

**Tech stack:**
- Java 17
- Swing + FlatLaf 3.x (modern look, embedded title bar, dark/light mode)
- JGit 6.8 (all Git operations — no native git binary required for core features)
- Apache Lucene (full-text search over commits, file contents, Office/CAD documents)
- Jackson (LFS batch API, AI API calls)

**What's new in 3.2:**

*Worktrees* — UI for `git worktree add/remove/prune`. Each worktree opens in a separate window. JGit doesn't fully support the `commondir` mechanism for linked worktrees, so I had to resolve the common `.git` dir manually and set the worktree-specific index path. If you've dealt with this in JGit, you know the pain.

*3-way merge conflict resolver* — reads stage 1/2/3 from the DirCache directly via JGit, renders BASE/OURS/THEIRS side by side, applies the resolved content on save, and re-stages via `git add`. No external tool dependency.

*AI integration* — OpenAI-compatible HTTP calls (works with Ollama, LM Studio, any local endpoint). Generates commit messages from the staged diff, PR descriptions from branch diff + history, and plain-language branch explanations.

The JavaFX → Swing migration in v3 was the interesting technical decision. Short version: JDK-bundled, fast startup, no CSS engine overhead, predictable cross-platform behaviour. FlatLaf handles the modern look well enough that users rarely ask "why Swing?".

Source: https://github.com/iazarny/gitember

---

## r/opensource

**Title:** Gitember 3.2 — free, open-source Git GUI with worktrees, 3-way merge editor, and optional AI commit messages

Hey r/opensource,

I've been building Gitember since 2016 — a free, open-source Git desktop client. Version 3.2 is out.

**License:** MIT
**Platform:** Windows, macOS, Linux (Java 17+)
**No telemetry, no accounts, no subscriptions.**

**New in 3.2:**

🌿 **Worktrees** — manage multiple worktrees from the UI. Each opens in its own window with correct status and history. Perfect for keeping a stable branch checked out while working on a feature.

⚔️ **3-way merge conflict resolver** — see BASE, OURS, and THEIRS side by side. Resolve conflicts inline, stage with one click. No extra tools needed.

🤖 **AI features (optional)** — connect to any OpenAI-compatible API, including local models like Ollama. Generates commit messages, PR descriptions, and branch diff explanations. You control the API key and endpoint — nothing is sent anywhere by default.

**Full feature list:**
- Interactive rebase (reorder, squash, fixup, drop, reword)
- GitHub, GitLab, Bitbucket, Gitea integration (PR creation, branch management)
- Full-text search (commits, code, and documents including .docx, .xlsx, CAD files)
- Git LFS support
- Repository statistics and blame
- Diff viewer (unified, side-by-side)
- Cherry-pick, revert, stash

Everything runs locally. No cloud required for core functionality.

GitHub: https://github.com/iazarny/gitember
Site: https://gitember.org

Contributions welcome — especially around platform-specific packaging.

---

## r/sysadmin

**Title:** Free Git GUI for developers who hate context-switching — v3.2 adds worktrees and a built-in merge resolver

If you manage multiple repos or work across several branches simultaneously, this might save you some time.

Gitember is a free, open-source Git desktop client. Version 3.2 adds two things that matter operationally:

**Worktrees** — create multiple working trees from one repo, each on a different branch. Useful when you need to stay on a stable branch (monitoring, quick fix) while a longer task runs in another. The UI makes it point-and-click instead of `git worktree add` and manually tracking paths.

**3-way merge resolver** — when you pull changes that conflict, you get a BASE/OURS/THEIRS view. Pick a side or edit inline, then stage. Replaces the need for a separate merge tool like meld or kdiff3.

Also has optional AI-generated commit messages if you connect it to an OpenAI-compatible endpoint (Ollama works fine for air-gapped setups).

Runs on Windows, macOS, Linux. Java 17+ required. No installer — just run the JAR.

https://gitember.org | https://github.com/iazarny/gitember

---

## r/foss

**Title:** Gitember 3.2 — MIT-licensed Git GUI client, no cloud, no tracking

Sharing Gitember 3.2 here because the values fit: fully open-source (MIT), self-hostable, no telemetry, no accounts, no vendor lock-in.

**What it is:** 
a Git desktop client . All Git operations go through JGit — no native git binary phoning home, no relay servers, no analytics. 
The only outbound connections are the ones you explicitly configure (your own remote, your own AI endpoint).

**Version 3.2 adds:**

🌿 **Worktrees** — manage `git worktree` from the UI. Create a new worktree for a branch, open it in a separate window, prune stale entries. Useful for parallel branch work without juggling stash or multiple clones.

⚔️ **3-way merge conflict resolver** — BASE / OURS / THEIRS in one view. No dependency on meld, kdiff3, or any other external tool. Everything stays local.

🤖 **AI integration — fully under your control.** Connect to any OpenAI-compatible endpoint. Works with Ollama and LM Studio on localhost — nothing leaves your machine if you use a local model. You supply the endpoint and key; the app sends your diff and gets back text. That's it. Generates commit messages, PR descriptions, and branch diff summaries.

**Full feature set:**
- Interactive rebase (reorder, squash, fixup, drop, reword)
- GitHub, GitLab, Bitbucket, Gitea — PR creation, branch management
- Full-text search across commits, source code, and documents (.docx, .xlsx, CAD files via Lucene)
- Git LFS
- Repository statistics and blame
- Cherry-pick, revert, stash

**Stack:** Java 17, Swing + FlatLaf, JGit 6.8, Apache Lucene — all open-source dependencies.

**License:** MIT
**Platforms:** Windows, macOS, Linux
**Install:** download the JAR, run it. No installer, no daemon, no update agent.

GitHub: https://github.com/iazarny/gitember
Site: https://gitember.org

Pull requests welcome. Packaging help (Flatpak, AUR, Homebrew formula) especially appreciated.


-----------------------
I've been building Gitember since 2016 — a free, open-source Git desktop client. It has been started as weekend experiment. And now version 3.2 is out with new features:
Worktrees - full UI support for creating, switching, and removing worktrees. If you juggle hotfix branches while keeping a long-running feature branch alive, this is the workflow improvement you've been waiting for.
3-way merge conflict resolver - BASE / OURS / THEIRS side-by-side. Pick a side, edit inline, stage with one click. No separate merge tool to install.
AI-assisted writing (experimental)- explain what changed between two branches in plain language, secret leak detection  ( is your GPU good enough ?)

It also covers everyday Git stuff (commit, branch, diff, etc.), but one thing I personally rely on a lot:
search through history including non-text formats (Office docs, DWG, PSD, etc.)
arbitrary file/folder comparison
The last one very useful feature in our days, when need quikly compare a lot of AI changes
Site here https://gitember.org/ 

Contributions, feedbacks, suggestions are welcome
