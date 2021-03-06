package com.az.gitember.controller;


import com.az.gitember.App;
import com.az.gitember.control.ChangeListenerHistoryHint;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by Igor_Azarny on 25 Dec 2016.
 */
public class CommitDialog extends Dialog<String> {


    /**************************************************************************
     *
     * Fields
     *
     **************************************************************************/

    private final GridPane grid;
    private final Label label;
    private final TextArea textField;
    private final String defaultValue;

    private final Label userNameLbl;
    private final TextField userNameTxt;
    private final String userNameValue;

    private final Label userEmailLbl;
    private final TextField userEmailTxt;
    private final String userEmailValue;

    private boolean hideUser;

    private ChangeListenerHistoryHint changeListenerHistoryHint;



    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Creates a new TextInputDialog without a default value entered into the
     * dialog {@link TextField}.
     */
    public CommitDialog() {
        this("", "", "");
    }


    /**
     * Creates a new TextInputDialog with the default value entered into the
     * dialog {@link TextField}.
     */
    public CommitDialog(@NamedArg("defaultValue") String defaultValue,
                        @NamedArg("userNameValue") String userNameValue,
                        @NamedArg("userEmailValue") String userEmailValue) {
        this(defaultValue, userNameValue, userEmailValue, false, Collections.emptyList());
    }
    /**
     * Creates a new TextInputDialog with the default value entered into the
     * dialog {@link TextField}.
     */
    public CommitDialog(@NamedArg("defaultValue") String defaultValue,
                        @NamedArg("userNameValue") String userNameValue,
                        @NamedArg("userEmailValue") String userEmailValue,
                        boolean hideUser,
                        Collection<String> history) {
        final DialogPane dialogPane = getDialogPane();
        this.hideUser = hideUser;

        // -- textfield
        this.textField = new TextArea(defaultValue);//new TextArea(defaultValue);

        this.textField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(textField, Priority.ALWAYS);
        GridPane.setFillWidth(textField, true);

        // -- label
        label = new Label(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        this.defaultValue = defaultValue;

        this.userEmailLbl = new Label("Email");
        this.userEmailTxt = new TextField(userEmailValue);
        this.userEmailValue = userEmailValue;

        this.userNameLbl = new Label("Name");
        this.userNameTxt = new TextField(userNameValue);
        this.userNameValue = userNameValue;


        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setVgap(3);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);

        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle("Commit");
        setContentText("Message:");
        dialogPane.setHeaderText("Provide commit message");

        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        updateGrid();

        Platform.runLater(() -> textField.requestFocus());

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? textField.getText() : null;
        });

        changeListenerHistoryHint = new ChangeListenerHistoryHint(textField, Context.settingsProperty.getValue().getCommitMsg());

        this.initOwner(App.getScene().getWindow());
    }



    /**************************************************************************
     *
     * Public API
     *
     **************************************************************************/

    /**
     * Returns the {@link TextField} used within this dialog.
     */
    public final TextArea getEditor() {
        return textField;
    }

    /**
     * Returns the default value that was specified in the constructor.
     */
    public final String getDefaultValue() {
        return defaultValue;
    }

    public String getUserName() {
        return this.userNameTxt.getText();
    }

    public String getUserEmail() {
        return this.userEmailTxt.getText();
    }



    /**************************************************************************
     *
     * Private Implementation
     *
     **************************************************************************/

    private void updateGrid() {
        grid.getChildren().clear();

        grid.add(label, 0, 0);
        grid.add(textField, 1, 0);

        if (!hideUser) {
            grid.add(userNameLbl, 0, 1);
            grid.add(userNameTxt, 1, 1);

            grid.add(userEmailLbl, 0, 2);
            grid.add(userEmailTxt, 1, 2);
        }


        getDialogPane().setContent(grid);

        Platform.runLater(() -> textField.requestFocus());
    }


}
