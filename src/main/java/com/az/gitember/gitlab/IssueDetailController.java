package com.az.gitember.gitlab;

import com.az.gitember.App;
import com.az.gitember.controller.DefaultProgressMonitor;
import com.az.gitember.controller.TextAreaDialog;
import com.az.gitember.gitlab.model.FxIssue;
import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;

import java.net.URL;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IssueDetailController implements Initializable {


    public Hyperlink numHyperlink;
    public TextField titleText;
    public ComboBox<Member> assigneeCmb;
    public Label authorLabel;
    public CheckBox confidentialCheckBox;
    public Label createdAtDateLabel;
    public DatePicker updatedAtDatePicker;
    public DatePicker dueDatePicker;
    public DatePicker closeDatePicker;
    public TextField closedByText;
    public Label closeDatePickerLabel;
    public Label closedByLabel;
    public TextArea description;
    public Label externalIdText;
    public Label externalIdLabel;
    public Label stateLabel;
    public HBox tagsHBox;
    public Label estimatedLabel;
    public Label estimatedText;
    public Label spendLabel;
    public Label spendText;
    public Label milestoneLabel;
    public ComboBox<Milestone> milestoneCmb;
    public Button addTag;
    public TextField newTag;
    public ColorPicker tagColorPicker;
    public TextField addSpendTimeText;
    public TextField setEstimatedTextText;
    public Button setEstimatedTimeBtn;
    public Button addSpendTimeBtn;
    public VBox comments;
    public Button addNewNoteBtn;


    private GitLabProject gitLabProject;

    private FxIssue fxIssue;
    private ObservableList<Discussion> notes = FXCollections.observableArrayList(new ArrayList<>());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println(fxIssue);
    }

    public void setIssue(final GitLabProject gitLabProject, final FxIssue fxissue) {
        this.fxIssue = fxissue;
        this.gitLabProject = gitLabProject;





        authorLabel.textProperty().setValue(fxIssue.getAuthor());
        createdAtDateLabel.textProperty().setValue(fxIssue.getCreatedAt().toString());

        Bindings.bindBidirectional(confidentialCheckBox.selectedProperty(), fxIssue.confidentialProperty());

        Bindings.bindBidirectional(updatedAtDatePicker.valueProperty(), fxIssue.updatedAtProperty());
        Bindings.bindBidirectional(dueDatePicker.valueProperty(), fxIssue.dueDateProperty());
        Bindings.bindBidirectional(closeDatePicker.valueProperty(), fxIssue.closedAtProperty());
        Bindings.bindBidirectional(closedByText.textProperty(), fxIssue.closedByProperty());
        Bindings.bindBidirectional(description.textProperty(), fxIssue.descriptionProperty());
        numHyperlink.textProperty().setValue(String.valueOf(fxIssue.getIid()));
        Bindings.bindBidirectional(externalIdText.textProperty(), fxIssue.externalIdProperty());
        Bindings.bindBidirectional(stateLabel.textProperty(), fxIssue.stateProperty());
        tagsHBox.setStyle("-fx-spacing: 10");
        GitLabUtil.fillContainerWithLabels(tagsHBox, gitLabProject.getProjectLabels(), fxIssue.getLabels());
        Bindings.bindBidirectional(externalIdText.textProperty(), fxIssue.externalIdProperty());
        externalIdLabel.setVisible(StringUtils.isNoneEmpty(fxIssue.getExternalId()));
        externalIdText.setVisible(StringUtils.isNoneEmpty(fxIssue.getExternalId()));
        Bindings.bindBidirectional(estimatedText.textProperty(), fxIssue.estimatedProperty());
        Bindings.bindBidirectional(spendText.textProperty(), fxIssue.sppendProperty());

        Bindings.bindBidirectional(titleText.textProperty(), fxIssue.titleProperty());


        assigneeCmb.setCellFactory(
                new Callback<ListView<Member>, ListCell<Member>>() {
                    @Override
                    public ListCell<Member> call(ListView<Member> param) {
                        return new ListCell<Member>() {
                            @Override
                            protected void updateItem(Member assignee, boolean empty) {
                                super.updateItem(assignee, empty);
                                setText(empty ? "" : assignee.getName() + " (" + assignee.getUsername() + ")");
                            }
                        };
                    }
                }
        );

        assigneeCmb.setConverter(new StringConverter<Member>() {
            @Override
            public String toString(Member object) {
                return object.getName();
            }

            @Override
            public Member fromString(String string) {
                return assigneeCmb.getItems().stream().filter(a -> a.getName().equals(string)).findFirst().orElse(null);
            }
        });

        assigneeCmb.setItems(FXCollections.observableList(gitLabProject.getActiveUsers()));
        if (fxIssue.getAssignee() != null) {
            gitLabProject.getActiveUsers().stream().filter( m -> m.getName().equals(fxIssue.getAssignee().getName()) ).findFirst().ifPresent( member -> {
                assigneeCmb.setValue(member);
            });
        }
        assigneeCmb.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    updateIssue();
                }
        );


        milestoneCmb.setCellFactory(
                new Callback<ListView<Milestone>, ListCell<Milestone>>() {
                    @Override
                    public ListCell<Milestone> call(ListView<Milestone> param) {
                        return new ListCell<Milestone>() {
                            @Override
                            protected void updateItem(Milestone milestone, boolean empty) {
                                super.updateItem(milestone, empty);
                                setText(empty ? "" : milestone.getTitle());
                            }
                        };
                    }
                }
        );
        milestoneCmb.setConverter(
                new StringConverter<Milestone>() {
                    @Override
                    public String toString(Milestone object) {
                        return object == null ? "" : object.getTitle();
                    }

                    @Override
                    public Milestone fromString(String string) {
                        return gitLabProject.getMilestones().stream()
                                .filter(m -> m.getTitle().equalsIgnoreCase(string))
                                .findFirst()
                                .orElse(null);
                    }
                }
        );
        milestoneCmb.setItems(FXCollections.observableList(gitLabProject.getMilestones()));
        milestoneCmb.setValue(fxissue.getMilestone());
        milestoneCmb.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    fxIssue.setMilestone(newValue);
                    updateIssue();
                }
        );

        notes.addListener(new ListChangeListener<Discussion>() {
            @Override
            public void onChanged(Change<? extends Discussion> change) {
                notes.stream().forEach(
                        d -> {

                            Note n = d.getNotes().get(0);
                            comments.getChildren().add(
                                    new CommentView(fxissue.getIid(), d.getId(), n, true)
                            );
                            for (int i = 1; i < d.getNotes().size(); i++) {
                                Note c = d.getNotes().get(i);
                                comments.getChildren().add(
                                        new CommentView(fxissue.getIid(), d.getId(), c, false)
                                );
                            }
                        }
                );
            }
        });


        numHyperlink.setOnAction(event -> {
            App.getShell().showDocument(fxissue.getWebUrl());
        });

        fxIssue.getLabels().addListener(
                (ListChangeListener<String>) c -> GitLabUtil.fillContainerWithLabels(tagsHBox, gitLabProject.getProjectLabels(), fxIssue.getLabels())
        );

        dueDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateIssue();
        });

        confidentialCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateIssue();
        });

        titleText.textProperty().addListener((observable, oldValue, newValue) -> {
            updateIssue();
        });


        fetchNotes();

    }

    private void fetchNotes() {
        Task<List<Discussion>> longTask = new Task<List<Discussion>>() {
            @Override
            protected List<Discussion> call() throws Exception {
                return gitLabProject.getDiscussion(fxIssue.getIid());
            }

        };

        longTask.setOnFailed(event -> {
            notes.clear();
        });

        longTask.setOnSucceeded(event -> {
            notes.clear();
            notes.setAll((Collection<? extends Discussion>) event.getSource().getValue());
        });

        new Thread(longTask).start();
    }


    private void updateIssue() {
        Object projectIdOrPath = Context.getCurrentProject().getGitLabProjectId();
        List<Integer> assigneeIds = new ArrayList<>();
        if (assigneeCmb.getValue() != null) {
            assigneeIds.add(assigneeCmb.getValue().getId());
        }
        String labels = fxIssue.getLabels().stream().collect(Collectors.joining(","));
        Date dueDate = null;
        if (fxIssue.getDueDate() != null) {
            dueDate = Date.from(fxIssue.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
        Integer mileStoneInteger = null;
        if (fxIssue.getMilestone() != null) {
            mileStoneInteger = fxIssue.getMilestone().getId();
        }


        try {
            Issue issue = gitLabProject.getIssuesApi().updateIssue(
                    projectIdOrPath,
                    fxIssue.getIid(),
                    fxIssue.getTitle(), //
                    fxIssue.getDescription(),
                    fxIssue.isConfidential(), //
                    assigneeIds,//
                    mileStoneInteger, //
                    labels, //
                    Constants.StateEvent.REOPEN,
                    new Date(), //
                    dueDate //
            );
            fxIssue.init(issue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addTag(ActionEvent actionEvent) {
        String newLabel = newTag.getText();

        try {

            if (gitLabProject.getProjectLabels().stream().filter(l -> l.getName().equalsIgnoreCase(newLabel)).findFirst().isEmpty()) {
                org.gitlab4j.api.models.Label newLabelToAdd = new org.gitlab4j.api.models.Label();
                newLabelToAdd.setName(newLabel);
                newLabelToAdd.setColor(GitemberUtil.toRGBCode(tagColorPicker.getValue()));
                gitLabProject.getLabelsApi().createProjectLabel(gitLabProject.getProject().getId(), newLabelToAdd);
            }
            if (fxIssue.getLabels().stream().filter(s -> s.equalsIgnoreCase(newLabel)).findFirst().isEmpty()) {
                fxIssue.getLabels().add(newLabel);
                updateIssue();
            }

            newTag.setText("");

        } catch (GitLabApiException e) {
            Context.getMain().showResult("Error",
                    "Cannot add issue label " + ExceptionUtils.getMessage(e), Alert.AlertType.ERROR);
        }
    }


    public void addSpendTime(ActionEvent actionEvent) {
        try {
            Issue updated = gitLabProject.addSpendTime(fxIssue.getIid(), addSpendTimeText.getText());
            fxIssue.init(updated);
        } catch (GitLabApiException e) {
            Context.getMain().showResult("Error",
                    "Cannot add time " + ExceptionUtils.getMessage(e), Alert.AlertType.ERROR);
        }
    }

    public void setEstimatedTime(ActionEvent actionEvent) {
        try {
            Issue updated = gitLabProject.  setEstimatedTime(fxIssue.getIid(), setEstimatedTextText.getText());
            fxIssue.init(updated);
        } catch (GitLabApiException e) {
            Context.getMain().showResult("Error",
                    "Cannot set estimated time " + ExceptionUtils.getMessage(e), Alert.AlertType.ERROR);
        }
    }

    public void addNewNote(ActionEvent actionEvent) {
        TextAreaDialog textAreaDialog = new TextAreaDialog("");
        //textAreaDialog.initOwner(getScene().getWindow()); //TODO
        textAreaDialog.showAndWait().ifPresent( body -> {
            try {
                Discussion discussion = gitLabProject.createIssueDiscussion(fxIssue.getIid(), body);
                Note n = discussion.getNotes().get(0);
                CommentView cv =  new CommentView(fxIssue.getIid(), discussion.getId(), n, true);
                //todo index comments.getChildren().add();
                comments.getChildren().add(
                        cv
                );
                Platform.runLater( () -> cv.requestFocus());
            } catch (GitLabApiException e) {
                Context.getMain().showResult("Error",
                        "Cannot create new discussion thread " + ExceptionUtils.getMessage(e), Alert.AlertType.ERROR);
            }
        });

    }
}
