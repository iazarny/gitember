package com.az.gitember.gitlab;

import com.az.gitember.gitlab.model.FxIssue;
import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.Assignee;
import org.gitlab4j.api.models.Issue;
import org.gitlab4j.api.models.Project;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class IssueDetailController implements Initializable {



    public Label numLabel;
    public Label titleLabel;
    public ComboBox<Assignee> assigneeCmb;
    public TextField authorText;
    public CheckBox confidentialCheckBox;
    public DatePicker createdAtDatePicker;
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
    public ComboBox milestoneCmb;
    public Button addTag;
    public TextField newTag;
    public ColorPicker tagColorPicker;
    private FxIssue fxIssue;
    private GitLabProject gitLabProject;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println(fxIssue);
    }

    public void setIssue(final GitLabProject gitLabProject, final Issue issue) {
        this.fxIssue = new FxIssue(issue);
        this.gitLabProject = gitLabProject;

        assigneeCmb.setItems(fxIssue.getAssignees());
        assigneeCmb.setConverter(new StringConverter<Assignee>() {
            @Override
            public String toString(Assignee object) {
                return object.getName();
            }

            @Override
            public Assignee fromString(String string) {
                return assigneeCmb.getItems().stream().filter( a -> a.getName().equals(string) ).findFirst().orElse(null);
            }
        });

        Bindings.bindBidirectional(assigneeCmb.valueProperty(), fxIssue.assigneeProperty());
        Bindings.bindBidirectional(authorText.textProperty(), fxIssue.authorProperty());
        Bindings.bindBidirectional(confidentialCheckBox.selectedProperty(), fxIssue.confidentialProperty());
        Bindings.bindBidirectional(createdAtDatePicker.valueProperty(), fxIssue.createdAtProperty());
        Bindings.bindBidirectional(updatedAtDatePicker.valueProperty(), fxIssue.updatedAtProperty());
        Bindings.bindBidirectional(dueDatePicker.valueProperty(), fxIssue.dueDateProperty());
        Bindings.bindBidirectional(closeDatePicker.valueProperty(), fxIssue.closedAtProperty());
        Bindings.bindBidirectional(closedByText.textProperty(), fxIssue.closedByProperty());
        Bindings.bindBidirectional(description.textProperty(), fxIssue.descriptionProperty());
        numLabel.textProperty().setValue(String.valueOf(fxIssue.iidProperty()));
        Bindings.bindBidirectional(externalIdText.textProperty(), fxIssue.externalIdProperty());
        Bindings.bindBidirectional(stateLabel.textProperty(), fxIssue.stateProperty());
        tagsHBox.setStyle("-fx-spacing: 10");
        GitLabUtil.fillContainerWithLabels(tagsHBox, gitLabProject.getProjectLabels(), fxIssue.getLabels());
        Bindings.bindBidirectional(estimatedText.textProperty(), fxIssue.estimatedProperty());
        Bindings.bindBidirectional(spendText.textProperty(), fxIssue.sppendProperty());
        Bindings.bindBidirectional(titleLabel.textProperty(), fxIssue.titleProperty());

        fxIssue.getLabels().addListener(
                new ListChangeListener<String>() {
                    @Override
                    public void onChanged(Change<? extends String> c) {
                        GitLabUtil.fillContainerWithLabels(tagsHBox, gitLabProject.getProjectLabels(), fxIssue.getLabels());
                    }
                }
        );

        dueDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            updateIssue();
        });




    }



    private void updateIssue() {
        Object projectIdOrPath = Context.getCurrentProject().getGitLabProjectId();
        List<Integer> assigneeIds = new ArrayList<>();
        fxIssue.getAssignees().forEach( a -> assigneeIds.add(a.getId()));
        String labels = fxIssue.getLabels().stream().collect(Collectors.joining(","));
        Date dueDate = null;
        Integer mileStone = null;
        if (fxIssue.getMilestone() != null) {
            mileStone = Integer.parseInt(fxIssue.getMilestone());
        }


        try {
            Issue issue = gitLabProject.getIssuesApi().updateIssue(
                    projectIdOrPath,
                    fxIssue.getIid(),
                    fxIssue.getTitle(),
                    fxIssue.getDescription(),
                    fxIssue.isConfidential(),
                    assigneeIds,
                    mileStone,
                    labels,
                    Constants.StateEvent.REOPEN,
                    new Date(),
                    dueDate
            );
            fxIssue.init(issue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public void addTag(ActionEvent actionEvent) {
        String newLabel = newTag.getText();

        try {

            if (gitLabProject.getProjectLabels().stream().filter( l -> l.getName().equalsIgnoreCase(newLabel)).findFirst().isEmpty()) {
                org.gitlab4j.api.models.Label newLabelToAdd = new org.gitlab4j.api.models.Label();
                newLabelToAdd.setName(newLabel);
                newLabelToAdd.setColor(GitemberUtil.toRGBCode(tagColorPicker.getValue()));
                gitLabProject.getLabelsApi().createProjectLabel(gitLabProject.getProject().getId(), newLabelToAdd);
            }
            if (fxIssue.getLabels().stream().filter( s->s.equalsIgnoreCase(newLabel)).findFirst().isEmpty()) {
                fxIssue.getLabels().add(newLabel);
                updateIssue();
            }

            newTag.setText("");

        } catch (GitLabApiException e) {
            e.printStackTrace();
        }
    }



}
