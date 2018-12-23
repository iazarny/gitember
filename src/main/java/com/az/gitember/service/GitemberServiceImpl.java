package com.az.gitember.service;

import com.az.gitember.GitemberApp;
import com.az.gitember.misc.*;
import com.az.gitember.scm.exception.GECannotDeleteCurrentBranchException;
import com.az.gitember.scm.exception.GECheckoutConflictException;
import com.az.gitember.scm.exception.GEScmAPIException;
import com.az.gitember.scm.impl.git.DefaultProgressMonitor;
import com.az.gitember.ui.CloneDialog;
import com.az.gitember.ui.CommitDialog;
import com.az.gitember.ui.LoginDialog;
import com.az.gitember.ui.TextAreaInputDialog;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * Created by Igor_Azarny on 26 -Dec -2016.
 */
public class GitemberServiceImpl {

    private final static Logger log = Logger.getLogger(GitemberServiceImpl.class.getName());

    private Optional<Pair<String, String>> uiInputResultToService;
    private CountDownLatch uiInputLatchToService;

    private String login = null;
    private String pwd = null;

    private ToolBar progressBar;
    private ProgressBar operationProgressBar;
    private Label operationName;


    public void setProgressBar(ToolBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setOperationProgressBar(ProgressBar operationProgressBar) {
        this.operationProgressBar = operationProgressBar;
    }

    public void setOperationName(Label operationName) {
        this.operationName = operationName;
    }

    /**
     * Merge given branch to head.
     *
     * @param branchName given branch name
     * @return truen in case if merge was ok
     */
    public boolean mergeToHead(final String branchName) {
        TextAreaInputDialog dialog = new TextAreaInputDialog(
                "Merge " + branchName + " to " + GitemberApp.workingBranch.get().getShortName());
        dialog.setTitle("Merge message");
        dialog.setHeaderText("Provide merge message");
        dialog.setContentText("Message:");
        Optional<String> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {
            try {
                GitemberApp.getRepositoryService().mergeLocalBranch(
                        branchName,
                        dialogResult.get());
                GitemberApp.showResult("Merge result", Alert.AlertType.INFORMATION);
                return true;
            } catch (Exception e) {
                String msg = String.format("Cannot merge local branch %s to %s",
                        branchName,
                        GitemberApp.workingBranch.get().getShortName());
                log.log(Level.SEVERE, msg, e);
                GitemberApp.showException(msg, e);
            }
        }
        return false;
    }



    /**
     * Checkout given branch into working copy.
     *
     * @param fullName given branch.
     * @param newLocalName optional , new local branch wil be created if given
     */
    public void checkout(final String fullName, final String newLocalName) {
        GitemberApp.getMainStage().getScene().setCursor(Cursor.WAIT);

        Task<Void> longTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                GitemberApp.getRepositoryService().checkoutLocalBranch(fullName, newLocalName);
                return null;
            }
        };

        longTask.setOnSucceeded(
                s -> Platform.runLater(
                        () -> {
                            GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                            try {
                                GitemberApp.setWorkingBranch(
                                        GitemberApp.getRepositoryService().getScmBranchByName(fullName)
                                );
                                GitemberApp.showResult("Branch " + fullName
                                        + " checked out", Alert.AlertType.INFORMATION);
                            } catch (Exception e) {
                                log.log(Level.WARNING, "Checkout failed" + fullName, e);
                            }
                        }
                )
        );

        longTask.setOnFailed(
                f -> Platform.runLater(
                        () -> {
                            GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                            Throwable ex = f.getSource().getException();
                            if (ex instanceof GECheckoutConflictException) {
                                GECheckoutConflictException e = (GECheckoutConflictException) ex;
                                String msg = String.format("Cannot checkout branch %s. List of conflicting files:\n%s",
                                        fullName,
                                        e.getConflicting().stream().collect(Collectors.joining("\n"))
                                );
                                log.log(Level.WARNING, msg, e);
                                GitemberApp.showResult(msg, Alert.AlertType.ERROR);
                            } else if (ex instanceof Exception) {
                                final String msg = "Cannot checkout branch " + fullName;
                                log.log(Level.SEVERE, msg, ex);
                                GitemberApp.showException(msg, ex);
                            }
                        }
                )
        );

