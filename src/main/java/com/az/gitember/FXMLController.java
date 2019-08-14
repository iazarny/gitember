package com.az.gitember;

import com.az.gitember.misc.*;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.ObjectUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */

public class FXMLController implements Initializable {

    private final static Logger log = Logger.getLogger(FXMLController.class.getName());

    public Button cloneBtn;
    public Button fetchBtn;
    public Button pullBtn;
    public Button pushBtn;
    public Button openBtn;

    public Menu openRecentMenuItem;

    public MenuItem pushToRemoteLocalBranchMenuItem;
    public MenuItem fetchLocalBranchMenuItem;
    public MenuItem pullLocalBranchMenuItem;
    public MenuItem checkoutRemoteBranchMenuItem;
    public MenuItem checkoutLocalBranchMenuItem;
    public MenuItem createLocalBranchMenuItem;
    public MenuItem mergeLocalBranchMenuItem;
    public MenuItem deleteLocalBranchMenuItem;

    public MenuItem deleteStashCtxMenuItem;
    public MenuItem applyStashCtxMenuItem;


    public MenuItem createTagMenuItem;

    public ContextMenu tagContextMenu;
    public ContextMenu contextMenu;
    public MenuBar mainMenuBar;
    public ToolBar mainToolBar;

    private List<MenuItemAvailbility> contextMenuItemVisibilityLTR;

    public ProgressBar operationProgressBar;
    public Label operationName;
    public ToolBar progressBar;

    public MenuItem openGitTerminalMenuItem;
    public MenuItem fetchMenuItem;
    public MenuItem pullMenuItem;
    public MenuItem settingsMenuItem;
    public MenuItem fetchAllMenuItem;

    public MenuItem pullAllMenuItem;
    public MenuItem pushMenuItem;
    public MenuItem pushAllMenuItem;
    public MenuItem compressDataMenuItem;
    public MenuItem statReportMenuItem;

    public TreeItem workingCopyTreeItem;
    public TreeItem workSpaceTreeItem;
    public TreeItem historyTreeItem;
    public TreeItem historyTreeItemAll;
    public TreeItem stashesTreeItem;
    public TreeItem localBranchesTreeItem;
    public TreeItem tagsTreeItem;
    public TreeItem remoteBranchesTreeItem;
    public TreeView repoTreeView;


    @FXML
    private AnchorPane hostPanel;


    private final Map<TreeItem, Consumer<Object>> treeItemClickHandlers = new HashMap<>();
    private List<ScmRevisionInformation> stashInfo;


