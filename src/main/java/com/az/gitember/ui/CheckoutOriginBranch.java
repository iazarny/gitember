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
public class CheckoutOriginBranch extends Dialog<Pair<Boolean, String>> {

    private TextField localBranchName;
    private Label localBranchLabel;

    public CheckoutOriginBranch(final String title,
                       final String header,
                       final String defaultLocalName) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);

        ButtonType checkoutBtn = new ButtonType("Checkout", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll(checkoutBtn, ButtonType.CANCEL);


        GridPane grid = new GridPane();
        //grid.setGridLinesVisible(true);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        final ToggleGroup group = new ToggleGroup();
        RadioButton checkoutRadioButton = new RadioButton("Just checkout");
        checkoutRadioButton.setSelected(true);
        checkoutRadioButton.setToggleGroup(group);
        RadioButton trackRadioButton = new RadioButton("Create local branch");
        trackRadioButton.setToggleGroup(group);
        group.selectedToggleProperty().addListener(
                observable -> {
                    localBranchName.setVisible(group.getSelectedToggle() == trackRadioButton);
                    localBranchLabel.setVisible(group.getSelectedToggle() == trackRadioButton);
                    if (group.getSelectedToggle() == trackRadioButton) {
                        Platform.runLater(() -> localBranchName.requestFocus());
                    }
                }
        );

        grid.add(new Label("Specify the action : "), 0, 0);
        grid.add(checkoutRadioButton, 1, 0);
        grid.add(trackRadioButton, 2, 0);

        localBranchName = new TextField(defaultLocalName);
        HBox.setHgrow(localBranchName, Priority.ALWAYS);
        localBranchName.setVisible(false);
        localBranchLabel = new Label("Track remote branch with local name : ");
        localBranchLabel.setVisible(false);

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
