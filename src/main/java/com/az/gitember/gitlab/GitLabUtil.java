package com.az.gitember.gitlab;

import com.az.gitember.gitlab.model.GitLabProject;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.util.List;

public class GitLabUtil {

    static void fillContainerWithLabels(Pane container, List<org.gitlab4j.api.models.Label> projectLabels, List<String> issueLabels) {
        issueLabels.stream().forEach(
                tag -> {
                    Label tagLabel = new Label(tag);
                    HBox bg = new HBox(tagLabel);
                    projectLabels.stream().filter( l -> l.getName().equalsIgnoreCase(tag)).findFirst().ifPresent(
                            label -> { bg.setStyle("-fx-padding: 5px; -fx-background-color: " + label.getColor()); }
                    );
                    container.getChildren().add(bg);
                }
        );
    }

}
