package com.az.gitember.controller.handlers;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public final class EscEventHandler implements EventHandler<KeyEvent> {

	private Pane pane;

	public EscEventHandler(Pane pane) {
		this.pane = pane;
	}

	@Override
	public void handle(KeyEvent e) {
		if (e.getCode() == KeyCode.ESCAPE) {
			Stage sb = (Stage) pane.getScene().getWindow();
			sb.close();
		}
	}
}