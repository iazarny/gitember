package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.data.Pair;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by Igor_Azarny on 25 - Feb -2017.
 */
public class CreateTagDialog extends Dialog<Pair<Boolean, String>> {

    private TextField localBranchName;
    private Label localBranchLabel;

    public CreateTagDialog(final String title,
                           final String header,
                           final String defaultLocalName,
                           final boolean hasRemoteUrl
    ) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);

        ButtonType checkoutBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(checkoutBtn, ButtonType.CANCEL);


        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(20, 20, 20, 20));

        final ToggleGroup group = new ToggleGroup();
        RadioButton checkoutRadioButton = new RadioButton("Just create    " );
        checkoutRadioButton.setSelected(true);
        checkoutRadioButton.setToggleGroup(group);
        RadioButton trackRadioButton = new RadioButton("Create and push    " );
        trackRadioButton.setToggleGroup(group);

        if (hasRemoteUrl) {
            grid.add(new Label("Specify the action : "), 0, 0);
            grid.add(checkoutRadioButton, 1, 0);
            grid.add(trackRadioButton, 2, 0);
        }

        localBranchName = new TextField(defaultLocalName);
        HBox.setHgrow(localBranchName, Priority.ALWAYS);
        localBranchLabel = new Label("New tag name : ");

        grid.add(localBranchLabel, 0, 1);
        grid.add(localBranchName, 1, 1, 2, 1);

        this.getDialogPane().setContent(grid);
        grid.requestLayout();

        Platform.runLater(() -> checkoutRadioButton.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == checkoutBtn) {
                return new Pair<>(trackRadioButton.isSelected(), localBranchName.getText());
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());

    }
}