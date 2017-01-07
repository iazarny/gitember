package com.az.gitember;

import com.az.gitember.misc.*;
import com.az.gitember.scm.impl.git.DefaultProgressMonitor;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.ui.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
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
import java.util.Optional;
import java.util.ResourceBundle;
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

    public TitledPane workSpaceTitlePane;
    public TitledPane branchesTitlePane;
    public TitledPane tagsTitlePane;
    public TitledPane remotesTitlePane;

    public MenuItem pushToRemoteMenuItem;

    
    public ContextMenu localBranchListItemContextMenu;

    
    public ProgressBar operationProgressBar;

    
    public MenuItem openGitTerminalMenuItem;
    public MenuItem fetchMenuItem;
    public MenuItem fetchAllMenuItem;
    public MenuItem pullMenuItem;
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

    @FXML
    private ListView localBranchesList;

    @FXML
    private ListView remoteBranchesList;

    @FXML
    private ListView tagList;

    @FXML
    private AnchorPane hostPanel;

    private String login = null;
    private String pwd = null;
    private Optional<Pair<String, String>> uiInputResultToService;
    private CountDownLatch uiInputLatchToService;


    
    public void exitActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }


    
    public void openHandler(ActionEvent actionEvent) throws Exception {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        if (MainApp.getSettingsService().getLastProject() != null) {
            directoryChooser.setInitialDirectory(new File(MainApp.getSettingsService().getLastProject()));
        }
        File selectedDirectory =
                directoryChooser.showDialog(MainApp.getMainStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            if (!absPath.endsWith(Const.GIT_FOLDER)) {
                absPath += File.separator + Const.GIT_FOLDER;
            }
            openRepository(absPath);
            openWorkingCopyHandler(actionEvent);
        }

    }

    @SuppressWarnings("unchecked")
    public void openRepository(final String absPath) {
        try {
            MainApp.setCurrentRepositoryPath(absPath);
            MainApp.setRepositoryService(new GitRepositoryService(absPath));
            MainApp.getSettingsService().saveLastProject(absPath);

            localBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getLocalBranches()));
            remoteBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getRemoteBranches()));
            tagList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getTags()));


            String head = MainApp.getRepositoryService().getHead();
            MainApp.setTitle(Const.TITLE + MainApp.getCurrentRepositoryPathWOGit() + " " + head);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL url, ResourceBundle rb) {
        final ScmItemCellFactory scmItemCellFactory = new ScmItemCellFactory();

        remoteBranchesList.setCellFactory(scmItemCellFactory);
        localBranchesList.setCellFactory(scmItemCellFactory);
        tagList.setCellFactory(scmItemCellFactory);

        openRecentMenuItem.getItems().removeAll(openRecentMenuItem.getItems());
        MainApp.getSettingsService().getRecentProjects().stream().forEach(
                o -> {
                    MenuItem mi = new MenuItem(o.getFirst());
                    mi.setUserData(o.getSecond());
                    mi.setOnAction(
                            event -> {
                                MainApp.getMainStage().getScene().setCursor(Cursor.WAIT);
                                Platform.runLater(() -> {
                                    openRepository(o.getSecond());
                                    openWorkingCopyHandler(null);
                                    MainApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);

                                });
                            }
                    );
                    openRecentMenuItem.getItems().add(mi);
                }
        );
        openRecentMenuItem.setDisable(
                openRecentMenuItem.getItems().size() < 1
        );

        openGitTerminalMenuItem.setVisible(
                MainApp.getSettingsService().isWindows()
        );


        MainApp.remoteUrl.addListener(
                (observable, oldValue, newValue) -> {
                    boolean disableRemoteOps = newValue == null;
                    pullBtn.setDisable(disableRemoteOps);
                    fetchBtn.setDisable(disableRemoteOps);
                    pushBtn.setDisable(disableRemoteOps);

                    fetchMenuItem.setDisable(disableRemoteOps);
                    fetchAllMenuItem.setDisable(disableRemoteOps);
                    pullMenuItem.setDisable(disableRemoteOps);
                    pushMenuItem.setDisable(disableRemoteOps);
                    pushAllMenuItem.setDisable(disableRemoteOps);
                    statReportMenuItem.setDisable(disableRemoteOps);

                    remotesTitlePane.setDisable(disableRemoteOps);


                }
        );

        MainApp.currentRepositoryPath.addListener(
                (observable, oldValue, newValue) -> {
                    boolean disable = newValue == null;
                    statReportMenuItem.setDisable(disable);
                    checkoutMenuItem.setDisable(disable);
                    mergeMenuItem.setDisable(disable);
                    rebaseMenuItem.setDisable(disable);

                    stashMenuItem.setDisable(disable);
                    applyStashMenuItem.setDisable(disable);
                    commitMenuItem.setDisable(disable);
                    resetVersionMenuItem.setDisable(disable);

                    branchesTitlePane.setDisable(disable);
                    tagsTitlePane.setDisable(disable);
                    workSpaceTitlePane.setDisable(disable);
                }
        );




    }

    
    public void branchesListMouseClicked(Event event) throws Exception {
        ScmBranch scmBranch = ((ListView<ScmBranch>) event.getSource()).getSelectionModel().getSelectedItem();

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





    public void pullHandler(ActionEvent actionEvent) {
        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> MainApp.getRepositoryService().remoteRepositoryPull(login, pwd, new DefaultProgressMonitor(d -> updateProgress(d, 1.0)))
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();
    }

    private void pushToRemoteRepository(String localBranchName, String remoteBranchName, boolean setOrigin) {
        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> MainApp.getRepositoryService().remoteRepositoryPush(localBranchName, remoteBranchName, login, pwd, setOrigin, new DefaultProgressMonitor(d -> updateProgress(d, 1.0)))
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();
    }

    private void prepareLongTask(final Task<RemoteOperationValue> longTask,
                                 final Consumer<RemoteOperationValue> onOk,
                                 final Consumer<RemoteOperationValue> onError) {


        operationProgressBar.progressProperty().bind(longTask.progressProperty());
        operationProgressBar.setVisible(true);
        MainApp.getMainStage().getScene().setCursor(Cursor.WAIT);

        longTask.setOnSucceeded(val -> {
            Platform.runLater(
                    () -> {
                        RemoteOperationValue rval = longTask.getValue();
                        switch (rval.getResult()) {
                            case OK: {
                                if (onOk != null) {
                                    onOk.accept(rval);
                                }
                                MainApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                                showResult("OK. TODO get info", Alert.AlertType.INFORMATION);
                                break;
                            }
                            case ERROR: {
                                if (onError != null) {
                                    onError.accept(rval);
                                }
                                MainApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                                showResult("ERROR. TODO get info", Alert.AlertType.ERROR);
                                break;
                            }
                        }
                        operationProgressBar.progressProperty().unbind();
                        operationProgressBar.setVisible(false);
                    }
            );
        });
    }


    public void exitHandler(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void openWorkingCopyHandler(ActionEvent actionEvent) {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = getClass().getResource("/fxml/WorkingCopyPane.fxml").openStream()) {
            final Parent workCopyView = fxmlLoader.load(is);
            final WorkingCopyController workingCopyController = fxmlLoader.getController();
            workingCopyController.open();
            hostPanel.getChildren().removeAll(hostPanel.getChildren());
            hostPanel.getChildren().add(workCopyView);
        } catch (IOException ioe) {
            log.log(Level.SEVERE, "Cannot open working copy view", ioe.getMessage());
        }

    }




//--------------------------------------------------------------------------------------------------------------------

    /**
     * Push all branches
     *
     * @param actionEvent
     */
    public void pushHandler(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Push all");
        alert.setContentText("Do you really want to push all branches ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            pushToRemoteRepository("+refs/heads/*", "refs/heads/*", false);
        }
    }


    /**
     * Push selected branch.
     *
     * @param actionEvent event
     * @throws Exception
     */
    public void localBranchPushHandler(ActionEvent actionEvent) throws Exception {
        ScmBranch scmBranch = (ScmBranch) localBranchesList.getSelectionModel().getSelectedItem();
        if (scmBranch.getRemoteName() == null) {
            TextInputDialog dialog = new TextInputDialog(scmBranch.getShortName());
            dialog.setTitle("Branch name");
            dialog.setHeaderText("Remote branch will be created");
            dialog.setContentText("Please enter new remote branch name:");

            Optional<String> dialogResult = dialog.showAndWait();
            if (dialogResult.isPresent()) {
                scmBranch.setRemoteName(dialogResult.get());
                pushToRemoteRepository(scmBranch.getShortName(), scmBranch.getRemoteName(), true);
            }
        } else {
            pushToRemoteRepository(scmBranch.getShortName(), scmBranch.getRemoteName(), true);
        }
    }


    @SuppressWarnings("unchecked")
    public void localBranchCheckoutHandler(ActionEvent actionEvent) throws Exception {

        ScmBranch scmBranch = (ScmBranch) localBranchesList.getSelectionModel().getSelectedItem();
        MainApp.getRepositoryService().checkoutLocalBranch(scmBranch.getFullName());
        localBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getLocalBranches()));
        localBranchesList.setCellFactory(new ScmItemCellFactory());
        localBranchesList.refresh();


        String head = MainApp.getRepositoryService().getHead();
        MainApp.setTitle(Const.TITLE + MainApp.getCurrentRepositoryPathWOGit() + " " + head);


    }


    @SuppressWarnings("unchecked")
    public void localBranchCreateHandler(ActionEvent actionEvent) throws Exception {

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("New branch");
        dialog.setHeaderText("Create new branch");
        dialog.setContentText("Please enter new branch name:");

        Optional<String> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            ScmBranch scmBranch = (ScmBranch) localBranchesList.getSelectionModel().getSelectedItem();
            MainApp.getRepositoryService().createLocalBranch(
                    scmBranch.getFullName(),
                    dialogResult.get()
            );

            localBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getLocalBranches()));
            localBranchesList.setCellFactory(new ScmItemCellFactory());
            localBranchesList.refresh();
        }


    }

    @SuppressWarnings("unchecked")
    public void localBranchMergeHandler(ActionEvent actionEvent) throws Exception {

        ScmBranch scmBranch = (ScmBranch) localBranchesList.getSelectionModel().getSelectedItem();

        TextAreaInputDialog dialog = new TextAreaInputDialog(
                "Merge " + scmBranch.getFullName() + " to " + MainApp.getRepositoryService().getHead()
        );

        dialog.setTitle("Merge message");
        dialog.setHeaderText("Provide merge message");
        dialog.setContentText("Message:");
        Optional<String> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            MainApp.getRepositoryService().mergeLocalBranch(
                    scmBranch.getFullName(),
                    dialogResult.get()
            );

            localBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getLocalBranches()));
            localBranchesList.setCellFactory(new ScmItemCellFactory());
            localBranchesList.refresh();

        }


    }

    @SuppressWarnings("unchecked")
    public void localBranchDeleteHandler(ActionEvent actionEvent) throws Exception {

        ScmBranch scmBranch = (ScmBranch) localBranchesList.getSelectionModel().getSelectedItem();

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Delete branch " + scmBranch.getShortName());
        alert.setContentText("Do you really want to delete " + scmBranch.getShortName() + " branch ?");

        Optional<ButtonType> dialogResult = alert.showAndWait();
        if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
            try {
                MainApp.getRepositoryService().deleteLocalBranch(
                        scmBranch.getFullName()
                );
                localBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getLocalBranches()));
                localBranchesList.setCellFactory(new ScmItemCellFactory());
                localBranchesList.refresh();
            } catch (CannotDeleteCurrentBranchException e) {
                showResult(e.getMessage(), Alert.AlertType.ERROR);
            }
        }

    }


    /**
     * Process remote operations with failback
     *
     * @param supplier remote repository command.
     * @return RemoteOperationValue which shall be interpret by caller
     */
    private RemoteOperationValue remoteRepositoryOperation(final Supplier<RemoteOperationValue> supplier) {
        boolean ok = false;
        RemoteOperationValue operationValue = null;


        while (!ok) {
            uiInputResultToService = null;
            operationValue = supplier.get();
            switch (operationValue.getResult()) {
                case AUTH_REQUIRED: {
                    uiInputLatchToService = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        System.out.println("### AUTH_REQUIRED Dialog " + Thread.currentThread().getName());
                        login = MainApp.getSettingsService().getLastLoginName();
                        uiInputResultToService = new LoginDialog("Login", "Please, provide login and password", login, null)
                                .showAndWait();
                        uiInputLatchToService.countDown();
                    });
                    try {
                        uiInputLatchToService.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("### AUTH_REQUIRED " + Thread.currentThread().getName());
                    break;
                }
                case NOT_AUTHORIZED: {
                    uiInputLatchToService = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        System.out.println("### NOT_AUTHORIZED dialog" + Thread.currentThread().getName());
                        uiInputResultToService = new LoginDialog("Login", "Not authorized. Provide correct credentials", login, pwd)
                                .showAndWait();
                        uiInputLatchToService.countDown();
                    });
                    try {
                        uiInputLatchToService.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("### NOT_AUTHORIZED " + Thread.currentThread().getName());
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
                MainApp.getSettingsService().saveLastLoginName(login);
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
    //-----------------------------    Menu file handlers -----------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//

    /**
     * Open GUI shell in home or in repository folder, if it was opened.
     *
     * @param actionEvent
     */
    public void openShellActionHandler(ActionEvent actionEvent) {
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(
                            Paths.get(
                                    ObjectUtils.defaultIfNull(
                                            MainApp.getCurrentRepositoryPathWOGit(),
                                            MainApp.getSettingsService().getUserHomeFolder()
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

    public void openGitTerminalActionHandler(ActionEvent actionEvent) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec(
                    new String[]{"cmd", "/c", "start"/*,  "\"C:\\Program Files\\Git\\bin\\sh.exe\" --login"*/},
                    null,
                    new File(
                            ObjectUtils.defaultIfNull(
                                    MainApp.getCurrentRepositoryPathWOGit(),
                                    MainApp.getSettingsService().getUserHomeFolder()
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
     * @param actionEvent
     */
    public void settingsActionHandler(ActionEvent actionEvent) {

        Settings settings = MainApp.getSettingsService().read();
        SettingsDialog settingsDialog = new SettingsDialog(new SettingsModel(settings));
        Optional<SettingsModel> model = settingsDialog.showAndWait();
        if (model.isPresent()) {
            MainApp.getSettingsService().save(model.get().createSettings());
        }

    }

    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //-----------------------------    Repository menu handlers -----------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//
    //---------------------------------------------------------------------------------------------------------------//

    /**
     * Clone and open remote repository.
     * @param actionEvent
     */
    public void cloneHandler(ActionEvent actionEvent) {
        CloneDialog dialog = new CloneDialog("Repository", "Remote repository URL"); // TODO history of repositories
        dialog.setContentText("Please provide remote repository URL:");
        Optional<Pair<String, String>> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
                @Override
                protected RemoteOperationValue call() throws Exception {
                    return remoteRepositoryOperation(
                            () -> MainApp.getRepositoryService().cloneRepository(dialogResult.get().getFirst(), dialogResult.get().getSecond(), login, pwd, new DefaultProgressMonitor(d -> updateProgress(d, 1.0)))
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
    public void createRepositoryHandler(ActionEvent actionEvent) throws Exception {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(MainApp.getSettingsService().getUserHomeFolder()));
        final File selectedDirectory =
                directoryChooser.showDialog(MainApp.getMainStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            MainApp.getRepositoryService().createRepository(absPath);

            openRepository(absPath + File.separator + Const.GIT_FOLDER);
            openWorkingCopyHandler(null);
        }

    }

    public void fetchHandlerAll(ActionEvent actionEvent) {
        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> MainApp.getRepositoryService().remoteRepositoryFetch(null, login, pwd, new DefaultProgressMonitor(d -> updateProgress(d, 1.0)))
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();
    }

    public void fetchHandler(ActionEvent actionEvent) {
        ScmBranch scmBranch = (ScmBranch) localBranchesList.getSelectionModel().getSelectedItem();
        scmBranch.getShortName();
        scmBranch.getRemoteName();

        //http://stackoverflow.com/questions/16319807/determine-if-a-branch-is-remote-or-local-using-jgit
        //http://stackoverflow.com/questions/12927163/jgit-checkout-a-remote-branch

        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> MainApp.getRepositoryService().remoteRepositoryFetch(
                                scmBranch.getShortName(), login, pwd,
                                new DefaultProgressMonitor(d -> updateProgress(d, 1.0)))
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();

    }
}
