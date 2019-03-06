package com.az.gitember.ui;

import com.az.gitember.misc.Pair;
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
                           final String firstRadioText,
                           final String secondRadioText
    ) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);

        ButtonType checkoutBtn = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(checkoutBtn, ButtonType.CANCEL);


        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        final ToggleGroup group = new ToggleGroup();
        RadioButton checkoutRadioButton = new RadioButton(firstRadioText);
        checkoutRadioButton.setSelected(true);
        checkoutRadioButton.setToggleGroup(group);
        RadioButton trackRadioButton = new RadioButton(secondRadioText);
        trackRadioButton.setToggleGroup(group);


        grid.add(new Label("Specify the action : "), 0, 0);
        grid.add(checkoutRadioButton, 1, 0);
        grid.add(trackRadioButton, 2, 0);

        localBranchName = new TextField(defaultLocalName);
        HBox.setHgrow(localBranchName, Priority.ALWAYS);
        localBranchLabel = new Label("New tag name : ");

        grid.add(localBranchLabel, 0, 1);
        grid.add(localBranchName, 1, 1, 2, 1);

        this.getDialogPane().setContent(grid);

        Platform.runLater(() -> checkoutRadioButton.requestFocus());

        this.setResultConverter(dialogButton -> {
            if (dialogButton == checkoutBtn) {
                return new Pair<>(trackRadioButton.isSelected(), localBranchName.getText());
            }
            return null;
        });

    }
}
