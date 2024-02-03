package com.az.gitember.service;

import com.az.gitember.data.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.errors.ConfigInvalidException;
import org.eclipse.jgit.lfs.CleanFilter;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.SmudgeFilter;
import org.eclipse.jgit.lfs.lib.LfsPointerFilter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revplot.PlotWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.transport.sshd.IdentityPasswordProvider;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.SystemReader;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GitRepoService {

    public static final String SMUDGE_NAME = org.eclipse.jgit.lib.Constants.BUILTIN_FILTER_PREFIX
            + org.eclipse.jgit.lfs.lib.Constants.ATTR_FILTER_DRIVER_PREFIX
            + org.eclipse.jgit.lib.Constants.ATTR_FILTER_TYPE_SMUDGE;

    public static final String CLEAN_NAME = org.eclipse.jgit.lib.Constants.BUILTIN_FILTER_PREFIX
            + org.eclipse.jgit.lfs.lib.Constants.ATTR_FILTER_DRIVER_PREFIX
            + org.eclipse.jgit.lib.Constants.ATTR_FILTER_TYPE_CLEAN;


    private final static Logger log = Logger.getLogger(GitRepoService.class.getName());

    private final static List<File> tempFiles = new ArrayList<>();

    private final Repository repository;

    private final BranchLiveTimeAdapter branchLiveTimeAdapter = new BranchLiveTimeAdapter();

    static {
        FilterCommandRegistry.register(GitRepoService.SMUDGE_NAME, SmudgeFilter.FACTORY);
        FilterCommandRegistry.register(GitRepoService.CLEAN_NAME, CleanFilter.FACTORY);
    }


    /**
     * Create new git repository.
     *
     * @param absPath path to repository.
     */
    public static Repository createRepository(final String absPath,
                                              final boolean withReadme,
                                              final boolean withGitIgnore,
                                              final boolean withLfs) throws Exception {
        try (final Git git = Git.init()
                .setDirectory(new File(absPath))
                .call()) {

            if (withReadme) {
                final String readmeInitialContent = String.format(
                        "# Project %s \n\n Created at %s \n folder %s",
                        (new File(absPath)).getName(), GitemberUtil.formatDate(new Date()), absPath);
                final Path readmePath = Paths.get(absPath + File.separator + Const.GIT_README_NAME);
                Files.write(
                        readmePath,
                        readmeInitialContent.getBytes(), StandardOpenOption.CREATE);
                git.add().addFilepattern(Const.GIT_README_NAME).call();
            }

            if (withGitIgnore) {
                final Path ignorePath = Paths.get(absPath + File.separator + Const.GIT_IGNORE_NAME);
                Files.write(ignorePath,
                        gitIgnoreContent.getBytes(), StandardOpenOption.CREATE);
                git.add().addFilepattern(Const.GIT_IGNORE_NAME).call();
            }

            if (withLfs) {
                addLFSSupport(git);
            }

            git.commit().setMessage("Init").call();
            log.log(Level.INFO, "Created new repository {0}", absPath);
            return git.getRepository();

        }

    }

    public static void addLFSSupport(Repository repository) throws IOException, GitAPIException {
        try (Git git = new Git(repository)) {
            addLFSSupport(git);
        }
    }

    public static void addLFSSupport(Git git) throws IOException, GitAPIException {
        StoredConfig config = git.getRepository().getConfig();
        config.setString("filter", "lfs", "clean", CLEAN_NAME);
        config.setString("filter", "lfs", "smudge", SMUDGE_NAME);
        config.save();

        final String gitFolder = git.getRepository().getDirectory().getAbsolutePath();
        final String absPath = gitFolder.replace(Const.GIT_FOLDER, "");
        final String lfsTmpPath = gitFolder + File.separator + "lfs" + File.separator + "tmp";

        final Path attrPath = Paths.get(absPath + File.separator + Const.GIT_ATTR_NAME);
        Files.write(attrPath,
                gitAttributesContent.getBytes(), StandardOpenOption.CREATE);
        git.add().addFilepattern(Const.GIT_ATTR_NAME).call();
        new File(lfsTmpPath).mkdirs();
    }

    public void unlink(final String fileName) {
        try (Git git = new Git(repository)) {
            final String gitFolder = git.getRepository().getDirectory().getAbsolutePath();
            final String absPath = gitFolder.replace(Const.GIT_FOLDER, "");
            final Path attrPath = Paths.get(absPath, fileName);
            Files.delete(attrPath);
        } catch (IOException e) {
            log.log(Level.SEVERE, "Cannot unlink ", e);
        }

    }


    public static void createRepository(final String absPath) throws Exception {
        createRepository(absPath, false, false);
    }

    /**
     * Create new git repository.
     *
     * @param absPath path to repository.
     */
    public static void createRepository(final String absPath,
                                        final boolean withReadme,
                                        final boolean withGitIgnore) throws Exception {
        createRepository(absPath, withReadme, withGitIgnore, false);

    }


    /**
     * Construct service, which work with git. Each service designated to work with the one repo.
     *
     * @param gitFolder folder with git repository, for example "~/project/.git"
     * @throws IOException in case of error
     */
    public GitRepoService(final String gitFolder) throws IOException {
        this.repository = new FileRepositoryBuilder()
                .readEnvironment() // scan environment GIT_* variables
                .setGitDir(new File(gitFolder))
                .findGitDir()
                .build();

    }

    public GitRepoService(final Repository repo) {
        this.repository = repo;
    }

    public GitRepoService() {
        super();
        repository = null;
    }

    public void shutdown() {
        if (repository != null) {
            repository.close();
        }
        cleanUpTempFiles();
    }


    /**
     * Add file to stage before commit.
     *
     * @param fileName given name
     * @throws GitAPIException in case of error
     */
    public DirCache addFileToCommitStage(final String fileName) throws GitAPIException {
        try (Git git = new Git(repository)) {
            return git.add().addFilepattern(fileName).call();
        } catch (GitAPIException e) {
            log.log(Level.WARNING, "Cannot add file to stage", e);
            throw e;
        }
    }

    /**
     * Git reset HEAD ...
     *
     * @param fileName fiven file name
     * @throws GitAPIException in case of error
     */
    public Ref removeFileFromCommitStage(final String fileName) throws GitAPIException {
        try (Git git = new Git(repository)) {
            return git.reset().addPath(fileName).call();
        } catch (GitAPIException e) {
            log.log(Level.WARNING, "Cannot unstage ", e);
            throw e;
        }
    }


    /**
     * Rename file. Used for undo original renaming, which is happens outside of gitember
     *
     * @param fileNameOld old file name
     * @param fileNameNew new file name
     * @throws Exception in case of error
     */
    public void renameFile(final String fileNameOld, final String fileNameNew) throws Exception {
        try (Git git = new Git(repository)) {
            String workignDir = repository.getDirectory().getAbsolutePath().replace(".git", "");
            Files.move(Path.of(workignDir, fileNameOld), Path.of(workignDir, fileNameNew));
            git.add().addFilepattern(fileNameNew).call();
            git.rm().addFilepattern(fileNameOld).call();
        } catch (IOException e) {
            log.log(Level.WARNING, "Cannot rename  file", e);
            throw e;
        }
    }

    /**
     * Commit changes.
     *
     * @param message commit message
     * @return result.
     * @throws GitAPIException in case of error
     */
    public RevCommit commit(final String message, final String name, final String email) throws GitAPIException {
        try (Git git = new Git(repository)) {
            CommitCommand cmd = git.commit();
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(email)) {
                cmd.setAuthor(name, email);
            }
            return cmd.setMessage(message).call();
        } catch (GitAPIException e) {
            log.log(Level.SEVERE, "Cannot commit", e);
            throw e;
        }
    }


    /**
     * Get all files in head
     *
     * @return all files in head
     * @throws IOException in case of error
     */
    public Set<String> getAllFiles() throws IOException {
        return getAllFiles(Constants.HEAD);
    }

    /**
     * Get all files in given name
     *
     * @return all files in head
     * @throws IOException in case of error
     */
    public Set<String> getAllFiles(String name) throws IOException {
        final Set<String> rez = new TreeSet<>();

        final Ref head = repository.exactRef(name);
        final RevWalk walk = new RevWalk(repository);
        final RevCommit commit = walk.parseCommit(head.getObjectId());
        final RevTree tree = commit.getTree();
        final TreeWalk treeWalk = new TreeWalk(repository);

        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);

        while (treeWalk.next()) {
            if (treeWalk.isSubtree()) {
                treeWalk.enterSubtree();
            } else {
                final String path = treeWalk.getPathString();
                rez.add(path);
            }
        }
        return rez;
    }

    /**
     * Create new branch.
     *
     * @param parent from
     * @param name   new name
     * @return ref to new branch
     * @throws IOException in case of error
     */
    public Ref createBranch(String parent, String name) throws IOException {
        try (Git git = new Git(repository)) {
            return git.branchCreate()
                    .setStartPoint(parent)
                    .setName(name)
                    .call();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot create branch " + name, e);
            throw new IOException("Cannot checkout", e);
        }

    }

    /**
     * Checkout branch.
     *
     * @param name branch name.
     * @return ref to branch
     * @throws IOException in case of error
     */
    public Ref checkoutBranch(final String name, final ProgressMonitor defaultProgressMonitor) throws IOException {
        return checkoutBranch(name, null, defaultProgressMonitor);
    }

    /**
     * Create <code>newName</code> branch from <code>name</code> and checkout it.
     *
     * @param name    given parent name
     * @param newName given new branch name
     * @return ref to branch
     * @throws IOException in case of error
     */
    public Ref checkoutBranch(final String name, final String newName, final ProgressMonitor defaultProgressMonitor) throws IOException {
        try (Git git = new Git(repository)) {
            if (newName == null) { //existing
                return git.checkout()
                        .setCreateBranch(false)
                        .setName(name)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .setProgressMonitor(defaultProgressMonitor)
                        .call();
            } else {
                return git.checkout()
                        .setCreateBranch(true)
                        .setName(newName)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint(name)
                        .setProgressMonitor(defaultProgressMonitor)
                        .call();
            }
        } catch (Exception e) {
            throw new IOException("Cannot checkout " + name + " " + newName, e);
        }

    }

    /**
     * Create difference between branches
     *
     * @param leftBranchName         left branch name
     * @param rihtBranchName         right branch name
     * @param defaultProgressMonitor optional monitor
     * @return list of DiffEntry
     * @throws IOException in case of errors
     */
    public List<DiffEntry> branchDiff(final String leftBranchName, final String rihtBranchName,
                                      final ProgressMonitor defaultProgressMonitor) throws IOException {
        AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, leftBranchName);
        AbstractTreeIterator newTreeParser = prepareTreeParser(repository, rihtBranchName);
        try (Git git = new Git(repository)) {
            try {
                return git.diff()
                        .setProgressMonitor(defaultProgressMonitor)
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .call();
            } catch (GitAPIException e) {
                throw new IOException("Cannot creat difference between " + leftBranchName + " and " + rihtBranchName, e);
            }
        }
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws IOException {
        // from the commit we can build the tree which allows us to construct the TreeParser
        Ref head = repository.exactRef(ref);
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(head.getObjectId());
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }

    public Ref checkoutRevCommit(final RevCommit revCommit, final String newBranchName,
                                 final ProgressMonitor defaultProgressMonitor) throws IOException {
        try (Git git = new Git(repository)) {
            if (StringUtils.isBlank(newBranchName)) {
                return git.checkout()
                        .setName(revCommit.getName())
                        .setProgressMonitor(defaultProgressMonitor)
                        .call();
            } else {
                return git.checkout()
                        .setName(newBranchName)
                        .setCreateBranch(true)
                        .setStartPoint(revCommit)
                        .setProgressMonitor(defaultProgressMonitor)
                        .call();
            }

        } catch (Exception e) {
            throw new IOException("Cannot checkout " + revCommit, e);
        }
    }

    public Ref checkoutRevCommit(final RevCommit revCommit, final ProgressMonitor defaultProgressMonitor) throws IOException {
        return checkoutRevCommit(revCommit, null, defaultProgressMonitor);
    }


    /**
     * Delete local branch
     *
     * @param name branch name
     * @throws IOException c
     */
    public void deleteLocalBranch(final String name) throws IOException {
        try (Git git = new Git(repository)) {
            git.branchDelete()
                    .setBranchNames(name)
                    .setForce(true)
                    .call();
        } catch (Exception e) {
            throw new IOException("Cannot delete branch " + name, e);
        }

    }

    /**
     * Merge two branches. The "to" must be checkouted.
     *
     * @param from    branch name
     * @param message merge message
     * @throws Exception * @throws IOException in case of error
     */
    public MergeResult mergeBranch(final String from, final String message) throws Exception {
        try (Git git = new Git(repository)) {
            return git.merge()
                    .include(repository.exactRef(from))
                    .setMessage(message)
                    .call();
        }
    }

    /**
     * @param upstream the name of the upstream branch
     * @return rebase result
     * @throws Exception * @throws IOException in case of error
     */
    public RebaseResult rebaseBranch(final String upstream) throws Exception {
        try (Git git = new Git(repository)) {
            return git.rebase()
                    .setUpstream(upstream)
                    .setPreserveMerges(true)
                    .call();
        }
    }

    /**
     * Get list of branches.
     *
     * @return list of local branches.
     */
    public List<ScmBranch> getBranches() throws Exception {
        return getBranches(ListBranchCommand.ListMode.ALL,
                Constants.R_HEADS, ScmBranch.BranchType.LOCAL);
    }

    /**
     * Get list of branches.
     *
     * @return list of local branches.
     */
    public List<ScmBranch> getRemoteBranches() throws Exception {

        return getBranches(ListBranchCommand.ListMode.REMOTE,
                Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + '/', ScmBranch.BranchType.REMOTE);
    }


    private List<ScmBranch> getBranches(final ListBranchCommand.ListMode listMode,
                                        final String prefix,
                                        final ScmBranch.BranchType branchType) throws Exception {

        try (Git git = new Git(repository)) {

            CommitInfo head = this.getHead();

            List<Ref> branchLst = git.branchList().setListMode(listMode).call();

            List<ScmBranch> rez = branchLst
                    .stream()
                    .filter(r -> r.getName().startsWith(prefix) || ("HEAD".equals(r.getName()) && Constants.R_HEADS.equals(prefix)))
                    .map(r -> getScmBranch(prefix, branchType, r))
                    .map(i -> {
                        i.setHead(i.getFullName().equals(head.getName()));
                        return i;
                    })
                    .sorted(Comparator.comparing(ScmBranch::getShortName))
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

    private ScmBranch getScmBranch(String prefix, ScmBranch.BranchType branchType, Ref r) {
        int aheadCount = 0;
        int behindCount = 0;

        try {
            BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(repository, r.getName());
            if (trackingStatus != null) {
                aheadCount = trackingStatus.getAheadCount();
                behindCount = trackingStatus.getBehindCount();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ScmBranch(
                "HEAD".equals(r.getName()) ? r.getName() : r.getName().substring(prefix.length()),
                r.getName(),
                branchType,
                r.getObjectId().getName(),
                aheadCount,
                behindCount
        );
    }

    private void checkIsTrackingRemoteBranch(Config cfg, ScmBranch item) {
        String remote = cfg.getString("branch", item.getShortName(), ConfigConstants.CONFIG_KEY_REMOTE);
        if (remote != null) {
            String mergeRef = cfg.getString("branch", item.getShortName(), "merge");
            String shortRemoteName = mergeRef.substring(Constants.R_HEADS.length());
            item.setRemoteName(shortRemoteName);
            item.setRemoteMergeName(mergeRef);
        }
    }


    /**
     * Get list of revision, where given file was changed
     *
     * @param fileName given fileName
     * @return list of revisions where file was changed.
     * @throws Exception
     */
    public List<ScmRevisionInformation> getFileHistory(final String fileName, final String treeName) throws Exception {
        final ArrayList<ScmRevisionInformation> rez = new ArrayList<>();
        try (Git git = new Git(repository)) {
            final LogCommand cmd = git.log()
                    .setRevFilter(RevFilter.ALL)
                    .addPath(fileName);
            if (treeName != null) {
                cmd.add(repository.resolve(treeName));
            }
            final Iterable<RevCommit> revCommits = cmd.call();
            for (RevCommit revCommit : revCommits) {
                final ScmRevisionInformation info = adapt(revCommit, fileName);
                rez.add(info);
                if (rez.size() >= 100) {
                    break;
                }
            }
        }
        return rez;
    }

    /**
     * Get list of revision, where given file was changed
     *
     * @param fileName given fileName
     * @return list of revisions where file was changed.
     * @throws Exception in case of errors
     */
    public List<ScmRevisionInformation> getFileHistory(final String fileName) throws Exception {

        return getFileHistory(fileName, null);

    }

    public Map<String, Set<String>> search(List<PlotCommit> commits, String term, boolean luceneIndexed) {
        Map<String, Set<String>> searchResultMap = new ConcurrentHashMap<>();
        String searchString = term.toLowerCase();

        Thread threadSearch1 = new Thread(() -> {
            commits.forEach(
                    plotCommit -> {

                        final List<ScmItem> affectedItems = adapt(plotCommit).getAffectedItems();

                        if (
                                plotCommit.getShortMessage().toLowerCase().contains(searchString)
                                        || plotCommit.getFullMessage().toLowerCase().contains(searchString)
                                        || plotCommit.getName().toLowerCase().contains(searchString)
                                        || prersonIndentContains(plotCommit.getCommitterIdent(), searchString)
                                        || prersonIndentContains(plotCommit.getAuthorIdent(), searchString)
                                        || affectedItems.stream().anyMatch(scmItem -> scmItem.getShortName().toLowerCase().contains(searchString))
                        ) {

                            Set<String> affectedFiles = searchResultMap.computeIfAbsent(plotCommit.getName(), s -> new HashSet<String>());

                            affectedItems.stream().forEach(
                                    item -> {
                                        if (item.getShortName().toLowerCase().contains(searchString)) {
                                            affectedFiles.add(item.getShortName());
                                        }
                                    }
                            );
                        }
                    }
            );

        });

        Thread threadSearch2 = new Thread(() -> {
            if (luceneIndexed) {
                SearchService service = getSearchService();
                try {
                    Map<String, Set<String>> lucineMap = service.search(term);
                    lucineMap.keySet().forEach(key -> {
                        Set<String> affectedFiles = searchResultMap.computeIfAbsent(key, s -> new HashSet<>());
                        affectedFiles.addAll(lucineMap.get(key));
                        lucineMap.get(key).clear();
                    });
                } catch (RuntimeException ex) {
                    log.log(Level.WARNING, "Cannot perform lucene search operation ", ex);
                    log.log(Level.WARNING, "Will try to drop index and disable search using lucine");

                    getSearchService().dropIndex();
                    Context.getCurrentProject().setIndexed(false);
                    Context.saveSettings();
                }

            }
        });

        threadSearch2.start();
        threadSearch1.start();

        try {
            threadSearch2.join();
            threadSearch1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return searchResultMap;
    }

    SearchService service = null;

    private synchronized SearchService getSearchService() {
        if (service == null) {
            service = new SearchService(Context.getProjectFolder());
        }

        return service;
    }


    private boolean prersonIndentContains(PersonIdent prersonIndent, String searchString) {
        return prersonIndent != null
                && (prersonIndent.getEmailAddress() != null
                && prersonIndent.getEmailAddress().toLowerCase().contains(searchString.toLowerCase()));

    }


    /**
     * Gt list of tags.
     *
     * @return
     */
    public List<ScmBranch> getTags() {
        try (Git git = new Git(repository)) {
            List<Ref> branchLst = git.tagList().call();
            return branchLst
                    .stream()
                    .map(r -> new ScmBranch(r.getName(), r.getName(), ScmBranch.BranchType.TAG, r.getObjectId().getName()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get tags", e);
        }
        return Collections.emptyList();
    }

    /**
     * Create new tag
     *
     * @param tagName tag name
     */
    public Ref createTag(String tagName) throws IOException {

        try (Git git = new Git(repository)) {
            return git.tag()
                    .setName(tagName)
                    .setForceUpdate(true)
                    .setAnnotated(true)
                    .call();
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot create tag " + tagName, e);
            throw new IOException(e.getMessage());
        }
    }

    public CommitInfo getHead() throws Exception {
        final Ref head = repository.exactRef(Constants.HEAD);
        return new CommitInfo(
                head.getTarget().getName(),
                head.getObjectId() == null ? null : head.getObjectId().getName()
        );
    }

    /**
     * Create stash.
     *
     * @throws IOException in case of error.
     */
    public void stash() throws IOException {
        try (Git git = new Git(repository)) {
            git.stashCreate().call();
        } catch (GitAPIException e) {
            log.log(Level.SEVERE, "Cannot stash", e);
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Get stash list.
     *
     * @return list of stashed changes
     */
    public List<ScmRevisionInformation> getStashList() {

        try (Git git = new Git(repository)) {
            List<ScmRevisionInformation> rez = git.stashList()
                    .call()
                    .stream()
                    .map(revCommit -> adapt(revCommit, null))
                    .collect(Collectors.toList());
            for (int i = 0; i < rez.size(); i++) {
                rez.get(i).setStashIndex(i);
            }
            return rez;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get stash list", e);
        }


        return Collections.emptyList();
    }

    /**
     * Delete stash.
     *
     * @param stashRef stash reference id.
     * @throws IOException in case of errors.
     */
    public void deleteStash(int stashRef) throws IOException {
        try (Git git = new Git(repository)) {
            git.stashDrop()
                    .setStashRef(stashRef)
                    .call();
        } catch (GitAPIException e) {
            log.log(Level.SEVERE, "Cannot delete stash " + stashRef, e);
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Apply stash.
     *
     * @param stashRef stash ref
     * @throws IOException in case of errors.
     */
    public void applyStash(String stashRef) throws IOException {
        try (Git git = new Git(repository)) {
            git.stashApply()
                    .setStashRef(stashRef)
                    .call();
        } catch (GitAPIException e) {
            log.log(Level.SEVERE, "Cannot spply stash " + stashRef, e);
            throw new IOException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Revert changes or reolve conflict
     *
     * @param fileName file name
     * @param stage    stage
     */
    public void checkoutFile(final String fileName, CheckoutCommand.Stage stage) throws IOException {

        try (Git git = new Git(repository)) {
            try {
                CheckoutCommand cmd = git.checkout().addPath(fileName);
                if (stage != null) {
                    cmd.setStage(stage);
                }
                cmd.call();
            } catch (Exception e) {
                log.log(Level.WARNING, "Cannot checkout file " + fileName, e);
                throw new IOException("Cannot checkout " + fileName, e);
            }
        }
    }

    public CherryPickResult cherryPick(RevCommit revCommit) throws IOException {
        try (Git git = new Git(repository)) {
            try {
                CherryPickCommand cherryPickCommand = git.cherryPick()
                        .include(revCommit)
                        .setNoCommit(true)
                        .setStrategy(MergeStrategy.RECURSIVE);
                return cherryPickCommand.call();
            } catch (Exception e) {
                log.log(Level.WARNING, "Cannot cherry pick   " + revCommit, e);
                throw new IOException("Cannot cherry pick " + revCommit, e);
            }
        }

    }


    public String creaeEmptyFile(String fileName) throws IOException {
        final String fileNameExtension = FilenameUtils.getExtension(fileName);
        final File temp = File.createTempFile(
                Const.TEMP_FILE_PREFIX,
                fileNameExtension.isEmpty() ? fileNameExtension : "." + fileNameExtension);
        return temp.getAbsolutePath();
    }


    public boolean isLfsRepo() { //TODO make lazy
        return isLfsRepo(repository);
    }

    boolean isLfsRepo(Repository repo) {
        return Files.exists(Paths.get(repo.getDirectory().getAbsolutePath(), Const.GIT_LFS_FOLDER));
    }

    public boolean isFileExists(String fileName) {
        return Files.exists(Paths.get(repository.getDirectory().getAbsolutePath().replace(Const.GIT_FOLDER, ""), fileName));
    }


    /**
     * Save given fileName at given revision into given file name.
     *
     * @param revisionName revision name sha
     * @param fileName     file name
     * @return absolute path to saved file
     */
    public String saveFile(String revisionName, String fileName) throws IOException {

        final ObjectId lastCommitId = repository.resolve(revisionName);
        final String fileNameExtension = FilenameUtils.getExtension(fileName);
        final File temp = File.createTempFile(
                Const.TEMP_FILE_PREFIX,
                fileNameExtension.isEmpty() ? fileNameExtension : "." + fileNameExtension);
        deleteOnExit(temp);

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
     * Save given fileName at tree/revision into output stream.
     *
     * @param revisionName revision name
     * @param fileName     file name in repository
     * @return absolute path to saved diff file
     */
    public String getRawDiff(String revisionName,
                             String fileName) throws IOException {

        String str;

        try (Git git = new Git(repository);) {

            final ObjectId objId = repository.resolve(revisionName);
            final RevCommit root = new PlotWalk(repository).parseCommit(objId);
            final RevCommit parent = root.getParent(0); //TODO is this correct parent ?


            OutputStream out = new ByteArrayOutputStream();
            DiffCommand diff = git.diff()
                    .setPathFilter(PathFilter.create(fileName))
                    .setOutputStream(out)
                    .setSourcePrefix("old:")
                    .setDestinationPrefix("new:")
                    .setNewTree(getTreeIterator(repository, revisionName))
                    .setOldTree(getTreeIterator(repository, parent.name()));
            diff.call();
            str = out.toString();
        } catch (GitAPIException e) {
            throw new IOException(e);
        }
        return str;
    }

    private AbstractTreeIterator getTreeIterator(Repository repo, String name) throws IOException {
        final ObjectId id = repo.resolve(name);
        final CanonicalTreeParser p = new CanonicalTreeParser();

        if (id == null) {
            throw new IllegalArgumentException(name);
        }

        try (ObjectReader or = repo.newObjectReader()) {
            p.reset(or, new RevWalk(repo).parseTree(id));
            return p;
        }
    }

    /**
     * Get branch from given commit name. revCommit.getName()
     *
     * @param commitName or null ifcommit not found.
     */
    public String getBranchName(String commitName) {

        try (Git git = new Git(repository)) {

            List<Ref> branchLst = git.branchList().setContains(commitName).call();
            if (!branchLst.isEmpty()) {
                //TODO not sure is it right
                return branchLst.get(0).getName();
            }

        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        return null;

    }


    public void removeFile(final String fileName) throws Exception {
        try (Git git = new Git(repository)) {
            git.rm().addFilepattern(fileName).call();
        }
    }


    /**
     * @param defaultProgressMonitor optional
     */
    public void compressDatabase(ProgressMonitor defaultProgressMonitor) throws IOException {
        try (Git git = new Git(repository)) {
            try {
                git.gc().setProgressMonitor(defaultProgressMonitor).call();
            } catch (GitAPIException e) {
                log.log(Level.SEVERE, "Cannot clean up db", e);
                throw new IOException(e);
            }
        }
    }

    /**
     * Get statuses
     *
     * @return list of ScmItem
     */
    public List<ScmItem> getStatuses(ProgressMonitor progressMonitor, boolean collectLastChanges) {

        final List<ScmItem> scmItems = new ArrayList<>();
        try (Git git = new Git(repository)) {

            Set<String> filter = new HashSet<>();

            Status status = git.status().setProgressMonitor(progressMonitor).call();

            Ref refHead = repository.exactRef(Constants.HEAD);
            RevWalk revWalk = new RevWalk(repository);
            RevCommit revCommitHead = revWalk.parseCommit(refHead.getObjectId());
            TreeWalk tw = new TreeWalk(repository);
            tw.setRecursive(false);
            tw.addTree(revCommitHead.getTree());
            tw.addTree(new FileTreeIterator(repository));
            RenameDetector rd = new RenameDetector(repository);
            rd.addAll(DiffEntry.scan(tw));
            List<DiffEntry> lde = rd.compute(tw.getObjectReader(), null);
            for (DiffEntry de : lde) {
                if (de.getScore() >= rd.getRenameScore()) {
                    scmItems.add(new ScmItem(de.getNewPath(),
                            new ScmItemAttribute().withStatus(ScmItem.Status.RENAMED).withOldName(de.getOldPath())));
                    filter.add(de.getOldPath());
                    filter.add(de.getNewPath());
                }
            }

            status.getRemoved().forEach(item -> {
                        if (!filter.contains(item)) {
                            scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.Status.REMOVED)));
                        }
                    }
            );
            status.getMissing().forEach(item -> scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.Status.MISSED))));
            status.getAdded().forEach(item -> {
                if (!filter.contains(item)) {
                    scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.Status.ADDED)));
                }
            });
            status.getUntracked().forEach(item -> scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.Status.UNTRACKED))));

            status.getModified().forEach(item -> scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.Status.MODIFIED))));
            status.getChanged().forEach(item -> scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.Status.CHANGED))));
            status.getConflicting().forEach(item -> scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.Status.CONFLICT))));
            //status.getUntrackedFolders().forEach(item -> scmItems.add(new ScmItem(item, new ScmItemAttribute().withStatus(ScmItem.ScmItemStatus.UNTRACKED_FOLDER))));

            if (isLfsRepo()) {
                mergeLfs(scmItems, getLfsFiles(Constants.HEAD));
            }

            if (collectLastChanges) {
                enrichWithLastChangesDetail(git, scmItems);
            }


        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get statuses", e);
        }

        Collections.sort(scmItems);
        return scmItems;
    }

    List<ScmItem> enrichWithLastChangesDetail(final Git git, final List<ScmItem> toEnrich) throws Exception {
        for (ScmItem item : toEnrich) {
            final LogCommand cmd = git.log()
                    .setRevFilter(RevFilter.ALL)
                    .setMaxCount(1)
                    .addPath(item.getShortName());
            final Iterable<RevCommit> lastCommitIter = cmd.call();
            while (lastCommitIter.iterator().hasNext()) {
                final RevCommit revCommit = lastCommitIter.iterator().next();
                if (revCommit != null) {
                    item.withChanges(revCommit);
                    break;
                }
            }

        }
        return toEnrich;

    }


    List<ScmItem> mergeLfs(final List<ScmItem> status, final List<ScmItem> lfs) {
        for (ScmItem scmItem : status) {
            for (ScmItem scmItemLfs : lfs) {
                if (scmItem.getShortName().equals(scmItemLfs.getShortName())) {
                    scmItem.getAttribute().setSubstatus(scmItemLfs.getAttribute().getSubstatus());
                    lfs.remove(scmItemLfs);
                    break;
                }
            }
        }

        status.addAll(lfs);

        return status;
    }

    public List<ScmItem> getLfsFiles(String name) throws IOException {
        final List<ScmItem> rez = new ArrayList<>();

        final Ref head = repository.exactRef(name);
        final RevWalk walk = new RevWalk(repository);
        final RevCommit commit = walk.parseCommit(head.getObjectId());
        final RevTree tree = commit.getTree();
        final TreeWalk treeWalk = new TreeWalk(repository);
        final LfsPointerFilter filter = new LfsPointerFilter();

        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(filter);

        while (treeWalk.next()) {
            if (treeWalk.isSubtree()) {
                treeWalk.enterSubtree();
            } else {
                final String path = treeWalk.getPathString();
                final Path filePath = Paths.get(Context.getProjectFolder(), path);
                final long sizeOnDisk = Files.size(filePath);
                //final LfsPointer pointer = filter.getPointer(); //TODO deleted file
                final String subStatus = (sizeOnDisk > LfsPointer.SIZE_THRESHOLD) ? ScmItem.Status.LFS_FILE : ScmItem.Status.LFS_POINTER;
                final ScmItem scmItem = new ScmItem(
                        path, new ScmItemAttribute().withStatus(ScmItem.Status.LFS).withSubStatus(subStatus)
                );
                rez.add(scmItem);
            }
        }
        return rez;
    }


    public List<ScmRevisionInformation> getItemsToIndex(final String treeName, final int qty, final ProgressMonitor progressMonitor) {
        PlotCommitList<PlotLane> commit = getCommitsByTree(treeName, true, qty, progressMonitor);
        List<ScmRevisionInformation> rez = commit.stream().map(this::adapt).collect(Collectors.toList());
        rez.forEach(sri -> {
            sri.getAffectedItems().removeIf(scmItem -> ScmItem.Status.REMOVED.equalsIgnoreCase(scmItem.getAttribute().getStatus()));
        });
        return rez;
    }

    /**
     * Get revisions to visualize. Fr more detail look at
     * https://stackoverflow.com/questions/12691633/jgit-get-all-commits-plotcommitlist-that-affected-a-file-path
     *
     * @param treeName tree name
     * @param all      to visualize with merges
     * @return PlotCommitList<PlotLane>
     */
    public PlotCommitList<PlotLane> getCommitsByTree(final String treeName, final boolean all, final int qtyRevs, final ProgressMonitor progressMonitor) {


        final PlotCommitList<PlotLane> plotCommitList = new PlotCommitList<>();
        try (PlotWalk revWalk = new PlotWalk(repository)) {

            if (treeName != null) {
                final ObjectId rootId = repository.resolve(treeName);
                final RevCommit root = revWalk.parseCommit(rootId);
                revWalk.markStart(root);
            }

            if (all) {
                revWalk.setTreeFilter(TreeFilter.ALL);

                Collection<Ref> allRefs = repository.getAllRefs().values();
                int idx = 0;
                if (progressMonitor != null) {
                    progressMonitor.beginTask("Collecting revision", allRefs.size());
                }
                for (Ref ref : allRefs) {
                    revWalk.markStart(revWalk.parseCommit(ref.getObjectId()));
                    if (progressMonitor != null) {
                        idx++;
                        progressMonitor.update(idx);
                    }
                }
            }

            plotCommitList.source(revWalk);
            if (qtyRevs == -1) {
                plotCommitList.fillTo(Integer.MAX_VALUE);
            } else {
                plotCommitList.fillTo(qtyRevs);
            }

            revWalk.dispose();

            if (progressMonitor != null) {
                progressMonitor.endTask();
            }

        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot get commit to draw ", e);
        }

        return plotCommitList;
    }


    public List<AverageLiveTime> getMergedBranches(final StatWPParameters params) {

        final String treeNameTarget = params.getBranchName();
        final List<BranchLiveTime> brandLiveTimes = new ArrayList<>();
        final Set<RevCommit> tails = new HashSet<>();
        final PlotCommitList<PlotLane> treeNameHistory = getCommitsByTree(treeNameTarget, false, -1, null);
        final Set<RevCommit> treeOnlyCommits = getRevCommits(treeNameHistory);

        RevCommit plotLane = treeNameHistory.get(0);

        while (plotLane.getParentCount() > 0) {
            if (plotLane.getParentCount() > 1) {
                // merge point
                for (int i = 0; i < plotLane.getParentCount(); i++) {
                    if (!treeOnlyCommits.contains(plotLane.getParent(i))) {
                        // parent not from main tree but from branch
                        RevCommit forkPoint = findParrent(plotLane.getParent(i), treeOnlyCommits);
                        if (forkPoint != null && !tails.contains(forkPoint)) {
                            tails.add(forkPoint);
                            brandLiveTimes.add(new BranchLiveTime(plotLane, forkPoint));
                        }
                    }
                }
            }
            plotLane = plotLane.getParent(0);
        }
        return calculateAverageperMonth(brandLiveTimes, params);
    }

    public List<AverageLiveTime> calculateAverageperMonth(final List<BranchLiveTime> brandLiveTimes, final StatWPParameters params) {
        if (params.isWorkingHours()) {
            brandLiveTimes.stream().forEach(BranchLiveTime::calculateDiff);
        }
        return branchLiveTimeAdapter.adapt(brandLiveTimes);
    }

    private Set<RevCommit> getRevCommits(PlotCommitList<PlotLane> treeNameHistory) {
        final Set<RevCommit> treeOnlyCommits = new HashSet<>();
        for (PlotCommit<PlotLane> plotLane : treeNameHistory) {
            for (int i = 0; i < plotLane.getChildCount(); i++) {
                PlotCommit plotCommit = plotLane.getChild(i);
                if (plotCommit.getLane().getPosition() == 0) {
                    treeOnlyCommits.add(plotCommit);
                }
            }
        }
        return treeOnlyCommits;
    }

    private RevCommit findParrent(RevCommit revCommit, Set<RevCommit> treeOnlyCommits) {

        if (revCommit.getParentCount() == 0) {
            return null;
        }
        RevCommit parrent = revCommit.getParent(0);
        if (treeOnlyCommits.contains(parrent)) {
            return parrent;
        }
        return findParrent(parrent, treeOnlyCommits);

    }


    /*TODO is it really need ? */
    private String getConflictSubStatus(String key, Map<String, IndexDiff.StageState> conflictingStageState) {

        return adaptConflictingState(conflictingStageState.get(key));
    }

    private String adaptConflictingState(IndexDiff.StageState stageState) {
        if (stageState == null) {
            return null;
        }
        switch (stageState) {

            case BOTH_DELETED:
                return ScmItem.Status.CONFLICT_BOTH_DELETED;
            case ADDED_BY_US:
                return ScmItem.Status.CONFLICT_ADDED_BY_US;
            case DELETED_BY_THEM:
                return ScmItem.Status.CONFLICT_DELETED_BY_THEM;
            case ADDED_BY_THEM:
                return ScmItem.Status.CONFLICT_ADDED_BY_THEM;
            case DELETED_BY_US:
                return ScmItem.Status.CONFLICT_DELETED_BY_US;
            case BOTH_ADDED:
                return ScmItem.Status.CONFLICT_BOTH_ADDED;
            case BOTH_MODIFIED:
                return ScmItem.Status.CONFLICT_BOTH_MODIFIED;
            default:
                return null;

        }
    }


    public Repository getRepository() {
        return repository;
    }

    public boolean isRepositoryHasRemoteUrl() {
        return getRepositoryRemoteUrl() != null;
    }

    public String getRepositoryRemoteUrl() {
        return getRepository().getConfig().getString(ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME, ConfigConstants.CONFIG_KEY_URL);
    }


    //----------------------------------- Remote section ---------------------------------

    /**
     * Clone remote repository.
     *
     * @param params clone parameters
     */
    public void cloneRepository(final RemoteRepoParameters params,
                                final ProgressMonitor progressMonitor) throws Exception {

        final String reporitoryUrl = params.getUrl();

        final CloneCommand cmd = Git.cloneRepository()
                .setURI(reporitoryUrl)
                .setDirectory(new File(params.getDestinationFolder()))
                .setCloneAllBranches(true)
                .setCloneSubmodules(true)
                .setProgressMonitor(progressMonitor);
        configureTransportCommand(cmd, params);

        Git result = null;

        Pair<String, String> smudgeAndClean = configureLfsSupport(SystemReader.getInstance().getUserConfig());

        try {
            result = cmd.call();
            String rez = result.getRepository().getDirectory().getAbsolutePath();
            log.log(Level.INFO, MessageFormat.format("Clone {0} result {1}", reporitoryUrl, rez));
        } catch (TransportException te) {
            log.log(Level.WARNING, MessageFormat.format("Clone {0} error {1}", reporitoryUrl, te.getMessage()));
            throw te;
        } finally {
            if (result != null && isLfsRepo(result.getRepository())) {
                StoredConfig repoCfg = result.getRepository().getConfig();
                repoCfg.setString("filter", "lfs", "clean", GitRepoService.CLEAN_NAME);
                repoCfg.setString("filter", "lfs", "smudge", GitRepoService.SMUDGE_NAME);
                repoCfg.save();
                log.log(Level.INFO, MessageFormat.format("Repo {0} configured  with LFS support by gitember", reporitoryUrl));
            }
            rollbackLfsSupport(SystemReader.getInstance().getUserConfig(), smudgeAndClean);
        }
    }


    /**
     * Temporally configure in the user config internal LFS
     *
     * @return pait of smugde and clean filterf for LFS
     * @throws Exception in case of error.
     */
    private Pair<String, String> configureLfsSupport(StoredConfig config) throws Exception {

        String smudge = config.getString("filter", "lfs", "smudge");
        String clean = config.getString("filter", "lfs", "clean");

        // config.setString("filter", "lfs", "clean", GitRepoService.CLEAN_NAME);
        // config.setString("filter", "lfs", "smudge", GitRepoService.SMUDGE_NAME);

        config.unset("filter", "lfs", "smudge");
        config.unset("filter", "lfs", "clean");

        //config.setString("filter", "lfs", "smudge", "git-lfs smudge --skip -- %f");

        config.save();
        return new Pair<>(smudge, clean);
    }

    private void rollbackLfsSupport(StoredConfig config, Pair<String, String> smudgeAndClean) throws Exception {
        String smudge = smudgeAndClean.getFirst();
        String clean = smudgeAndClean.getSecond();


        if (clean == null) {
            config.unset("filter", "lfs", "clean");
        } else {
            config.setString("filter", "lfs", "clean", clean);
        }

        if (smudge == null) {
            config.unset("filter", "lfs", "smudge");
        } else {
            config.setString("filter", "lfs", "smudge", smudge);
        }
        config.save();
    }


    public void setRemoteUrl(String remoteUrl) throws Exception {
        try (Git git = new Git(repository)) {
            RemoteSetUrlCommand remoteSetUrlCommand = git.remoteSetUrl();
            remoteSetUrlCommand.setRemoteUri(new URIish(remoteUrl));
            remoteSetUrlCommand.setRemoteName("origin");
            remoteSetUrlCommand.call();
        }
    }


    /**
     * Push to remote repo
     *
     * @param refSpec         spec
     * @param progressMonitor progress monitor
     * @return string with push details
     * @throws Exception in case of error
     */
    public String remoteRepositoryPush(final RemoteRepoParameters parameters,
                                       final RefSpec refSpec,
                                       final ProgressMonitor progressMonitor) throws Exception {
        try (Git git = new Git(repository)) {

            final PushCommand pushCommand = git.push()
                    .setRefSpecs(refSpec)
                    .setProgressMonitor(progressMonitor);

            configureTransportCommand(pushCommand, parameters);

            final String projectRemoteUrl = git.getRepository()
                    .getConfig()
                    .getString("remote", "origin", "url");
            log.log(Level.INFO, "Pushing to " + projectRemoteUrl + " ref: " + refSpec);

            Iterable<PushResult> pushResults = pushCommand.call();

            final FetchCommand fetchCommand = git
                    .fetch()
                    .setCheckFetchedObjects(true)
                    .setProgressMonitor(progressMonitor);

            configureTransportCommand(fetchCommand, parameters);

            fetchCommand.call();

            StringBuilder stringBuilder = new StringBuilder();
            pushResults.forEach(
                    pushResult -> {
                        stringBuilder.append(pushResult.getMessages());
                        log.log(Level.INFO,
                                "Pushed " + pushResult.getMessages() + " " + pushResult.getURI()
                                        + " updates: " + pushResult.getRemoteUpdates());
                    }
            );
            return stringBuilder.toString();
        } catch (Exception e) {
            //} catch (CheckoutConflictException conflictException) {
            //.TransportException: https://github.com/iazarny/jmicroscope.git: Authentication is required but no CredentialsProvider has been registered
            log.log(Level.WARNING,
                    MessageFormat.format("Not pushed {0} ", e.getMessage()), e);
            throw e;
        }
    }


    /**
     * Set track remote branch
     *
     * @param localBranch  local branch name
     * @param remoteBranch remote branch name
     * @return full remote name
     * @throws Exception in case of error
     */
    public String trackRemote(String localBranch, String remoteBranch) throws IOException {
        try (Git git = new Git(repository)) {
            final StoredConfig config = git.getRepository().getConfig();
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranch,
                    ConfigConstants.CONFIG_KEY_REMOTE, Constants.DEFAULT_REMOTE_NAME);
            config.setString(ConfigConstants.CONFIG_BRANCH_SECTION, localBranch,
                    ConfigConstants.CONFIG_KEY_MERGE, Constants.R_HEADS + remoteBranch);
            config.save();

            return git.getRepository().getConfig().getString(
                    ConfigConstants.CONFIG_BRANCH_SECTION,
                    localBranch,
                    ConfigConstants.CONFIG_KEY_MERGE);

        }
    }


    /**
     * Pull changes.
     *
     * @param remoteBranch optional name on remote repo,
     *                     i.e. with ref/heads/ prefix, if not provided command will fetch all
     * @return result of opertion
     * @throws Exception
     */
    public String remoteRepositoryPull(final RemoteRepoParameters parameters,
                                       final String remoteBranch,
                                       final ProgressMonitor progressMonitor) throws Exception {

        boolean lfsrepo = isLfsRepo();
        Pair<String, String> smudgeAndCleanRepo = null;
        Pair<String, String> smudgeAndCleanUser = null;

        try (Git git = new Git(repository)) {

            if (lfsrepo) { //TODO check ir required=false may help
                smudgeAndCleanRepo = configureLfsSupport(repository.getConfig());
                smudgeAndCleanUser = configureLfsSupport(SystemReader.getInstance().getUserConfig());
            }

            PullCommand pullCommand = git
                    .pull()
                    .setRemoteBranchName(remoteBranch)
                    .setProgressMonitor(progressMonitor);
            configureTransportCommand(pullCommand, parameters);

            PullResult pullRez = pullCommand.call();
            if (pullRez.isSuccessful()) {
                return pullRez.getMergeResult().getMergeStatus().toString();
            }
            return pullRez.getMergeResult().toString(); // TODo more info
        } finally {
            if (smudgeAndCleanRepo != null) {
                rollbackLfsSupport(repository.getConfig(), smudgeAndCleanRepo);
                rollbackLfsSupport(SystemReader.getInstance().getUserConfig(), smudgeAndCleanUser);
            }
        }
    }


    /**
     * Fetch all or particular branch.
     * <p>
     * //http://stackoverflow.com/questions/16319807/determine-if-a-branch-is-remote-or-local-using-jgit
     * //http://stackoverflow.com/questions/12927163/jgit-checkout-a-remote-branch
     *
     * @param remoteBranch    optional name on remote repo,
     *                        i.e. with ref/heads/ prefix, if not provided command will fetch all
     * @param progressMonitor optional progress
     * @return result of operation
     */
    public String remoteRepositoryFetch(final RemoteRepoParameters parameters,
                                        final String remoteBranch,
                                        final ProgressMonitor progressMonitor) throws Exception {

        try (Git git = new Git(repository)) {

            final FetchCommand cmd = git
                    .fetch()
                    .setCheckFetchedObjects(true)
                    .setProgressMonitor(progressMonitor);

            configureTransportCommand(cmd, parameters);

            if (remoteBranch != null) {
                cmd.setRefSpecs(new RefSpec(remoteBranch));
            }
            final FetchResult fetchResult = cmd.call();
            String rezMsg = "\nMessage " + fetchResult.getMessages();
            rezMsg += "\nTracking " + fetchResult.getTrackingRefUpdates()
                    .stream()
                    .map(tr -> String.format("%s local %s remote %s", tr.getResult().toString(), tr.getLocalName(), tr.getRemoteName()))
                    .collect(Collectors.joining("\n"));

            rezMsg += "\nAdvertised " + fetchResult.getAdvertisedRefs()
                    .stream()
                    .map(r -> r.getName())
                    .collect(Collectors.joining("\n"));

            return rezMsg;
        }

    }

    public String getMessage(final MergeResult mergeResult) {

        final StringBuilder stringBuilder = new StringBuilder();
        final String indent = "\n    ";

        if (mergeResult.getMergeStatus().isSuccessful()) {

            if (mergeResult.getNewHead() != null) {
                stringBuilder.append("New head: ").append(mergeResult.getNewHead().getName()).append("\n");
            }

            if (mergeResult.getBase() != null) {
                stringBuilder.append("Base: ").append(mergeResult.getBase().getName()).append("\n");
            }

            String merged = Arrays.stream(mergeResult.getMergedCommits())
                    .map(o -> ObjectId.toString(o))
                    .collect(Collectors.joining(indent)); //TODO message

            stringBuilder.append("Merged:").append(indent).append(merged).append("\n");

        } else {

            String mergeConflicts = mergeResult.getConflicts().entrySet().stream()
                    .map(es -> es.getKey())
                    .collect(Collectors.joining(indent));

            stringBuilder.append("Conflict:").append(indent).append(mergeConflicts).append("\n");

            if (mergeResult.getFailingPaths() != null) {
                String failures = mergeResult.getFailingPaths().entrySet().stream()
                        .map(es -> es.getKey())
                        .collect(Collectors.joining(indent));
                stringBuilder.append("Failed:").append(indent).append(failures).append("\n");
            }


        }
        return stringBuilder.toString();
    }


    private void configureTransportCommand(
            final TransportCommand cmd, RemoteRepoParameters params) throws ConfigInvalidException, IOException {

        if (params.getUserName() != null) {
            cmd.setCredentialsProvider(
                    new UsernamePasswordCredentialsProvider(params.getUserName(), params.getUserPwd())
            );
        }

        final String reporitoryUrl = params.getUrl();

        if (reporitoryUrl.toLowerCase(Locale.ROOT).startsWith(Const.Config.HTTPS)) {
            StoredConfig fbcOrig = SystemReader.getInstance().getUserConfig();
            fbcOrig.setBoolean(Const.Config.HTTP, null, Const.Config.SLL_VERIFY, false);
            fbcOrig.save();

        } else if (reporitoryUrl.toLowerCase(Locale.ROOT).startsWith(Const.Config.SSH)
                || reporitoryUrl.toLowerCase(Locale.ROOT).startsWith(Const.Config.GIT)) {
            /*cmd.setTransportConfigCallback(
                    new SshTransportConfigCallback(
                            params.getPathToKey(),
                            params.getKeyPassPhrase()
                    )
            );*/

            File sshDir = new File(FS.DETECTED.userHome(), File.separator + SshConstants.SSH_DIR);

            SshdSessionFactory sshSessionFactory = new SshdSessionFactoryBuilder()
                    .setPreferredAuthentications("publickey")
                    .setHomeDirectory(FS.DETECTED.userHome()).setSshDirectory(sshDir)
                    .setKeyPasswordProvider(cp -> new IdentityPasswordProvider(cp) {
                        @Override
                        protected char[] getPassword(URIish uri, String message) {
                            return "".toCharArray();//passphrase.toCharArray();
                        }
                    }).build(null);

            cmd.setTransportConfigCallback(
                    transport -> ((SshTransport) transport).setSshSessionFactory(sshSessionFactory));



            /*TransportCommand<T, C> transportCommand = <Any Transport command in JGIT>;
File sshDir = new File(FS.DETECTED.userHome(), File.separator+SSH_DIR);
      SshdSessionFactory sshSessionFactory = new SshdSessionFactoryBuilder().setPreferredAuthentications("publickey")
        .setHomeDirectory(FS.DETECTED.userHome()).setSshDirectory(sshDir)
        .setKeyPasswordProvider(cp -> new IdentityPasswordProvider(cp)
        {
          @Override
          protected char[] getPassword(URIish uri, String message)
          {
            return passphrase.toCharArray();
          }
        }).build(null);
 transportCommand.setTransportConfigCallback(
    transport -> ((SshTransport) transport).setSshSessionFactory(sshSessionFactory));*/

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


    public ScmRevisionInformation adapt(final RevCommit revCommit) {
        return adapt(revCommit, null);
    }

    /**
     * Adapt given rev commit to <code>ScmRevisionInformation</code>
     *
     * @param revCommit given <code>RevCommit</code>
     * @param fileName  optional file filter
     * @return instance of {@link ScmRevisionInformation}
     */
    public ScmRevisionInformation adapt(final RevCommit revCommit, final String fileName) {

        return Context.scmRevisionInformationCache.computeIfAbsent(revCommit.getId().toString(), s -> {
            final ScmRevisionInformation info = new ScmRevisionInformation();
            info.setShortMessage(revCommit.getShortMessage());
            info.setFullMessage(revCommit.getFullMessage());
            info.setRevisionFullName(revCommit.getName());
            info.setDate(GitemberUtil.intToDate(revCommit.getCommitTime()));
            info.setAuthorName(revCommit.getAuthorIdent().getName());
            info.setAuthorEmail(revCommit.getAuthorIdent().getEmailAddress());
            info.setParents(
                    Arrays.stream(revCommit.getParents()).map(AnyObjectId::getName).collect(Collectors.toList())
            );
            if (revCommit instanceof PlotCommit) {
                ArrayList<String> refs = new ArrayList<>();
                for (int i = 0; i < ((PlotCommit) revCommit).getRefCount(); i++) {
                    if (revCommit != null && ((PlotCommit) revCommit).getRef(i) != null && ((PlotCommit) revCommit).getRef(i).getName() != null) {
                        refs.add(
                                ((PlotCommit) revCommit).getRef(i).getName()
                        );

                    }
                }
                info.setRef(refs);

            }
            info.setAffectedItems(getScmItems(revCommit, fileName));
            return info;
        });


    }

    /**
     * Get list of changed files in given revision.
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
                    rez.forEach(scmItem -> scmItem.setCommitName(revCommit));

                } catch (IOException e) {
                    log.log(Level.SEVERE, "Cannot collect items from rev commit", e);
                }
            }
            rw.dispose();
            return rez;
        }
    }

    public List<ScmStat> blame(final PlotCommitList<PlotLane> lastCommitPerMonth, final ProgressMonitor progressMonitor) throws Exception {
        final List<ScmStat> scmStats = new ArrayList<>(lastCommitPerMonth.size());
        for (PlotCommit pc : lastCommitPerMonth) {
            checkoutRevCommit(pc, progressMonitor);
            final Set<String> files = getAllFiles();
            final Date date = GitemberUtil.intToDate(pc.getCommitTime());
            final String taskName = new SimpleDateFormat("yyyy MMM  ").format(date);
            final ScmStat stat = blame(files, taskName, progressMonitor);
            stat.setDate(date);
            scmStats.add(stat);
        }
        return scmStats;
    }

    public ScmStat blame(final Set<String> files, final ProgressMonitor progressMonitor) throws Exception {
        return blame(files, "Blame", progressMonitor);
    }

    public BlameResult blame(ScmItem scmItem) throws Exception {
        final BlameCommand blamer = new BlameCommand(repository);
        if (scmItem.getRevCommit() != null) {
            blamer.setStartCommit(scmItem.getRevCommit());
        }
        blamer.setFilePath(scmItem.getShortName());
        return blamer.call();
    }

    ScmStat blame(final Set<String> files, final String taskName, final ProgressMonitor progressMonitor) throws Exception {

        final Ref head = repository.exactRef(Constants.HEAD);
        final RevWalk walk = new RevWalk(repository);
        final RevCommit commit = walk.parseCommit(head.getObjectId());
        final Map<String, Map> rez = new TreeMap<>();
        final Map<String, Integer> totalLines = new TreeMap<>();
        final Map<String, Integer> commitsMap = new HashMap<>();
        final Iterator<String> pathIter = files.iterator();

        progressMonitor.beginTask(taskName, files.size());

        int cnt = 0;

        while (pathIter.hasNext()) {
            final String path = pathIter.next();
            final BlameCommand blamer = new BlameCommand(repository);
            blamer.setStartCommit(commit);
            blamer.setFilePath(path);
            final BlameResult blame = blamer.call();
            rez.put(path, countLines(blame));
            progressMonitor.update(cnt++);
        }

        rez.values().stream().forEach(
                m -> m.forEach(
                        (author, c) -> {
                            totalLines.computeIfPresent(
                                    (String) author,
                                    (s, integer) -> integer + (Integer) c
                            );
                            totalLines.computeIfAbsent(
                                    (String) author,
                                    s -> (Integer) c
                            );
                        }
                )
        );

        progressMonitor.endTask();

        progressMonitor.beginTask("Log", 300);

        try (Git git = new Git(repository)) {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit rc : commits) {
                String author = rc.getAuthorIdent().getName();
                commitsMap.computeIfPresent(
                        author,
                        (a, i) -> i + 1
                );
                commitsMap.computeIfAbsent(
                        author,
                        s -> 1
                );
            }
        }

        progressMonitor.endTask();

        return new ScmStat(totalLines, commitsMap);

    }

    private Map<String, Integer> countLines(BlameResult blame) {
        Map<String, Integer> rez = new HashMap<>();
        if (blame != null && blame.getResultContents() != null) {
            int lines = blame.getResultContents().size();
            for (int i = 0; i < lines; i++) {
                String author = blame.getSourceCommitter(i).getName();
                Integer cnt = rez.getOrDefault(author, 0) + 1;
                rez.put(author, cnt);
            }
        }
        return rez;
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
        final ScmItemAttribute attr = new ScmItemAttribute().withStatus(adaptDiffStatus(diff.getChangeType()));
        String name = diff.getOldPath();
        if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
            name = diff.getNewPath();
            attr.withOldName(diff.getOldPath());
        } else if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
            name = diff.getNewPath();
        }
        return new ScmItem(name, attr);
    }

    private String adaptDiffStatus(DiffEntry.ChangeType diffChangeType) {
        if (diffChangeType == DiffEntry.ChangeType.ADD) {
            return ScmItem.Status.ADDED;
        } else if (diffChangeType == DiffEntry.ChangeType.MODIFY) {
            return ScmItem.Status.MODIFIED;
        } else if (diffChangeType == DiffEntry.ChangeType.DELETE) {
            return ScmItem.Status.REMOVED;
        } else if (diffChangeType == DiffEntry.ChangeType.RENAME) {
            return ScmItem.Status.RENAMED;
        }
        return ScmItem.Status.MODIFIED;
    }

    private static synchronized void deleteOnExit(File file) {
        tempFiles.add(file);
    }

    private static synchronized void cleanUpTempFiles() {
        tempFiles.forEach(f -> {
            if (f.exists()) f.delete();
        });
        tempFiles.clear();
    }

    private static String gitIgnoreContent = "HELP.md\n" +
            "target/\n" +
            "!.mvn/wrapper/maven-wrapper.jar\n" +
            "!**/src/main/**/target/\n" +
            "!**/src/test/**/target/\n" +
            "\n" +
            "### STS ###\n" +
            ".apt_generated\n" +
            ".classpath\n" +
            ".factorypath\n" +
            ".project\n" +
            ".settings\n" +
            ".springBeans\n" +
            ".sts4-cache\n" +
            "\n" +
            "### IntelliJ IDEA ###\n" +
            ".idea\n" +
            "*.iws\n" +
            "*.iml\n" +
            "*.ipr\n" +
            "\n" +
            "### NetBeans ###\n" +
            "/nbproject/private/\n" +
            "/nbbuild/\n" +
            "/dist/\n" +
            "/nbdist/\n" +
            "/.nb-gradle/\n" +
            "build/\n" +
            "!**/src/main/**/build/\n" +
            "!**/src/test/**/build/\n" +
            "\n" +
            "### VS Code ###\n" +
            ".vscode/\n\n";

    private static String gitAttributesContent = "*.psd filter=lfs diff=lfs merge=lfs -text\n" +
            "*.bmp filter=lfs diff=lfs merge=lfs -text\n";


}
