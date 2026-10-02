package com.az.gitember.service;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.AddNoteCommand;
import org.eclipse.jgit.api.ApplyCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.DeleteTagCommand;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.GarbageCollectCommand;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.RemoveNoteCommand;
import org.eclipse.jgit.api.RenameBranchCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.ShowNoteCommand;
import org.eclipse.jgit.api.StashApplyCommand;
import org.eclipse.jgit.api.StashCreateCommand;
import org.eclipse.jgit.api.StashDropCommand;
import org.eclipse.jgit.api.StashListCommand;
import org.eclipse.jgit.api.SubmoduleAddCommand;
import org.eclipse.jgit.api.SubmoduleDeinitCommand;
import org.eclipse.jgit.api.SubmoduleInitCommand;
import org.eclipse.jgit.api.SubmoduleStatusCommand;
import org.eclipse.jgit.api.SubmoduleSyncCommand;
import org.eclipse.jgit.api.SubmoduleUpdateCommand;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Responsible to generate vanilla git command.
 */
public class DocGenService {

    public DocGenService() {
    }

    /**
     * Get JGit command and create pure git command line.
     * @param jgitCommand give jgit command.
     * @return list of git command.
     */
    public List<String> commandLine(GitCommand<?> jgitCommand) {
        List<String> commandLine = new ArrayList<>();
        if (jgitCommand != null) {
            String git = toGit(jgitCommand);
            if (git != null && !git.isBlank()) {
                commandLine.add(git);
            }
        }
        return commandLine;
    }

    private String toGit(GitCommand<?> jgitCommand) {
        return switch (jgitCommand) {
            case AddCommand c -> gitAdd(c);
            case RmCommand c -> gitRm(c);
            case ResetCommand c -> gitReset(c);
            case CommitCommand c -> gitCommit(c);
            case CheckoutCommand c -> gitCheckout(c);
            case CreateBranchCommand c -> gitBranchCreate(c);
            case RenameBranchCommand c -> gitBranchRename(c);
            case DeleteBranchCommand c -> gitBranchDelete(c);
            case MergeCommand c -> gitMerge(c);
            case RebaseCommand c -> gitRebase(c);
            case LogCommand c -> gitLog(c);
            case DiffCommand c -> gitDiff(c);
            case ApplyCommand applyCommand -> "git apply";
            case TagCommand c -> gitTag(c);
            case DeleteTagCommand c -> gitTagDelete(c);
            case ListBranchCommand c -> gitBranchList(c);
            case StashListCommand stashListCommand -> "git stash list";
            case StashDropCommand c -> gitStashDrop(c);
            case StashApplyCommand c -> gitStashApply(c);
            case StashCreateCommand c -> gitStashCreate(c);
            //case ShowNoteCommand c -> gitNotesShow(c);
            case AddNoteCommand c -> gitNotesAdd(c);
            case RemoveNoteCommand c -> gitNotesRemove(c);
            case GarbageCollectCommand gcCommand -> "git gc";
            case SubmoduleInitCommand c -> gitSubmodule("init", c, false);
            case SubmoduleUpdateCommand c -> gitSubmodule("update", c, false);
            case SubmoduleAddCommand c -> gitSubmoduleAdd(c);
            case SubmoduleDeinitCommand c -> gitSubmodule("deinit", c, boolField(c, "force", false));
            case SubmoduleSyncCommand c -> gitSubmodule("sync", c, false);
            case SubmoduleStatusCommand c -> gitSubmodule("status", c, false);
            default -> "";//gitFallback(jgitCommand);
        };
    }

    private String gitAdd(AddCommand command) {
        List<String> tokens = git("add");
        if (command.isUpdate()) {
            tokens.add("--update");
        }
        if (command.isAll()) {
            tokens.add("--all");
        }
        appendPaths(tokens, collectionField(command, "filepatterns"));
        return join(tokens);
    }

