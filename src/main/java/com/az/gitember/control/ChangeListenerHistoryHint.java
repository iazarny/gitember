package com.az.gitember.control;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Igor_Azarny on 28 - Jan -2017.
 */
public class ChangeListenerHistoryHint implements ChangeListener<String> {

    private final SortedSet<String> entries;
    /**
     * The popup used to select an entry.
     */
    private ContextMenu entriesPopup = new ContextMenu();

    private final TextInputControl textInputControl;

    public ChangeListenerHistoryHint(TextInputControl textInputControl, SortedSet<String> entries) {
        this.entries = new TreeSet<>(entries);
        this.textInputControl = textInputControl;


        textInputControl.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                entriesPopup.hide();
            }
        });

        textInputControl.textProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

        if (textInputControl.getText().length() == 0) {
            entriesPopup.hide();
        } else {
            LinkedList<String> searchResult = new LinkedList<>();
            searchResult.addAll(entries.subSet(textInputControl.getText(), textInputControl.getText() + Character.MAX_VALUE));
            if (entries.size() > 0) {
                populatePopup(searchResult);
                if (!entriesPopup.isShowing()) {
                    // textInputControl.
                    entriesPopup.show(textInputControl, Side.BOTTOM,
                            2 * textInputControl.getFont().getSize(),
                            -1 * textInputControl.getHeight() + 2 * textInputControl.getFont().getSize());

                }
            } else {
                entriesPopup.hide();
            }
        }

    }


    /**
     * Populate the entry set with the given search results.  Display is limited to 10 entries, for performance.
     *
     * @param searchResult The set of matching strings.
     */
    private void populatePopup(List<String> searchResult) {
        List<CustomMenuItem> menuItems = new LinkedList<>();
        // If you'd like more entries, modify this line.
        int maxEntries = 10;
        int count = Math.min(searchResult.size(), maxEntries);
        for (int i = 0; i < count; i++) {
            final String result = searchResult.get(i);
            Label entryLabel = new Label(result);
            CustomMenuItem item = new CustomMenuItem(entryLabel, true);
            item.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    textInputControl.setText(result);
                    entriesPopup.hide();
                }
            });
            menuItems.add(item);
        }
        entriesPopup.getItems().clear();
        entriesPopup.getItems().addAll(menuItems);

    }

}
