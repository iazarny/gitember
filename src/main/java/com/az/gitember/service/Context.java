package com.az.gitember.service;

import com.az.gitember.controller.Main;
import com.az.gitember.controller.handlers.StatusUpdateEventHandler;
import com.az.gitember.data.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Context {

    private final static String OS = System.getProperty("os.name").toLowerCase();


    private static GitRepoService gitRepoService = new GitRepoService();
    private final static SettingService settingService = new SettingService();


    public static final StringProperty repositoryPathProperty = new SimpleStringProperty();
    public static ObjectProperty<ScmBranch> workingBranch = new SimpleObjectProperty<ScmBranch>();
    public static final ObjectProperty<Settings> settingsProperty = new SimpleObjectProperty<Settings>();
    public static final ObjectProperty<ScmRevisionInformation> scmRevCommitDetails = new SimpleObjectProperty<ScmRevisionInformation>();

    public static final StringProperty selectedTreeName = new SimpleStringProperty();
    public static final SimpleObjectProperty<LocalDateTime> lastUpdate = new SimpleObjectProperty();



    //-------------------------------------------------------

    public static final StringProperty branchFilter =
            new SimpleStringProperty();


    private static final List<ScmBranch> remoteBranchesRaw = new ArrayList<>();
    private static final List<ScmBranch> localBranchesRaw = new ArrayList<>();
    private static final List<ScmBranch> tagsRaw = new ArrayList<>();

    public static final ObjectProperty<List<ScmBranch>> remoteBrancesProperty =
            new SimpleObjectProperty<List<ScmBranch>>(Collections.emptyList());

    public static final ObjectProperty<List<ScmBranch>> localBrancesProperty =
            new SimpleObjectProperty<List<ScmBranch>>(Collections.emptyList());

    public static final ObjectProperty<List<ScmBranch>> tagsProperty =
            new SimpleObjectProperty<List<ScmBranch>>(Collections.emptyList());
    //-------------------------------------------------------

    public static final ObjectProperty<List<ScmRevisionInformation>> stashProperty =
            new SimpleObjectProperty<List<ScmRevisionInformation>>(Collections.emptyList());

    public static final ObservableList<ScmItem> statusList = FXCollections.observableList(new ArrayList<>());
    public static final ObservableList<ScmItem> stashItemsList = FXCollections.observableList(new ArrayList<>());
    public static final ObservableList<PlotLane> plotCommitList = FXCollections.observableList(new ArrayList<>());

    public static final StringProperty fileHistoryTree = new SimpleStringProperty();
    public static final StringProperty fileHistoryName = new SimpleStringProperty();
    public static final StringProperty mainPaneName = new SimpleStringProperty();

    public static final ObjectProperty<ScmStat> scmStatProperty =
            new SimpleObjectProperty(new ScmStat());

    public static final ObjectProperty<List<ScmStat>> scmStatListProperty =
            new SimpleObjectProperty(Collections.EMPTY_LIST);

    public static final ObjectProperty<List<AverageLiveTime>> scmStatBranchLiveTimeProperty =
            new SimpleObjectProperty(Collections.EMPTY_LIST);

    public static final ObjectProperty<StatWPParameters> scmStatListPropertyParam =
            new SimpleObjectProperty(null);

    public static StringProperty searchValue = new SimpleStringProperty();


    private static Main main;




    public static void init(RemoteRepoParameters remoteRepoParameters) throws Exception {



        String gitFolder = remoteRepoParameters.getDestinationFolder();

        if (!gitFolder.endsWith(Const.GIT_FOLDER)) {
            gitFolder += File.separator + Const.GIT_FOLDER;
        }


        gitRepoService = new GitRepoService(gitFolder);
        //scmBranchProperty = new SimpleObjectProperty(new GitRepoService(gitFolder));

        updateBranches();
        updateTags();
        stashProperty.setValue(gitRepoService.getStashList());
        //statusList.addAll(gitRepoService.getStatuses()); // TODO only when is open the form ? ??

        Project project = new Project();
        project.setOpenTime(new Date());
        project.setProjectHomeFolder(gitFolder);
        project.setUserName(remoteRepoParameters.getUserName());
        project.setUserPwd(remoteRepoParameters.getUserPwd()); //TODO encrypt passwords
        project.setUserKey(remoteRepoParameters.getPathToKey());
        project.setKeyPass(remoteRepoParameters.getKeyPassPhrase());

        updateWorkingBranch();
        settingsProperty.get().getProjects().add(project);
        saveSettings();

        getMain().mainTreeChangeListener.changed(null, null, Context.getMain().workingCopyTreeItem);
        branchFilter.addListener(
                (observable, oldValue, newValue) -> {
                    filterBranches();
                    filterTags();
                }
        );

        repositoryPathProperty.setValue(gitFolder);
    }

    public static void init(String gitFolder) throws Exception {
        RemoteRepoParameters params = new RemoteRepoParameters();
        params.setDestinationFolder(gitFolder);
        init(params);
    }


    public static void saveSettings() {
        settingService.write(settingsProperty.get());
    }

    public static Project getCurrentProject() {
        String folder = repositoryPathProperty.getValueSafe();
        Project rez = settingsProperty.get().getProjects().stream()
                .filter( p -> p.getProjectHomeFolder().equalsIgnoreCase(folder))
                .findFirst()
                .get();
        return rez;
    }

    public static void readSettings() {
        Settings settings = settingService.read();
        settingsProperty.setValue(settings);
    }

    public static void updatePlotCommitList(final String treeName, final boolean allHistory, final ProgressMonitor progressMonitor) {

        final PlotCommitList<PlotLane> plotCommits = gitRepoService.getCommitsByTree(treeName, allHistory, progressMonitor);

        plotCommitList.clear();

        plotCommitList.addAll((Collection) plotCommits);

        selectedTreeName.set(treeName);
    }


    public static void updateAll() {
        new StatusUpdateEventHandler(true).handle(null);
        updateWorkingBranch();
        try {
            updateBranches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateTags();
        updateStash();
    }

    public static void updateStatus(ProgressMonitor progressMonitor) {
        List<ScmItem> getStatuses = gitRepoService.getStatuses(progressMonitor);
        plotCommitList.clear();
        statusList.clear();
        statusList.addAll(getStatuses);
        lastUpdate.set(LocalDateTime.now());
    }

    public static void updateWorkingBranch() {
        workingBranch.setValue(
                localBrancesProperty.get().stream().filter(ScmBranch::isHead).findFirst().orElse(null)
        );
    }

    public static void updateBranches() throws Exception {
        localBranchesRaw.clear();
        localBranchesRaw.addAll(gitRepoService.getBranches());
        remoteBranchesRaw.clear();
        remoteBranchesRaw.addAll(gitRepoService.getRemoteBranches());
        filterBranches();
    }

    public static void filterBranches()  {
        localBrancesProperty.setValue(localBranchesRaw.stream()
                .filter( b -> b.getNameExt().toLowerCase().contains(branchFilter.getValueSafe().toLowerCase(Locale.ROOT)) )
                .collect(Collectors.toList()));
        remoteBrancesProperty.setValue(remoteBranchesRaw.stream()
                .filter( b -> b.getNameExt().toLowerCase().contains(branchFilter.getValueSafe().toLowerCase(Locale.ROOT)) )
                .collect(Collectors.toList()));
    }

    public static void updateTags() {
        tagsRaw.clear();
        tagsRaw.addAll(gitRepoService.getTags());
        filterTags();
    }

    public static void filterTags() {
        tagsProperty.setValue(tagsRaw.stream()
                .filter( b -> b.getNameExt().toLowerCase().contains(branchFilter.getValueSafe().toLowerCase()) )
                .collect(Collectors.toList()));
    }

    public static void updateSettings(Settings settings) {
        settingsProperty.setValue(settings);
    }

    public static void updateStash() {
        stashProperty.setValue(gitRepoService.getStashList());
    }

    public static GitRepoService getGitRepoService() {
        return gitRepoService;
    }

    public static SettingService getSettingService() {
        return settingService;
    }

    public static Main getMain() {
        return main;
    }

    public static void setMain(Main main) {
        Context.main = main;
    }

    public static String getProjectFolder() {
        return repositoryPathProperty.getValueSafe().replace(Const.GIT_FOLDER, "");
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }
}