    private String gitRm(RmCommand command) {
        List<String> tokens = git("rm");
        if (boolField(command, "cached", false)) {
            tokens.add("--cached");
        }
        appendPaths(tokens, collectionField(command, "filepatterns"));
        return join(tokens);
    }

    private String gitReset(ResetCommand command) {
        List<String> tokens = git("reset");
        Collection<?> paths = collectionField(command, "filepaths");
        ResetCommand.ResetType mode = (ResetCommand.ResetType) rawField(command, "mode");
        String ref = stringField(command, "ref");
        if (paths.isEmpty()) {
            if (mode == ResetCommand.ResetType.HARD) {
                tokens.add("--hard");
            } else if (mode == ResetCommand.ResetType.SOFT) {
                tokens.add("--soft");
            } else if (mode == ResetCommand.ResetType.MIXED) {
                tokens.add("--mixed");
            } else if (mode == ResetCommand.ResetType.KEEP) {
                tokens.add("--keep");
            } else if (mode == ResetCommand.ResetType.MERGE) {
                tokens.add("--merge");
            }
            if (ref != null && !ref.isBlank()) {
                tokens.add(quote(shortRef(ref)));
            }
        } else {
            if (ref != null && !ref.isBlank()) {
                tokens.add(quote(shortRef(ref)));
            }
            appendPaths(tokens, paths);
        }
        return join(tokens);
    }

    private String gitCommit(CommitCommand command) {
        List<String> tokens = git("commit");
        if (command.getAuthor() != null) {
            tokens.add("--author=" + quote(ident(command.getAuthor())));
        }
        if (boolField(command, "amend", false)) {
            tokens.add("--amend");
        }
        Boolean sign = (Boolean) rawField(command, "signCommit");
        if (Boolean.TRUE.equals(sign)) {
            tokens.add("-S");
        } else if (Boolean.FALSE.equals(sign)) {
            tokens.add("--no-gpg-sign");
        }
        String message = command.getMessage();
        if (message != null && !message.isEmpty()) {
            tokens.add("-m");
            tokens.add(quote(message));
        }
        appendPaths(tokens, collectionField(command, "only"));
        return join(tokens);
    }

    private String gitCheckout(CheckoutCommand command) {
        List<String> tokens = git("checkout");
        Collection<?> paths = collectionField(command, "paths");
        if (boolField(command, "forced", false) || boolField(command, "forceRefUpdate", false)) {
            tokens.add("--force");
        }
        if (!paths.isEmpty()) {
            appendPaths(tokens, paths);
        } else {
            if (boolField(command, "orphan", false)) {
                tokens.add("--orphan");
            } else if (boolField(command, "createBranch", false)) {
                tokens.add("-b");
            }
            String name = stringField(command, "name");
            if (name != null && !name.isBlank()) {
                tokens.add(quote(shortRef(name)));
            }
            String startPoint = stringField(command, "startPoint");
            RevObject startCommit = (RevObject) rawField(command, "startCommit");
            if (startPoint != null && !startPoint.isBlank()) {
                tokens.add(quote(shortRef(startPoint)));
            } else if (startCommit != null) {
                tokens.add(startCommit.getName());
            }
        }
        return join(tokens);
    }

    private String gitBranchCreate(CreateBranchCommand command) {
        List<String> tokens = git("branch");
        if (boolField(command, "force", false)) {
            tokens.add("--force");
        }
        CreateBranchCommand.SetupUpstreamMode upstream =
                (CreateBranchCommand.SetupUpstreamMode) rawField(command, "upstreamMode");
        if (upstream == CreateBranchCommand.SetupUpstreamMode.TRACK) {
            tokens.add("--track");
        } else if (upstream == CreateBranchCommand.SetupUpstreamMode.NOTRACK) {
            tokens.add("--no-track");
        } else if (upstream == CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM) {
            tokens.add("--set-upstream");
        }
        String name = stringField(command, "name");
        if (name != null && !name.isBlank()) {
            tokens.add(quote(shortRef(name)));
        }
        String startPoint = stringField(command, "startPoint");
        RevObject startCommit = (RevObject) rawField(command, "startCommit");
        if (startPoint != null && !startPoint.isBlank() && !"HEAD".equals(startPoint)) {
            tokens.add(quote(shortRef(startPoint)));
        } else if (startCommit != null) {
            tokens.add(startCommit.getName());
        }
        return join(tokens);
    }

