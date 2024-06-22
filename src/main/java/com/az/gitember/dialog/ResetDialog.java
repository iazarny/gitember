package com.az.gitember.dialog;

import com.az.gitember.App;
import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.data.MergeDialogResult;
import com.az.gitember.data.ResetDialogResult;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.Collection;

public class ResetDialog extends Dialog<ResetDialogResult> {

    private static ResetCommand.ResetType lastResetType = ResetCommand.ResetType.HARD;

    public ResetDialog(RevCommit commit) {
        super();
        setTitle("Reset");
        setHeaderText("Would you like to reset the current branch to " + commit.getName().substring(0,6) + " commit ?");
        getDialogPane().getStyleClass().add("text-input-dialog");

        final VBox vbox = new VBox();
        vbox.setSpacing(10);

        final ComboBox<ResetCommand.ResetType> resetTypeComboBox = new ComboBox<>();
        resetTypeComboBox.setMaxWidth(Double.MAX_VALUE);

        ObservableList<ResetCommand.ResetType> ffModes = resetTypeComboBox.getItems();
        ffModes.add(ResetCommand.ResetType.SOFT);
        ffModes.add(ResetCommand.ResetType.MIXED);
        ffModes.add(ResetCommand.ResetType.HARD);

        resetTypeComboBox.setCellFactory(param -> new ListCell<ResetCommand.ResetType>() {
            @Override
            protected void updateItem(ResetCommand.ResetType item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    String text = convert(item);
                    setText(text);
                }
            }
        });
        resetTypeComboBox.setConverter(new StringConverter<ResetCommand.ResetType>() {
            @Override
            public String toString(ResetCommand.ResetType object) {
                return object == null ? "" : convert(object);
            }

            @Override
            public ResetCommand.ResetType fromString(String string) {
                return ResetCommand.ResetType.valueOf(string);
            }
        });
        resetTypeComboBox.setValue(lastResetType);

        vbox.getChildren().add(new Label("Reset Mode"));
        vbox.getChildren().add(resetTypeComboBox);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        getDialogPane().setContent(vbox);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                lastResetType = resetTypeComboBox.getSelectionModel().getSelectedItem();
                return new ResetDialogResult(
                        resetTypeComboBox.getSelectionModel().getSelectedItem()
                );

            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());
        Platform.runLater(() -> resetTypeComboBox.requestFocus());

    }

    private String convert(ResetCommand.ResetType item) {

        String text;
        switch (item) {
            case SOFT:
                text = "Soft. Just change the ref, the index and workdir are not changed";
                break;
            case MIXED:
                text = "Mixed. Change the ref and the index, the workdir is not changed";
                break;
            case HARD:
                text = "Hard. Change the ref, the index and the workdir";
                break;
            default:
                text = item.toString();
        }
        return  text;
    }
}
