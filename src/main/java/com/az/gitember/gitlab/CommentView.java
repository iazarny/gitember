package com.az.gitember.gitlab;

import com.az.gitember.controller.TextAreaDialog;
import com.az.gitember.service.Context;
import com.az.gitember.service.GitemberUtil;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

    private final Integer issueId;
    private final String discussionId;
    private final Note note;
    private final TextArea comment;
    private final String SPACES = "   ";
    private final boolean editable;
    private Integer idxToAddNote = null;
    private ObservableList<Node> parentChildren;


    public CommentView(final Integer issueId, final String discussionId, final Note note, final boolean parent, final boolean editable) {

        this.issueId = issueId;
        this.discussionId = discussionId;
        this.note = note;
        this.editable = editable;

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

        this.comment = new TextArea(note.getBody());
        this.comment.setMinHeight(75);
        this.comment.setPrefHeight(150);
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



        this.getChildren().addAll(head, body, new Label(" "));

        this.setStyle("-fx-border-width : 1 ; ");


    }

    public void setIdxToAddNote(Integer idxToAddNote) {
        this.idxToAddNote = idxToAddNote;
    }

    public void setParentChldren(ObservableList<Node> parentChildren) {
        this.parentChildren = parentChildren;

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
                editBtn.setDisable(!editable);
                editBtn.setOnAction(event -> {
                    TextAreaDialog textAreaDialog = new TextAreaDialog(note.getBody());
                    textAreaDialog.initOwner(CommentView.this.getScene().getWindow());
                    textAreaDialog.showAndWait().ifPresent( body -> {
                        try {
                            Context.getGitLabProject().modifyIssueThreadNote(issueId, discussionId, note.getId(),  body);
                            comment.setText(body);
                        } catch (GitLabApiException e) {
                            e.printStackTrace();
                        }
                    });
                });

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
            replyBtn.setDisable(!editable);

            replyBtn.setOnAction(event -> {
                TextAreaDialog textAreaDialog = new TextAreaDialog("");
                textAreaDialog.initOwner(CommentView.this.getScene().getWindow());
                textAreaDialog.showAndWait().ifPresent( body -> {
                    try {
                        Note note = Context.getGitLabProject().addIssueThreadNote(issueId, discussionId,  body);
                        parentChildren.add(
                                idxToAddNote,
                                new CommentView(issueId, discussionId, note, false, editable)
                                );
                        idxToAddNote++;

                    } catch (GitLabApiException e) {
                        e.printStackTrace();
                    }
                });
            });
        }
    }

}