    private String gitBranchRename(RenameBranchCommand command) {
        List<String> tokens = git("branch");
        tokens.add("-m");
        String oldName = stringField(command, "oldName");
        String newName = stringField(command, "newName");
        if (oldName != null && !oldName.isBlank()) {
            tokens.add(quote(shortRef(oldName)));
        }
        if (newName != null && !newName.isBlank()) {
            tokens.add(quote(shortRef(newName)));
        }
        return join(tokens);
    }

    private String gitBranchDelete(DeleteBranchCommand command) {
        List<String> tokens = git("branch");
        if (boolField(command, "force", false)) {
            tokens.add("-D");
        } else {
            tokens.add("-d");
        }
        appendPaths(tokens, collectionField(command, "branchNames"));
        return join(tokens);
    }

    private String gitMerge(MergeCommand command) {
        List<String> tokens = git("merge");
        Boolean squash = (Boolean) rawField(command, "squash");
        if (Boolean.TRUE.equals(squash)) {
            tokens.add("--squash");
        }
        MergeCommand.FastForwardMode ff =
                (MergeCommand.FastForwardMode) rawField(command, "fastForwardMode");
        if (ff == MergeCommand.FastForwardMode.NO_FF) {
            tokens.add("--no-ff");
        } else if (ff == MergeCommand.FastForwardMode.FF_ONLY) {
            tokens.add("--ff-only");
        }
        String message = stringField(command, "message");
        if (message != null && !message.isBlank()) {
            tokens.add("-m");
            tokens.add(quote(message));
        }
        Object commits = rawField(command, "commits");
        if (commits instanceof Collection<?> refs) {
            for (Object item : refs) {
                if (item instanceof Ref ref && ref.getName() != null) {
                    tokens.add(quote(shortRef(ref.getName())));
                } else if (item instanceof AnyObjectId id) {
                    tokens.add(id.getName());
                }
            }
        }
        return join(tokens);
    }

    private String gitRebase(RebaseCommand command) {
        List<String> tokens = git("rebase");
        RebaseCommand.Operation operation =
                (RebaseCommand.Operation) rawField(command, "operation");
        if (operation == RebaseCommand.Operation.CONTINUE) {
            tokens.add("--continue");
        } else if (operation == RebaseCommand.Operation.ABORT) {
            tokens.add("--abort");
        } else if (operation == RebaseCommand.Operation.SKIP) {
            tokens.add("--skip");
        } else {
            if (rawField(command, "interactiveHandler") != null) {
                tokens.add("--interactive");
            }
            if (boolField(command, "preserveMerges", false)) {
                tokens.add("--rebase-merges");
            }
            String upstream = stringField(command, "upstreamCommitName");
            RevObject upstreamCommit = (RevObject) rawField(command, "upstreamCommit");
            if (upstream != null && !upstream.isBlank()) {
                tokens.add(quote(shortRef(upstream)));
            } else if (upstreamCommit != null) {
                tokens.add(upstreamCommit.getName());
            }
        }
        return join(tokens);
    }

    private String gitLog(LogCommand command) {
        List<String> tokens = git("log");
        int maxCount = intField(command, "maxCount", -1);
        if (maxCount > -1) {
            tokens.add("-n");
            tokens.add(Integer.toString(maxCount));
        }
        Collection<?> pathFilters = collectionField(command, "pathFilters");
        if (!pathFilters.isEmpty()) {
            tokens.add("--");
            for (Object filter : pathFilters) {
                if (filter instanceof PathFilter pathFilter) {
                    tokens.add(quote(pathFilter.getPath()));
                }
            }
        }
        return join(tokens);
    }

