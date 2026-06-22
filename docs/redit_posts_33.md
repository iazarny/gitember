# Reddit Posts — Gitember 3.3

---

## r/ukranian_dev

**Title:** Gitember 3.3 — безкоштовний open-source Git GUI клієнт (Java + Swing). Шукаю людей у команду

Привіт, спільното 👋

Я з 2016 року розвиваю **Gitember** — безкоштовний Git десктоп-клієнт з відкритим кодом. 
Усе почалося як експеримент на вихідні, а зараз вийшла версія **3.3**. Працює на Windows, macOS і Linux.

**Що нового у 3.3:**

🔧 **Покращений інтерактивний rebase** — зміна порядку, squash, fixup, drop, reword прямо з UI.

🌿 **Повна підтримка worktree** — коректне відображення статусу й diff для кожного worktree. Зручно, коли треба тримати стабільну гілку, поки працюєш над фічею в іншій.

⚔️ **Покращений 3-way резолвер конфліктів** — BASE / OURS / THEIRS поруч, редагування inline, staging одним кліком. Окремий merge-інструмент не потрібен.

🤖 **AI (експериментально)** — генерація commit-повідомлень, опис змін між гілками, виявлення витоку секретів. Працює через локальну модель (Ollama), за замовчуванням тепер `qwen2.5-coder`. Нічого не йде в хмару без вашого дозволу.

🔒 **Безпека** — перевірка контрольної суми Ollama після встановлення, інтеграція з OS keychain через Java Keyring, увімкнена перевірка TLS-сертифікатів.

**Інше з можливостей:** повнотекстовий пошук по історії (включно з Office, PDF, CAD, зображеннями), Git LFS, інтеграція з GitHub/GitLab/Bitbucket/Gitea, статистика репозиторію, порівняння файлів і тек.

---

**🙋 Приєднуйтесь до проєкту!**

Проєкт відкритий, і я шукаю людей, яким цікаво долучитися:
- **розробники** — Java 21, Swing + FlatLaf, JGit, Lucene; багато цікавих задач (AI, packaging, UI)
- **тестувальники** — потрібен фідбек на різних ОС і з різними репозиторіями
- будь-які ідеї, баг-репорти та пропозиції теж дуже вітаються

Це гарна нагода попрактикуватися на реальному open-source продукті українською командою.

GitHub: https://github.com/iazarny/gitember
Сайт: https://gitember.org

Радий буду відповісти на питання в коментарях 🙂

---

## r/git

**Title:** Gitember Git GUI v3.3 — better interactive rebase, worktrees, and a sharper 3-way merge editor

Just shipped v3.3 of Gitember, a free open-source Git GUI.

**What's new:**

**Interactive rebase improvements** — reorder, squash, fixup, drop, reword with a cleaner flow. No terminal gymnastics.

**Worktrees, done right** — full UI support with correct per-worktree status and diff display. Keep a long-running feature branch alive while you hop onto a hotfix, all from the UI.

**3-way merge conflict resolver (improved)** — BASE / OURS / THEIRS side-by-side, edit inline, stage with one click. No separate merge tool to install.

**AI-assisted writing (experimental)** — generate commit messages, explain what changed between two branches in plain language. Runs against a local Ollama model (default is now `qwen2.5-coder`), so your code stays on your machine.

The rest of the feature set:
- full-text search across commits/code/docs (including Office, PDF, and CAD files)
- Git LFS
- GitHub/GitLab/Bitbucket/Gitea integration
- repository statistics
- arbitrary file & folder comparison

**Want to help?** The project is open and looking for contributors and testers — devs comfortable with Java/Swing, and anyone willing to test across OSes and report bugs. PRs, issues, and feature ideas all welcome.

Source and downloads: https://gitember.org | https://github.com/iazarny/gitember

What Git GUI features do you wish existed? Genuinely curious.

---

## r/programming

**Title:** Gitember 3.3 — what I added to my Swing Git GUI, and why it now talks to a local LLM by default

In 2016 I started Gitember as a weekend experiment: a Git desktop client in Java. For v3 I rewrote the entire UI from JavaFX to Swing — JDK-bundled, fast startup, low memory, no CSS-engine overhead for a tool you open 20 times a day. FlatLaf handles the modern look.

Version 3.3 is out, and the theme this round is **polish + privacy**:

1. **Interactive rebase** — refined the reorder/squash/fixup/drop/reword flow so it feels less like editing a todo file and more like a UI.

2. **Worktrees** — full UI support with correct status and diff display per worktree. JGit doesn't fully implement the linked-worktree `commondir` mechanism, so the common `.git` dir and worktree-specific index path are resolved manually.

3. **3-way merge editor** — BASE / OURS / THEIRS in one view, inline editing, one-click staging. Reads stages 1/2/3 from the DirCache via JGit.

