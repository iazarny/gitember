package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.data.MergeDialogResult;
import com.az.gitember.data.Pair;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.eclipse.jgit.api.MergeCommand;

import java.util.Collection;

public class MergeDialog extends Dialog<MergeDialogResult> {

    private static boolean lastSquash = true;
    private static MergeCommand.FastForwardMode lastFFmode = MergeCommand.FastForwardMode.FF;

    public MergeDialog(final Collection<String> branches,
                       final String selectedBranch) {
        super();
        setTitle("Merge");
        setHeaderText("Merge selected branch to working copy");
        getDialogPane().getStyleClass().add("text-input-dialog");

        final VBox vbox = new VBox();
        vbox.setSpacing(10);

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
        new ChangeListenerHistoryHint(messageText, Context.settingsProperty.getValue().getCommitMsg());
        GridPane.setHgrow(messageText, Priority.ALWAYS);
        GridPane.setFillWidth(messageText, true);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final CheckBox squashCheckBox = new CheckBox("Squash");
        squashCheckBox.setSelected(lastSquash);

        final ComboBox<MergeCommand.FastForwardMode> ffModeComboBox = new ComboBox<>();
        ffModeComboBox.setMaxWidth(Double.MAX_VALUE);
        ObservableList<MergeCommand.FastForwardMode> ffModes = ffModeComboBox.getItems();
        ffModes.add(MergeCommand.FastForwardMode.FF);
        ffModes.add(MergeCommand.FastForwardMode.NO_FF);
        ffModes.add(MergeCommand.FastForwardMode.FF_ONLY);
        ffModeComboBox.setCellFactory(param -> new ListCell<MergeCommand.FastForwardMode>() {
            @Override
            protected void updateItem(MergeCommand.FastForwardMode item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    String text = convert(item);
                    setText(text);
                }
            }
        });
        ffModeComboBox.setConverter(new StringConverter<MergeCommand.FastForwardMode>() {
            @Override
            public String toString(MergeCommand.FastForwardMode object) {
                return object == null ? "" : convert(object);
            }

            @Override
            public MergeCommand.FastForwardMode fromString(String string) {
                return MergeCommand.FastForwardMode.valueOf(string);
            }
        });
        ffModeComboBox.setValue(lastFFmode);

        vbox.getChildren().add(new Label("Branches"));
        vbox.getChildren().add(branchesComboBox);
        vbox.getChildren().add(new Label("Message"));
        vbox.getChildren().add(messageText);
        vbox.getChildren().add(squashCheckBox);
        vbox.getChildren().add(new Label("Fast Forward Mode"));
        vbox.getChildren().add(ffModeComboBox);

        getDialogPane().setContent(vbox);

        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                lastSquash = squashCheckBox.isSelected();
                lastFFmode = MergeCommand.FastForwardMode.FF; //TODO from cmb
                return new MergeDialogResult(
                        branchesComboBox.getSelectionModel().getSelectedItem(),
                        messageText.getText(),
                        false,
                        MergeCommand.FastForwardMode.FF
                );

            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());
        Platform.runLater(() -> messageText.requestFocus());

    }

    private String convert(MergeCommand.FastForwardMode item) {
        String text;
        switch (item) {
            case FF:
                text = "Fast Forward";
                break;
            case NO_FF:
                text = "No Fast Forward";
                break;
            case FF_ONLY:
                text = "Fast Forward Only";
                break;
            default:
                text = item.toString();
        }
        return  text;
    }
}
