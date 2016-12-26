package com.az.gitember.ui;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * Created by Igor_Azarny on 25 Dec 2016.
 */
public class TextAreaInputDialog extends Dialog<String> {


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



    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Creates a new TextInputDialog without a default value entered into the
     * dialog {@link TextField}.
     */
    public TextAreaInputDialog() {
        this("", "", "");
    }

    /**
     * Creates a new TextInputDialog with the default value entered into the
     * dialog {@link TextField}.
     */
    public TextAreaInputDialog(@NamedArg("defaultValue") String defaultValue,
                               @NamedArg("userNameValue") String userNameValue,
                               @NamedArg("userEmailValue") String userEmailValue) {
        final DialogPane dialogPane = getDialogPane();

        // -- textfield
        this.textField = new TextArea(defaultValue);
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

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? textField.getText() : null;
        });
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

        grid.add(userNameLbl, 0, 1);
        grid.add(userNameTxt, 1, 1);

        grid.add(userEmailLbl, 0, 2);
        grid.add(userEmailTxt, 1, 2);

        getDialogPane().setContent(grid);

        Platform.runLater(() -> textField.requestFocus());
    }


}