        new Thread(longTask).start();
    }

    /**
     * Rebase . Integrate changes from given branch  into working copy
     *
     * @param fullName of branch.
     */
    public void rebase(final String fullName) {


        Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
            @Override
            protected RemoteOperationValue call() throws Exception {
                return remoteRepositoryOperation(
                        () -> GitemberApp.getRepositoryService().rebase(
                                fullName,
                                new DefaultProgressMonitor((t, d) -> {
                                    updateTitle(t);
                                    updateProgress(d, 1.0);
                                }
                                )
                        )
                );
            }
        };
        prepareLongTask(longTask, null, null);
        new Thread(longTask).start();


    }


    public boolean commit(ScmBranch brnch) {
        CommitDialog dialog = new CommitDialog(
                "",
                GitemberApp.getRepositoryService().getUserName(),
                GitemberApp.getRepositoryService().getUserEmail(),
                false,
                GitemberApp.getSettingsService().read().getCommitMessages()

        );
        dialog.setTitle("Commit message");
        dialog.setHeaderText("Provide commit message");
        dialog.setContentText("Message:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                Settings settings = GitemberApp.getSettingsService().read();
                String msg = result.get();
                settings.getCommitMessages().add(msg);
                GitemberApp.getSettingsService().save(settings);

                GitemberApp.getRepositoryService().setUserEmail(dialog.getUserEmail());
                GitemberApp.getRepositoryService().setUserName(dialog.getUserName());

                GitemberApp.getRepositoryService().commit(
                        result.get(),
                        settings.isOverwriteAuthorWithCommiter());
                return true;

            } catch (GEScmAPIException e) {
                GitemberApp.showResult("Cannot commit, because: " + e.getMessage(), Alert.AlertType.ERROR);
                log.log(Level.SEVERE, "Cannot commit", e);
            }
        }

        return false;
    }


    public boolean createNewBranchFrom(String name) {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("New branch");
        dialog.setHeaderText("Create new branch");
        dialog.setContentText("Please enter new branch name:");

        Optional<String> dialogResult = dialog.showAndWait();

        if (dialogResult.isPresent()) {

            try {
                GitemberApp.getRepositoryService().createLocalBranch(
                        name,
                        dialogResult.get());
                return true;

            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot create new local branch " + name, e);
            }
        }

        return false;
    }

    /**
     * @param scmBranch
     * @return in case if new local branch was created.
     */
    public boolean pushBranch(ScmBranch scmBranch) {
        final String remoteName = scmBranch.getRemoteName();

        if (remoteName == null) {
            TextInputDialog dialog = new TextInputDialog(scmBranch.getShortName());
            dialog.setTitle("Branch name");
            dialog.setHeaderText("Remote branch will be created");
            dialog.setContentText("Please enter new remote branch name:");

            Optional<String> dialogResult = dialog.showAndWait();
            if (dialogResult.isPresent()) {
                scmBranch.setRemoteName(dialogResult.get());
                pushToRemoteRepository(scmBranch.getShortName(), scmBranch.getRemoteName(), true, true);
                return true;
            }
            return false;
        } else {
            pushToRemoteRepository(scmBranch.getShortName(), scmBranch.getRemoteName(), true, false);
        }
        return false;
    }


    public void deleteRemoteBranch(ScmBranch scmBranch,
                                   Consumer<RemoteOperationValue> operationRezConsumer,
                                   Consumer<RemoteOperationValue> operationErrConsumer) {

        Optional<ButtonType> dialogResult = getDeleteBranchConfirmDialogValue(scmBranch);
        if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
            String nameOnRemoteSide = Constants.R_HEADS + scmBranch.getShortName();
            RefSpec refSpec = new RefSpec().setSource(null).setDestination(nameOnRemoteSide);
            Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
                @Override
                protected RemoteOperationValue call() throws Exception {
                    return remoteRepositoryOperation(
                            () -> GitemberApp.getRepositoryService().remoteRepositoryPush(
                                    login, pwd, false,
                                    new DefaultProgressMonitor((t, d) -> {
                                        updateTitle(t);
                                        updateProgress(d, 1.0);
                                    }), refSpec)
                    );
                }
            };
            prepareLongTask(longTask, operationRezConsumer, operationErrConsumer);
            new Thread(longTask).start();
        }
    }

    public void deleteLocalBranch(ScmBranch scmBranch) {

        Optional<ButtonType> dialogResult = getDeleteBranchConfirmDialogValue(scmBranch);
        if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
            try {
                GitemberApp.getRepositoryService().deleteLocalBranch(scmBranch.getFullName());
            } catch (GECannotDeleteCurrentBranchException e) {
                GitemberApp.showResult(e.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception e) {
                String msg = "Cannot delete local branch " + scmBranch.getFullName();
                log.log(Level.SEVERE, msg, e);
                GitemberApp.showException(msg, e);
            }
        }
    }

    private Optional<ButtonType> getDeleteBranchConfirmDialogValue(ScmBranch scmBranch) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText(String.format("Delete %s %s", scmBranch.getBranchType().getTypeName(),
                scmBranch.getShortName()));
        alert.setContentText(String.format("Do you really want to delete %s %s ?",
                scmBranch.getBranchType().getTypeName(), scmBranch.getShortName()));

        return alert.showAndWait();
    }

    public void applyStash(ScmRevisionInformation ri) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText(String.format("Apply stash", ri.getShortMessage()));
        alert.setContentText(String.format("Do you really want to apply %s stash ?", ri.getShortMessage()));

        Optional<ButtonType> dialogResult = alert.showAndWait();
        if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
            try {
                GitemberApp.getRepositoryService().applyStash(ri.getRevisionFullName());
            } catch (GEScmAPIException e) {
                log.log(Level.SEVERE, "Cannot apply stash " + ri, e);
                GitemberApp.showResult("Cannot apply stash " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }


    public void pushToRemoteRepository(String localBranchName, String remoteBranchName,
                                       boolean setOrigin, boolean setTrackeRemote) {
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

    /**
     * Process remote operations with failback
     *
     * @param supplier remote repository command.
     * @return RemoteOperationValue which shall be interpret by caller
     */
    public RemoteOperationValue remoteRepositoryOperation(final Supplier<RemoteOperationValue> supplier) {
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
                            String okInfo = rval.getValue().toString(); //todo rval value can be not an string.
                            GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                            GitemberApp.showResult(okInfo, Alert.AlertType.INFORMATION);
                            break;
                        }
                        case ERROR: {
                            if (onError != null) {
                                onError.accept(rval);
                            }
                            String errInfo = rval.getValue().toString(); //todo rval value can be not an string.
                            GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
                            GitemberApp.showResult(errInfo, Alert.AlertType.ERROR);
                            break;
                        }
                    }
                    operationProgressBar.progressProperty().unbind();
                    operationName.textProperty().unbind();
                    progressBar.setVisible(false);
                }
        ));
    }

    public boolean deleteStash(ScmRevisionInformation ri, int index) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText(String.format("Delete stash", ri.getShortMessage()));
        alert.setContentText(String.format("Do you really want to delete %s stash ?", ri.getShortMessage()));

        Optional<ButtonType> dialogResult = alert.showAndWait();
        if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {
            try {
                GitemberApp.getRepositoryService().deleteStash(index);
                return true;

            } catch (GEScmAPIException e) {
                log.log(Level.SEVERE, "Cannot delete stash " + ri, e);
                GitemberApp.showResult("Cannot delete stash " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
        return false;
    }

    public void fetch(final String branchName) {
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

    public void pull(final String branchName) {
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

    public void pushAll() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Push all");
        alert.setContentText("Do you really want to push all branches ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            GitemberApp.getGitemberService().pushToRemoteRepository("+refs/heads/*", "refs/heads/*", false, false);
        } else {
            GitemberApp.getMainStage().getScene().setCursor(Cursor.DEFAULT);
        }
    }

    public void cloneRepo(final Consumer<RemoteOperationValue> onOk,
                          final Consumer<RemoteOperationValue> onError) {
        final Settings settings = GitemberApp.getSettingsService().read();
        final Set<String> urls = new TreeSet<>(settings.getGiturls());
        CloneDialog dialog = new CloneDialog(
                "Repository",
                "Remote repository URL",
                urls);
        dialog.setContentText("Please provide remote repository URL:");
        Optional<Pair<String, String>> dialogResult = dialog.showAndWait();
        if (dialogResult.isPresent()) {

            String url = dialogResult.get().getFirst();
            GitemberApp.remoteUrl.setValue(url);
            urls.add(url);
            settings.getGiturls().clear();
            settings.getGiturls().addAll(urls);
            GitemberApp.getSettingsService().save(settings);


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
            prepareLongTask(longTask, onOk, onError);
            new Thread(longTask).start();
        }
    }

    public void createRepo(final Consumer<String> onOk) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(GitemberApp.getSettingsService().getUserHomeFolder()));
        final File selectedDirectory =
                directoryChooser.showDialog(GitemberApp.getMainStage());
        if (selectedDirectory != null) {
            String absPath = selectedDirectory.getAbsolutePath();
            try {
                GitemberApp.getRepositoryService().createRepository(absPath);
                String repoPath = absPath + File.separator + Const.GIT_FOLDER;
                log.log(Level.INFO, "New repository was created - " + absPath);
                onOk.accept(repoPath);
            } catch (Exception e) {
                log.log(Level.SEVERE, "Cannot create repository", e);
                GitemberApp.showException("Cannot create repository", e);
            }

        }
    }

    public void makeBranchOperation(String title, String header, ThrowingConsumer<String> op) {
        final ArrayList<String> branches = new ArrayList<>();
        try {
            GitemberApp.getRepositoryService().getLocalBranches().forEach(b -> branches.add(b.getFullName()));
            GitemberApp.getRepositoryService().getTags().forEach(b -> branches.add(b.getFullName()));
            GitemberApp.getRepositoryService().getRemoteBranches().forEach(b -> branches.add(b.getFullName()));

            ChoiceDialog<String> dialog = new ChoiceDialog<>(branches.get(0), branches);
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText("Choose branch:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                op.accept(result.get());
            }
        } catch (RuntimeException e) {
            if (e.getCause() != null) {
                Throwable ex = e.getCause();
                if (ex instanceof GECheckoutConflictException) {
                    GitemberApp.showResult(ex.getMessage(), Alert.AlertType.WARNING);
                } else {
                    GitemberApp.showException("Error :(", ex);
                    log.log(Level.SEVERE, e.getMessage(), ex);
                }
            }
        }
    }

    public void createStatReport() {
        try {
            GitemberApp.getRepositoryService().blame(
                    GitemberApp.workingBranch.get().getFullName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void compressDatabase() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Please confirm");
        alert.setHeaderText("Cleanup database");
        alert.setContentText("Do you really want to remove unused objects from database ? It may take some time.");

        Optional<ButtonType> dialogResult = alert.showAndWait();
        if (dialogResult.isPresent() && dialogResult.get() == ButtonType.OK) {


            Task<RemoteOperationValue> longTask = new Task<RemoteOperationValue>() {
                @Override
                protected RemoteOperationValue call() throws Exception {
                    return remoteRepositoryOperation(
                            () -> GitemberApp.getRepositoryService().compressDatabase(
                                    new DefaultProgressMonitor((t, d) -> {
                                        updateTitle(t);
                                        updateProgress(d, 1.0);
                                    }
                                    )
                            )
                    );
                }
            };
            prepareLongTask(longTask, null, null);
            new Thread(longTask).start();
        }
    }
}
