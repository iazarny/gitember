package com.az.gitember;

import com.az.gitember.misc.*;
import com.az.gitember.scm.impl.git.DefaultProgressMonitor;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jgit.api.errors.CannotDeleteCurrentBranchException;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */

public class FXMLController implements Initializable {

    private final Logger log = Logger.getLogger(FXMLController.class.getName());

    public Button cloneBtn;
    public Button fetchBtn;
    public Button pullBtn;
    public Button pushBtn;
    public Button openBtn;

    public Menu openRecentMenuItem;

    public MenuItem pushToRemoteLocalBranchMenuItem;
    public MenuItem fetchLocalBranchMenuItem;
    public MenuItem checkoutLocalBranchMenuItem;
    public MenuItem createLocalBranchMenuItem;
    public MenuItem mergeLocalBranchMenuItem;
    public MenuItem deleteLocalBranchMenuItem;


    public ContextMenu contextMenu;
    private List<MenuItemAvailbility> contextMenuItemVisibilityLTR;


    public ProgressBar operationProgressBar;
    public Label operationName;
    public ToolBar progressBar;


    public MenuItem openGitTerminalMenuItem;
    public MenuItem fetchMenuItem;
    public MenuItem fetchAllMenuItem;
    public MenuItem pullMenuItem;
    public MenuItem pullAllMenuItem;
    public MenuItem pushMenuItem;
    public MenuItem pushAllMenuItem;
    public MenuItem statReportMenuItem;
    public MenuItem checkoutMenuItem;
    public MenuItem mergeMenuItem;
    public MenuItem rebaseMenuItem;
    public MenuItem stashMenuItem;
    public MenuItem applyStashMenuItem;
    public MenuItem commitMenuItem;
    public MenuItem resetVersionMenuItem;

    public TreeItem workingCopyTreeItem;
    public TreeItem workSpaceTreeItem;
    public TreeItem historyTreeItem;
    public TreeItem stashesTreeItem;
    public TreeItem localBranchesTreeItem;
    public TreeItem tagsTreeItem;
    public TreeItem remoteBranchesTreeItem;
    public TreeView repoTreeView;


    @FXML
    private AnchorPane hostPanel;

    private String login = null;
    private String pwd = null;
    private Optional<Pair<String, String>> uiInputResultToService;
    private CountDownLatch uiInputLatchToService;
    private final Map<TreeItem, Consumer<Object>> treeItemClickHandlers = new HashMap<>();


    @SuppressWarnings("unchecked")
    public void openRepository(final String absPath) {
        try {
            GitemberApp.setCurrentRepositoryPath(absPath);
            GitemberApp.setRepositoryService(new GitRepositoryService(absPath));
            GitemberApp.getSettingsService().saveLastProject(absPath);

            List<ScmBranch> branches = GitemberApp.getRepositoryService().getLocalBranches();
            branches.addAll(GitemberApp.getRepositoryService().getRemoteBranches());
            branches.addAll(GitemberApp.getRepositoryService().getTags());

            GitemberApp.setWorkingBranch(
                    branches.stream().filter(ScmBranch::isHead).findFirst().orElse(null)
            );

            repoTreeView.setCellFactory(
                    new ScmItemCellFactory(contextMenu, this::validateContextMenu)
            );
            repoTreeView.getSelectionModel().selectedItemProperty().addListener(
                    (ob, o, n) -> treeItemClickHandlers.getOrDefault(n, z -> {
                    }).accept(n)
            );

            fillAllBranchTrees();

            treeItemClickHandlers.put(workingCopyTreeItem, o -> openWorkingCopyHandler(null));

            treeItemClickHandlers.put(historyTreeItem, o -> openBranchHistory(GitemberApp.workingBranch.get())); // <- todo incorrect here must be lazy call

            repoTreeView.setDisable(false);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot open repository", e);
        }
    }

    private void fillAllBranchTrees() throws Exception {
        fillBranchTree(GitemberApp.getRepositoryService().getLocalBranches(), localBranchesTreeItem);
        fillBranchTree(GitemberApp.getRepositoryService().getTags(), tagsTreeItem);
        fillBranchTree(GitemberApp.getRepositoryService().getRemoteBranches(), remoteBranchesTreeItem);
    }

