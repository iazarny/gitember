package com.az.gitember.controller;


import com.az.gitember.App;
import com.az.gitember.data.LfsData;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

/**
 * Created by Igor_Azarny on 25 - Dec -2016.
 */
public class LFSDialog extends Dialog<LfsData> {

    private final LfsData lfsData;

    public LFSDialog(String title, String header, LfsData lfsData) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString())); todo
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        //ButtonType okButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll( ButtonType.OK, ButtonType.CANCEL);
        this.lfsData = lfsData;

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));
        CheckBox lfsCombobox = new CheckBox();
        lfsCombobox.setText("Large file support");
        HBox.setHgrow(lfsCombobox, Priority.ALWAYS);
        ListView<String> extension = new ListView(lfsData.getExtentions());
        var stringTextFieldListCell = TextFieldListCell.forListView();
        extension.setEditable(true);
        extension.setCellFactory(stringTextFieldListCell);
        extension.setDisable(true);
        extension.setPrefHeight(140);

        extension.setOnEditCommit(t -> extension.getItems().set(t.getIndex(), t.getNewValue()));

        extension.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.DELETE || evt.getCode() == KeyCode.BACK_SPACE) {
                if (extension.getSelectionModel().getSelectedIndex() > -1) {
                    extension.getItems().remove(extension.getSelectionModel().getSelectedIndex());
                }
            } else if (evt.getCode() == KeyCode.INSERT) {
                addItemAndStartEdit(extension, "");

            } else if (evt.getCode() == KeyCode.DIGIT8 && evt.isShiftDown()) {
                addItemAndStartEdit(extension, "*");
            } else if (evt.getCode().isLetterKey() || evt.getCode().isDigitKey()) {
                addItemAndStartEdit(extension, evt.getText());
            }
        });


        Button editButton = new Button();
        StackedFontIcon plusIcon = new StackedFontIcon();
        plusIcon.getChildren().add(new FontIcon(FontAwesome.PLUS));
        plusIcon.setStyle("-fx-icon-color: text_color");
        editButton.setGraphic(plusIcon  );
        editButton.setOnAction((ActionEvent event) -> {
            addItemAndStartEdit(extension, "");
        });
        editButton.setDisable(true);


        Pane pane = new Pane();
        HBox.setHgrow(pane, Priority.ALWAYS);


        grid.add(lfsCombobox, 1, 0);
        grid.add(new HBox(new Label("Files to track : "), pane,  editButton), 1, 1);
        grid.add(extension, 1, 2);

        lfsCombobox.selectedProperty().addListener( (observable, oldValue, newValue) -> {
            if (newValue) {
                Platform.runLater(() -> extension.requestFocus() );
                lfsCombobox.setDisable(newValue);
                editButton.setDisable(!newValue);
            }


        });

        this.getDialogPane().setContent(grid);

        lfsCombobox.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    extension.setDisable(!newValue);
                }
        );
        System.out.println(">>>>>>>>>>>>>>>>>> " + lfsData.lfsSupportProperty().get() );
        Bindings.bindBidirectional( lfsCombobox.selectedProperty(), lfsData.lfsSupportProperty());


        this.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return this.lfsData;
            }
            return null;
        });

        this.initOwner(App.getScene().getWindow());

    }



    private void addItemAndStartEdit(ListView<String> extension, String text) {
        extension.getItems().add(text);
        extension.scrollTo(extension.getItems().size() - 1);
        extension.layout();
        extension.edit(extension.getItems().size() - 1);
        extension.getSelectionModel().clearSelection();
    }


}
