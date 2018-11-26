package com.az.gitember.scm.impl.git;

import com.az.gitember.GitemberApp;
import com.az.gitember.misc.*;
import com.az.gitember.scm.exception.GECannotDeleteCurrentBranchException;
import com.az.gitember.scm.exception.GECheckoutConflictException;
import com.az.gitember.scm.exception.GEScmAPIException;
import javafx.scene.control.Alert;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheCheckout;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.eclipse.jgit.lib.IndexDiff.StageState.BOTH_DELETED;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 * TODO extract interface
 */
public class GitRepositoryService {

    private final static Logger log = Logger.getLogger(GitRepositoryService.class.getName());

    private final static List<File> tempFiles = new ArrayList<>();

    private final Repository repository;

    private final StoredConfig config;

    /**
     * Construct service, which work with git
     *
     * @param gitFolder folder with git repository, for example "~/project/.git"
     * @throws IOException
     */
    public GitRepositoryService(final String gitFolder) throws IOException {
        this.repository = GitUtil.openRepository(gitFolder);
        this.config = repository.getConfig();
    }

    public GitRepositoryService() {
        repository = null;
        config = null;
    }

    public void setUserName(String userName) {
        try {
            config.setString("user", null, "name", userName);
            config.save();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot save settings", e);
        }
    }

    public void setUserEmail(String userEmail) {
        try {
            config.setString("user", null, "email", userEmail);
            config.save();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot save settings", e);
        }
    }

    public String getUserName() {
        return config.getString("user", null, "name");
    }

    public String getUserEmail() {
        return config.getString("user", null, "email");
    }

    public String getRemoteUrl() {
        return config.getString(ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME, "url");
    }

    public ScmBranch getScmBranchByName(String name) {
        try (Git git = new Git(repository)) {
            List<Ref> branchLst = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            Ref r = branchLst.stream()
                    .filter(ref -> ref.getName().equals(name))
                    .findFirst()
                    .get();
            if (r != null) {
                ScmBranch rez = new ScmBranch(
                        r.getName().substring(Constants.R_HEADS.length()),
                        r.getName(),
                        ScmBranch.BranchType.LOCAL,
                        r.getObjectId().getName()
                );
                checkIsTrackingRemoteBranch(git.getRepository().getConfig(), rez);
                return rez;
            }
        } catch (GitAPIException e) {
            log.log(Level.SEVERE, "Cannot get list of branches", e);
        }
        return null;
    }

    private List<ScmBranch> getBranches(final ListBranchCommand.ListMode listMode,
                                        final String prefix,
                                        final ScmBranch.BranchType branchType) throws Exception {

        try (Git git = new Git(repository)) {
            Pair<String, String> head = this.getHead();
            String headName = head.getFirst();

            List<Ref> branchLst = git.branchList().setListMode(listMode).call();

            List<ScmBranch> rez = branchLst
                    .stream()
                    .filter(r -> r.getName().startsWith(prefix) || ("HEAD".equals(r.getName()) && Constants.R_HEADS.equals(prefix)))
                    .map(r -> new ScmBranch(
                            "HEAD".equals(r.getName()) ? r.getName() : r.getName().substring(prefix.length()),
                            r.getName(),
                            branchType,
                            r.getObjectId().getName()

                    ))
                    .map(i -> {
                        i.setHead(i.getFullName().equals(headName));
                        return i;
                    })
                    .sorted((o1, o2) -> o1.getShortName().compareTo(o2.getShortName()))
                    .collect(Collectors.toList());


            // Just check is local branch has remote part
            if (Constants.R_HEADS.equals(prefix)) {
                Config cfg = repository.getConfig();
                for (ScmBranch item : rez) {
                    checkIsTrackingRemoteBranch(cfg, item);
                }

            }
            return rez;
        }
    }

    private void checkIsTrackingRemoteBranch(Config cfg, ScmBranch item) {
        String remote = cfg.getString("branch", item.getShortName(), ConfigConstants.CONFIG_KEY_REMOTE);
        if (remote != null) {
            String mergeRef = cfg.getString("branch", item.getShortName(), "merge");
            String shortRemoteName = mergeRef.substring(Constants.R_HEADS.length());
            log.log(Level.INFO, "Local branch " + item.getFullName()
                    + " is set to track remote " + mergeRef
                    + "(" + shortRemoteName + ")");
            item.setRemoteName(shortRemoteName);
        }
    }

