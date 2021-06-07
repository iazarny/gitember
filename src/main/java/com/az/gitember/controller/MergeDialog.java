package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.data.Pair;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Collection;

public class MergeDialog extends Dialog<Pair<String,String>> {

    private final GridPane grid;
    private final TextArea messageText;
    private final ComboBox<String> branchesComboBox;
    private final ChangeListenerHistoryHint changeListenerHistoryHint;

    public MergeDialog(final Collection<String> branches,
                       final String selectedBranch) {
        super();
        this.setTitle("Merge");
        this.setHeaderText("Merge selected branch to working copy");

        final Label branchesLabel;
        final Label messageLabel;


        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setVgap(10);
        this.grid.setPadding(new Insets(10,10,10,10));
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);


        messageLabel =  new Label("Message");
        messageLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);

        this.messageText = new TextArea("");
        this.messageText.setMaxWidth(Double.MAX_VALUE);
        this.changeListenerHistoryHint = new ChangeListenerHistoryHint(this.messageText, Context.settingsProperty.getValue().getCommitMsg());
        GridPane.setHgrow(messageText, Priority.ALWAYS);
        GridPane.setFillWidth(messageText, true);

        branchesLabel =  new Label("Branches");
        branchesLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);


        this.branchesComboBox = new ComboBox<>();
        this.branchesComboBox.setMinWidth(120);
        if (branches != null) {
            this.branchesComboBox.getItems().addAll(branches);
        }
        this.branchesComboBox.setMaxWidth(Double.MAX_VALUE);
        if (selectedBranch == null) {
            this.branchesComboBox.getSelectionModel().selectFirst();
        } else {
            this.branchesComboBox.getSelectionModel().select(selectedBranch);
        }


        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);


        this.grid.getChildren().clear();

        this.grid.add(branchesLabel, 0, 0);
        this.grid.add(branchesComboBox, 1, 0);
        this.grid.add(messageLabel, 0, 1);
        this.grid.add(messageText, 1, 1);

        getDialogPane().setContent(grid);

        Platform.runLater(this.branchesComboBox::requestFocus);


        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(
                        this.branchesComboBox.getSelectionModel().getSelectedItem(),
                        this.messageText.getText()
                );
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());
        Platform.runLater(() -> messageText.requestFocus());


    }
}
