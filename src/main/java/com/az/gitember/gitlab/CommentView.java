package com.az.gitember.gitlab;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Const;
import com.az.gitember.gitlab.model.FxIssue;
import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.gitlab4j.api.models.Note;

public class CommentView extends VBox {

    private final String SPACES = "   ";
    public CommentView(final Note note, final boolean parent) {
        HBox head = new HBox();

        head.getChildren().add(new Label(SPACES));
        head.getChildren().add(new Label(note.getAuthor().getName()));
        head.getChildren().add(new Label(SPACES));
        head.getChildren().add(new Label(note.getAuthor().getUsername()));
        head.getChildren().add(new Label(SPACES));
        head.getChildren().add(new Label(SPACES));
        head.getChildren().add(new Label(GitemberUtil.formatDateOnly(note.getCreatedAt())));
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        head.getChildren().add(region);
        head.getChildren().add(new Label("X"));
        head.getChildren().add(new Label(SPACES));

        TextArea comment = new TextArea(note.getBody());
        HBox.setHgrow(comment, Priority.ALWAYS);

        HBox body = new HBox();
        if (!parent) {
            body.getChildren().add(new Label(SPACES));
            body.getChildren().add(new Label(SPACES));
        }
        body.getChildren().add(comment);

        if (parent) {
            body.getChildren().add(new Label(" "));
        }

        this.getChildren().addAll(head, body);

        this.setStyle("-fx-border-width : 1 ; ");


    }


}