    @SuppressWarnings("unchecked")
    public void openRepository(final String absPath) {
        try {

            GitemberApp.currentRepositoryPath.setValue(absPath);

            GitemberApp.repositoryService = new GitRepositoryService(absPath);
            //todo it it right place ?
            GitemberApp.remoteUrl.setValue(GitemberApp.getSettingsService().getRemoteUrlFromStoredRepoConfig());

            List<ScmBranch> branches = GitemberApp.getRepositoryService().getLocalBranches();
            branches.addAll(GitemberApp.getRepositoryService().getRemoteBranches());
            branches.addAll(GitemberApp.getRepositoryService().getTags());

            GitemberApp.setWorkingBranch(
                    branches.stream().filter(ScmBranch::isHead).findFirst().orElse(null)
            );

            repoTreeView.setCellFactory(
                    new ScmItemCellFactory(contextMenu, tagContextMenu, this::validateContextMenu)
            );
            repoTreeView.getSelectionModel().selectedItemProperty().addListener(
                    (ob, o, n) -> treeItemClickHandlers.getOrDefault(n, z -> {
                    }).accept(n)
            );

            fillAllBranchTrees();

            treeItemClickHandlers.put(workingCopyTreeItem, o -> {
                undockSlaveItems();
                Parent workCopyView = WorkingCopyController.openWorkingCopyHandler(
                        GitemberApp.workingBranch.get(),
                        mainMenuBar,
                        mainToolBar,
                        st -> fillStashTree());
                hostPanel.getChildren().removeAll(hostPanel.getChildren());
                hostPanel.getChildren().add(workCopyView);
            });

            // Just show revision
            treeItemClickHandlers.put(historyTreeItem, o -> {

                undockSlaveItems();
                Parent branchView = BranchViewController
                        .openBranchHistory(GitemberApp.workingBranch.get(), mainToolBar);

                hostPanel.getChildren().removeAll(hostPanel.getChildren());
                hostPanel.getChildren().add(branchView);
            });

            // Show all revision
            treeItemClickHandlers.put(historyTreeItemAll, o -> {

                undockSlaveItems();
                Parent branchView = BranchViewController
                        .openBranchHistory(GitemberApp.workingBranch.get(), mainToolBar, true);

                hostPanel.getChildren().removeAll(hostPanel.getChildren());
                hostPanel.getChildren().add(branchView);
            });

            repoTreeView.setDisable(false);
            workSpaceTreeItem.setExpanded(true);
            repoTreeView.getSelectionModel().select(workSpaceTreeItem);
            repoTreeView.getSelectionModel().select(workingCopyTreeItem);

        } catch (Exception e) {
            final String msg = "Cannot open repository";
            log.log(Level.SEVERE, msg , e);
            GitemberApp.showException(msg, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void fillAllBranchTrees() {
        fillBranchTree(GitemberApp.getRepositoryService().getLocalBranches(), localBranchesTreeItem);
        fillBranchTree(GitemberApp.getRepositoryService().getTags(), tagsTreeItem);
        fillBranchTree(GitemberApp.getRepositoryService().getRemoteBranches(), remoteBranchesTreeItem);
        fillStashTree();

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

    private BlockingQueue<Object> msgQ = new ArrayBlockingQueue<Object>(10);

        private void fillStashTree() {
        stashInfo = GitemberApp.getRepositoryService().getStashList();
        cleanUp(stashesTreeItem);
        stashInfo.stream().forEach(
                ri -> {
                    TreeItem<ScmRevisionInformation> stashTree = new TreeItem<>(ri);
                    stashesTreeItem.getChildren().add(stashTree);
                    treeItemClickHandlers.put(stashTree, o -> {
                        undockSlaveItems();
                        Parent commitView = CommitViewController.openCommitViewWindow(
                                ri,
                                stashInfo.indexOf(ri),
                                GitemberApp.workingBranch.get().getFullName(),
                                mainMenuBar,
                                mainToolBar,
                                scmRevisionInformation -> {
                                    this.fillStashTree();
                                });
                        hostPanel.getChildren().removeAll(hostPanel.getChildren());
                        hostPanel.getChildren().add(commitView);
                    });
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
                branch -> rez.put(new TreeItem<>(branch), o -> {
                            undockSlaveItems();
                            Parent branchView = BranchViewController.openBranchHistory(branch, mainToolBar);
                            hostPanel.getChildren().removeAll(hostPanel.getChildren());
                            hostPanel.getChildren().add(branchView);
                        }
                )
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

        createTagContextMenu();

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
                    pullLocalBranchMenuItem.setDisable(disableRemoteOps);
                }
        );

        GitemberApp.currentRepositoryPath.addListener(
                (o, ov, nv) -> {
                    boolean disable = nv == null;
                    statReportMenuItem.setDisable(disable);
                    compressDataMenuItem.setDisable(disable);
                    settingsMenuItem.setDisable(disable);
                }
        );

        GitemberApp.workingBranch.addListener(
                observable -> {
                    ScmBranch newValue = (ScmBranch) ((SimpleObjectProperty) observable).get();
                    boolean trackingRemoteBranch = newValue.getRemoteName() != null;
                    try {
                        fillAllBranchTrees();
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
                    } catch (Exception e) {
                        GitemberApp.showException("Cannot reload list of branches", e);
                    }

                }
        );


        List<Pair<String, String>> recenntProjects = GitemberApp.getSettingsService().getRecentProjects();
        if (!recenntProjects.isEmpty()) {


            GridPane gridPane= new GridPane();
            //gridPane.setStyle("-fx-background-color: rgba(200, 18, 18, 0.5);");

            for (int i = 0; i < 9; i++) {
                Label lbl = new Label("      ");
                GridPane.setRowIndex(lbl, i);
                GridPane.setColumnIndex(lbl, 1);
                gridPane.getChildren().add(lbl);

            }

            VBox hBox = new VBox();
            GridPane.setRowIndex(hBox, 3);
            GridPane.setColumnIndex(hBox, 3);
            gridPane.getChildren().add(hBox);

            hostPanel.getChildren().addAll( gridPane);

            Label lbl = new Label("Open recent project(s)");
            lbl.setStyle("-fx-font-size: 18 pt;");
            hBox.getChildren().add(lbl);
            hBox.getChildren().add(new Label(" "));
            hBox.getChildren().add(new Separator());
            hBox.getChildren().add(new Label(" "));


            recenntProjects.stream().forEach(
                    pairOfProject -> {
                        Hyperlink  projectNameHl = new Hyperlink (
                                pairOfProject.getFirst() + "   " + pairOfProject.getSecond());
                        projectNameHl.setStyle("-fx-font-size: 16 pt;");
                        hBox.getChildren().add(projectNameHl);
                        projectNameHl.setOnAction(
                                event -> {
                                    openRepository(pairOfProject.getSecond());
                                    createOpenRecentMenu();
                                }
                        );

                    }
            );
        }
    }

    /**
     * Undock added items.
     */
    private void undockSlaveItems() {
        mainMenuBar.getMenus().removeIf(menu -> Const.MERGED.equals(menu.getId()));
        mainToolBar.getItems().removeIf(node -> Const.MERGED.equals(node.getId()));
    }

    private void createOpenRecentMenu() {
        openRecentMenuItem.getItems().removeAll(openRecentMenuItem.getItems());
        GitemberApp.getSettingsService().getRecentProjects().stream().forEach(
                o -> {
                    MenuItem mi = new MenuItem(o.getFirst());
                    mi.setUserData(o.getSecond());
                    mi.setOnAction(
                            event -> {
                                openRepository(o.getSecond());
                                undockSlaveItems();
                                Parent workCopyView = WorkingCopyController.openWorkingCopyHandler(
                                        GitemberApp.workingBranch.get(),
                                        mainMenuBar,
                                        mainToolBar,
                                        st -> fillStashTree()
                                );
                                hostPanel.getChildren().removeAll(hostPanel.getChildren());
                                hostPanel.getChildren().add(workCopyView);

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
     * @param obj branch or revision information
     */
    public void validateContextMenu(final Object obj) {
        if (obj instanceof ScmBranch) {

            ScmBranch scmBranch = (ScmBranch) obj;

            contextMenuItemVisibilityLTR.stream().forEach(
                    i -> i.getMenuItem().setVisible(isMenuItemVisibleForScmBranch(i, scmBranch))
            );

            if (ScmBranch.BranchType.LOCAL.equals(scmBranch.getBranchType())) {
                boolean disableRemoteOp = scmBranch.getRemoteName() == null;
                fetchLocalBranchMenuItem.setDisable(disableRemoteOp);
                pullLocalBranchMenuItem.setDisable(disableRemoteOp);
                if (disableRemoteOp) {
                    pushToRemoteLocalBranchMenuItem.setText("Push to ... ");
                } else {
                    fetchLocalBranchMenuItem.setText("Fetch remote " + scmBranch.getRemoteName());
                    pullLocalBranchMenuItem.setText("Pull remote " + scmBranch.getRemoteName());
                    pushToRemoteLocalBranchMenuItem.setText("zzz Push to remote " + scmBranch.getRemoteName());
                }
                fetchLocalBranchMenuItem.setVisible(!disableRemoteOp);
                pullLocalBranchMenuItem.setVisible(!disableRemoteOp);

            } else if (ScmBranch.BranchType.REMOTE.equals(scmBranch.getBranchType())) {
            } else if (ScmBranch.BranchType.TAG.equals(scmBranch.getBranchType())) {

            }
        } else if (obj instanceof ScmRevisionInformation) {

            ScmRevisionInformation ri = (ScmRevisionInformation) obj;

            contextMenuItemVisibilityLTR.stream().forEach(
                    i -> i.getMenuItem().setVisible(isMenuItemVisibleForScmBranch(i, ri))
            );


        }
    }

    private boolean isMenuItemVisibleForScmBranch(MenuItemAvailbility i, Object obj) {
        if (obj instanceof ScmBranch) {
            ScmBranch scmBranch = (ScmBranch) obj;
            if (ScmBranch.BranchType.LOCAL.equals(scmBranch.getBranchType())) {
                return i.isLocalVisible();
            } else if (ScmBranch.BranchType.TAG.equals(scmBranch.getBranchType())) {
                return i.isTagVisible();
            } else if (ScmBranch.BranchType.REMOTE.equals(scmBranch.getBranchType())) {
                return i.isRemoteVisible();
            }

        } else if (obj instanceof ScmRevisionInformation) {
            return i.isStashVisible();
        }
        return false;
    }


    private ContextMenu createTagContextMenu() {

        createTagMenuItem = new MenuItem("Create tag ...");
        createTagMenuItem.setOnAction(this::createTagHandler);
        tagContextMenu = new ContextMenu(createTagMenuItem);

        return tagContextMenu;
    }

    private ContextMenu createLocalBranchContextMenu() {

        checkoutRemoteBranchMenuItem = new MenuItem("Checkout ...");
        checkoutRemoteBranchMenuItem.setOnAction(this::remoteBranchCheckoutHandler);

        checkoutLocalBranchMenuItem = new MenuItem("Checkout");
        checkoutLocalBranchMenuItem.setOnAction(this::localBranchCheckoutHandler);

        createLocalBranchMenuItem = new MenuItem("Create branch ...");
        createLocalBranchMenuItem.setOnAction(this::localBranchCreateHandler);

        mergeLocalBranchMenuItem = new MenuItem("Merge ...");
        mergeLocalBranchMenuItem.setOnAction(this::localBranchMergeHandler);

        fetchLocalBranchMenuItem = new MenuItem("Fetch");
        fetchLocalBranchMenuItem.setOnAction(this::fetchHandler);

        pullLocalBranchMenuItem = new MenuItem("Pull");
        pullLocalBranchMenuItem.setOnAction(this::pullHandler);

        pushToRemoteLocalBranchMenuItem = new MenuItem("Push ...");
        pushToRemoteLocalBranchMenuItem.setOnAction(this::localBranchPushHandler);

        deleteLocalBranchMenuItem = new MenuItem("Delete ...");
        deleteLocalBranchMenuItem.setOnAction(this::branchDeleteHandler);


        applyStashCtxMenuItem = new MenuItem("Apply stash ...");
        applyStashCtxMenuItem.setOnAction(this::applyStashHandler);


        deleteStashCtxMenuItem = new MenuItem("Delete stash ...");
        deleteStashCtxMenuItem.setOnAction(this::deleteStashHandler);

        contextMenu = new ContextMenu(
                checkoutRemoteBranchMenuItem,
                checkoutLocalBranchMenuItem,
                createLocalBranchMenuItem,
                mergeLocalBranchMenuItem,
                fetchLocalBranchMenuItem,
                pullLocalBranchMenuItem,
                pushToRemoteLocalBranchMenuItem,
                applyStashCtxMenuItem,
                new SeparatorMenuItem(),
                deleteStashCtxMenuItem,
                deleteLocalBranchMenuItem
        );

        contextMenuItemVisibilityLTR = new ArrayList<MenuItemAvailbility>() {{
            add(new MenuItemAvailbility(checkoutRemoteBranchMenuItem, false, false, true, false));
            add(new MenuItemAvailbility(checkoutLocalBranchMenuItem, true, true, false, false));
            add(new MenuItemAvailbility(createLocalBranchMenuItem, true, true, true, false));
            add(new MenuItemAvailbility(mergeLocalBranchMenuItem, true, false, true, false));
            add(new MenuItemAvailbility(fetchLocalBranchMenuItem, true, false, true, false));
            add(new MenuItemAvailbility(pullLocalBranchMenuItem, true, false, true, false));
            add(new MenuItemAvailbility(pushToRemoteLocalBranchMenuItem, true, false, true, false));
            add(new MenuItemAvailbility(deleteLocalBranchMenuItem, true, true, true, false));
            add(new MenuItemAvailbility(deleteStashCtxMenuItem, false, false, false, true));
            add(new MenuItemAvailbility(applyStashCtxMenuItem, false, false, false, true));

        }};

        return contextMenu;
    }


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Tag handler                            ---------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    @SuppressWarnings("unchecked")
    public void createTagHandler(ActionEvent actionEvent) {

        CreateTagDialog dialog = new CreateTagDialog(
                "Create tag",
                "Create tag",
                "",
                "Just create", "Create and push"
        );
        Optional<Pair<Boolean, String>> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            GitemberApp.getGitemberService().createTagZZZ(dialogResult.get().getFirst(), dialogResult.get().getSecond());
            tagsTreeItem.getChildren().clear();
            fillBranchTree(GitemberApp.getRepositoryService().getTags(), tagsTreeItem);
        }

    }


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Local branch context menu item handlers---------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//


    @SuppressWarnings("unchecked")
    public void remoteBranchCheckoutHandler(ActionEvent actionEvent) {
        CheckoutOriginBranch dialog = new CheckoutOriginBranch(
                "Checkout remote branch",
                "Please, specify action for branch " + getScmBranch().getShortName(),
                getScmBranch().getShortName(),
                "Just checkout", "Create local branch"
        );
        Optional<Pair<Boolean, String>> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            if (dialogResult.get().getFirst()) {
                GitemberApp.getGitemberService().checkout(getScmBranch().getFullName(), dialogResult.get().getSecond());

            } else {
                GitemberApp.getGitemberService().checkout(getScmBranch().getFullName(), null);

            }
        }
    }


    @SuppressWarnings("unchecked")
    public void localBranchCheckoutHandler(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().checkout(getScmBranch().getFullName(), null);
    }

    @SuppressWarnings("unchecked")
    public void localBranchMergeHandler(ActionEvent actionEvent) {
        final ScmBranch scmBranch = getScmBranch();
        if (GitemberApp.getGitemberService().mergeToHead(scmBranch.getFullName())) {
            fillBranchTree(GitemberApp.getRepositoryService().getLocalBranches(), localBranchesTreeItem);
        }
    }


    @SuppressWarnings("unchecked")
    public void localBranchCreateHandler(ActionEvent actionEvent) {
        if (GitemberApp.getGitemberService().createNewBranchFrom(getScmBranch().getFullName())) {
            fillAllBranchTrees();
        }
    }


    /**
     * Push selected branch.
     * New remote branch will be created if it not tracked some remote branch.
     *
     * @param actionEvent event
     */
    public void localBranchPushHandler(ActionEvent actionEvent) {
        ScmBranch scmBranch = GitemberApp.workingBranch.get();
        if (GitemberApp.getGitemberService().pushBranch(scmBranch)) {
            fillBranchTree(GitemberApp.getRepositoryService().getLocalBranches(), localBranchesTreeItem);
        }
    }


    @SuppressWarnings("unchecked")
    public void branchDeleteHandler(ActionEvent actionEvent) {
        final ScmBranch scmBranch = getScmBranch();
        if (scmBranch.getBranchType() == ScmBranch.BranchType.REMOTE) {
            GitemberApp.getGitemberService().deleteRemoteBranch(
                    getScmBranch(),
                    remoteOperationValue -> {
                        try {
                            GitemberApp.getRepositoryService().deleteLocalBranch(scmBranch.getFullName());
                        } catch (Exception e) {
                            String msg = "Cannot delete remote branch "
                                    + scmBranch.getFullName()
                                    + " on local disk ";
                            log.log(Level.SEVERE, msg, e);
                            GitemberApp.showException(msg, e);
                        }
                        fillAllBranchTrees();
                    },
                    remoteOperationErr -> {
                        GitemberApp.showResult("Cannot delete remote branch " + scmBranch.getFullName(),
                                Alert.AlertType.ERROR);
                        fillAllBranchTrees();
                    }
            );
        } else {
            GitemberApp.getGitemberService().deleteLocalBranch(scmBranch);
            fillAllBranchTrees();
        }
    }


    /**
     * Apply stash.
     *
     * @param actionEvent event
     */
    public void applyStashHandler(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().applyStash(getScmRevision());
    }

    /**
     * Delete stash.
     *
     * @param actionEvent event
     */
    public void deleteStashHandler(ActionEvent actionEvent) {
        ScmRevisionInformation ri = getScmRevision();
        if (GitemberApp.getGitemberService().deleteStash(ri, stashInfo.indexOf(ri))) {
            fillStashTree();
        }
    }

    @SuppressWarnings("unchecked")
    private ScmBranch getScmBranch() {
        TreeItem<ScmBranch> item = (TreeItem<ScmBranch>) repoTreeView.getSelectionModel().getSelectedItem();
        return item == null ? null : item.getValue();
    }

    @SuppressWarnings("unchecked")
    private ScmRevisionInformation getScmRevision() {
        TreeItem<ScmRevisionInformation> item = (TreeItem<ScmRevisionInformation>) repoTreeView.getSelectionModel().getSelectedItem();
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
        if (GitemberApp.getSettingsService().getUserHomeFolder() != null) {
            directoryChooser.setInitialDirectory(new File(GitemberApp.getSettingsService().getUserHomeFolder()));
        }
        File selectedDirectory =
                directoryChooser.showDialog(GitemberApp.getMainStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            if (!absPath.endsWith(Const.GIT_FOLDER)) {
                absPath += File.separator + Const.GIT_FOLDER;
            }
            openRepository(absPath);
        }
        createOpenRecentMenu();
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
            GitemberApp.showResult(msg, Alert.AlertType.ERROR);
        }
    }

    /**
     * Open settings dialog .
     *
     * @param actionEvent action event
     */
    @SuppressWarnings("unused")
    public void settingsActionHandler(ActionEvent actionEvent) {
        GitemberSettings gitemberSettings = GitemberApp.getSettingsService().getGitemberSettings();
        SettingsDialog settingsDialog = new SettingsDialog(new SettingsModel(gitemberSettings.getLastProjectSettings()));
        Optional<SettingsModel> model = settingsDialog.showAndWait();
        if (model.isPresent()) {
            //todo  update last projet
            SettingsModel settingsModel = model.get();
            GitemberProjectSettings newGitemberSettings = settingsModel.toGitemberProjectSettings();
            GitemberApp.getSettingsService().save();

        }
    }


    /**
     * Close application.
     *
     * @param actionEvent action event
     */
    @SuppressWarnings("unused")
    public void exitActionHandler(ActionEvent actionEvent) {
        GitRepositoryService.cleanUpTempFiles();
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
        GitemberApp.getGitemberService().cloneRepo(
                remoteOperationValue -> {
                    String repoPath = (String) remoteOperationValue.getValue();
                    openRepository(repoPath);
                },
                null
        );
    }

    /**
     * Create new git repository.
     *
     * @param actionEvent action event
     */
    @SuppressWarnings("unused")
    public void createRepositoryHandler(ActionEvent actionEvent) throws Exception {
        GitemberApp.getGitemberService().createRepo(
                path -> openRepository(path)
        );
    }

    /**
     * Fetch all.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void fetchHandlerAll(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().fetch(null);
    }

    /**
     * Fetch particular branch, if it track some remote.
     *
     * @param actionEvent event
     */
    public void fetchHandler(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().fetch(GitemberApp.workingBranch.get().getRemoteName());
    }

    @SuppressWarnings("unused")
    public void pullHandler(ActionEvent actionEvent) {
        String branchName = GitemberApp.workingBranch.get().getRemoteName();
        GitemberApp.getGitemberService().pull(branchName);
    }

    @SuppressWarnings("unused")
    public void pullAllHandler(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().pull(null);
    }

    /**
     * Push all branches.
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void pushAllHandler(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().pushAll();
    }

    /**
     * Remove unused garbage from database
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void compressDataHandler(ActionEvent actionEvent) {

        GitemberApp.getGitemberService().compressDatabase();

    }

    /**
     * Create t report
     *
     * @param actionEvent event
     */
    @SuppressWarnings("unused")
    public void createStatReport(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().createStatReport();
    }


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Help menu handlers -----------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//

    public void createBugReportHandler(ActionEvent actionEvent) {
        showExternalPage("https://github.com/iazarny/gitember/issues/new");
    }

    public void chckeForUpdate(ActionEvent actionEvent) {
        showExternalPage("http://gitember.com");
    }

    private void showExternalPage(String httpaddr) {
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(URI.create(httpaddr));
            } catch (IOException e) {
                String msg = "Cannot open url " + httpaddr;
                log.log(Level.WARNING, msg , e);
                GitemberApp.showException(msg, e);

            }
        }).start();
    }

    public void aboutHandler(ActionEvent actionEvent) {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.showAndWait();
    }
}
