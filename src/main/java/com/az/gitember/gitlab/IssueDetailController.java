package com.az.gitember.gitlab;

import com.az.gitember.App;
import com.az.gitember.gitlab.model.FxIssue;
import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.ObjectUtils;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.Assignee;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Milestone;
import org.gitlab4j.api.models.Project;

import java.net.URL;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class IssueDetailController implements Initializable {


    public Hyperlink numHyperlink;
    public TextField titleText;
    public ComboBox<Assignee> assigneeCmb;
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
    public TextField externalIdText;
    public Label externalIdLabel;
    public Label stateLabel;
    public HBox tagsHBox;
    public Label estimatedLabel;
    public TextField estimatedText;
    public Label spendLabel;
    public TextField spendText;
    public Label milestoneLabel;
    public ComboBox<Milestone> milestoneCmb;
    public Button addTag;
    public TextField newTag;
    public ColorPicker tagColorPicker;
    private FxIssue fxIssue;
    private GitLabProject gitLabProject;


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
        Bindings.bindBidirectional(estimatedText.textProperty(), fxIssue.estimatedProperty());
        Bindings.bindBidirectional(spendText.textProperty(), fxIssue.sppendProperty());
        Bindings.bindBidirectional(titleText.textProperty(), fxIssue.titleProperty());


        assigneeCmb.setCellFactory(
                new Callback<ListView<Assignee>, ListCell<Assignee>>() {
                    @Override
                    public ListCell<Assignee> call(ListView<Assignee> param) {
                        return new ListCell<Assignee>() {
                            @Override
                            protected void updateItem(Assignee assignee, boolean empty) {
                                super.updateItem(assignee, empty);
                                setText(empty ? "" : assignee.getName());
                            }
                        };
                    }
                }
        );
        assigneeCmb.setConverter(new StringConverter<Assignee>() {
            @Override
            public String toString(Assignee object) {
                return object.getName();
            }

            @Override
            public Assignee fromString(String string) {
                return assigneeCmb.getItems().stream().filter(a -> a.getName().equals(string)).findFirst().orElse(null);
            }
        });
        //assigneeCmb.setItems(FXCollections.observableList(gitLabProject.getUserApi().getActiveUsers()));
        //assigneeCmb.setValue(); //!!!!!!!!!!

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


    }


    private void updateIssue() {
        Object projectIdOrPath = Context.getCurrentProject().getGitLabProjectId();
        List<Integer> assigneeIds = new ArrayList<>();
        fxIssue.getAssignees().forEach(a -> assigneeIds.add(a.getId()));
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
                    assigneeIds,
                    mileStoneInteger, //
                    labels, //
                    Constants.StateEvent.REOPEN,
                    new Date(),
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
            e.printStackTrace();
        }
    }


}