    private void fillBranchTree(List<ScmBranch> branches, TreeItem<ScmBranch> treeItem) {
        cleanUp(treeItem);
        createTreeItemWithHandler(branches).entrySet().stream().forEach(
                (e) -> {
                    treeItem.getChildren().add(e.getKey());
                    treeItemClickHandlers.put(e.getKey(), e.getValue());
                }
        );
    }


    /**
     * Replace with something like
     * data.stream().collect(Collectors.toMap( b -> new TreeItem<>(b),  (branch) -> openBranchHistory(branch) ));
     *
     * @param data list of branches
     * @return map of tree item - handler
     */
    private Map<TreeItem, Consumer<Object>> createTreeItemWithHandler(List<ScmBranch> data) {
        Map<TreeItem, Consumer<Object>> rez = new LinkedHashMap<>();
        data.stream().forEach(
                branch -> rez.put(new TreeItem<>(branch), o -> openBranchHistory(branch))
        );
        return rez;
    }

    @SuppressWarnings("unchecked")
    private void cleanUp(TreeItem treeItem) {
        treeItem.getChildren().stream().forEach(treeItemClickHandlers::remove);
        treeItem.getChildren().removeAll(treeItem.getChildren());
    }


    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL url, ResourceBundle rb) {

        createLocalBranchContextMenu();

        createOpenRecentMenu();

        openGitTerminalMenuItem.setVisible(GitemberApp.getSettingsService().isWindows());

        GitemberApp.remoteUrl.addListener(
                (o, ov, nv) -> {
                    boolean disableRemoteOps = nv == null;
                    fetchMenuItem.setDisable(disableRemoteOps);
                    fetchAllMenuItem.setDisable(disableRemoteOps);
                    pullMenuItem.setDisable(disableRemoteOps);

                    pullAllMenuItem.setDisable(disableRemoteOps);
                    pushMenuItem.setDisable(disableRemoteOps);
                    pushAllMenuItem.setDisable(disableRemoteOps);
                    statReportMenuItem.setDisable(disableRemoteOps);
                    pushToRemoteLocalBranchMenuItem.setDisable(disableRemoteOps);
                    fetchLocalBranchMenuItem.setDisable(disableRemoteOps);
                }
        );

        GitemberApp.currentRepositoryPath.addListener(
                (o, ov, nv) -> {
                    boolean disable = nv == null;
                    statReportMenuItem.setDisable(disable);
                    checkoutMenuItem.setDisable(disable);
                    mergeMenuItem.setDisable(disable);
                    rebaseMenuItem.setDisable(disable);

                    stashMenuItem.setDisable(disable);
                    applyStashMenuItem.setDisable(disable);
                    commitMenuItem.setDisable(disable);
                    resetVersionMenuItem.setDisable(disable);

                }
        );

        GitemberApp.workingBranch.addListener(
                (observable, oldValue, newValue) -> {
                    boolean trackingRemoteBranch = newValue.getRemoteName() != null;
                    if (trackingRemoteBranch) {
                        String localBranch = GitemberApp.workingBranch.get().getShortName();
                        String remoteBranch = newValue.getRemoteName();
                        fetchMenuItem.setText("Fetch remote " + remoteBranch + " to " + localBranch);
                        pullMenuItem.setText("Pull remote " + remoteBranch + " to " + localBranch);
                        pushMenuItem.setText("Push " + localBranch + " remote to " + remoteBranch);

                        Tooltip tt = new Tooltip(
                                MessageFormat.format(
                                        "Branch {0} tracking remote {1}",
                                        newValue.getShortName(),
                                        newValue.getRemoteName()));
                        fetchBtn.setTooltip(tt);
                        pullBtn.setTooltip(tt);
                        pushBtn.setTooltip(tt);
                    } else {
                        fetchMenuItem.setText("Fetch");
                        pullMenuItem.setText("Pull");
                        pushMenuItem.setText("Push");

                    }

                    fetchMenuItem.setDisable(!trackingRemoteBranch);
                    pullMenuItem.setDisable(!trackingRemoteBranch);
                    pushMenuItem.setDisable(!trackingRemoteBranch);

                    fetchBtn.setDisable(!trackingRemoteBranch);
                    pullBtn.setDisable(!trackingRemoteBranch);
                    pushBtn.setDisable(!trackingRemoteBranch);

                }
        );


    }

    private void createOpenRecentMenu() {
        openRecentMenuItem.getItems().removeAll(openRecentMenuItem.getItems());
        GitemberApp.getSettingsService().getRecentProjects().stream().forEach(
                o -> {
                    MenuItem mi = new MenuItem(o.getFirst());
                    mi.setUserData(o.getSecond());
                    mi.setOnAction(
                            event -> {
                                GitemberApp.getMainStage().getScene().setCursor(Cursor.WAIT);
                                Platform.runLater(() -> {
                                    openRepository(o.getSecond());
                                    openWorkingCopyHandler(null);
                                    GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                                });
                            }
                    );
                    openRecentMenuItem.getItems().add(mi);
                }
        );
        openRecentMenuItem.setDisable(openRecentMenuItem.getItems().size() < 1);
    }

    /**
     * Disable or enable remote operations if local branch not tracking remote branch.
     *
     * @param scmBranch branch
     */
    public void validateContextMenu(final ScmBranch scmBranch) {
        if (scmBranch != null) {

            contextMenuItemVisibilityLTR.stream().forEach(
                    i -> i.getMenuItem().setVisible(isMenuItemVisibleForScmBranch(i, scmBranch))
            );

            if (ScmBranch.BranchType.LOCAL.equals(scmBranch.getBranchType())) {
                boolean disableRemoteOp = scmBranch.getRemoteName() == null;
                fetchLocalBranchMenuItem.setDisable(disableRemoteOp);
                if (disableRemoteOp) {
                    pushToRemoteLocalBranchMenuItem.setText("Push to ... ");
                } else {
                    fetchLocalBranchMenuItem.setText("Fetch remote " + scmBranch.getRemoteName());
                    pushToRemoteLocalBranchMenuItem.setText("Push to remote " + scmBranch.getRemoteName());
                }
                fetchLocalBranchMenuItem.setVisible(!disableRemoteOp);

            } else if (ScmBranch.BranchType.REMOTE.equals(scmBranch.getBranchType())) {
            } else if (ScmBranch.BranchType.TAG.equals(scmBranch.getBranchType())) {

            }
        }
    }

    private boolean isMenuItemVisibleForScmBranch(MenuItemAvailbility i, ScmBranch scmBranch) {
        if (ScmBranch.BranchType.LOCAL.equals(scmBranch.getBranchType())) {
            return i.isLocalVisible();
        } else if (ScmBranch.BranchType.TAG.equals(scmBranch.getBranchType())) {
            return i.isTagVisible();
        } else if (ScmBranch.BranchType.REMOTE.equals(scmBranch.getBranchType())) {
            return i.isRemoteVisible();
        }
        return false;
    }


    private ContextMenu createLocalBranchContextMenu() {

        checkoutLocalBranchMenuItem = new MenuItem("Checkout");
        checkoutLocalBranchMenuItem.setOnAction(this::localBranchCheckoutHandler);

        createLocalBranchMenuItem = new MenuItem("Create branch ...");
        createLocalBranchMenuItem.setOnAction(this::localBranchCreateHandler);

        mergeLocalBranchMenuItem = new MenuItem("Merge ...");
        mergeLocalBranchMenuItem.setOnAction(this::localBranchMergeHandler);

        fetchLocalBranchMenuItem = new MenuItem("Fetch");
        fetchLocalBranchMenuItem.setOnAction(this::fetchHandler);

        pushToRemoteLocalBranchMenuItem = new MenuItem("Push ...");
        pushToRemoteLocalBranchMenuItem.setOnAction(this::localBranchPushHandler);

        deleteLocalBranchMenuItem = new MenuItem("Delete ...");
        deleteLocalBranchMenuItem.setOnAction(this::localBranchDeleteHandler);

        contextMenu = new ContextMenu(
                checkoutLocalBranchMenuItem,
                createLocalBranchMenuItem,
                mergeLocalBranchMenuItem,
                fetchLocalBranchMenuItem,
                pushToRemoteLocalBranchMenuItem,
                new SeparatorMenuItem(),
                deleteLocalBranchMenuItem
        );

        contextMenuItemVisibilityLTR = new ArrayList<MenuItemAvailbility>() {{
            add(new MenuItemAvailbility(checkoutLocalBranchMenuItem, true, true, true));
            add(new MenuItemAvailbility(createLocalBranchMenuItem, true, true, true));
            add(new MenuItemAvailbility(mergeLocalBranchMenuItem, true, false, true));
            add(new MenuItemAvailbility(fetchLocalBranchMenuItem, true, false, true));
            add(new MenuItemAvailbility(pushToRemoteLocalBranchMenuItem, true, false, true));
            add(new MenuItemAvailbility(deleteLocalBranchMenuItem, true, true, true));

        }};

        return contextMenu;
    }

    public void openBranchHistory(final ScmBranch scmBranch) {
        try {
            if (scmBranch != null) {
                final FXMLLoader fxmlLoader = new FXMLLoader();
                try (InputStream is = getClass().getResource("/fxml/BranchViewPane.fxml").openStream()) {
                    final Parent branchView = fxmlLoader.load(is);
                    final BranchViewController branchViewController = fxmlLoader.getController();
                    branchViewController.setTreeName(scmBranch.getFullName());
                    branchViewController.open();
                    hostPanel.getChildren().removeAll(hostPanel.getChildren());
                    hostPanel.getChildren().add(branchView);
                }
            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot open branch view", e);
        }
    }


    private void pushToRemoteRepository(String localBranchName, String remoteBranchName, boolean setOrigin, boolean setTrackeRemote) {
        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> GitemberApp.getRepositoryService().remoteRepositoryPush(
                                localBranchName, remoteBranchName, login, pwd, setOrigin, setTrackeRemote,
                                new DefaultProgressMonitor((t, d) -> {
                                    updateTitle(t);
                                    updateProgress(d, 1.0);
                                }))
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();
    }

    private void prepareLongTask(final Task<RemoteOperationValue> longTask,
                                 final Consumer<RemoteOperationValue> onOk,
                                 final Consumer<RemoteOperationValue> onError) {


        progressBar.setVisible(true);
        operationProgressBar.progressProperty().bind(longTask.progressProperty());
        operationName.textProperty().bind(longTask.titleProperty());
        GitemberApp.getMainStage().getScene().setCursor(Cursor.WAIT);

        longTask.setOnSucceeded(val -> Platform.runLater(
                () -> {
                    RemoteOperationValue rval = longTask.getValue();
                    switch (rval.getResult()) {
                        case OK: {
                            if (onOk != null) {
                                onOk.accept(rval);
                            }
                            GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                            showResult("OK. TODO get info", Alert.AlertType.INFORMATION);
                            break;
                        }
                        case ERROR: {
                            if (onError != null) {
                                onError.accept(rval);
                            }
                            GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                            showResult("ERROR. TODO get info", Alert.AlertType.ERROR);
                            break;
                        }
                    }
                    operationProgressBar.progressProperty().unbind();
                    operationName.textProperty().unbind();
                    progressBar.setVisible(false);
                }
        ));
    }


    public void openWorkingCopyHandler(Object param) {

        GitemberApp.getMainStage().getScene().setCursor(Cursor.WAIT);

        Platform.runLater(
                () ->
                {
                    final FXMLLoader fxmlLoader = new FXMLLoader();
                    try (InputStream is = getClass().getResource("/fxml/WorkingCopyPane.fxml").openStream()) {
                        final Parent workCopyView = fxmlLoader.load(is);
                        final WorkingCopyController workingCopyController = fxmlLoader.getController();
                        workingCopyController.open();
                        hostPanel.getChildren().removeAll(hostPanel.getChildren());
                        hostPanel.getChildren().add(workCopyView);
                    } catch (IOException ioe) {
                        log.log(Level.SEVERE, "Cannot open working copy view", ioe.getMessage());
                    } finally {
                        GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);

                    }

                }
        );


    }


