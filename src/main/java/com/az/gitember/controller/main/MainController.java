package com.az.gitember.controller.main;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.controller.handlers.*;
import com.az.gitember.data.*;
import com.az.gitember.dialog.*;
import com.az.gitember.os.IconFactory;
import com.az.gitember.os.ProxyIconFactory;
import com.az.gitember.service.Context;
import com.az.gitember.service.SearchService;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import javafx.scene.input.MouseEvent;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ConfigConstants;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController implements Initializable {

    private final static Logger log = Logger.getLogger(MainController.class.getName());

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
    public ComboBox<Project> projectsCmb;
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
    public Menu branchMenu;
    public Button mergeBtn;
    public Button rebaseBtn;
    public Button commitBtn;
    public BorderPane mainBorderPane;
    public ImageView macCloseImgView;
    public ImageView macMinimizeImgView;
    public ImageView macMaximizeImgView;
    public HBox winControlBar;
    public HBox toolBarContainer;
    public HBox menuContainer;
    public VBox mainPaneTop;
    public Button branchBtn;
    public VBox infoVBox;
    public ScrollPane infoScrollPane;

    private double xOffset = 0;
    private double yOffset = 0;

    private IconFactory factory = new ProxyIconFactory();


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
            mainMenuBar.setStyle("");
            menuContainer.getChildren().remove(mainMenuBar);
            mainPaneTop.getChildren().add(mainMenuBar);
            mainPaneTop.getChildren().remove(menuContainer);

        } else if (Context.isWindows() || Context.isLinux()) {
            mainBorderPane.getChildren().remove(mainMenuBar);
            toolBarContainer.getChildren().remove(winControlBar);
            menuContainer.getChildren().add(winControlBar);
            winControlBar.getStyleClass().clear();
            winControlBar.getStyleClass().addAll(mainMenuBar.getStyleClass());
            winControlBar.setSpacing(1);
            winControlBar.setStyle("-fx-background-color:transparent; -fx-padding: 10 0 0 20;");
            if (Context.isLinux()) {
                winControlBar.setStyle("-fx-background-color:transparent; -fx-padding: 10 10 0 20;");
            }
            winControlBar.getChildren().remove(macMaximizeImgView);
            winControlBar.getChildren().add(macMaximizeImgView);
            winControlBar.getChildren().remove(macCloseImgView);
            winControlBar.getChildren().add(macCloseImgView);
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
                    branchMenu.setVisible(true);
                    mergeBtn.setDisable(false);
                    branchBtn.setDisable(false);
                    commitBtn.setDisable(false);
                    updateButtonUI();
                }
        );

        Context.statusList.addListener(
                (InvalidationListener) observable -> {
                    Platform.runLater(
                            () -> {
                                fillInfo();
                            }
                    );
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
                    compressDataMenuItem.setDisable(newValue == null);
                    reindexDataMenuItem.setDisable(newValue == null);
                    repoSettingsMenuItem.setDisable(false);
                    statReportMenu.setDisable(newValue == null);
                    statReportMenu.setVisible(newValue != null);
                    repoTreeView.setDisable(false);
                    infoScrollPane.setVisible(true);
                    boolean attrFileExists = Context.getGitRepoService().isFileExists(Const.GIT_ATTR_NAME);
                    boolean ignoreFileExists = Context.getGitRepoService().isFileExists(Const.GIT_IGNORE_NAME);
                    editRawIgnoreMenuItem.setVisible(ignoreFileExists);
                    editRawAttrsMenuItem.setVisible(attrFileExists);
                    Context.getCurrentProject().ifPresent(
                            p -> {
                                dropIndexDataMenuItem.setDisable(!p.isIndexed());
                            }
                    );
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
            projectsCmb.getItems().clear();
            projectsCmb.getItems().addAll(Context.settingsProperty.getValue().getProjects());
            Context.settingsProperty.get().getProjects().stream().forEach(
                    o -> {
                        MenuItem mi = new MenuItem(o.getProjectHomeFolder().replace(".git", ""));
                        mi.setOnAction(
                                event -> {
                                    try {
                                        //Context.init(o.getProjectHomeFolder(), getClass().getName() + "#openRecent");
                                        projectsCmb.getSelectionModel().select(o);
                                    } catch (Exception e) {
                                        Context.settingsProperty.getValue().getProjects().remove(o);
                                        Context.saveSettings();
                                        Context.readSettings();
                                        Context.getMain().showResult("Cannot load project ",
                                                "Cannot load project " + o.getProjectHomeFolder()
                                                        + "\n   It will be removed from the list of recent projects",
                                                Alert.AlertType.WARNING);

                                        log.log(Level.WARNING, "Cannot load project {0}. {1}", new String[]{o.getProjectHomeFolder(), e.getMessage()});
                                    }

                                }
                        );
                        openRecentMenuItem.getItems().add(mi);
                    }
            );
            openRecentMenuItem.setDisable(Context.settingsProperty.get().getProjects().isEmpty());
        });

        Context.settingsProperty.addListener(observable -> {
            boolean visible = Context.settingsProperty.getValue().getProjects().size() > 1;
            projectsCmb.setPromptText(visible ? "Select a project" : "");
            projectsCmb.setMaxWidth(visible ? Region.USE_COMPUTED_SIZE : 1);
            projectsCmb.setPrefWidth(visible ? Region.USE_COMPUTED_SIZE : 1);
            projectsCmb.setVisible(visible);
            projectsCmb.getItems().clear();
            projectsCmb.getItems().addAll(Context.settingsProperty.getValue().getProjects());
        });

        projectsCmb.setCellFactory(new ProjectCellFactory());
        projectsCmb.setConverter(new ProjectConverter());
        projectsCmb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Project>() {
            @Override
            public void changed(ObservableValue<? extends Project> observableValue,
                                Project oldProject, Project newProject) {
                try {
                    if (newProject != null) {
                        Context.init(newProject.getProjectHomeFolder());
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
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

        onFocusSubscribe();
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


    /**
     * Open repository.
     *
     * @param actionEvent event
     * @throws Exception
     */
    public void openHandler(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory =
                directoryChooser.showDialog(App.getStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            try {

                Project project = new Project();
                project.setOpenTime(new Date());
                project.setProjectHomeFolder(absPath);

                Context.settingsProperty.get().getProjects().add(project);
                Context.saveSettings();
                Context.readSettings();

                Context.settingsProperty.get().getProjects().stream()
                        .filter(p -> p.getProjectHomeFolder().equalsIgnoreCase(absPath))
                        .findFirst()
                        .ifPresent(
                                p -> Context.getMain().projectsCmb.getSelectionModel().select(p)
                        );

                Context.init(absPath);
            } catch (Exception e) {
                showResult("Error", "Cannot open repository " + absPath, Alert.AlertType.WARNING);
            }
        }
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
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                SearchService service = new SearchService(Context.getProjectFolder());
                service.dropIndex();
                Context.getCurrentProject().get().setIndexed(false);
                Context.saveSettings();
                dropIndexDataMenuItem.setDisable(true);

            }
        });

    }

    public void reindexDataHandler(ActionEvent actionEvent) {
        new IndexEventHandler().handle(actionEvent);
        Context.getCurrentProject().ifPresent(
                p -> {
                    dropIndexDataMenuItem.setDisable(!p.isIndexed());
                }
        );
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

    public void checkoutEventHandler(ActionEvent actionEvent) {
        new CheckoutEventHandler().handle(actionEvent);
        Context.updateAll();
        Context.updateWorkingBranch();
    }

    public void branchEventHandler(ActionEvent actionEvent) {
        final ScmBranch scmWorkingBranch = Context.workingBranch.getValue();
        new CreateBranchEventHandler (scmWorkingBranch.getFullName()).handle(actionEvent);
        Context.updateWorkingBranch();
    }

    public void mergeEventHandler(ActionEvent actionEvent) {
        new MergeBranchEventHandler(null).handle(actionEvent);
        Context.updateWorkingBranch();
    }

    public void rebaseEventHandler(ActionEvent actionEvent) {
        new RebaseBranchEventHandler(null).handle(actionEvent);
        Context.updateWorkingBranch();
    }

    public void commitEventHandler(ActionEvent actionEvent) {

        final Project proj = Context.getCurrentProject().get();
        final Config gitConfig = Context.getGitRepoService().getRepository().getConfig();
        final String cfgCommitName = gitConfig.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME);
        final String cfgCommitEmail = gitConfig.getString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_EMAIL);
        String commitName = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(proj.getUserCommitName(), cfgCommitName), proj.getUserName());
        String commitEmail = StringUtils.defaultIfBlank(StringUtils.defaultIfBlank(proj.getUserCommitEmail(), cfgCommitEmail), "");

        CommitDialog dialog = new CommitDialog(
                "",
                commitName,
                commitEmail,
                false,
                Collections.EMPTY_LIST

        );
        dialog.showAndWait().ifPresent(r -> {
            if (!commitEmail.equals(dialog.getUserName()) && !commitEmail.equalsIgnoreCase(dialog.getUserEmail())) {
                proj.setUserCommitName(dialog.getUserName());
                proj.setUserCommitEmail(dialog.getUserEmail());
                Context.saveSettings();
            }
            try {
                Context.settingsProperty.getValue().getCommitMsg().add(r);
                Context.getGitRepoService().commit(r, dialog.getUserName(), dialog.getUserEmail());
                new StatusUpdateEventHandler(true).handle(null);
                Context.updateBranches();
            } catch (GitAPIException e) {
                Context.getMain().showResult("Commit error", e.getMessage(), Alert.AlertType.ERROR);
            }
        });
        Context.updateWorkingBranch();
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
        String [] cmd;
        if (Context.isWindows()) {
            cmd  = new String[] {"cmd", "/c", "start"};
        } else if (Context.isMac()) {

            cmd = new String[]{
                    "osascript",
                    "-e",
                    "tell application \"Terminal\" to activate", // Bring Terminal to the foreground
                    "-e",
                    "tell application \"Terminal\" to do script \"zsh\"" // Start zsh shell
            };
        } else {
            cmd = new String[]{"open", "/bin/bash"};
        }

        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec(
                    cmd,
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


    public void reopenProject(ActionEvent actionEvent) {
    }

    public void mainBarMousePressed(MouseEvent mouseEvent) {
        // Add dragging functionality
        xOffset = mouseEvent.getSceneX();
        yOffset = mouseEvent.getSceneY();
    }

    public void mainBarMouseDragged(MouseEvent mouseEvent) {
        App.getStage().setX(mouseEvent.getScreenX() - xOffset);
        App.getStage().setY(mouseEvent.getScreenY() - yOffset);
    }

    public void mainBarMouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            javafx.stage.Stage stage = App.getStage();
            stage.setMaximized(!stage.isMaximized());
            winIconMouseExit(mouseEvent);
        }
    }


    public void closeHandler(MouseEvent mouseEvent) {
        Platform.exit();
    }

    public void minimizeHandler(MouseEvent mouseEvent) {
        javafx.stage.Stage stage = App.getStage();
        stage.setIconified(!stage.isIconified());
    }

    public void maximizeHandler(MouseEvent mouseEvent) {
        javafx.stage.Stage stage = App.getStage();
        if (Context.isMac()) {
            stage.setFullScreen(!stage.isFullScreen());
        } else if (Context.isWindows()) {
            stage.setMaximized(!stage.isMaximized());
        }

    }

    private void onFocusSubscribe() {
        App.getIsFocused().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                macCloseImgView.setImage(factory.createImage(IconFactory.WinIconType.CLOSE, IconFactory.WinIconMode.NORMAL, getTheme(), App.getStage().isMaximized()));
                macMinimizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MINIMIZE, IconFactory.WinIconMode.NORMAL, getTheme(), App.getStage().isMaximized()));
                macMaximizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MAXIMIZE, IconFactory.WinIconMode.NORMAL, getTheme(), App.getStage().isMaximized()));
            } else {
                macCloseImgView.setImage(factory.createImage(IconFactory.WinIconType.CLOSE, IconFactory.WinIconMode.INACTIVE, getTheme(), App.getStage().isMaximized()));
                macMinimizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MINIMIZE, IconFactory.WinIconMode.INACTIVE, getTheme(), App.getStage().isMaximized()));
                macMaximizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MAXIMIZE, IconFactory.WinIconMode.INACTIVE, getTheme(), App.getStage().isMaximized()));
            }
        });
    }

    public void winIconMouseEnter(MouseEvent mouseEvent) {
        if (Context.isWindows() || Context.isLinux()) {
            if (mouseEvent.getTarget() == macCloseImgView) {
                macCloseImgView.setImage(factory.createImage(IconFactory.WinIconType.CLOSE, IconFactory.WinIconMode.HOVER, getTheme(), App.getStage().isMaximized()));
            } else if (mouseEvent.getTarget() == macMinimizeImgView) {
                macMinimizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MINIMIZE, IconFactory.WinIconMode.HOVER, getTheme(), App.getStage().isMaximized()));
            } else if (mouseEvent.getTarget() == macMaximizeImgView) {
                macMaximizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MAXIMIZE, IconFactory.WinIconMode.HOVER, getTheme(), App.getStage().isMaximized()));
            }
        } else {
            macCloseImgView.setImage(factory.createImage(IconFactory.WinIconType.CLOSE, IconFactory.WinIconMode.HOVER, getTheme(), App.getStage().isMaximized()));
            macMinimizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MINIMIZE, IconFactory.WinIconMode.HOVER, getTheme(), App.getStage().isMaximized()));
            macMaximizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MAXIMIZE, IconFactory.WinIconMode.HOVER, getTheme(), App.getStage().isMaximized()));
        }

    }

    public void winIconMouseExit(MouseEvent mouseEvent) {
        if (App.getStage().isFocused()) {
            macCloseImgView.setImage(factory.createImage(IconFactory.WinIconType.CLOSE, IconFactory.WinIconMode.NORMAL, getTheme(), App.getStage().isMaximized()));
            macMinimizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MINIMIZE, IconFactory.WinIconMode.NORMAL, getTheme(), App.getStage().isMaximized()));
            macMaximizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MAXIMIZE, IconFactory.WinIconMode.NORMAL, getTheme(), App.getStage().isMaximized()));
        } else {
            macCloseImgView.setImage(factory.createImage(IconFactory.WinIconType.CLOSE, IconFactory.WinIconMode.INACTIVE, getTheme(), App.getStage().isMaximized()));
            macMinimizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MINIMIZE, IconFactory.WinIconMode.INACTIVE, getTheme(), App.getStage().isMaximized()));
            macMaximizeImgView.setImage(factory.createImage(IconFactory.WinIconType.MAXIMIZE, IconFactory.WinIconMode.INACTIVE, getTheme(), App.getStage().isMaximized()));

        }
    }

    private IconFactory.Theme getTheme() {
        if ("Dark".equalsIgnoreCase(Context.settingsProperty.get().getTheme())) {
            return IconFactory.Theme.DARK;

        }
        return IconFactory.Theme.WHITE;
    }

    public void keyPressedHandler(KeyEvent keyEvent) {
        //System.out.println(keyEvent);
    }

    public void fillInfo() {

        infoVBox.getChildren().clear();
        if (Context.workingBranch.getValue() != null) {
            Label br = new Label(Context.workingBranch.getValue().getNameExt());
            br.setStyle(LookAndFeelSet.INFO_LABEL);
            infoVBox.getChildren().add(br);
        }
        Map<String, AtomicInteger> statuses = new TreeMap<>();
        Context.statusList.forEach( scmItem -> {
            String status = scmItem.getAttribute().getStatus();
            statuses.computeIfAbsent(status, (k) ->  new AtomicInteger()).incrementAndGet();
        });


        HBox hb = null;
        int idx = 0 ;
        for (String st : statuses.keySet()) {
            if (idx%3 == 0) {
                hb = new HBox();
                hb.setStyle("-fx-border-color: transparent; -fx-border-width: 1px;");

                infoVBox.getChildren().add(hb);
            }
            AtomicInteger ai = statuses.get(st);
            if (ai.get() > 0) {
                Label lb = new Label(String.format("%d %s", ai.get(), st));
                lb.setStyle(LookAndFeelSet.INFO_LABEL);
                hb.getChildren().add(lb);
            }
            idx++;
        }

        Context.getCurrentProject().ifPresent(
                pr -> {
                    if (pr.isIndexed() || Context.lfsRepo.getValue()) {
                        HBox idxHBox = new HBox();
                        idxHBox.setStyle("-fx-border-color: transparent; -fx-border-width: 1px;");
                        if (pr.isIndexed()) {
                            Label idxLbl = new Label("Indexed");
                            idxLbl.setStyle(LookAndFeelSet.INFO_LABEL);
                            idxHBox.getChildren().add(idxLbl);
                        }
                        if (Context.lfsRepo.getValue()) {
                            Label lfsLbl = new Label("LFS");
                            lfsLbl.setStyle(LookAndFeelSet.INFO_LABEL);
                            idxHBox.getChildren().add(lfsLbl);
                        }

                        infoVBox.getChildren().add(idxHBox);
                    }
                }
        );
    }
}
