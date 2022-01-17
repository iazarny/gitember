package com.az.gitember.gitlab;

import com.az.gitember.gitlab.model.FxIssue;
import javafx.beans.binding.Bindings;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.gitlab4j.api.models.Issue;

import java.net.URL;
import java.util.ResourceBundle;

public class IssueDetailController implements Initializable {

    public Label numLabel;
    public Label titleLabel;
    public ComboBox assigneeCmb;
    private FxIssue fxIssue;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println(fxIssue);
    }

    public void setIssue(final Issue issue) {
        this.fxIssue = new FxIssue(issue);

        assigneeCmb.setItems(fxIssue.getAssignees());
        Bindings.bindBidirectional(assigneeCmb.valueProperty(), fxIssue.assigneeProperty()
        );

        Bindings.bindBidirectional(titleLabel.textProperty(), fxIssue.titleProperty());

    }

}