    public List<ScmBranch> getTags() {
        try (Git git = new Git(repository)) {
            List<Ref> branchLst = git.tagList().call();
            return branchLst
                    .stream()
                    .map(r -> new ScmBranch(
                            r.getName(),
                            r.getName(),
                            ScmBranch.BranchType.TAG,
                            r.getObjectId().getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get tags", e);
        }
        return Collections.emptyList();
    }


    public Pair<String, String> getHead() throws Exception {
        final Ref head = repository.exactRef(Constants.HEAD);
        return new Pair<>(
                head.getTarget().getName(),
                head.getObjectId() == null ? null : head.getObjectId().getName()
        );

    }

    /**
     * Get list of local branches.
     *
     * @return list of local branches.
     */
    public List<ScmBranch> getLocalBranches() {
        try {
            return getBranches(ListBranchCommand.ListMode.ALL, Constants.R_HEADS,
                    ScmBranch.BranchType.LOCAL);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get local branches", e);
        }
        return Collections.emptyList();
    }

    public List<ScmBranch> getRemoteBranches() {
        try {
            return getBranches(
                    ListBranchCommand.ListMode.REMOTE,
                    GitConst.REMOTE_PREFIX,
                    ScmBranch.BranchType.REMOTE);
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get tags", e);
        }
        return Collections.emptyList();
    }


    /**
     * Get list of revision, where given file was changed
     *
     * @param treeName tree name
     * @param fileName given fileName
     * @return list of revisions where file was changed.
     * @throws Exception
     */
    public List<ScmRevisionInformation> getFileHistory(final String treeName, final String fileName) throws Exception {
        return getFileHistory(treeName, fileName, Integer.MAX_VALUE);
    }

    /**
     * Get list of revision, where given file was changed
     *
     * @param treeName tree name
     * @param fileName given fileName
     * @param limit    limit for history items
     * @return list of revisions where file was changed.
     * @throws Exception
     */
    public List<ScmRevisionInformation> getFileHistory(final String treeName,
                                                       final String fileName,
                                                       final int limit) throws Exception {
        final ArrayList<ScmRevisionInformation> rez = new ArrayList<>();
        try (Git git = new Git(repository)) {
            final LogCommand cmd = git.log()
                    .add(repository.resolve(treeName))
                    .setRevFilter(RevFilter.ALL)
                    .addPath(fileName);
            final Iterable<RevCommit> revCommits = cmd.call();
            for (RevCommit revCommit : revCommits) {
                final ScmRevisionInformation info = adapt(revCommit, fileName);
                rez.add(info);
                if (rez.size() >= limit) {
                    break;
                }
            }
        }
        return rez;
    }


    /**
     * Adapt given rev commit to <code>ScmRevisionInformation</code>
     *
     * @param revCommit given <code>RevCommit</code>
     * @param fileName  optional file filter
     * @return instance of {@link ScmRevisionInformation}
     */
    public ScmRevisionInformation adapt(final RevCommit revCommit, final String fileName) {
        ScmRevisionInformation info = new ScmRevisionInformation();
        info.setShortMessage(revCommit.getShortMessage());
        info.setFullMessage(revCommit.getFullMessage());
        info.setRevisionFullName(revCommit.getName());
        info.setDate(GitemberUtil.intToDate(revCommit.getCommitTime()));
        info.setAuthorName(revCommit.getCommitterIdent().getName());
        info.setAuthorEmail(revCommit.getCommitterIdent().getEmailAddress());
        info.setParents(
                Arrays.stream(revCommit.getParents()).map(AnyObjectId::getName).collect(Collectors.toList())
        );
        if (revCommit instanceof PlotCommit) {
            ArrayList<String> refs = new ArrayList<>();
            for (int i = 0; i < ((PlotCommit) revCommit).getRefCount(); i++) {
                refs.add(
                        ((PlotCommit) revCommit).getRef(i).getName()
                );
            }
            info.setRef(refs);

        }
        info.setAffectedItems(getScmItems(revCommit, fileName));
        return info;
    }


    /**
     * Get lis of changed files.
     *
     * @param treeName           given tree
     * @param revisionCommitName given revision
     * @return list of changed files
     * @throws Exception
     */
    public List<ScmItem> getChangedFiles(final String treeName, final String revisionCommitName) throws Exception {
        try (Git git = new Git(repository)) {
            final LogCommand cmd = git.log()
                    .add(repository.resolve(treeName))
                    .setRevFilter(new SingleRevisionFilter(revisionCommitName));
            return getScmItems(cmd);
        }
    }


    /**
     * Get list of  revisions for given cmd
     *
     * @param cmd given cmd
     * @return list of {@link ScmItem}
     * @throws GitAPIException
     * @throws IOException
     */
    private List<ScmItem> getScmItems(LogCommand cmd) throws GitAPIException, IOException {
        ArrayList<ScmItem> scmItems = new ArrayList<>();
        final Iterable<RevCommit> revCommits = cmd.call();
        final RevCommit revCommit = revCommits.iterator().next();
        scmItems.addAll(
                getScmItems(revCommit, null)
        );
        return scmItems;
    }

    /**
     * Get list of files in given revision.
     *
     * @param revCommit rev commit instance
     * @param filePath  optional value to filter list of changed files
     * @return list of files in given revision
     * @throws IOException
     */
    public ArrayList<ScmItem> getScmItems(RevCommit revCommit, String filePath) {
        try (RevWalk rw = new RevWalk(repository)) {
            ArrayList<ScmItem> rez = new ArrayList<>();
            if (revCommit != null) {
                final DiffFormatter df = getDiffFormatter(filePath);
                final List<DiffEntry> diffs;
                try {
                    diffs = df.scan(
                            revCommit.getParentCount() > 0 ? rw.parseCommit(revCommit.getParent(0).getId()).getTree() : null,
                            revCommit.getTree());
                    diffs.stream()
                            .map(this::adaptDiffEntry)
                            .collect(Collectors.toCollection(() -> rez));

                } catch (IOException e) {
                    log.log(Level.SEVERE, "Cannot collect items from rev commit", e);
                }
            }
            rw.dispose();
            return rez;
        }
    }


    /**
     * Get diff formatter.
     *
     * @param filePath optional filter for diff
     * @return diff formater
     */
    private DiffFormatter getDiffFormatter(String filePath) {
        final DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        if (filePath != null) {
            df.setPathFilter(PathFilter.create(filePath));
        }
        return df;
    }

    private ScmItem adaptDiffEntry(DiffEntry diff) {
        ScmItemAttribute attr = new ScmItemAttribute();
        if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
            attr.setName(diff.getOldPath());
        } else if (diff.getChangeType() == DiffEntry.ChangeType.COPY || diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
            attr.setName(diff.getNewPath());
            attr.setOldName(diff.getOldPath());
        } else {
            attr.setName(diff.getNewPath());
        }
        return new ScmItem(diff.getChangeType().name(), attr);
    }


    public PlotCommitList<PlotLane> getCommitsByTree(final String treeName) throws Exception {
        try (PlotWalk revWalk = new PlotWalk(repository)) {
            final ObjectId rootId = repository.resolve(treeName);
            final RevCommit root = revWalk.parseCommit(rootId);
            revWalk.markStart(root);

            final PlotCommitList<PlotLane> plotCommitList = new PlotCommitList<>();
            plotCommitList.source(revWalk);
            plotCommitList.fillTo(Integer.MAX_VALUE);
            revWalk.dispose();
            return plotCommitList;
        }
    }


    public String saveDiff(String treeName, String oldRevision, String newRevision, String fileName) throws Exception {

        final File temp = File.createTempFile(Const.TEMP_FILE_PREFIX, Const.DIFF_EXTENSION);
        GitRepositoryService.deleteOnExit(temp);

        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldRevision);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newRevision);

