package com.az.gitember.gitlab;

import com.az.gitember.App;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.controller.TextAreaDialog;
import com.az.gitember.data.Const;
import com.az.gitember.gitlab.model.FxIssue;
import com.az.gitember.gitlab.model.GitLabProject;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Note;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

public class CommentView extends VBox {

    private final String SPACES = "   ";
    public CommentView(final Note note, final boolean parent) {

        if (parent) {
            this.getChildren().add(new HBox(new Label(SPACES)));
        }
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
        addReplyButton(head, parent);
        addEditButton(head, note);
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

    private void addEditButton(HBox head, final Note note) {
        String currentUserName = null;
        try {
            currentUserName = Context.getGitLabProject().getGitLabApi().getUserApi().getCurrentUser().getUsername();
            if (note.getAuthor().getUsername().trim().equals(currentUserName)) {
                Button editBtn = new Button("Edit");

                StackedFontIcon replyIcon = new StackedFontIcon();
                replyIcon.getChildren().add(new FontIcon(FontAwesome.EDIT));
                replyIcon.setStyle("-fx-icon-color: text_color");
                editBtn.setGraphic(replyIcon);
                head.getChildren().add(editBtn);
            }
        } catch (GitLabApiException e) {
            e.printStackTrace();
        }


    }

    private void addReplyButton(HBox head, boolean parent) {
        if (parent) {
            Button replyBtn = new Button("Reply");
            StackedFontIcon replyIcon = new StackedFontIcon();
            replyIcon.getChildren().add(new FontIcon(FontAwesome.REPLY));
            replyIcon.setStyle("-fx-icon-color: text_color");
            replyBtn.setGraphic(replyIcon);
            head.getChildren().add(replyBtn);

            replyBtn.setOnAction(event -> {
                TextAreaDialog textAreaDialog = new TextAreaDialog("");
                textAreaDialog.showAndWait().ifPresent( note -> {
                    System.out.println(note);
                });

            });
        }
    }


}
