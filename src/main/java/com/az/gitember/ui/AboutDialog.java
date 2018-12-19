package com.az.gitember.ui;

import com.az.gitember.misc.Pair;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Created by Igor_Azarny on 28 - Jan - 2017.
 */
public class AboutDialog extends Dialog {


    int LOGO_WIDTH = 1184;
    int LOGO_HEIGHT = 1184;
    int RADIUS = 60;
    int E_RADIUS_INT = 190;
    int E_RADIUS_EXT = 310;
    int SMALL_DELTA_A = 44;
    int SMALL_DELTA_B = 6;
    int CENTER_X = LOGO_WIDTH / 2;
    int CENTER_Y = LOGO_HEIGHT / 2;

    int BIG_DELTA_A = 64;
    int BIG_DELTA_B = 7;

    int G_RADIUS_INT = 465;
    int G_RADIUS_EXT = 586;

    Label label;


    public AboutDialog() {
        super();
        String version = "";
        try (InputStream is = getClass().getResource("/version.properties").openStream()) {
            Properties prop = new Properties();
            prop.load(is);
            version = prop.get("version").toString();
        } catch (Exception e) {
            e.printStackTrace();

        }
        this.setTitle("Gitember");
        //this.setHeaderText(header);
        //dialog.setGraphic(new ImageView(this.getClass().getResource("login.png").toString())); todo

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
        grid.add(new Label(version ), 1, 1);

        grid.add(new Label("Web site : " ), 0, 2);
        Hyperlink gitember = new Hyperlink("http://www.gitember.com/");
        gitember.setOnAction(
                event1 -> {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().browse(URI.create("http://www.gitember.com/"));
                        } catch (IOException e) {  }
                    }).start();
                }
        );
        grid.add(gitember, 1, 2);

        grid.add(new Label("Author : " ), 0, 3);
        Hyperlink linkedIn = new Hyperlink("Igor Azarny");
        linkedIn.setOnAction(
                event -> {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().browse(URI.create("https://www.linkedin.com/in/igorazarny"));
                        } catch (IOException e) {  }
                    }).start();
                }
        );
        grid.add(linkedIn, 1, 3);


        this.getDialogPane().setContent(grid);


    }

    private Path createLogoPath() {

        Path path = new Path();

        path.setStrokeWidth(30);
        path.setStroke(javafx.scene.paint.Color.valueOf("#42f4dc"));
        path.setFill(javafx.scene.paint.Color.valueOf("#3e7cef"));


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
