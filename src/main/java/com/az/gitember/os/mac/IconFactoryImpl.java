package com.az.gitember.os.mac;

import com.az.gitember.os.IconFactory;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class IconFactoryImpl implements IconFactory {


    private static double width = 13.3;
    private static double height = 13.3;

    @Override
    public Image createImage(IconFactory.WinIconType type, IconFactory.WinIconMode mode, IconFactory.Theme theme) {

        Canvas cnv = new Canvas(width, height);
        GraphicsContext gc = cnv.getGraphicsContext2D();
        gc.setFill(Color.TRANSPARENT);
        gc.fillRect(0, 0, cnv.getWidth(), cnv.getHeight());

        Color circleColor = Color.GRAY;
        switch(type) {
            case CLOSE: circleColor = Color.valueOf("#ff605c"); break;
            case MINIMIZE: circleColor = Color.valueOf("#ffbd66");  break;
            case MAXIMIZE:  circleColor = Color.valueOf("#00ca4e"); break;
        };
        if (WinIconMode.INACTIVE == mode) {
            circleColor = Color.GRAY;
        }
        gc.setFill(circleColor);
        gc.setLineWidth(1.5);

        double centerX = cnv.getWidth() / 2;
        double centerY = cnv.getHeight() / 2;
        double radius = cnv.getWidth() / 2;

        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        //gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);


        if (WinIconMode.HOVER == mode) {
            Color lineColor = Color.color(0,0,0,0.6);
            gc.setStroke(lineColor);

            gc.setLineWidth(1);
            double startx = cnv.getWidth()/3.5;
            double starty = cnv.getHeight()/3.5;
            switch(type) {
                case CLOSE: {
                    gc.strokeLine(startx,starty,
                            cnv.getWidth() - startx,cnv.getHeight() - starty);
                    gc.strokeLine(cnv.getWidth() - startx,starty,
                            startx ,cnv.getHeight() - starty);
                    break;
                }
                case MINIMIZE: {
                    gc.strokeLine(startx,cnv.getHeight()/2,
                            cnv.getWidth() - startx,cnv.getHeight()/2);
                    break;
                }
                case MAXIMIZE: {
                    gc.setLineWidth(2);
                     startx = cnv.getWidth()/4.0;
                     starty = cnv.getHeight()/4.0;
                    gc.setFill(lineColor);
                    gc.fillRect(startx,starty,
                            cnv.getWidth() - 2*startx,cnv.getHeight() - 2*starty);
                    gc.setStroke(Color.valueOf("#00ca4e"));
                    gc.strokeLine(cnv.getWidth() - startx,starty,
                            startx ,cnv.getHeight() - starty);

                }
            };

        }



        WritableImage writableImage = new WritableImage((int) cnv.getWidth(), (int) cnv.getHeight());


        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);

        return cnv.snapshot(sp, writableImage);
    }

}