//--------------------------------------------------------------------------------------------------------------------


    /**
     * Process remote operations with failback
     *
     * @param supplier remote repository command.
     * @return RemoteOperationValue which shall be interpret by caller
     */
    private RemoteOperationValue remoteRepositoryOperation(final Supplier<RemoteOperationValue> supplier) {
        boolean ok = false;
        RemoteOperationValue operationValue = null;
        Settings settings = GitemberApp.getSettingsService().read();


        while (!ok) {
            uiInputResultToService = null;
            operationValue = supplier.get();
            switch (operationValue.getResult()) {
                case AUTH_REQUIRED: {
                    uiInputLatchToService = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        if (settings.isRememberPasswords()) {
                            Triplet<String, String, String> loginPwd = GitemberApp.getSettingsService()
                                    .getLoginAndPassword(GitemberApp.remoteUrl.get());
                            login = loginPwd.getSecond();
                            pwd = loginPwd.getThird();

                        } else {
                            login = GitemberApp.getSettingsService().getLastLoginName();
                            pwd = null;
                        }

                        uiInputResultToService = new LoginDialog("Login", "Please, provide login and password", login, pwd)
                                .showAndWait();
                        uiInputLatchToService.countDown();
                    });
                    try {
                        uiInputLatchToService.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case NOT_AUTHORIZED: {
                    uiInputLatchToService = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        uiInputResultToService = new LoginDialog("Login", "Not authorized. Provide correct credentials", login, pwd)
                                .showAndWait();
                        uiInputLatchToService.countDown();
                    });
                    try {
                        uiInputLatchToService.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                default: {
                    //ATM we have ERROR and OK, which shall be handled
                    ok = true;
                }
            }
            if (uiInputResultToService != null && uiInputResultToService.isPresent()) {
                login = uiInputResultToService.get().getFirst();
                pwd = uiInputResultToService.get().getSecond();
                if (settings.isRememberPasswords()) {
                    GitemberApp.getSettingsService().saveLoginAndPassword(GitemberApp.remoteUrl.get(), login, pwd);
                } else {
                    GitemberApp.getSettingsService().saveLastLoginName(login);
                }
                continue;
            } else {
                ok = true;
            }
            uiInputResultToService = null;
        }

        login = pwd = null;

        return operationValue;

    }


    private void showResult(String text, Alert.AlertType alertTypet) {
        Alert alert = new Alert(alertTypet);
        alert.setWidth(600); //TODO width
        alert.setTitle("Operation result");
        //alert.setHeaderText("Result of pull operation");
        alert.setContentText(text);
        alert.showAndWait();
    }


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Local branch context menu item handlers---------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//


    @SuppressWarnings("unchecked")
    public void localBranchCheckoutHandler(ActionEvent actionEvent) {
        GitemberApp.getMainStage().getScene().setCursor(Cursor.WAIT);
        ScmBranch scmBranch = getScmBranch();
        Platform.runLater(
                () -> {
                    try {
                        GitemberApp.getRepositoryService().checkoutLocalBranch(scmBranch.getFullName());
                        GitemberApp.setWorkingBranch(scmBranch);
                        fillAllBranchTrees();
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Cannot checkout branch " + scmBranch.getShortName(), e);
                    } finally {
                        GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);

                    }

                }
        );
    }


    @SuppressWarnings("unchecked")
    public void localBranchCreateHandler(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("New branch");
        dialog.setHeaderText("Create new branch");
        dialog.setContentText("Please enter new branch name:");

        Optional<String> dialogResult = dialog.showAndWait();

        if (dialogResult.isPresent()) {
            ScmBranch scmBranch = getScmBranch();
            try {
                GitemberApp.getRepositoryService().createLocalBranch(
                        scmBranch.getFullName(),
                        dialogResult.get());
                fillAllBranchTrees();

            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot create new local branch " + scmBranch, e);
            }
        }


    }


    @SuppressWarnings("unchecked")
    public void localBranchMergeHandler(ActionEvent actionEvent) {

        ScmBranch scmBranch = getScmBranch();

        TextAreaInputDialog dialog = new TextAreaInputDialog(
                "Merge " + scmBranch.getFullName() + " to " + GitemberApp.workingBranch.get().getShortName()
        );

        dialog.setTitle("Merge message");
        dialog.setHeaderText("Provide merge message");
        dialog.setContentText("Message:");
        Optional<String> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            try {
                GitemberApp.getRepositoryService().mergeLocalBranch(
                        scmBranch.getFullName(),
                        dialogResult.get()
                );

                fillBranchTree(GitemberApp.getRepositoryService().getLocalBranches(), localBranchesTreeItem);

            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot merge local branch " + scmBranch.getFullName()
                        + " to " + GitemberApp.workingBranch.get().getShortName(), e);

            }

        }


    }

    /**
     * Push selected branch.
     * New remote branch will be created if it not tracked some remote branch.
     *
     * @param actionEvent event
     */
    public void localBranchPushHandler(ActionEvent actionEvent) {
        ScmBranch scmBranch = getScmBranch();
        if (scmBranch.getRemoteName() == null) {
            TextInputDialog dialog = new TextInputDialog(scmBranch.getShortName());
            dialog.setTitle("Branch name");
            dialog.setHeaderText("Remote branch will be created");
            dialog.setContentText("Please enter new remote branch name:");

            Optional<String> dialogResult = dialog.showAndWait();
            if (dialogResult.isPresent()) {
                scmBranch.setRemoteName(dialogResult.get());
                pushToRemoteRepository(scmBranch.getShortName(), scmBranch.getRemoteName(), true, true);
                try {
                    fillBranchTree(GitemberApp.getRepositoryService().getLocalBranches(), localBranchesTreeItem);
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Cannot load list of local branches after push " + scmBranch, e);
                }
            }
        } else {
            pushToRemoteRepository(scmBranch.getShortName(), scmBranch.getRemoteName(), true, false);
        }
    }

    @SuppressWarnings("unchecked")
    public void localBranchDeleteHandler(ActionEvent actionEvent) {

        final ScmBranch scmBranch = getScmBranch();


        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Please confirm");
        alert.setHeaderText(String.format("Delete %s %s", scmBranch.getBranchType().getTypeName(), scmBranch.getShortName()));
        alert.setContentText(String.format("Do you really want to delete %s %s ?", scmBranch.getBranchType().getTypeName(), scmBranch.getShortName()));

        Optional<ButtonType> dialogResult = alert.showAndWait();
        if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
            try {
                GitemberApp.getRepositoryService().deleteLocalBranch(scmBranch.getFullName());
                fillAllBranchTrees();
            } catch (CannotDeleteCurrentBranchException e) {
                showResult(e.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot delete local branch " + scmBranch.getFullName(), e);

            }
        }

    }

    private ScmBranch getScmBranch() {
        TreeItem<ScmBranch> item = (TreeItem<ScmBranch>) repoTreeView.getSelectionModel().getSelectedItem();
        return item == null ? null : item.getValue();
    }


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Menu file handlers -----------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//

    /**
     * Open repository.
     *
     * @param actionEvent event
     * @throws Exception
     */
    public void openHandler(ActionEvent actionEvent) throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (GitemberApp.getSettingsService().getLastProject() != null) {
            directoryChooser.setInitialDirectory(new File(GitemberApp.getSettingsService().getLastProject()));
        }
        File selectedDirectory =
                directoryChooser.showDialog(GitemberApp.getMainStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            if (!absPath.endsWith(Const.GIT_FOLDER)) {
                absPath += File.separator + Const.GIT_FOLDER;
            }
            openRepository(absPath);
            openWorkingCopyHandler(null);
        }

        createOpenRecentMenu();
    }

    /**
     * Open GUI shell in home or in repository folder, if it was opened.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void openShellActionHandler(ActionEvent actionEvent) {
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(
                            Paths.get(
                                    ObjectUtils.defaultIfNull(
                                            GitemberApp.getCurrentRepositoryPathWOGit(),
                                            GitemberApp.getSettingsService().getUserHomeFolder()
                                    )
                            ).toUri()
                    );
                } catch (Exception e) {
                    String msg = "Cannot open GUI shell";
                    log.log(Level.WARNING, msg, e);
                    showResult(msg, Alert.AlertType.ERROR);
                }
            }).start();
        }

    }

    /**
     * Open terminal in home or in repository folder, if it was opened.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void openGitTerminalActionHandler(ActionEvent actionEvent) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec(
                    new String[]{"cmd", "/c", "start"/*,  "\"C:\\Program Files\\Git\\bin\\sh.exe\" --login"*/},
                    null,
                    new File(
                            ObjectUtils.defaultIfNull(
                                    GitemberApp.getCurrentRepositoryPathWOGit(),
                                    GitemberApp.getSettingsService().getUserHomeFolder()
                            )
                    )
            );
        } catch (Exception e) {
            String msg = "Cannot open GIT terminal";
            log.log(Level.WARNING, msg, e);
            showResult(msg, Alert.AlertType.ERROR);
        }
    }

    /**
     * Open settings dialog .
     *
     * @param actionEvent action event
     */
    @SuppressWarnings("unused")
    public void settingsActionHandler(ActionEvent actionEvent) {

        Settings settings = GitemberApp.getSettingsService().read();
        SettingsDialog settingsDialog = new SettingsDialog(new SettingsModel(settings));
        Optional<SettingsModel> model = settingsDialog.showAndWait();
        if (model.isPresent()) {
            GitemberApp.getSettingsService().save(model.get().createSettings());
        }

    }


    /**
     * Close application.
     *
     * @param actionEvent action event
     */
    @SuppressWarnings("unused")
    public void exitActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }

    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Repository menu handlers -----------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//

    /**
     * Clone and open remote repository.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void cloneHandler(ActionEvent actionEvent) {
        CloneDialog dialog = new CloneDialog("Repository", "Remote repository URL"); // TODO history of repositories
        dialog.setContentText("Please provide remote repository URL:");
        Optional<Pair<String, String>> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            GitemberApp.remoteUrl.setValue(dialogResult.get().getFirst());
            Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
                @Override
                protected RemoteOperationValue call() throws Exception {
                    return remoteRepositoryOperation(
                            () -> GitemberApp.getRepositoryService().cloneRepository(
                                    dialogResult.get().getFirst(),
                                    dialogResult.get().getSecond(),
                                    login,
                                    pwd,
                                    new DefaultProgressMonitor((t, d) -> {
                                        updateTitle(t);
                                        updateProgress(d, 1.0);
                                    })
                            )
                    );
                }
            };
            prepareLongTask(longTask,
                    remoteOperationValue -> {
                        openRepository((String) remoteOperationValue.getValue());
                        openWorkingCopyHandler(null);
                    }, null);
            new Thread(longTask).start();
        }
    }

    /**
     * Create new git repository.
     *
     * @param actionEvent action event
     */
    @SuppressWarnings("unused")
    public void createRepositoryHandler(ActionEvent actionEvent) throws Exception {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(GitemberApp.getSettingsService().getUserHomeFolder()));
        final File selectedDirectory =
                directoryChooser.showDialog(GitemberApp.getMainStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            GitemberApp.getRepositoryService().createRepository(absPath);

            openRepository(absPath + File.separator + Const.GIT_FOLDER);
            openWorkingCopyHandler(null);
        }

    }

    /**
     * Fetch all.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void fetchHandlerAll(ActionEvent actionEvent) {
        fetch(null);
    }

    /**
     * Fetch particular branch, if it track some remote.
     *
     * @param actionEvent event
     */
    public void fetchHandler(ActionEvent actionEvent) {
        fetch(GitemberApp.workingBranch.get().getRemoteName());
    }

    private void fetch(final String branchName) {
        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> GitemberApp.getRepositoryService().remoteRepositoryFetch(
                                branchName, login, pwd,
                                new DefaultProgressMonitor((t, d) -> {
                                    updateTitle(t);
                                    updateProgress(d, 1.0);
                                }))
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();
    }

    @SuppressWarnings("unused")
    public void pullHandler(ActionEvent actionEvent) {
        String branchName = GitemberApp.workingBranch.get().getRemoteName();
        pull(branchName);
    }

    @SuppressWarnings("unused")
    public void pullAllHandler(ActionEvent actionEvent) {
        pull(null);
    }

    private void pull(final String branchName) {
        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> GitemberApp.getRepositoryService().remoteRepositoryPull(
                                branchName, login, pwd,
                                new DefaultProgressMonitor((t, d) -> {
                                    updateTitle(t);
                                    updateProgress(d, 1.0);
                                }))
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();
    }


    /**
     * Push all branches.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void pushAllHandler(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Push all");
        alert.setContentText("Do you really want to push all branches ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            pushToRemoteRepository("+refs/heads/*", "refs/heads/*", false, false);
        }
    }

}