        try (Git git = new Git(repository); OutputStream outputStream = new FileOutputStream(temp)) {
            List<DiffEntry> diff = git.diff().
                    setOldTree(oldTreeParser).
                    setNewTree(newTreeParser).
                    setPathFilter(PathFilter.create(fileName)).
                    call();
            for (DiffEntry entry : diff) {
                //System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
                try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
                    formatter.setRepository(repository);
                    formatter.format(entry);
                }
            }
        }

        return temp.getAbsolutePath();


    }

    private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        //noinspection Duplicates
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = repository.newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            }

            walk.dispose();

            return oldTreeParser;
        }
    }


    /**
     * Save given fileName at tree/revision into output stream.
     *
     * @param treeName     tree name
     * @param revisionName revision name
     * @param fileName     file name in repository
     * @return absolute path to saved diff file
     */
    public String saveDiff(String treeName, String revisionName,
                           String fileName) throws Exception {

        final File temp = File.createTempFile(Const.TEMP_FILE_PREFIX, Const.DIFF_EXTENSION);
        GitRepositoryService.deleteOnExit(temp);

        try (Git git = new Git(repository);
             RevWalk rw = new RevWalk(repository);
             OutputStream outputStream = new FileOutputStream(temp)) {

            final LogCommand cmd = git.log()
                    .add(repository.resolve(treeName))
                    .setRevFilter(new SingleRevisionFilter(revisionName))
                    .addPath(fileName);

            final Iterable<RevCommit> revCommits = cmd.call();
            final RevCommit revCommit = revCommits.iterator().next();

            if (revCommit != null) {
                final DiffFormatter df = getDiffFormatter(fileName);
                final List<DiffEntry> diffs = df.scan(
                        revCommit.getParentCount() > 0 ? rw.parseCommit(revCommit.getParent(0).getId()).getTree() : null,
                        revCommit.getTree());
                final DiffEntry diffEntry = diffs.get(0);

                try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
                    formatter.setRepository(repository);
                    formatter.format(diffEntry);
                }

            }
            rw.dispose();

        }
        return temp.getAbsolutePath();

    }


    /**
     * Save given fileName at tree/revision into output stream.
     *
     * @param treeName     tree name
     * @param revisionName revision name
     * @param fileName     file name
     * @return absolute path to saved file
     */
    public String saveFile(String treeName, String revisionName,
                           String fileName) throws IOException {

        final ObjectId lastCommitId = repository.resolve(revisionName);
        final String fileNameExtension = GitemberUtil.getExtension(fileName);
        final File temp = File.createTempFile(
                Const.TEMP_FILE_PREFIX,
                fileNameExtension.isEmpty() ? fileNameExtension : "." + fileNameExtension);
        GitRepositoryService.deleteOnExit(temp);

        try (RevWalk revWalk = new RevWalk(repository);
             OutputStream outputStream = new FileOutputStream(temp)) {

            RevCommit commit = revWalk.parseCommit(lastCommitId);
            // and using commit's tree find the path
            RevTree tree = commit.getTree();

            // now try to find a specific file
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(fileName));
                if (!treeWalk.next()) {
                    throw new IllegalStateException("Did not find expected file " + fileName);
                }


                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repository.open(objectId);

                // and then one can the loader to read the file
                loader.copyTo(outputStream);
            }

            revWalk.dispose();
        }

        return temp.getAbsolutePath();
    }

    /**
     * Get statuses
     *
     * @return
     */
    public List<ScmItem> getStatuses(final String path) throws Exception {

        Map<String, List<String>> statusMap = new HashMap<>();
        List<ScmItem> scmItems = new ArrayList<>();
        try (Git git = new Git(repository)) {

            StatusCommand statusCommand = git.status();
            if (path != null) {
                statusCommand.addPath(path);
            }
            Status status = statusCommand.call();

            Map<String, IndexDiff.StageState> conflictingStageState = status.getConflictingStageState();
//            for(Map.Entry<String, IndexDiff.StageState> entry : conflictingStageState.entrySet()) {
            //              System.out.println("ConflictingState: " + entry);
            //        }


            status.getConflicting().forEach(item -> enrichStatus(statusMap, item, ScmItemStatus.CONFLICT));
            status.getAdded().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.ADDED));
            status.getAdded().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.CHANGED));
            status.getMissing().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.MISSED));
            status.getModified().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.MODIFIED));
            status.getRemoved().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.REMOVED));
            status.getUncommittedChanges().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.UNCOMMITED));
            status.getUntracked().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.UNTRACKED));
            status.getUntrackedFolders().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.UNTRACKED_FOLDER));


            statusMap.forEach((s, strings) -> {
                scmItems.add(
                        new ScmItem(s, new ScmItemAttribute(strings, getConflictSubStatus(s, conflictingStageState)))
                );
            });

        }


        scmItems.sort((o1, o2) -> o1.getShortName().compareTo(o2.getShortName()));

        return scmItems;

    }

    private String getConflictSubStatus(String key, Map<String, IndexDiff.StageState> conflictingStageState) {

        return adaptConflictingState(conflictingStageState.get(key));
    }

    private String adaptConflictingState(IndexDiff.StageState stageState) {
        if (stageState == null) {
            return null;
        }
        switch (stageState) {

            case BOTH_DELETED:
                return ScmItemStatus.CONFLICT_BOTH_DELETED;
            case ADDED_BY_US:
                return ScmItemStatus.CONFLICT_ADDED_BY_US;
            case DELETED_BY_THEM:
                return ScmItemStatus.CONFLICT_DELETED_BY_THEM;
            case ADDED_BY_THEM:
                return ScmItemStatus.CONFLICT_ADDED_BY_THEM;
            case DELETED_BY_US:
                return ScmItemStatus.CONFLICT_DELETED_BY_US;
            case BOTH_ADDED:
                return ScmItemStatus.CONFLICT_BOTH_ADDED;
            case BOTH_MODIFIED:
                return ScmItemStatus.CONFLICT_BOTH_MODIFIED;
            default:
                return null;

        }
    }

    private void enrichStatus(Map<String, List<String>> rez, String item, String itemStatus) {
        List<String> list = rez.getOrDefault(item, new ArrayList<>());
        list.add(itemStatus);
        rez.put(item, list);
    }


    /**
     * Add file to stage before commit.
     *
     * @param fileName
     * @throws Exception
     */
    public void addFileToCommitStage(String fileName) {
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(fileName).call();
        } catch (GitAPIException e) {
            log.log(Level.WARNING, "Cannot add file to stage", e);
        }
    }


    /**
     * Rebase changes from given upstream into working copy.
     *
     * @param upstream
     * @throws GEScmAPIException
     */
    public RemoteOperationValue rebase(final String upstream,
                                       final ProgressMonitor progressMonitor) {
        try (Git git = new Git(repository)) {

            RebaseCommand rebaseCommand = git
                    .rebase()
                    .setUpstream(upstream)
                    .setProgressMonitor(progressMonitor);
            RebaseResult rez = rebaseCommand.call();

            String rezExplanation = getRebaseResultExplanation(rez);
            if (rez.getStatus().isSuccessful()) {
                log.log(Level.INFO, "Rebase result " + rezExplanation);
                return new RemoteOperationValue(RemoteOperationValue.Result.OK, rezExplanation);

            } else {
                log.log(Level.INFO, "Rebase result was not successful, so revert changes " + rezExplanation);
                rez = git
                        .rebase()
                        .setUpstream(upstream)
                        .setProgressMonitor(progressMonitor)
                        .setOperation(RebaseCommand.Operation.ABORT)
                        .call();
                rezExplanation = getRebaseResultExplanation(rez);
                return new RemoteOperationValue(RemoteOperationValue.Result.ERROR, rezExplanation);
            }
        } catch (GitAPIException e) {
            log.log(Level.WARNING, "Rebase error", e);
            return new RemoteOperationValue(RemoteOperationValue.Result.ERROR, e.getMessage());
        }
    }

    private String getRebaseResultExplanation(RebaseResult rez) {
        StringBuilder rezInfo = new StringBuilder();
        rezInfo.append("Status : ");
        rezInfo.append(rez.getStatus());
        if (rez.getConflicts() != null) {
            rezInfo.append("\nConflicts : ");
            rezInfo.append(rez.getConflicts().stream().collect(Collectors.joining(",\n")));
        }
        if (rez.getFailingPaths() != null) {
            rezInfo.append("\nFailing paths : ");
            rezInfo.append(rez
                    .getFailingPaths()
                    .entrySet()
                    .stream()
                    .map(stringMergeFailureReasonEntry -> stringMergeFailureReasonEntry.getKey() + " " + stringMergeFailureReasonEntry.getValue())
                    .collect(Collectors.joining(",\n")));
        }
        if (rez.getUncommittedChanges() != null) {
            rezInfo.append("\nUncommitted changes : ");
            rezInfo.append(rez.getUncommittedChanges().stream().collect(Collectors.joining(",\n")));
        }
        return rezInfo.toString();
    }

    /**
     * Commit changes.
     *
     * @param message                     commit message
     * @param overwriteAuthorWithCommiter
     * @return result.
     * @throws GEScmAPIException
     */
    public RevCommit commit(final String message,
                            final boolean overwriteAuthorWithCommiter) throws GEScmAPIException {
        try (Git git = new Git(repository)) {
            final CommitCommand cmd = git
                    .commit()
                    .setMessage(message);

            if (overwriteAuthorWithCommiter) {
                cmd.setAuthor(
                        GitemberApp.getRepositoryService().getUserName(),
                        GitemberApp.getRepositoryService().getUserEmail());
            }


            return cmd.call();
        } catch (GitAPIException e) {
            log.log(Level.SEVERE, "Cannot commit", e);
            throw new GEScmAPIException(e.getMessage());
        }
    }

    public void removeMissedFile(String fileName) throws Exception {
        try (Git git = new Git(repository)) {
            git.rm().addFilepattern(fileName).call();
        }

    }

    //TODO unstage
    public void removeFileFromCommitStage(String fileName) throws Exception {


    }

    /**
     * Create new git repository.
     *
     * @param absPath path to repository.
     */
    public void createRepository(final String absPath) throws Exception {
        String readmeInitialContent = "# " + (new File(absPath)).getName();
        try (Git git = Git.init()
                .setDirectory(new File(absPath))
                .call()) {

            git.commit().setMessage("Init").call();

            Files.write(
                    Paths.get(absPath + File.separator + "README.md"),
                    readmeInitialContent.getBytes(),
                    StandardOpenOption.CREATE);
            Files.write(
                    Paths.get(absPath + File.separator + ".gitignore"),
                    "".getBytes(),
                    StandardOpenOption.CREATE);

        }

    }


    /**
     * Clone remote repository.
     *
     * @param reporitoryUrl repo url
     * @param folder        optional folder where store cloned repository
     * @param userName      optional user name
     * @param password      optional password
     */
    public RemoteOperationValue cloneRepository(final String reporitoryUrl, final String folder,
                                                final String userName, final String password,
                                                final ProgressMonitor progressMonitor) {

        final CloneCommand cmd = Git.cloneRepository()
                .setURI(reporitoryUrl)
                .setDirectory(new File(folder))
                .setCloneAllBranches(true)
                .setCloneSubmodules(true)
                .setProgressMonitor(progressMonitor);
        if (userName != null) {
            cmd.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider(userName, password)
            );
        }


        if (userName != null) {
            return fetchRepository(reporitoryUrl, folder, userName, password, progressMonitor);
        }

        try {
            try (Git result = cmd.call()) {
                Object rez = result.getRepository().getDirectory().getAbsolutePath();
                return new RemoteOperationValue(rez);

            }
        } catch (TransportException te) {
            if (te.getCause() != null && te.getCause().getCause() != null
                    && te.getCause().getCause().getClass().equals(SSLHandshakeException.class)) {
                return fetchRepository(reporitoryUrl, folder, userName, password, progressMonitor);
            } else {
                return processError(te);
            }
        } catch (Exception e) {
            return processError(e);
        }

    }

    private RemoteOperationValue processError(Exception e) {
        if (e instanceof TransportException) {
            if (e.getMessage().contains("Authentication is required")) {
                log.log(Level.INFO, e.getMessage());
                return new RemoteOperationValue(RemoteOperationValue.Result.AUTH_REQUIRED, e.getMessage());
            } else if (e.getMessage().contains("not authorized")) {
                log.log(Level.INFO, e.getMessage());
                return new RemoteOperationValue(RemoteOperationValue.Result.NOT_AUTHORIZED, e.getMessage());
            } else if (e.getMessage().contains("remote end: unpack-objects")) {
                log.log(Level.WARNING, "Unexpected transport issue. Check disk space on remote repository", e);
                return new RemoteOperationValue(RemoteOperationValue.Result.ERROR, e.getMessage());
            } else {
                log.log(Level.WARNING, "Unexpected transport issue", e);
                return new RemoteOperationValue(RemoteOperationValue.Result.ERROR, e.getMessage());
            }
        } else if (e instanceof GitAPIException) {
            log.log(Level.WARNING, "GitAPIException", e);
            return new RemoteOperationValue(RemoteOperationValue.Result.ERROR, e.getMessage());
        }
        log.log(Level.WARNING, "Unexpected", e);
        return new RemoteOperationValue(RemoteOperationValue.Result.ERROR, e.getMessage());

    }


    /**
     * Fetch all or particular branch.
     * <p>
     * //http://stackoverflow.com/questions/16319807/determine-if-a-branch-is-remote-or-local-using-jgit
     * //http://stackoverflow.com/questions/12927163/jgit-checkout-a-remote-branch
     *
     * @param shortRemoteBranch optional short name on remote repo, i.e. without ref/heads/ prefix, if not provided command will fetch all
     * @param userName          optional username
     * @param password          optional password
     * @param progressMonitor   optional progress
     * @return result of operation
     */
    public RemoteOperationValue remoteRepositoryFetch(final String shortRemoteBranch,
                                                      final String userName, final String password,
                                                      final ProgressMonitor progressMonitor) {
        log.log(Level.INFO,
                MessageFormat.format("Fetch {0} null means all, for user {1}", shortRemoteBranch, userName));

        try (Git git = new Git(repository)) {
            final FetchCommand fetchCommand = git
                    .fetch()
                    .setCheckFetchedObjects(true)
                    .setProgressMonitor(progressMonitor);
            if (shortRemoteBranch != null) {
                fetchCommand.setRefSpecs(new RefSpec(Constants.R_HEADS + shortRemoteBranch));
            }

            if (userName != null) {
                fetchCommand.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(userName, password)
                );
            }
            FetchResult fetchResult = fetchCommand.call();
            return new RemoteOperationValue("TODO fetch result more human readable or " + fetchResult.toString());
        } catch (CheckoutConflictException conflictException) {
            return new RemoteOperationValue("TODO fetch conflict error" + conflictException.getMessage());
        } catch (GitAPIException e) {
            return processError(e);
        }

    }

    /**
     * Pull changes.
     *
     * @param userName optional user name if required
     * @param password optional password
     * @return
     * @throws Exception
     */
    public RemoteOperationValue remoteRepositoryPull(final String shortRemoteBranch,
                                                     final String userName, final String password,
                                                     final ProgressMonitor progressMonitor) {
        log.log(Level.INFO,
                MessageFormat.format("Fetch {0} null means all, for user {1}", shortRemoteBranch, userName));

        try (Git git = new Git(repository)) {
            PullCommand pullCommand = git.pull()
                    .setProgressMonitor(progressMonitor);

            if (shortRemoteBranch != null) {
                pullCommand.setRemoteBranchName(Constants.R_HEADS + shortRemoteBranch);
            }

            if (userName != null) {
                pullCommand.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(userName, password));
            }
            PullResult pullRez = pullCommand.call();
            return new RemoteOperationValue(pullRez.toString());
        } catch (CheckoutConflictException conflictException) {
            return new RemoteOperationValue("TODO fetch conflict error" + conflictException.getMessage());

        } catch (Exception e) {
            return processError(e);
        }
    }

    private RemoteOperationValue fetchRepository(final String reporitoryUrl, final String folder,
                                                 final String userName, final String password,
                                                 final ProgressMonitor progressMonitor) {
        try (Git git = Git.open(new File(folder))) {
            final StoredConfig config = git.getRepository().getConfig();
            config.setString(ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME, "url", reporitoryUrl);
            config.setString(ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME, "fetch", "+refs/heads/*:refs/remotes/origin/*");
            config.setBoolean("http", null, "sslVerify", false);
            config.save();
            final FetchCommand fetchCommand = git.fetch()
                    .setRemote(Constants.DEFAULT_REMOTE_NAME)
                    .setProgressMonitor(progressMonitor);

            if (userName != null) {
                fetchCommand.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(userName, password)
                );
            }
            FetchResult fetchResult = fetchCommand.call();
            checkout(git.getRepository(), fetchResult);

            return new RemoteOperationValue(git.getRepository().getDirectory().getAbsolutePath());
        } catch (Exception e) {
            return processError(e);
        }
    }


    /**
     * Create stash
     *
     * @throws GEScmAPIException in case of error.
     */
    public void stash() throws GEScmAPIException {
        try (Git git = new Git(repository)) {
            git.stashCreate().call();
        } catch (GitAPIException e) {
            throw new GEScmAPIException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Get stash list.
     *
     * @return list of stashed changes
     * @throws GEScmAPIException
     */
    public List<ScmRevisionInformation> getStashList() {
        try (Git git = new Git(repository)) {
            return git.stashList()
                    .call()
                    .stream()
                    .map(revCommit -> adapt(revCommit, null))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get tags", e);
        }
        return Collections.emptyList();
    }

    /**
     * Delete stash.
     *
     * @param stashRef stash reference id.
     * @throws GEScmAPIException in case of errors.
     */
    public void deleteStash(int stashRef) throws GEScmAPIException {
        try (Git git = new Git(repository)) {
            git.stashDrop()
                    .setStashRef(stashRef)
                    .call();
        } catch (GitAPIException e) {
            throw new GEScmAPIException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Apply stash.
     *
     * @param stashRef stash ref
     * @throws GEScmAPIException in case of errors.
     */
    public void applyStash(String stashRef) throws GEScmAPIException {
        try (Git git = new Git(repository)) {
            git.stashApply()
                    .setStashRef(stashRef)
                    .call();
        } catch (GitAPIException e) {
            throw new GEScmAPIException(e.getMessage(), e.getCause());
        }
    }


    private void checkout(Repository clonedRepo, FetchResult result)
            throws MissingObjectException, IncorrectObjectTypeException,
            IOException, GitAPIException {

        String branch = Constants.HEAD;

        Ref head = null;
        if (branch.equals(Constants.HEAD)) {
            Ref foundBranch = findBranchToCheckout(result);
            if (foundBranch != null)
                head = foundBranch;
        }
        if (head == null) {
            head = result.getAdvertisedRef(branch);
            if (head == null)
                head = result.getAdvertisedRef(Constants.R_HEADS + branch);
            if (head == null)
                head = result.getAdvertisedRef(Constants.R_TAGS + branch);
        }

        if (head == null || head.getObjectId() == null)
            return; // TODO throw exception?

        if (head.getName().startsWith(Constants.R_HEADS)) {
            final RefUpdate newHead = clonedRepo.updateRef(Constants.HEAD);
            newHead.disableRefLog();
            newHead.link(head.getName());
            addMergeConfig(clonedRepo, head);
        }

        final RevCommit commit = parseCommit(clonedRepo, head);

        boolean detached = !head.getName().startsWith(Constants.R_HEADS);
        RefUpdate u = clonedRepo.updateRef(Constants.HEAD, detached);
        u.setNewObjectId(commit.getId());
        u.forceUpdate();

        DirCache dc = clonedRepo.lockDirCache();
        DirCacheCheckout co = new DirCacheCheckout(clonedRepo, dc,
                commit.getTree());
        co.checkout();
    }

    private void addMergeConfig(Repository clonedRepo, Ref head)
            throws IOException {
        String branchName = Repository.shortenRefName(head.getName());
        clonedRepo.getConfig().setString(ConfigConstants.CONFIG_BRANCH_SECTION,
                branchName, ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME);
        clonedRepo.getConfig().setString(ConfigConstants.CONFIG_BRANCH_SECTION,
                branchName, ConfigConstants.CONFIG_KEY_MERGE, head.getName());
        String autosetupRebase = clonedRepo.getConfig().getString(
                ConfigConstants.CONFIG_BRANCH_SECTION, null,
                ConfigConstants.CONFIG_KEY_AUTOSETUPREBASE);
        if (ConfigConstants.CONFIG_KEY_ALWAYS.equals(autosetupRebase)
                || ConfigConstants.CONFIG_KEY_REMOTE.equals(autosetupRebase))
            clonedRepo.getConfig().setEnum(
                    ConfigConstants.CONFIG_BRANCH_SECTION, branchName,
                    ConfigConstants.CONFIG_KEY_REBASE, BranchConfig.BranchRebaseMode.REBASE);
        clonedRepo.getConfig().save();
    }

    private RevCommit parseCommit(final Repository clonedRepo, final Ref ref)
            throws IOException {
        final RevCommit commit;
        try (final RevWalk rw = new RevWalk(clonedRepo)) {
            commit = rw.parseCommit(ref.getObjectId());
        }
        return commit;
    }

    private Ref findBranchToCheckout(FetchResult result) {
        final Ref idHEAD = result.getAdvertisedRef(Constants.HEAD);
        ObjectId headId = idHEAD != null ? idHEAD.getObjectId() : null;
        if (headId == null) {
            return null;
        }

        Ref master = result.getAdvertisedRef(Constants.R_HEADS
                + Constants.MASTER);
        ObjectId objectId = master != null ? master.getObjectId() : null;
        if (headId.equals(objectId)) {
            return master;
        }

        Ref foundBranch = null;
        for (final Ref r : result.getAdvertisedRefs()) {
            final String n = r.getName();
            if (!n.startsWith(Constants.R_HEADS))
                continue;
            if (headId.equals(r.getObjectId())) {
                foundBranch = r;
                break;
            }
        }
        return foundBranch;
    }


    /**
     * Push to remote directory.
     * TODO support ssh http://www.codeaffine.com/2014/12/09/jgit-authentication/
     * Additional info  http://stackoverflow.com/questions/13446842/how-do-i-do-git-push-with-jgit
     * git push origin remotepush:r-remotepush
     *
     * @param localBranch    local branch name
     * @param remoteBranch   remote branch name
     * @param userName       optional login name
     * @param password       optional password
     * @param setTrackRemote set track remote
     * @return
     * @throws Exception in case of error
     */
    public RemoteOperationValue remoteRepositoryPush(String localBranch, String remoteBranch,
                                                     String userName, String password,
                                                     boolean setOrigin,
                                                     boolean setTrackRemote,
                                                     final ProgressMonitor progressMonitor) {

        RefSpec refSpec = new RefSpec(localBranch + ":" + remoteBranch);

        RemoteOperationValue val = remoteRepositoryPush(
                userName, password, setOrigin,
                progressMonitor, refSpec);

        if (val.getResult() == RemoteOperationValue.Result.OK) {
            try (Git git = new Git(repository)) {
                if (setTrackRemote /*&& res is ok */) {
                    final StoredConfig config = git.getRepository().getConfig();
                    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranch,
                            ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME);
                    config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranch,
                            ConfigConstants.CONFIG_KEY_MERGE, Constants.R_HEADS + remoteBranch);
                    config.save();
                }

            } catch (IOException e) {
                log.log(Level.WARNING, "Cannot track remote branch", e);

            }

        }


        return val;

    }

    public RemoteOperationValue remoteRepositoryPush(String userName, String password,
                                                     boolean setOrigin,
                                                     ProgressMonitor progressMonitor,
                                                     RefSpec refSpec) {
        try (Git git = new Git(repository)) {

            PushCommand cmd = git.push().setRefSpecs(refSpec).setProgressMonitor(progressMonitor);
            if (setOrigin) {
                cmd.setRemote(Constants.DEFAULT_REMOTE_NAME);
            }
            if (userName != null) {
                cmd.setCredentialsProvider(
                        new UsernamePasswordCredentialsProvider(userName, password)
                );
            }
            Iterable<PushResult> pushResults = cmd.call();
            StringBuilder stringBuilder = new StringBuilder();
            pushResults.forEach(
                    pushResult -> {
                        String rezInfo = "";//todo
                        stringBuilder.append(rezInfo);
                    }
            );
            return new RemoteOperationValue(stringBuilder.toString());
        } catch (TransportException te) {
            if (te.getCause() != null && te.getCause().getCause() != null
                    && te.getCause().getCause().getClass().equals(SSLHandshakeException.class)) {
                try (Git git = new Git(repository)) {
                    log.log(Level.INFO, "Add ssl ignore to " + repository.getDirectory().getAbsolutePath());
                    final StoredConfig config = git.getRepository().getConfig();
                    config.setBoolean("http", null, "sslVerify", false);
                    config.save();
                    return remoteRepositoryPush(
                            userName, password,
                            setOrigin, progressMonitor,
                            refSpec
                    );
                } catch (IOException e) {
                    return processError(e);
                }
            } else {
                return processError(te);
            }
        } catch (Exception e) {
            return processError(e);
        }
    }

    public Ref createLocalBranch(String parent, String name) throws GEScmAPIException {
        try (Git git = new Git(repository)) {
            return git.branchCreate()
                    .setStartPoint(parent)
                    .setName(name)
                    .call();
        } catch (Exception e) {
            throw new GEScmAPIException("Cannot checkout", e);
        }

    }

    public Ref checkoutLocalBranch(final String name, final String newName) throws GECheckoutConflictException, GEScmAPIException {
        try (Git git = new Git(repository)) {
            if (newName == null) {
                return git.checkout()
                        .setCreateBranch(false)
                        .setName(name)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .call();
            } else {
                return git.checkout()
                        .setCreateBranch(true)
                        .setName(newName)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint(name)
                        .call();
            }
        } catch (CheckoutConflictException e) {
            throw new GECheckoutConflictException(e.getMessage(), e.getConflictingPaths());
        } catch (Exception e) {
            throw new GEScmAPIException("Cannot checkout", e);
        }

    }


    public void deleteLocalBranch(final String name) throws GECannotDeleteCurrentBranchException, GEScmAPIException {
        try (Git git = new Git(repository)) {
            git.branchDelete()
                    .setBranchNames(name)
                    .setForce(true)
                    .call();
        } catch (CannotDeleteCurrentBranchException e) {
            throw new GECannotDeleteCurrentBranchException("Cannot delete current branch", e);
        } catch (Exception e) {
            throw new GEScmAPIException("Cannot delete branch ", e);
        }

    }

    /**
     * Merge two branches. The "to" must be checkouted.
     *
     * @param from    branch name
     * @param message merge message
     * @throws Exception
     */
    public MergeResult mergeLocalBranch(final String from, final String message) throws Exception {
        try (Git git = new Git(repository)) {
            return git.merge()
                    .include(repository.exactRef(from))
                    .setMessage(message)
                    .call();
        }
    }


    public String getRepositoryAbsolutePath() {
        return repository.getDirectory().getAbsolutePath();
    }

    public void checkoutFile(final String fileName, Stage stage) {

        try (Git git = new Git(repository)) {
            try {
                CheckoutCommand cmd = git.checkout().addPath(fileName);
                if (stage != null) {
                    cmd.setStage(adaptStage(stage));
                }
                cmd.call();
            } catch (JGitInternalException | GitAPIException e) {
                log.log(Level.WARNING, "Cannot checkout file " + fileName, e);
                GitemberApp.showResult(e.getMessage(), Alert.AlertType.ERROR);
            }
        }


    }

    private CheckoutCommand.Stage adaptStage(Stage stage) {
        if (stage == Stage.OURS) {
            return CheckoutCommand.Stage.OURS;
        } else if (stage == Stage.THEIRS) {
            return CheckoutCommand.Stage.THEIRS;
        }
        return CheckoutCommand.Stage.BASE;
    }

    public void checkoutFile(final String fileName) {
        checkoutFile(fileName, null);
    }

    public RemoteOperationValue compressDatabase(ProgressMonitor defaultProgressMonitor) {

        try (Git git = new Git(repository)) {
            try {
                git.gc().setProgressMonitor(defaultProgressMonitor).call();
                return new RemoteOperationValue("Garbage was removed");
            } catch (GitAPIException e) {

                log.log(Level.SEVERE, "Cannot clean up db", e);
                return new RemoteOperationValue(RemoteOperationValue.Result.ERROR, "Error during cleanup. " + e.getMessage());
            }
        }

    }


    public static void deleteOnExit(File file) {
        tempFiles.add(file);
        log.log(Level.FINE, "File " + file.getAbsolutePath() + " will be deleted on exit");
    }

    public static void cleanUpTempFiles() {
        for (File file : tempFiles) {
            if (file.exists()) {
                if (file.delete()) {
                    log.log(Level.FINE, "File " + file.getAbsolutePath() + " was deleted");
                } else {
                    log.log(Level.FINE, "File " + file.getAbsolutePath() + " was not deleted");
                }
            }
        }
    }

}
