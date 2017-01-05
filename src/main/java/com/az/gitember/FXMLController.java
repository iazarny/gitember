package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.Pair;
import com.az.gitember.misc.RemoteOperationValue;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.scm.impl.git.DefaultProgressMonitor;
import com.az.gitember.scm.impl.git.GitRepositoryService;
import com.az.gitember.service.SettingsServiceImpl;
import com.az.gitember.ui.CloneDialog;
import com.az.gitember.ui.LoginDialog;
import com.az.gitember.ui.ScmItemCellFactory;
import com.az.gitember.ui.TextAreaInputDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import org.eclipse.jgit.api.errors.CannotDeleteCurrentBranchException;
import org.eclipse.jgit.api.errors.TransportException;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.function.Supplier;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */

public class FXMLController implements Initializable {


    @FXML
    public Button cloneBtn;

    @FXML
    public Button fetchBtn;

    @FXML
    public Button pullBtn;

    @FXML
    public Button pushBtn;

    @FXML
    public Button openBtn;

    @FXML
    public Menu openRecentMenuItem;

    @FXML
    public TitledPane workSpaceTitlePane;

    @FXML
    public MenuItem pushToRemoteMenuItem;

    @FXML
    public ContextMenu localBranchListItemContextMenu;

    @FXML
    public ProgressBar operationProgressBar;

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


    @FXML
    public void exitActionHandler(ActionEvent actionEvent) {
        Platform.exit();
    }


    @FXML
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
            MainApp.getSettingsService().saveRepository(absPath);

            localBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getLocalBranches()));
            remoteBranchesList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getRemoteBranches()));
            tagList.setItems(FXCollections.observableList(MainApp.getRepositoryService().getTags()));

            this.pullBtn.setDisable(MainApp.getRepositoryService().getRemoteUrl() == null);
            this.fetchBtn.setDisable(MainApp.getRepositoryService().getRemoteUrl() == null);
            this.pushBtn.setDisable(MainApp.getRepositoryService().getRemoteUrl() == null);

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

        try {

            openRecentMenuItem.getItems().removeAll(openRecentMenuItem.getItems());
            MainApp.getSettingsService().getRecentProjects().stream().forEach(
                    o -> {
                        MenuItem mi = new MenuItem(o.getFirst());
                        mi.setUserData(o.getSecond());
                        mi.setOnAction(
                                event -> {
                                    try {
                                        openRepository(o.getSecond());
                                        openWorkingCopyHandler(null);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                        );
                        openRecentMenuItem.getItems().add(mi);
                    }

            );
            openRecentMenuItem.setDisable(
                    openRecentMenuItem.getItems().size() < 1
            );

        } catch (Exception e) {

        }


    }

    @FXML
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

    public void cloneHandler(ActionEvent actionEvent) {
        CloneDialog dialog = new CloneDialog("Repository", "Remote repository URL"); // TODO history of repositories
        dialog.setContentText("Please provide remote repository URL:");
        Optional<Pair<String, String>> dialogResult = dialog.showAndWait();

        if (dialogResult.isPresent()) {
            Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
                @Override
                protected RemoteOperationValue call() throws Exception {
                    operationProgressBar.setVisible(true);
                    RemoteOperationValue res = remoteRepositoryOperation(
                            () -> MainApp.getRepositoryService().cloneRepository(
                                    dialogResult.get().getFirst(), dialogResult.get().getSecond(), login, pwd,
                                    new DefaultProgressMonitor(d -> updateProgress(d, 1.0))
                            )
                    );
                    return res;
                }
            };
            operationProgressBar.progressProperty().bind(longTask.progressProperty());

            longTask.setOnSucceeded(val -> {
                Platform.runLater(
                        () -> {
                            RemoteOperationValue rval = longTask.getValue();
                            switch (rval.getResult()) {
                                case OK: {
                                    openRepository((String) rval.getValue());
                                    showResult("OK. TODO get info", Alert.AlertType.INFORMATION);
                                    break;
                                }
                                case ERROR: {
                                    showResult("ERROR. TODO get info", Alert.AlertType.ERROR);
                                    break;
                                }
                            }
                            operationProgressBar.progressProperty().unbind();
                            operationProgressBar.setVisible(false);
                        }
                );
            });

            new Thread(longTask).start();

        }
    }

    public void fetchHandler(ActionEvent actionEvent) {
        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> MainApp.getRepositoryService().remoteRepositoryFetch(login, pwd, new DefaultProgressMonitor(d -> updateProgress(d, 1.0)))
                );
            }
        };
        setDefaultSucceedHandler(longTask);
        new Thread(longTask).start();
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
        setDefaultSucceedHandler(longTask);
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
        setDefaultSucceedHandler(longTask);
        new Thread(longTask).start();
    }

    private void setDefaultSucceedHandler(Task<RemoteOperationValue> longTask) {

        operationProgressBar.progressProperty().bind(longTask.progressProperty());
        operationProgressBar.setVisible(true);

        longTask.setOnSucceeded(val -> {
            Platform.runLater(
                    () -> {
                        RemoteOperationValue rval = longTask.getValue();
                        switch (rval.getResult()) {
                            case OK: {
                                showResult("OK. TODO get info", Alert.AlertType.INFORMATION);
                                break;
                            }
                            case ERROR: {
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

    public void openWorkingCopyHandler(ActionEvent actionEvent) throws Exception {
        final FXMLLoader fxmlLoader = new FXMLLoader();
        try (InputStream is = getClass().getResource("/fxml/WorkingCopyPane.fxml").openStream()) {
            final Parent workCopyView = fxmlLoader.load(is);
            final WorkingCopyController workingCopyController = fxmlLoader.getController();
            workingCopyController.open();
            hostPanel.getChildren().removeAll(hostPanel.getChildren());
            hostPanel.getChildren().add(workCopyView);
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
    Optional<Pair<String, String>> dialogResult = null;
    CountDownLatch latch = new CountDownLatch(1);

    private RemoteOperationValue remoteRepositoryOperation(final Supplier<RemoteOperationValue> supplier) {
        boolean ok = false;
        RemoteOperationValue operationValue = null;


        while (!ok) {
            operationValue = supplier.get();
            switch (operationValue.getResult()) {
                case AUTH_REQUIRED: {
                    Platform.runLater(() -> {
                        System.out.println("### AUTH_REQUIRED Dialog " + Thread.currentThread().getName());
                        login = MainApp.getSettingsService().getLogin();
                        dialogResult = new LoginDialog("Login", "Please, provide login and password", login, null)
                                .showAndWait();
                        latch.countDown();
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("### AUTH_REQUIRED " + Thread.currentThread().getName());
                    break;
                }
                case NOT_AUTHORIZED: {
                    Platform.runLater(() -> {
                        System.out.println("### NOT_AUTHORIZED dialog" + Thread.currentThread().getName());
                        dialogResult = new LoginDialog("Login", "Not authorized. Provide correct credentials", login, pwd)
                                .showAndWait();
                        latch.countDown();
                    });
                    try {
                        latch.await();
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
            if (dialogResult != null && dialogResult.isPresent()) {
                login = dialogResult.get().getFirst();
                pwd = dialogResult.get().getSecond();
                MainApp.getSettingsService().saveLogin(login);
                continue;
            } else {
                ok = true;
            }
            dialogResult = null;
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

}
