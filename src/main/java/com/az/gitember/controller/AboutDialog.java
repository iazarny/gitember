package com.az.gitember.controller;


import com.az.gitember.App;
import javafx.animation.PathTransition;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.MotionBlur;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import static com.az.gitember.data.Const.APP_NAME;

/**
 * Created by Igor_Azarny on 28 - Jan - 2017.
 */
public class AboutDialog extends Dialog {


    private float CHAR_WIDTH = 100;
    private float CHAR_HEIGHT = 133;


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
        this.setTitle(APP_NAME);

        this.getDialogPane().getStyleClass().add("text-input-dialog");
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 20));

        Path [] path = createLogoPath();

        Pane pane = new Pane();
        pane.setMinWidth(400);
        pane.setMinHeight(400);
        pane.setMaxWidth(400);
        pane.setMaxHeight(400);
        pane.getChildren().addAll(path);


        PathTransition pathTransition1 = new PathTransition();
        pathTransition1.setDuration(Duration.millis(25000));
        pathTransition1.setNode(pane);
        pathTransition1.setPath(path[0]);
        pathTransition1.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        pathTransition1.setCycleCount(1);
        pathTransition1.setAutoReverse(true);



        grid.add(pane, 0, 0);
        GridPane.setColumnSpan(pane, 2);


        grid.add(new Label("Web site : " ), 0, 1);
        Hyperlink gitember = new Hyperlink("https://github.com/iazarny/gitember");
        gitember.setOnAction(
                event1 -> {
                    App.getShell().showDocument("https://github.com/iazarny/gitember");
                }
        );
        grid.add(gitember, 1, 1);

        grid.add(new Label("Author : " ), 0, 2);
        Hyperlink linkedIn = new Hyperlink("Igor Azarny");

        grid.add(linkedIn, 1, 2);

        TextArea creditsTextArea = new TextArea();
        creditsTextArea.setEditable(false);
        creditsTextArea.setMinWidth(250);
        creditsTextArea.setMinHeight(100);
        creditsTextArea.setMaxWidth(250);
        creditsTextArea.setMaxHeight(100);
        creditsTextArea.setText(
                "Thanks for antlr grammar\n" +
                        "  Asm, Basic, Html, Pascal - Tom Everett <tom@khubla.com>\n" +
                        "  C - Sam Harwell\n" +
                        "  Cpp - Camilo Sanchez, Martin Mirchev\n" +
                        "  C# - Christian Wulf <chwchw@gmx.de>, Ivan Kochurkin <kvanttt@gmail.com>\n" +
                        "  Erlang, Xml, Json - Terence Parr\n" +
                        "  Fortran - Olivier Dragon, Terence Parr\n" +
                        "  Go - Sasa Coh, Michał Błotniak, Ivan Kochurkin, <kvanttt@gmail.com>, \n" +
                        "     Dmitry Rassadin, <flipparassa@gmail.com>, Martin Mirchev, <mirchevmartin2203@gmail.com>\n" +
                        "  Java - Terence Parr, Sam Harwell, Chan Chung Kwong\n" +
                        "  JavaScript - Bart Kiers, Alexandre Vitorelli, Ivan Kochurkin, Juan Alvarez, Student Main\n" +
                        "  Kotlin - Anastasiya Shadrina <a.shadrina5@mail.ru>\n" +
                        "  Lua - Kazunori Sakamoto, Alexander Alexeev\n" +
                        "  Python - Bart Kiers\n" +
                        "  Ruby - Alexander Belov\n" +
                        "  Rust - The Rust Project Developers, Student Main\n" +
                        "  Sql - Canwei He\n" +
                        "  Swift - ?\n" +
                        "  Typescript - Bart Kiers, Alexandre Vitorelli, Ivan Kochurkin, Juan Alvarez, Andrii Artiushok\n"
        );
        grid.add(new Label("Credits to : " ), 0, 4);
        grid.add(creditsTextArea, 1, 4);
        this.getDialogPane().setContent(grid);
        this.initOwner(App.getScene().getWindow());

        pane.setOnMouseClicked(event -> {
            pathTransition1.play();
        });

    }


    private Path[] createLogoPath() {

        Path path = new Path();

        path.setStrokeWidth(55);

        path.setStroke(javafx.scene.paint.Color.valueOf("#2020ea"));
        Stop[] stops = new Stop[] {
                new Stop(0d, javafx.scene.paint.Color.valueOf("#FFD700")),
                new Stop(1d, javafx.scene.paint.Color.valueOf("FFD700"))};
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

//version
        Path char2 = getPathChar2();
        char2.setLayoutX(270);
        char2.setLayoutY(300);
        applyStyle(char2);

        Path dot = getPathCharDot();
        dot.setLayoutX(333.5);
        dot.setLayoutY(380 );
        applyStyle(dot);

        Path char5 = getPathChar5();
        char5.setLayoutX(300);
        char5.setLayoutY(300);
        applyStyle(char5);

        return new Path [] {
                path, char2, dot,  char5,
        };
    }

    private void applyStyle(Path dot) {
        dot.setScaleX(.2);
        dot.setScaleY(.2);
        dot.setStrokeWidth(10);
        dot.setStroke(Color.valueOf("#3c7070"));
    }

    private Path getPathCharDot() {
        Path path = new Path();

        path.getElements().add(new MoveTo(   0,       0 ));
        path.getElements().add(new LineTo(0,0));
        return path;
    }

    private Path getPathChar2() {
        Path path = new Path();

        path.getElements().add(new MoveTo(   0,       0 ));
        path.getElements().add(new LineTo(CHAR_WIDTH  , 0 ));
        path.getElements().add(new LineTo(CHAR_WIDTH  , CHAR_HEIGHT/2 ));
        path.getElements().add(new LineTo(0  , CHAR_HEIGHT ));
        path.getElements().add(new LineTo(CHAR_WIDTH , CHAR_HEIGHT ));
        return path;
    }

    private Path getPathChar3() {
        Path path = new Path();
        path.getElements().add(new MoveTo(   0,       0 ));
        path.getElements().add(new LineTo(CHAR_WIDTH  , 0 ));
        path.getElements().add(new LineTo(0  , CHAR_HEIGHT/2 ));
        path.getElements().add(new LineTo(CHAR_WIDTH  , CHAR_HEIGHT/2 ));
        path.getElements().add(new LineTo(0 , CHAR_HEIGHT ));
        return path;
    }

    private Path getPathChar4() {
        Path path = new Path();
        path.getElements().add(new MoveTo(   0,       0 ));
        path.getElements().add(new LineTo(0  , CHAR_HEIGHT/2 ));
        path.getElements().add(new LineTo(CHAR_WIDTH  , CHAR_HEIGHT/2 ));
        path.getElements().add(new MoveTo(   CHAR_WIDTH,       0 ));
        path.getElements().add(new LineTo(CHAR_WIDTH  , CHAR_HEIGHT ));
        return path;
    }

    private Path getPathChar5() {
        Path path = new Path();
        path.getElements().add(new MoveTo(   CHAR_WIDTH,       0 ));
        path.getElements().add(new LineTo(0  , 0 ));
        path.getElements().add(new LineTo(0  , CHAR_HEIGHT/2 ));
        path.getElements().add(new LineTo(   CHAR_WIDTH,       CHAR_HEIGHT/2 ));
        path.getElements().add(new LineTo(CHAR_WIDTH  , CHAR_HEIGHT ));
        path.getElements().add(new LineTo(0  , CHAR_HEIGHT ));
        return path;
    }

}
