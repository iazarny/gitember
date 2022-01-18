package com.az.gitember.gitlab;

import com.az.gitember.gitlab.model.FxIssue;
import javafx.beans.binding.Bindings;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.gitlab4j.api.models.Assignee;
import org.gitlab4j.api.models.Issue;

import java.net.URL;
import java.util.ResourceBundle;

public class IssueDetailController implements Initializable {

    public Label numLabel;
    public Label titleLabel;
    public ComboBox<Assignee> assigneeCmb;
    public TextField authorText;
    public CheckBox confidentialCheckBox;
    public DatePicker createdAtDatePicker;
    public DatePicker updatedAtDatePicker;
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




        Bindings.bindBidirectional(titleLabel.textProperty(), fxIssue.titleProperty());

    }

}
