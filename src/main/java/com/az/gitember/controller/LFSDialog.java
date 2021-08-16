package com.az.gitember.controller;


import com.az.gitember.App;
import com.az.gitember.controller.handlers.MainTreeBranchSearchHandler;
import com.az.gitember.data.RemoteRepoParameters;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Created by Igor_Azarny on 25 - Dec -2016.
 */
public class LFSDialog extends Dialog<RemoteRepoParameters> {

    public LFSDialog(String title, String header) {

        super();
        this.setTitle(title);
        this.setHeaderText(header);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString())); todo
        this.getDialogPane().getStyleClass().add("text-input-dialog");
        //ButtonType okButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().addAll( ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        CheckBox lfsCombobox = new CheckBox();
        lfsCombobox.setText("Large file support");

        HBox.setHgrow(lfsCombobox, Priority.ALWAYS);

        ObservableList<String> items = FXCollections.observableArrayList("*.bmp", "*.gif");

        ListView<String> extension = new ListView(items);
        var stringTextFieldListCell = TextFieldListCell.forListView();
        extension.setEditable(true);
        extension.setCellFactory(stringTextFieldListCell);
        extension.setDisable(true);
        extension.setPrefHeight(140);


        extension.setOnEditCommit(new EventHandler<ListView.EditEvent<String>>() {
            @Override
            public void handle(ListView.EditEvent<String> t) {
                extension.getItems().set(t.getIndex(), t.getNewValue());
                System.out.println("setOnEditCommit [" + t.getNewValue() + "]");
            }

        });

        extension.setOnEditCancel(new EventHandler<ListView.EditEvent<String>>() {
            @Override
            public void handle(ListView.EditEvent<String> t) {
                System.out.println("setOnEditCancel");
            }
        });

        extension.setOnKeyPressed(evt -> {
            System.out.println(evt.getCode() + " " + evt.getCharacter() + " " +  evt.getText() + " " + evt);
            if (evt.getCode() == KeyCode.DELETE || evt.getCode() == KeyCode.BACK_SPACE) {

                System.out.println("delete " + extension.getSelectionModel().getSelectedItem());

            } else if (evt.getCode() == KeyCode.INSERT) {
                addItemAndStartEdit(extension, "");

            } else if (evt.getCode() == KeyCode.DIGIT8 && evt.isShiftDown()) {
                addItemAndStartEdit(extension, "*");
            } else if (evt.getCode().isLetterKey() || evt.getCode().isDigitKey()) {
                addItemAndStartEdit(extension, evt.getText());
                System.out.println(" key [" + evt.getText() + "]");
            }
        });


        Button editButton = new Button("Add & Edit");
        editButton.setOnAction((ActionEvent event) -> {
            addItemAndStartEdit(extension, "");
        });


        grid.add(lfsCombobox, 1, 0);
        grid.add(new HBox(new Label("Files to track : "), editButton), 1, 1);
        grid.add(extension, 1, 2);


        this.getDialogPane().setContent(grid);

        lfsCombobox.selectedProperty().addListener(
                (observable, oldValue, newValue) -> {
                    extension.setDisable(!newValue);
                }
        );

        

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
