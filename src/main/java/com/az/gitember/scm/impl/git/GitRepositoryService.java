package com.az.gitember.scm.impl.git;

import com.az.gitember.misc.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 * TODO extract inteface
 */
public class GitRepositoryService {

    private final String gitFolder;

    private final Repository repository;

    private final StoredConfig config;

    /**
     * Construct service, which work with git
     *
     * @param gitFolder folder with git repository, for example "~/project/.git"
     * @throws IOException
     */
    public GitRepositoryService(final String gitFolder) throws IOException {
        this.gitFolder = gitFolder;
        this.repository = GitUtil.openRepository(gitFolder);
        this.config = repository.getConfig();
    }

    public GitRepositoryService() {
        gitFolder = null;
        repository = null;
        config = null;
    }

    public void setUserName(String userName) throws IOException {
        config.setString("user", null, "name", userName);
        config.save();
    }

    public void setUserEmail(String userEmail) throws IOException {
        config.setString("user", null, "email", userEmail);
        config.save();
    }

    public String getUserName() {
        return config.getString("user", null, "name");
    }

    public String getUserEmail() {
        return  config.getString("user", null, "email");
    }

    public String getRemoteUrl() {
        return config.getString("remote", "origin", "url");
    }


    private List<ScmBranch> getBranches(final ListBranchCommand.ListMode listMode,
                                        final String prefix) throws Exception {
        try (Git git = new Git(repository)) {
            List<Ref> branchLst = git.branchList().setListMode(listMode).call();
            return branchLst
                    .stream()
                    .filter(r -> r.getName().startsWith(prefix))
                    .map(r -> new ScmBranch(
                            r.getName().substring(prefix.length()),
                            r.getName()))
                    .sorted((o1, o2) -> o1.getShortName().compareTo(o2.getShortName()))
                    .collect(Collectors.toList());
        }
    }

    public List<ScmBranch> getTags() throws Exception {
        try (Git git = new Git(repository)) {
            List<Ref> branchLst = git.tagList().call();
            return branchLst
                    .stream()
                    .map(Ref::getName)
                    .sorted()
                    .map(r -> new ScmBranch(r, null))
                    .collect(Collectors.toList());
        }
    }


    public List<ScmBranch> getLocalBranches() throws Exception {
        final Ref head = repository.exactRef(GitConst.HEAD);
        final List<ScmBranch> scmItems = getBranches(ListBranchCommand.ListMode.ALL, GitConst.HEAD_PREFIX);
        scmItems.forEach(
                i -> i.setHead(head.getTarget().getName().equals(i.getFullName()))
        );
        return scmItems;
    }

    public List<ScmBranch> getRemoteBranches() throws Exception {
        return getBranches(ListBranchCommand.ListMode.REMOTE, GitConst.REMOTE_PREFIX);
    }


