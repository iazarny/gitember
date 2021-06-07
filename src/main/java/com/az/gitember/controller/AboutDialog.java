package com.az.gitember.controller;


import com.az.gitember.App;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * Created by Igor_Azarny on 28 - Jan - 2017.
 */
public class AboutDialog extends Dialog {


    private int LOGO_WIDTH = 1184;
    private int LOGO_HEIGHT = 1184;
    private int RADIUS = 60;
    private int E_RADIUS_INT = 190;
    private int E_RADIUS_EXT = 310;
    private int SMALL_DELTA_A = 44;
    private int SMALL_DELTA_B = 6;
    private int CENTER_X = LOGO_WIDTH / 2;
    private int CENTER_Y = LOGO_HEIGHT / 2;

    private int BIG_DELTA_A = 64;
    private int BIG_DELTA_B = 7;

    private int G_RADIUS_INT = 465;
    private int G_RADIUS_EXT = 586;

    public AboutDialog() {
        super();
        this.setTitle("Gitember 2");

        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Pane pane = new Pane();
        pane.setMinWidth(400);
        pane.setMinHeight(400);
        pane.setMaxWidth(400);
        pane.setMaxHeight(400);
        pane.getChildren().add(createLogoPath());


        grid.add(pane, 0, 0);
        GridPane.setColumnSpan(pane, 2);

        grid.add(new Label("Gitember version : " ), 0, 1);
        grid.add(new Label("2.0" ), 1, 1);

        grid.add(new Label("Web site : " ), 0, 2);
        Hyperlink gitember = new Hyperlink("https://github.com/iazarny/gitember");
        gitember.setOnAction(
                event1 -> {
                    App.getShell().showDocument("https://github.com/iazarny/gitember");
                }
        );
        grid.add(gitember, 1, 2);

        grid.add(new Label("Author : " ), 0, 3);
        Hyperlink linkedIn = new Hyperlink("Igor Azarny");

        grid.add(linkedIn, 1, 3);
        this.getDialogPane().setContent(grid);
        this.initOwner(App.getScene().getWindow());

    }


    private Path createLogoPath() {

        Path path = new Path();

        path.setStrokeWidth(45);

        path.setStroke(javafx.scene.paint.Color.valueOf("#808080"));
        Stop[] stops = new Stop[] {
                new Stop(0d, javafx.scene.paint.Color.valueOf("#17e8ab")),
                new Stop(1d, javafx.scene.paint.Color.valueOf("#88cdeb"))};
        LinearGradient lg1 = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
        path.setFill(lg1);

        path.getElements().add(new MoveTo(
                CENTER_X + E_RADIUS_INT - SMALL_DELTA_B,
                CENTER_Y + SMALL_DELTA_A));
        path.getElements().add(new ArcTo(E_RADIUS_INT, E_RADIUS_INT, 0,
                CENTER_X - SMALL_DELTA_A, CENTER_Y - E_RADIUS_INT + SMALL_DELTA_B, true, true));
        path.getElements().add(new ArcTo(RADIUS, RADIUS, 0,
                CENTER_X - BIG_DELTA_A, CENTER_Y - E_RADIUS_EXT + BIG_DELTA_B, false, false));
        path.getElements().add(new ArcTo(E_RADIUS_EXT, E_RADIUS_EXT, 0,
                CENTER_X + E_RADIUS_EXT - BIG_DELTA_B, CENTER_Y + BIG_DELTA_A, true, false));
        path.getElements().add(new ArcTo(RADIUS, RADIUS, 0,
                CENTER_X + E_RADIUS_INT - SMALL_DELTA_B, CENTER_Y + SMALL_DELTA_A, false, false));


        path.getElements().add(new MoveTo(
                CENTER_X + G_RADIUS_INT / Math.sqrt(2.0) + 39,
                CENTER_Y - G_RADIUS_INT / Math.sqrt(2.0) + 42
        ));

        path.getElements().add(new ArcTo(G_RADIUS_INT, G_RADIUS_INT, 0,
                CENTER_X - SMALL_DELTA_A, CENTER_Y - G_RADIUS_INT + SMALL_DELTA_B - 3, true, true));
        path.getElements().add(new ArcTo(RADIUS, RADIUS, 0,
                CENTER_X - BIG_DELTA_A, CENTER_Y - G_RADIUS_EXT + BIG_DELTA_B - 3, false, false));
        path.getElements().add(new ArcTo(G_RADIUS_EXT, G_RADIUS_EXT, 0,
                CENTER_X + G_RADIUS_EXT / Math.sqrt(2.0) - 22,
                CENTER_Y - G_RADIUS_EXT / Math.sqrt(2.0) - 22, true, false));

        path.getElements().add(new ArcTo(30, 30, 0,
                CENTER_X + G_RADIUS_EXT / Math.sqrt(2.0) - 68,
                CENTER_Y - G_RADIUS_EXT / Math.sqrt(2.0) - 22, false, false));

        path.getElements().add(new LineTo(CENTER_X - BIG_DELTA_A,
                CENTER_Y - SMALL_DELTA_A + 16));
        path.getElements().add(new ArcTo(RADIUS, RADIUS, 0,
                CENTER_X + 20, CENTER_Y + SMALL_DELTA_A + 4, false, false));

        path.getElements().add(new LineTo(
                CENTER_X + G_RADIUS_INT / Math.sqrt(2.0) + 39,
                CENTER_Y - G_RADIUS_INT / Math.sqrt(2.0) + 42));

        path.setScaleX(.3);
        path.setScaleY(.3);
        path.setLayoutY(-400);
        path.setLayoutX(-400);

        return path;
    }

}
