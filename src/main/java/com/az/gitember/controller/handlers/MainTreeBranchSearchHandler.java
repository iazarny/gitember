package com.az.gitember.controller.handlers;

import com.az.gitember.App;
import com.az.gitember.service.Context;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainTreeBranchSearchHandler implements EventHandler<ActionEvent> {

    private  double posX;
    private  double posY;
    private MenuItem searchMenuItem = null;

    public MainTreeBranchSearchHandler(double posX, double posY) {
        this.posX = posX;
        this.posY = posY;
    }

    public MainTreeBranchSearchHandler(MenuItem searchMenuItem) {
        this.searchMenuItem = searchMenuItem;
    }

    @Override
    public void handle(ActionEvent event) {
        TextField searchTextField = new TextField();

        Stage toolStage = new Stage(StageStyle.TRANSPARENT);
        toolStage.sizeToScene();
        Scene toolScene = new Scene(new HBox(searchTextField));

        Bindings.bindBidirectional(searchTextField.textProperty(), Context.branchFilter);


        searchTextField.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.ESCAPE) {
                Bindings.unbindBidirectional(searchTextField.textProperty(), Context.branchFilter);
                Context.branchFilter.setValue("");
                toolStage.close();
            }
        });

        searchTextField.focusedProperty().addListener((arg0, oldPropertyValue, newPropertyValue) -> {
            if (newPropertyValue) {
                // Textfield on focus;
            } else {
                // Textfield out focus
                Platform.runLater(() -> {

                    Bindings.unbindBidirectional(searchTextField.textProperty(), Context.branchFilter);
                    Context.branchFilter.setValue("");
                    toolStage.close();
                });

            }
        });

        if (searchMenuItem != null) {
            posX = searchMenuItem.getParentPopup().getX();
            posY = searchMenuItem.getParentPopup().getY();
        }


        toolStage.setX(posX);
        toolStage.setY(posY);

        toolStage.setScene(toolScene);
        toolStage.initOwner(App.getScene().getWindow());
        toolStage.setAlwaysOnTop(true);
        toolStage.show();

        searchTextField.selectEnd();
        searchTextField.deselect();

        Platform.runLater(() -> {
            searchTextField.requestFocus();

        });
    }


}