    private String gitDiff(DiffCommand command) {
        List<String> tokens = git("diff");
        if (boolField(command, "cached", false)) {
            tokens.add("--cached");
        }
        if (boolField(command, "showNameOnly", false)) {
            tokens.add("--name-only");
        } else if (boolField(command, "showNameAndStatusOnly", false)) {
            tokens.add("--name-status");
        }
        int contextLines = intField(command, "contextLines", -1);
        if (contextLines > -1) {
            tokens.add("-U" + contextLines);
        }
        return join(tokens);
    }

    private String gitTag(TagCommand command) {
        List<String> tokens = git("tag");
        if (command.isForceUpdate()) {
            tokens.add("--force");
        }
        if (command.isAnnotated()) {
            tokens.add("-a");
        }
        if (command.isSigned()) {
            tokens.add("-s");
        }
        String message = command.getMessage();
        if (message != null && !message.isBlank()) {
            tokens.add("-m");
            tokens.add(quote(message));
        }
        if (command.getName() != null && !command.getName().isBlank()) {
            tokens.add(quote(command.getName()));
        }
        if (command.getObjectId() != null) {
            tokens.add(command.getObjectId().getName());
        }
        return join(tokens);
    }

    private String gitTagDelete(DeleteTagCommand command) {
        List<String> tokens = git("tag");
        tokens.add("-d");
        appendPaths(tokens, collectionField(command, "tags"));
        return join(tokens);
    }

    private String gitBranchList(ListBranchCommand command) {
        List<String> tokens = git("branch");
        ListBranchCommand.ListMode mode =
                (ListBranchCommand.ListMode) rawField(command, "listMode");
        if (mode == ListBranchCommand.ListMode.ALL) {
            tokens.add("-a");
        } else if (mode == ListBranchCommand.ListMode.REMOTE) {
            tokens.add("-r");
        }
        String contains = stringField(command, "containsCommitish");
        if (contains != null && !contains.isBlank()) {
            tokens.add("--contains");
            tokens.add(quote(contains));
        }
        return join(tokens);
    }

    private String gitStashDrop(StashDropCommand command) {
        List<String> tokens = git("stash");
        if (boolField(command, "all", false)) {
            tokens.add("clear");
        } else {
            tokens.add("drop");
            tokens.add("stash@{" + intField(command, "stashRefEntry", 0) + "}");
        }
        return join(tokens);
    }

    private String gitStashApply(StashApplyCommand command) {
        List<String> tokens = git("stash");
        tokens.add("apply");
        String stashRef = stringField(command, "stashRef");
        if (stashRef != null && !stashRef.isBlank()) {
            tokens.add(quote(stashRef));
        }
        return join(tokens);
    }

    private String gitStashCreate(StashCreateCommand command) {
        List<String> tokens = git("stash");
        tokens.add("push");
        if (boolField(command, "includeUntracked", false)) {
            tokens.add("--include-untracked");
        }
        String message = stringField(command, "workingDirectoryMessage");
        if (message != null && !message.isBlank() && !message.contains("{0}")) {
            tokens.add("-m");
            tokens.add(quote(message));
        }
        return join(tokens);
    }

    private String gitNotesShow(ShowNoteCommand command) {
        List<String> tokens = git("notes");
        tokens.add("show");
        appendObjectId(tokens, rawField(command, "id"));
        return join(tokens);
    }

    private String gitNotesAdd(AddNoteCommand command) {
        List<String> tokens = git("notes");
        tokens.add("add");
        tokens.add("--force");
        String message = stringField(command, "message");
        if (message != null) {
            tokens.add("-m");
            tokens.add(quote(message));
        }
        appendObjectId(tokens, rawField(command, "id"));
        return join(tokens);
    }

    private String gitNotesRemove(RemoveNoteCommand command) {
        List<String> tokens = git("notes");
        tokens.add("remove");
        appendObjectId(tokens, rawField(command, "id"));
        return join(tokens);
    }

