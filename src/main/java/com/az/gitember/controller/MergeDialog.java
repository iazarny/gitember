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

import java.util.Collection;

public class MergeDialog extends Dialog<Pair<String,String>> {

    public MergeDialog(final Collection<String> branches,
                       final String selectedBranch) {
        super();
        setTitle("Merge");
        setHeaderText("Merge selected branch to working copy");
        getDialogPane().getStyleClass().add("text-input-dialog");

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10,10,10,10));
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);

        final ComboBox<String> branchesComboBox = new ComboBox<>();
        branchesComboBox.setMinWidth(120);
        if (branches != null) {
            branchesComboBox.getItems().addAll(branches);
        }
        branchesComboBox.setMaxWidth(Double.MAX_VALUE);
        if (selectedBranch == null) {
            branchesComboBox.getSelectionModel().selectFirst();
        } else {
            branchesComboBox.getSelectionModel().select(selectedBranch);
        }

        final TextArea messageText = new TextArea("Merge " + branchesComboBox.getSelectionModel().getSelectedItem() );
        messageText.setMaxWidth(Double.MAX_VALUE);
        new ChangeListenerHistoryHint(messageText, Context.settingsProperty.getValue().getCommitMsg());
        GridPane.setHgrow(messageText, Priority.ALWAYS);
        GridPane.setFillWidth(messageText, true);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        grid.getChildren().clear();
        grid.add(new Label("Branches"), 0, 0);
        grid.add(branchesComboBox, 1, 0);
        grid.add(new Label("Message"), 0, 1);
        grid.add(messageText, 1, 1);

        getDialogPane().setContent(grid);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<String, String>(
                        branchesComboBox.getSelectionModel().getSelectedItem(),
                        messageText.getText()
                );
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());
        Platform.runLater(() -> messageText.requestFocus());

    }
}