4. **AI, but local-first** — commit message generation and branch-diff explanations now default to a local Ollama model (`qwen2.5-coder` instead of `llama3.2`). Nothing leaves your machine unless you point it somewhere.

5. **Security hardening** — verify the Ollama binary checksum after download, store credentials in the OS keychain via Java Keyring, and (embarrassingly) stop disabling TLS certificate verification.

The rest: full-text search (commits, code, Office/PDF/CAD), LFS, GitHub/GitLab/Bitbucket/Gitea integration, statistics.

Stack: Java 21, Swing + FlatLaf, JGit, Lucene, LangChain4j (Ollama).

The project is open-source and I'd genuinely welcome contributors and testers — happy to mentor anyone who wants a real-world Swing/JGit codebase to hack on.

Repo: https://github.com/iazarny/gitember | https://gitember.org

Happy to talk about the JavaFX → Swing migration or JGit worktree internals if anyone's curious.

---

## r/java

**Title:** Gitember 3.3 — open-source Swing Git GUI: interactive rebase, worktrees, local-LLM commit messages (JGit + FlatLaf + Lucene + LangChain4j)

Released version 3.3 of Gitember, a Git desktop client I've been building since 2016. Sharing here because the stack might interest folks.

**Tech stack:**
- Java 21
- Swing + FlatLaf 3.7 (modern look, embedded title bar, dark/light)
- JGit 7.6 (all Git operations — no native git binary required)
- Apache Lucene 9.9 (full-text search over commits, code, Office/PDF/CAD documents)
- LangChain4j (Ollama) for the local-LLM features
- Jackson (settings, LFS batch API)

**What's new in 3.3:**

*Interactive rebase* — improved reorder/squash/fixup/drop/reword flow.

*Worktrees* — full UI with correct per-worktree status and diff. JGit doesn't fully support the `commondir` mechanism for linked worktrees, so the common `.git` dir is resolved manually and the worktree-specific index path is set explicitly.

*3-way merge resolver* — reads stage 1/2/3 from the DirCache directly via JGit, renders BASE/OURS/THEIRS side by side, applies resolved content on save, re-stages via `git add`.

*AI integration* — commit message generation and branch explanations via LangChain4j against a local Ollama endpoint. Default model changed from `llama3.2` to `qwen2.5-coder`.

*Security* — verify Ollama checksum after install, integrate Java Keyring with the CipherService for OS keychain storage of tokens, and remove the TLS-verification bypass.

**Looking for contributors & testers** — if you want to work on a real Swing + JGit codebase (AI features, packaging, UI polish), or just test on your platform and file issues, jump in. Happy to help newcomers get oriented.

Source: https://github.com/iazarny/gitember

---

## r/opensource

**Title:** Gitember 3.3 — free, MIT-licensed Git GUI with interactive rebase, worktrees, and optional local-AI commit messages

Hey r/opensource,

I've been building Gitember since 2016 — a free, open-source Git desktop client. Version 3.3 is out.

**License:** MIT
**Platform:** Windows, macOS, Linux (Java 21)
**No telemetry, no accounts, no subscriptions.**

**New in 3.3:**

🔧 **Interactive rebase improvements** — reorder, squash, fixup, drop, reword from the UI.

🌿 **Worktrees** — full UI support with correct status and diff per worktree. Keep a stable branch checked out while working on a feature.

⚔️ **3-way merge conflict resolver (improved)** — BASE, OURS, and THEIRS side by side. Resolve inline, stage with one click. No extra tools.

🤖 **AI features (optional, local-first)** — commit message generation and branch-diff explanations via a local Ollama model (default now `qwen2.5-coder`). Nothing is sent anywhere by default.

🔒 **Security** — Ollama checksum verification, OS keychain storage via Java Keyring, TLS certificate verification always on.

**Full feature list:**
- GitHub, GitLab, Bitbucket, Gitea integration (PR review/creation, branch management)
- Full-text search (commits, code, and documents including .docx, .xlsx, PDF, CAD files)
- Git LFS support
- Repository statistics and blame
- Diff viewer (unified, side-by-side)
- Cherry-pick, revert, stash
- Arbitrary file & folder comparison

Everything runs locally. No cloud required for core functionality.

**🙋 Contributors and testers wanted!** The project is actively looking for help — Java/Swing developers, and testers across platforms (and anyone interested in packaging: Flatpak, AUR, Homebrew). Issues, PRs, and ideas all welcome.

GitHub: https://github.com/iazarny/gitember
Site: https://gitember.org

---

## r/sysadmin

**Title:** Free Git GUI for people who hate context-switching — v3.3 sharpens worktrees, merge resolver, and adds air-gap-friendly AI

If you manage multiple repos or work across several branches at once, this might save you some time.

Gitember is a free, open-source Git desktop client. Version 3.3 highlights:

**Worktrees** — full UI support with correct per-worktree status and diff. Stay on a stable branch (monitoring, quick fix) while a longer task runs in another worktree. Point-and-click instead of `git worktree add` and manually tracking paths.

