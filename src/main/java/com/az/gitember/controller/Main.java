package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.controller.handlers.*;
import com.az.gitember.data.Const;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.data.ScmBranch;
import com.az.gitember.data.ScmItem;
import com.az.gitember.service.Context;
import com.az.gitember.service.SearchService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements Initializable {

    private final static Logger log = Logger.getLogger(Main.class.getName());

    public ToolBar toolBar;
    public Label operationName;
    public ProgressBar progressBar;
    public AnchorPane hostPanel;
    public MenuBar mainMenuBar;
    public ToolBar mainToolBar;
    public Menu openRecentMenuItem;
    public MenuItem compressDataMenuItem;
    public MenuItem reindexDataMenuItem;
    public MenuItem dropIndexDataMenuItem;
    public MainTreeChangeListener mainTreeChangeListener;
    public MenuItem fetchMenuItem;
    public MenuItem pullMenuItem;
    public MenuItem puchMenuItem;
    public MenuItem lfsMenuItem;
    public MenuItem editRawConfigMenuItem;
    public MenuItem editRawIgnoreMenuItem;
    public MenuItem editRawAttrsMenuItem;
    public Menu repoSettingsMenuItem;
    public Button pullBtn;
    public Button pushBtn;
    public Button fetchBtn;
    public Menu statReportMenu;
    public TreeView repoTreeView;
    public TreeItem workingCopyTreeItem;
    public TreeItem historyTreeItem;
    public TreeItem stashesTreeItem;

    public TreeItem localBranchesTreeItem;
    public TreeItem remoteBranchesTreeItem;
    public TreeItem tagsTreeItem;
    public Label aheadIconQty;
    public Label behindIconQty;
    public StackedFontIcon aheadIcon;
    public StackedFontIcon behindIcon;


    /**
     * Init and subscribe on git repo changes.
     *
     * @param url            url
     * @param resourceBundle resource bundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Context.setMain(this);
        if (Context.isMac()) {
            mainMenuBar.setUseSystemMenuBar(true);
        }


        mainTreeChangeListener = new MainTreeChangeListener();

        Context.workingBranch.addListener(
                (observableValue, oldValue, newValue) -> {
                    final String remUrl = Context.getGitRepoService().getRepositoryRemoteUrl();
                    final ScmBranch scmBranch = Context.workingBranch.getValue();
                    App.getStage().setTitle(
                            Const.APP_NAME + " " + Context.repositoryPathProperty.getValueSafe() + " " + (remUrl == null ? "" : remUrl) + " "
                                    + ScmBranch.getNameSafe(scmBranch));
                    pushBtn.setDisable(remUrl == null || false);
                    pushBtn.setTooltip(new Tooltip("Push " + ScmBranch.getNameExtSafe(scmBranch)));
                    if (!pushBtn.isDisable()) {
                        scmBranch.getScmBranchPushTooltip().ifPresent(
                                t -> {
                                    pushBtn.setTooltip(
                                            new Tooltip(
                                                    "Push " + ScmBranch.getNameExtSafe(scmBranch)
                                                    + ". " + t
                                            )
                                    );
                                }
                        );
                    }


                    pullBtn.setDisable(remUrl == null || newValue.getRemoteMergeName() == null);
                    pullBtn.setTooltip(new Tooltip("Pull " + ScmBranch.getNameExtSafe(scmBranch)));
                    if (!pullBtn.isDisable()) {
                        scmBranch.getScmBranchPullTooltip().ifPresent(
                                t -> {
                                    pullBtn.setTooltip(new Tooltip(
                                            "Pull " + ScmBranch.getNameExtSafe(scmBranch)
                                                    + ". " + t
                                    ));
                                }
                        );
                    }

                    puchMenuItem.setDisable(pushBtn.isDisable());
                    pullMenuItem.setDisable(pullBtn.isDisable());

                    fetchBtn.setTooltip(new Tooltip("Fetch all"));

                    updateButtonUI();


                }
        );

        Context.repositoryPathProperty.addListener(
                (observableValue, oldValue, newValue) ->
                {
                    String remUrl = Context.getGitRepoService().getRepositoryRemoteUrl();

                    final ScmBranch scmBranch = Context.workingBranch.getValue();
                    App.getStage().setTitle(
                            Const.APP_NAME + " " + Context.repositoryPathProperty.getValueSafe() + " " + (remUrl == null ? "" : remUrl) + " "
                                    + ScmBranch.getNameSafe(scmBranch));
                    boolean disable = remUrl == null;
                    compressDataMenuItem.setDisable(newValue == null);
                    reindexDataMenuItem.setDisable(newValue == null);
                    fetchMenuItem.setDisable(disable);
                    repoSettingsMenuItem.setDisable(false);
                    statReportMenu.setDisable(newValue == null);
                    statReportMenu.setVisible(newValue != null);
                    fetchBtn.setDisable(disable);
                    repoTreeView.setDisable(false);

                    //boolean lfsRepo = Context.getGitRepoService().isLfsRepo();
                    boolean attrFileExists = Context.getGitRepoService().isFileExists(Const.GIT_ATTR_NAME);
                    boolean ignoreFileExists = Context.getGitRepoService().isFileExists(Const.GIT_IGNORE_NAME);

                    editRawIgnoreMenuItem.setVisible(ignoreFileExists);
                    editRawAttrsMenuItem.setVisible(attrFileExists);

                    dropIndexDataMenuItem.setDisable(!Context.getCurrentProject().isIndexed());



                }
        );

        Context.localBrancesProperty.addListener(
                (observableValue, oldValue, newValue) -> {
                    localBranchesTreeItem.getChildren().clear();
                    newValue.forEach(branch -> {
                        localBranchesTreeItem.getChildren().add(new TreeItem<>(branch));
                    });
                }
        );


        Context.remoteBrancesProperty.addListener(
                (observableValue, oldValue, newValue) -> {
                    remoteBranchesTreeItem.getChildren().clear();
                    newValue.forEach(branch -> {
                        remoteBranchesTreeItem.getChildren().add(new TreeItem<>(branch));
                    });
                }
        );


        Context.tagsProperty.addListener(
                (observableValue, oldValue, newValue) -> {
                    tagsTreeItem.getChildren().clear();
                    newValue.forEach(tag -> {
                        tagsTreeItem.getChildren().add(new TreeItem<>(tag));
                    });
                }
        );

        Context.stashProperty.addListener(
                (observableValue, oldValue, newValue) -> {
                    stashesTreeItem.getChildren().clear();
                    newValue.forEach(stash -> {
                        stashesTreeItem.getChildren().add(new TreeItem<>(stash));
                    });
                }
        );

        Context.settingsProperty.addListener(observable -> {
            openRecentMenuItem.getItems().clear();
            Context.settingsProperty.get().getProjects().stream().forEach(
                    o -> {
                        MenuItem mi = new MenuItem(o.getProjectHomeFolder().replace(".git", ""));
                        mi.setOnAction(
                                event -> {
                                    try {
                                        Context.init(o.getProjectHomeFolder());
                                    } catch (Exception e) {
                                        Context.settingsProperty.getValue().getProjects().remove(o);
                                        Context.saveSettings();
                                        Context.readSettings();
                                        Context.getMain().showResult("Cannot load project ", "Cannot load project " + o.getProjectHomeFolder() + "\n   It will be removed from the list of recent projects", Alert.AlertType.WARNING);

                                        log.log(Level.WARNING, "Cannot load project {0}. {1}", new String[]{o.getProjectHomeFolder(), e.getMessage()});
                                    }

                                }
                        );
                        openRecentMenuItem.getItems().add(mi);
                    }
            );
            openRecentMenuItem.setDisable(Context.settingsProperty.get().getProjects().size() < 1);
        });

        Context.readSettings();

        repoTreeView.getSelectionModel().selectedItemProperty().addListener(mainTreeChangeListener);


        //search on letter only to not react on Alt, Ctrl, etc
        repoTreeView.setOnKeyPressed(event -> {
            if (event.getCode().isLetterKey()) {
                Context.branchFilter.set(event.getText());
                localBranchesTreeItem.setExpanded(true);
                remoteBranchesTreeItem.setExpanded(true);
                tagsTreeItem.setExpanded(true);
                new MainTreeBranchSearchHandler(App.getStage().getX() + 150, App.getStage().getY() + 120).handle(null);

            }
        });
    }

    public void updateButtonUI() {
        ScmBranch scmBranch = Context.workingBranch.getValue();
        int aheadQty = scmBranch.getAheadCount();
        int behindQty = scmBranch.getBehindCount();
        aheadIconQty.setText(String.valueOf(aheadQty));
        behindIconQty.setText(String.valueOf(behindQty));
        aheadIcon.setVisible(aheadQty > 0);
        behindIcon.setVisible(behindQty > 0);
    }


    /**
     * Open repository.
     *
     * @param actionEvent event
     * @throws Exception
     */
    public void openHandler(ActionEvent actionEvent)  {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(App.getStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            try {
                Context.init(absPath);
            } catch (Exception e) {
                showResult("Error", "Cannot open repository " + absPath, Alert.AlertType.WARNING);
            }
        }
    }

    public void fetchHandler(ActionEvent actionEvent) {
        RemoteRepoParameters repoParameters = new RemoteRepoParameters();
        new FetchEventHandler(repoParameters, null).handle(actionEvent);
        final ScmBranch scmBranch = Context.workingBranch.get();
        updateButtonUI();
    }

    public void largeFileSupportHandler(ActionEvent actionEvent) throws IOException {
        new LfsSupportDialogEventHandler().handle(actionEvent);
    }

    public void remoteURLtHandler(ActionEvent actionEvent) throws IOException {
        String url = Context.getGitRepoService().getRepository().getConfig().getString("remote", "origin", "url");
        new RemoteUrlDialog("Remote URL", "Charge repository remote URL", url).showAndWait().ifPresent(
                nreUrl -> {
                    new SetRemoteUrlEventHandler(nreUrl).handle(actionEvent);
                }
        );
    }

    public void editRawAttrsHandler(ActionEvent actionEvent) {
        edit(Const.GIT_ATTR_NAME);
    }

    public void editRawIgnoreHandler(ActionEvent actionEvent) {
        edit(Const.GIT_IGNORE_NAME);
    }

    public void editRawConfigHandler(ActionEvent actionEvent) {
        edit(".git/config");
    }

    private void edit(String fileName) {
        ScmItem item = new ScmItem(fileName, null);
        OpenFileEventHandler handler = new OpenFileEventHandler(item, ScmItem.BODY_TYPE.WORK_SPACE);
        handler.setForceText(true);
        handler.setEditable(true);
        handler.setOverwrite(true);
        handler.handle(null);
    }


    public void cloneHandler(ActionEvent actionEvent) {
        CloneDialog dialog = new CloneDialog(
                "Repository",
                "Remote repository URL",
                Collections.EMPTY_LIST);
        dialog.setContentText("Please provide remote repository URL:");
        dialog.showAndWait().ifPresent(params -> {
                    new CloneEventHandler(params).handle(actionEvent);
                }
        );

    }

    public void dropIndexDataHandler(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setWidth(LookAndFeelSet.DIALOG_DEFAULT_WIDTH);
        alert.setTitle("Question");
        alert.setContentText("Would you like to drop history indexes for current project ?");
        alert.initOwner(App.getScene().getWindow());
        alert.showAndWait().ifPresent( r -> {
            if (r == ButtonType.OK) {
                SearchService service = new SearchService( Context.getProjectFolder() );
                service.dropIndex();
                Context.getCurrentProject().setIndexed(false);
                Context.saveSettings();
                dropIndexDataMenuItem.setDisable(true);

            }
        });

    }

    public void reindexDataHandler(ActionEvent actionEvent) {
        new IndexEventHandler().handle(actionEvent);
        dropIndexDataMenuItem.setDisable(!Context.getCurrentProject().isIndexed());
    }


    public void compressDataHandler(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Cleanup database");
        alert.setContentText("Do you really want to remove unused objects from database ? It may take some time.");
        alert.initOwner(App.getScene().getWindow());
        alert.showAndWait().ifPresent(dialogResult -> {
                    if (dialogResult == ButtonType.OK) {
                        new CompressDataBaseEventHandler().handle(actionEvent);
                    }
                }
        );

    }

    public void pullHandler(ActionEvent actionEvent) {
        final ScmBranch scmBranch = Context.workingBranch.get();
        new PullHandler(scmBranch).handle(actionEvent);
    }

    public void pushHandler(ActionEvent actionEvent) {
        final ScmBranch scmBranch = Context.workingBranch.get();
        new PushHandler(scmBranch).handle(actionEvent);
    }

    public void createRepositoryHandler(ActionEvent actionEvent) {

        new CreateDialog("Create", "Create new repository").showAndWait().ifPresent(
                rp -> {
                    try {
                        Context.getGitRepoService().createRepository(
                                rp.getDestinationFolder(),
                                rp.isInitWithReame(),
                                rp.isInitWithIgnore(),
                                rp.isInitWithLfs());
                        log.log(Level.INFO, "New repository was created  " + rp.getDestinationFolder());
                        Context.init(rp.getDestinationFolder());
                    } catch (Exception e) {
                        String msg = "Cannot create repository " + rp.getDestinationFolder();
                        log.log(Level.SEVERE, msg, e);
                        showResult(msg, ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);
                    }
                }
        );
    }


    public void openGitTerminalActionHandler(ActionEvent actionEvent) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec(
                    Context.isWindows() ? new String[]{"cmd", "/c", "start"} : new String[]{"open", "/bin/bash"},
                    null,
                    new File(
                            Context.getProjectFolder().isEmpty() ?
                                    System.getProperty("user.home") :
                                    Context.getProjectFolder()
                    )
            );
        } catch (Exception e) {
            String msg = "Cannot open GIT terminal";
            log.log(Level.WARNING, msg, e);
            showResult(msg, ExceptionUtils.getStackTrace(e), Alert.AlertType.ERROR);
        }
    }

    public void checkForUpdate(ActionEvent actionEvent) {

        App.getShell().showDocument("https://github.com/iazarny/gitember");

    }

    public void createBugReportHandler(ActionEvent actionEvent) {
        App.getShell().showDocument("https://github.com/iazarny/gitember/issues/new");
    }


    public void createStatReportBranches(ActionEvent actionEvent) throws IOException {
        new StatBranchLiveTimeHandler(mainTreeChangeListener, true).handle(actionEvent);
    }

    public void createStatReportBranchTime(ActionEvent actionEvent) throws IOException {
        new StatBranchLiveTimeHandler(mainTreeChangeListener).handle(actionEvent);
    }

    public void createStatReportProgress(ActionEvent actionEvent) throws IOException {
        new StatWorkProgressHandler(mainTreeChangeListener).handle(actionEvent);
    }

    public void createStatReport(ActionEvent actionEvent) throws IOException {
        new StarHandler(mainTreeChangeListener).handle(actionEvent);
    }


    public void aboutHandler(ActionEvent actionEvent) {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.showAndWait();
    }

    public void exitActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }


    public static Optional<ButtonType> showResult(final String title, final String text, final Alert.AlertType alertTypet) {
        return new ResultDialog(title, text, alertTypet).showAndWait();
    }

    public TreeView getMainTreeView() {
        return repoTreeView;
    }

    public TreeItem getStashRoot() {
        return stashesTreeItem;
    }

    public void setThemeLight(ActionEvent actionEvent) {
        Context.settingsProperty.get().setTheme("Light");
        confirmReboot();
    }

    public void setThemeDark(ActionEvent actionEvent) {
        Context.settingsProperty.get().setTheme("Dark");
        confirmReboot();
    }

    public void confirmReboot() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setWidth(LookAndFeelSet.DIALOG_DEFAULT_WIDTH);
        alert.setTitle("Information");
        alert.setContentText("Please restart application to apply theme change");
        alert.initOwner(App.getScene().getWindow());
        alert.showAndWait();

    }


}
