package com.az.gitember.gitlab;

import com.az.gitember.gitlab.model.FxIssue;
import com.az.gitember.service.Context;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
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
    private FxIssue fxIssue;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println(fxIssue);
    }

    public void setIssue(final Issue issue) {
        this.fxIssue = new FxIssue(issue);

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
        Bindings.bindBidirectional(numLabel.textProperty(), fxIssue.iidProperty());
        Bindings.bindBidirectional(externalIdText.textProperty(), fxIssue.externalIdProperty());
        Bindings.bindBidirectional(stateLabel.textProperty(), fxIssue.stateProperty());

        tagsHBox.setStyle("-fx-spacing: 10");
        fxIssue.getLabels().stream().forEach(
                s -> {
                    Label tagLabel = new Label(s);
                    HBox bg = new HBox(tagLabel);
                    bg.setStyle("-fx-padding: 5px; -fx-background-color: #40400f");
                    tagsHBox.getChildren().add(bg);
                }
        );

        Bindings.bindBidirectional(estimatedText.textProperty(), fxIssue.estimatedProperty());
        Bindings.bindBidirectional(spendText.textProperty(), fxIssue.sppendProperty());
        Bindings.bindBidirectional(titleLabel.textProperty(), fxIssue.titleProperty());

        fxIssue.getLabels().addListener(
                new ListChangeListener<String>() {
                    @Override
                    public void onChanged(Change<? extends String> c) {
                        System.out.println(c);
                    }
                }
        );

        addTag.setOnAction(event -> {
            System.out.println("new tag " + newTag.getText());



            org.gitlab4j.api.models.Label newLabelToAdd = new org.gitlab4j.api.models.Label();
            newLabelToAdd.setName(newTag.getText());
            newLabelToAdd.setColor("#AA8080");
            try {
                Object projectIdOrPath = Context.getCurrentProject().getGitLabProjectId();

                Project project = getProjectApi().getProject(projectIdOrPath);

                //
                List<org.gitlab4j.api.models.Label> allProjectLabels = getLabelsApi().getProjectLabels(projectIdOrPath);
                if (allProjectLabels.stream().filter( l -> l.getName().equalsIgnoreCase(newLabelToAdd.getName())).findFirst().isEmpty()) {
                    getLabelsApi().createProjectLabel(project.getId(), newLabelToAdd);
                }
                if (fxIssue.getLabels().stream().filter( s->s.equalsIgnoreCase(newLabelToAdd.getName())).findFirst().isEmpty()) {
                    //iapi.updateIssue(projectIdOrPath, fxIssue.getId(), )
                    fxIssue.getLabels().add(newLabelToAdd.getName());
                    updateIssue();
                }


            } catch (GitLabApiException e) {
                e.printStackTrace();
            }
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
            Issue issue = getIssuesApi().updateIssue(
                    projectIdOrPath,
                    fxIssue.getId(),
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
            System.out.println(issue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ProjectApi projectApi = null;
    private IssuesApi issuesApi = null;
    private LabelsApi labelsApi = null;

    private LabelsApi getLabelsApi() {
        if (labelsApi == null) {
            GitLabApi glApi = Context.getGitLabApi();
            labelsApi = new LabelsApi(glApi);
        }
        return labelsApi;
    }

    private IssuesApi getIssuesApi() {
        if (issuesApi == null) {
            GitLabApi glApi = Context.getGitLabApi();
            issuesApi = new IssuesApi(glApi);
        }
        return issuesApi;
    }

    private ProjectApi getProjectApi() {
        if (projectApi == null) {
            GitLabApi glApi = Context.getGitLabApi();

            projectApi  = new ProjectApi(glApi);
        }
        return projectApi;
    }





}