    //all files in revision
    /*RevTree tree = revCommit.getTree();
    try (TreeWalk treeWalk = new TreeWalk(repository)) {
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while (treeWalk.next()) {
            System.out.println(">>> found: " + treeWalk.getPathString());
        }

    }*/

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
     * @param revCommit
     * @param fileName  optional file filter
     * @return instance of {@link ScmRevisionInformation}
     */
    public ScmRevisionInformation adapt(final RevCommit revCommit, final String fileName) throws IOException {
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
     * @param revCommit
     * @param filePath  optional value to filter list of changed files
     * @return list of files in given revision
     * @throws IOException
     */
    private ArrayList<ScmItem> getScmItems(RevCommit revCommit, String filePath) throws IOException {
        try (RevWalk rw = new RevWalk(repository)) {
            ArrayList<ScmItem> rez = new ArrayList<>();
            if (revCommit != null) {
                final DiffFormatter df = getDiffFormatter(filePath);
                final List<DiffEntry> diffs = df.scan(
                        revCommit.getParentCount() > 0 ? rw.parseCommit(revCommit.getParent(0).getId()).getTree() : null,
                        revCommit.getTree());
                diffs.stream()
                        .map(this::adaptDiffEntry)
                        .collect(Collectors.toCollection(() -> rez));
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
    public List<ScmItem> getStatuses() throws Exception {

        Map<String, List<String>> statusMap = new HashMap<>();
        try (Git git = new Git(repository)) {
            Status status = git.status().call();

            status.getConflicting().forEach(item -> enrichStatus(statusMap, item, ScmItemStatus.CONFLICT));
            status.getAdded().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.ADDED));
            status.getAdded().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.CHANGED));
            status.getMissing().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.MISSED));
            status.getModified().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.MODIFIED));
            status.getRemoved().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.REMOVED));
            status.getUncommittedChanges().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.UNCOMMITED));
            status.getUntracked().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.UNTRACKED));
            status.getUntrackedFolders().forEach(s -> enrichStatus(statusMap, s, ScmItemStatus.UNTRACKED_FOLDER));

            //TODO
            /* Map<String, StageState> conflictingStageState = status.getConflictingStageState();
                for(Map.Entry<String, StageState> entry : conflictingStageState.entrySet()) {
                    System.out.println("ConflictingState: " + entry);
                }*/


        }


        List<ScmItem> scmItems = new ArrayList<>(statusMap.size());
        statusMap.forEach((s, strings) -> {
            scmItems.add(
                    new ScmItem(s, new ScmItemAttribute(strings))
            );
        } );


        scmItems.sort((o1, o2) -> o1.getShortName().compareTo(o2.getShortName()));

        return scmItems;

    }

    private void enrichStatus(Map<String, List<String>> rez, String item, String itemStatus) {
        List<String> list = rez.getOrDefault(item, new ArrayList<>());
        list.add(itemStatus);
        rez.put(item, list);
    }


    /**
     * Add file to stage before commit.
     * @param fileName
     * @throws Exception
     */
    public void addFileToCommitStage(String fileName) throws Exception {
        try (Git git = new Git(repository)) {
            git.add().addFilepattern(fileName).call();
        }
    }

    public void commit(String message) throws Exception {
        try (Git git = new Git(repository)) {
            git
                    .commit()
                    .setMessage(message)
                    .setAuthor("Igor Azarny", "iazarny@yahoo.com")
                    .call();
        }
    }

    public void removeMissedFile(String fileName) throws Exception {
        try (Git git = new Git(repository)) {
            git.rm().addFilepattern(fileName).call();
        }

    }

    //TODO
    public void removeFileFromCommitStage(String fileName) throws Exception {

        try (Git git = new Git(repository)) {
            git.reset().addPath(fileName);
        }

    }

    /**
     * Create new git repository.
     * @param absPath path to repository.
     */
    public void createRepository(final String absPath) throws Exception {
        String readmeInitialContent = "# " + (new File(absPath)).getName();
        try (Git git = Git.init()
                .setDirectory(new File(absPath))
                .call()) {
            Files.write(
                    Paths.get(absPath + File.separator + "README.md"),
                    readmeInitialContent.getBytes(),
                    StandardOpenOption.CREATE);
            Files.write(
                    Paths.get(absPath + File.separator + ".gitignore"),
                    readmeInitialContent.getBytes(),
                    StandardOpenOption.CREATE);
        }

    }


    public String  remoteRepositoryPull() throws Exception {

        try(Git git = new Git(repository)) {
            PullResult pullRez =  git.pull().call();
            return pullRez.toString();
        } catch (CheckoutConflictException conflictException) {
            return conflictException.getMessage();
        }

    }

    public String  remoteRepositoryFetch() throws Exception {

        try(Git git = new Git(repository)) {
            FetchResult pullRez =  git.fetch().call();
            return pullRez.getMessages();
        } catch (CheckoutConflictException conflictException) {
            return conflictException.getMessage();
        }

    }


    public void stash() throws Exception {

        try(Git git = new Git(repository)) {
            git.stashCreate().call();
        }

    }

    public String remoteRepositoryPush()  throws Exception {
        try(Git git = new Git(repository)) {
            RefSpec refSpec = new RefSpec("refs/heads/master:refs/remotes/origin/master"); //TODO not only master
            Iterable<PushResult> pushResults =  git.push().setRefSpecs(refSpec).call();
            StringBuilder stringBuilder = new StringBuilder();
            pushResults.forEach(
                    pushResult -> {
                        String rezInfo = "";
                        stringBuilder.append(  rezInfo  )  ;
                    }
            );

            return stringBuilder.toString();
        } catch (Exception e) {
            return e.getMessage();
        }

    }
}
