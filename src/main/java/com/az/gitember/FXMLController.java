package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.misc.ScmRevisionInformation;
import com.az.gitember.misc.Settings;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.*;
import javafx.application.Platform;
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
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import org.apache.commons.lang3.ObjectUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
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
    public MenuItem checkoutLocalBranchMenuItem;
    public MenuItem createLocalBranchMenuItem;
    public MenuItem mergeLocalBranchMenuItem;
    public MenuItem deleteLocalBranchMenuItem;

    public MenuItem deleteStashCtxMenuItem;
    public MenuItem applyStashCtxMenuItem;

    public ContextMenu contextMenu;
    public MenuBar mainMenuBar;
    public ToolBar mainToolBar;

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
    public MenuItem compressDataMenuItem;
    public MenuItem statReportMenuItem;

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


    private final Map<TreeItem, Consumer<Object>> treeItemClickHandlers = new HashMap<>();
    private List<ScmRevisionInformation> stashInfo;


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

            treeItemClickHandlers.put(historyTreeItem, o -> {
                undockSlaveItems();
                Parent branchView = BranchViewController.openBranchHistory(GitemberApp.workingBranch.get());
                hostPanel.getChildren().removeAll(hostPanel.getChildren());
                hostPanel.getChildren().add(branchView);
            });

            repoTreeView.setDisable(false);
            workSpaceTreeItem.setExpanded(true);
            repoTreeView.getSelectionModel().select(workSpaceTreeItem);
            repoTreeView.getSelectionModel().select(workingCopyTreeItem);

        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot open repository", e);
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

    private void test() {

        try {
            msgQ.put("eeee");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

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
                            Parent branchView = BranchViewController.openBranchHistory(branch);
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
                    compressDataMenuItem.setDisable(disableRemoteOps);
                    statReportMenuItem.setDisable(disableRemoteOps);
                    pushToRemoteLocalBranchMenuItem.setDisable(disableRemoteOps);
                    fetchLocalBranchMenuItem.setDisable(disableRemoteOps);
                }
        );

        GitemberApp.currentRepositoryPath.addListener(
                (o, ov, nv) -> {
                    boolean disable = nv == null;
                    statReportMenuItem.setDisable(disable);
                }
        );

        GitemberApp.workingBranch.addListener(
                (observable, oldValue, newValue) -> {
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


        applyStashCtxMenuItem = new MenuItem("Apply stash ...");
        applyStashCtxMenuItem.setOnAction(this::applyStashHandler);


        deleteStashCtxMenuItem = new MenuItem("Delete stash ...");
        deleteStashCtxMenuItem.setOnAction(this::deleteStashHandler);

        contextMenu = new ContextMenu(
                checkoutLocalBranchMenuItem,
                createLocalBranchMenuItem,
                mergeLocalBranchMenuItem,
                fetchLocalBranchMenuItem,
                pushToRemoteLocalBranchMenuItem,
                applyStashCtxMenuItem,
                new SeparatorMenuItem(),
                deleteStashCtxMenuItem,
                deleteLocalBranchMenuItem
        );


        contextMenuItemVisibilityLTR = new ArrayList<MenuItemAvailbility>() {{
            add(new MenuItemAvailbility(checkoutLocalBranchMenuItem, true, true, true, false));
            add(new MenuItemAvailbility(createLocalBranchMenuItem, true, true, true, false));
            add(new MenuItemAvailbility(mergeLocalBranchMenuItem, true, false, true, false));
            add(new MenuItemAvailbility(fetchLocalBranchMenuItem, true, false, true, false));
            add(new MenuItemAvailbility(pushToRemoteLocalBranchMenuItem, true, false, true, false));
            add(new MenuItemAvailbility(deleteLocalBranchMenuItem, true, true, true, false));
            add(new MenuItemAvailbility(deleteStashCtxMenuItem, false, false, false, true));
            add(new MenuItemAvailbility(applyStashCtxMenuItem, false, false, false, true));

        }};

        return contextMenu;
    }


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Local branch context menu item handlers---------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//


    @SuppressWarnings("unchecked")
    public void localBranchCheckoutHandler(ActionEvent actionEvent) {
        GitemberApp.getGitemberService().checkout(getScmBranch().getFullName());
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
    public void localBranchDeleteHandler(ActionEvent actionEvent) {
        if (GitemberApp.getGitemberService().deleteBranch(getScmBranch())) {
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
                    GitemberApp.showResult(msg, Alert.AlertType.ERROR);
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


    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Help menu handlers -----------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//

    public void createBugReportHandler(ActionEvent actionEvent) {
        String httpaddr = "https://github.com/iazarny/gitember/issues/new";
        new Thread(() -> {
            try {
                Desktop.getDesktop().browse(URI.create(httpaddr));
            } catch (IOException e) {
                log.log(Level.WARNING, "Cannot open url " + httpaddr, e);

            }
        }).start();
    }

    public void aboutHandler(ActionEvent actionEvent) {
        AboutDialog aboutDialog = new AboutDialog();
        aboutDialog.showAndWait();
    }
}