**3-way merge resolver (improved)** — BASE/OURS/THEIRS view when a pull conflicts. Pick a side or edit inline, then stage. Replaces a separate tool like meld or kdiff3.

**Local-first AI (optional)** — commit messages and branch-diff summaries run against a local Ollama model (default `qwen2.5-coder`). Fine for air-gapped setups; nothing leaves the box.

**Security tightening** — Ollama binary checksum verified after download, credentials stored in the OS keychain (Java Keyring), TLS certificate verification always enforced.

Runs on Windows, macOS, Linux. Java 21. A runnable JAR is available if you'd rather skip the installer.

**Open to contributors/testers** — if you want to test on your fleet's setups and file issues, or contribute, you're welcome.

https://gitember.org | https://github.com/iazarny/gitember

---

## r/foss

**Title:** Gitember 3.3 — MIT-licensed Git GUI, no cloud, no tracking, AI runs locally

Sharing Gitember 3.3 here because the values fit: fully open-source (MIT), no telemetry, no accounts, no vendor lock-in.

**What it is:** a Git desktop client. All Git operations go through JGit — no native git binary phoning home, no relay servers, no analytics. The only outbound connections are the ones you explicitly configure (your own remote, your own AI endpoint).

**Version 3.3 adds:**

🔧 **Interactive rebase improvements** — reorder, squash, fixup, drop, reword from the UI.

🌿 **Worktrees** — full UI support with correct per-worktree status and diff. Parallel branch work without juggling stash or multiple clones.

⚔️ **3-way merge conflict resolver (improved)** — BASE / OURS / THEIRS in one view. No dependency on meld, kdiff3, or any external tool. Everything stays local.

🤖 **AI — fully under your control, and now local-by-default.** Commit messages, PR descriptions, and branch-diff summaries run against a local Ollama model (default changed to `qwen2.5-coder`). Nothing leaves your machine unless you point it elsewhere.

🔒 **Security improvements** — Ollama checksum verification after install, OS keychain integration via Java Keyring, TLS certificate verification always on (no bypass).

**Full feature set:**
- GitHub, GitLab, Bitbucket, Gitea — PR review/creation, branch management
- Full-text search across commits, source code, and documents (.docx, .xlsx, PDF, CAD via Lucene)
- Git LFS
- Repository statistics and blame
- Cherry-pick, revert, stash
- Arbitrary file & folder comparison

**Stack:** Java 21, Swing + FlatLaf, JGit 7.6, Apache Lucene 9.9, LangChain4j — all open-source dependencies.

**License:** MIT
**Platforms:** Windows, macOS, Linux

**🙋 Want to get involved?** The project is open and looking for contributors and testers. Developers (Java/Swing/JGit), testers on different platforms, and packaging help (Flatpak, AUR, Homebrew formula) are all especially appreciated. PRs, issues, and feedback welcome.

GitHub: https://github.com/iazarny/gitember
Site: https://gitember.org

---

## LinkedIn

🚀 Gitember 3.3 is out — and I'm looking for people to build it with.

Back in 2016 I started Gitember as a weekend experiment: a free, open-source Git desktop client in Java. Nine years later it runs on Windows, macOS, and Linux, and version 3.3 just shipped.

Here's what's new in this release:

🔧 Interactive rebase — a cleaner UI for reorder, squash, fixup, drop, and reword.
🌿 Worktrees — full UI support with correct per-worktree status and diff. Stay on a stable branch while you work a feature in another.
⚔️ 3-way merge conflict resolver — BASE / OURS / THEIRS side by side, edit inline, stage with one click. No external merge tool needed.
🤖 AI, local-first — commit message generation and branch-diff explanations run against a local Ollama model (now defaulting to qwen2.5-coder). Your code never leaves your machine unless you choose otherwise.
🔒 Security hardening — Ollama checksum verification, OS keychain integration via Java Keyring, and TLS certificate verification always on.

It also does all the everyday Git work — full-text search across history (including Office, PDF, and CAD files), Git LFS, GitHub/GitLab/Bitbucket/Gitea integration, statistics, and arbitrary file & folder comparison.

The stack: Java 21, Swing + FlatLaf, JGit, Apache Lucene, and LangChain4j. MIT licensed. No telemetry, no accounts.

🙋 The project is open, and I'd love help. I'm looking for:
• Developers who enjoy Java / Swing / JGit — there's interesting work in AI features, packaging, and UI.
• Testers willing to try it across different OSes and repositories and file issues.
• Anyone with feedback, ideas, or bug reports.

Whether you want real-world open-source experience or just to make a tool you use better — you're welcome to jump in.

⭐ GitHub: https://github.com/iazarny/gitember
🌐 Site: https://gitember.org

#git #opensource #java #softwaredevelopment #developertools #devtools #swing #foss