    private String gitSubmodule(String subcommand, GitCommand<?> command, boolean force) {
        List<String> tokens = git("submodule");
        tokens.add(subcommand);
        if (force) {
            tokens.add("--force");
        }
        appendPaths(tokens, collectionField(command, "paths"));
        return join(tokens);
    }

    private String gitSubmoduleAdd(SubmoduleAddCommand command) {
        List<String> tokens = git("submodule");
        tokens.add("add");
        String uri = stringField(command, "uri");
        String path = stringField(command, "path");
        if (uri != null && !uri.isBlank()) {
            tokens.add(quote(uri));
        }
        if (path != null && !path.isBlank()) {
            tokens.add(quote(path));
        }
        return join(tokens);
    }

    private String gitFallback(GitCommand<?> jgitCommand) {
        String simple = jgitCommand.getClass().getSimpleName();
        if (simple.endsWith("Command")) {
            simple = simple.substring(0, simple.length() - "Command".length());
        }
        StringBuilder verb = new StringBuilder();
        for (int i = 0; i < simple.length(); i++) {
            char c = simple.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                verb.append('-');
            }
            verb.append(Character.toLowerCase(c));
        }
        return "git " + verb;
    }

    private List<String> git(String verb) {
        List<String> tokens = new ArrayList<>();
        tokens.add("git");
        tokens.add(verb);
        return tokens;
    }

    private void appendPaths(List<String> tokens, Collection<?> paths) {
        if (paths != null && !paths.isEmpty()) {
            tokens.add("--");
            for (Object path : paths) {
                if (path != null) {
                    tokens.add(quote(shortRef(String.valueOf(path))));
                }
            }
        }
    }

    private void appendObjectId(List<String> tokens, Object id) {
        if (id instanceof AnyObjectId objectId) {
            tokens.add(objectId.getName());
        }
    }

    private String ident(PersonIdent person) {
        return person.getName() + " <" + person.getEmailAddress() + ">";
    }

    private String shortRef(String ref) {
        String name = ref;
        if (name.startsWith("refs/heads/")) {
            name = name.substring("refs/heads/".length());
        } else if (name.startsWith("refs/tags/")) {
            name = name.substring("refs/tags/".length());
        }
        return name;
    }

    private String join(List<String> tokens) {
        return String.join(" ", tokens);
    }

    private String quote(String token) {
        String quoted = token;
        if (token != null) {
            boolean needsQuotes = false;
            for (int i = 0; i < token.length(); i++) {
                char c = token.charAt(i);
                if (!(Character.isLetterOrDigit(c)
                        || c == '.' || c == '/' || c == '_' || c == '-'
                        || c == '@' || c == ':' || c == '{' || c == '}'
                        || c == '^' || c == '~' || c == '+' || c == '='
                        || c == '*')) {
                    needsQuotes = true;
                }
            }
            if (needsQuotes) {
                quoted = "'" + token.replace("'", "'\\''") + "'";
            }
        }
        return quoted;
    }

    private Object rawField(Object target, String name) {
        Object value = null;
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            value = field.get(target);
        } catch (ReflectiveOperationException ignored) {
        }
        return value;
    }

    private String stringField(Object target, String name) {
        String value = null;
        Object raw = rawField(target, name);
        if (raw instanceof String s) {
            value = s;
        }
        return value;
    }

    private boolean boolField(Object target, String name, boolean defaultValue) {
        boolean value = defaultValue;
        Object raw = rawField(target, name);
        if (raw instanceof Boolean b) {
            value = b;
        }
        return value;
    }

    private int intField(Object target, String name, int defaultValue) {
        int value = defaultValue;
        Object raw = rawField(target, name);
        if (raw instanceof Integer i) {
            value = i;
        }
        return value;
    }

    private Collection<?> collectionField(Object target, String name) {
        Collection<?> value = List.of();
        Object raw = rawField(target, name);
        if (raw instanceof Collection<?> c) {
            value = c;
        }
        return value;
    }
}
