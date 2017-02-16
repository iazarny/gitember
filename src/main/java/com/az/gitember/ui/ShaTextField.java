package com.az.gitember.ui;

import javafx.scene.control.TextField;

/**
 * Created by Igor_Azarny on 12 - Feb - 2017.
 */
public class ShaTextField extends TextField {

    public ShaTextField() {
        this("");
    }

    public ShaTextField(String text) {
        super(text);
        this.setEditable(false);
        this.getStyleClass().add("copy-label");
    }
}
