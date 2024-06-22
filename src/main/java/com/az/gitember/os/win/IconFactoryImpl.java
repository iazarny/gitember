package com.az.gitember.os.win;

import com.az.gitember.os.IconFactory;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.checkerframework.checker.units.qual.C;

public class IconFactoryImpl implements IconFactory {



    @Override
    public Image createImage(WinIconType type, WinIconMode mode, Theme theme, boolean isAppMaximised) {

        double width = 25;
        double height = 20;
        if (type == WinIconType.CLOSE) {
            width= 45;
        }


        Canvas cnv = new Canvas(width, height);
        GraphicsContext gc = cnv.getGraphicsContext2D();
        gc.fillRect(0, 0, cnv.getWidth(), cnv.getHeight());

        Color closeColor = Color.valueOf("#c75050");
        Color closeStrokeColor = Color.WHITE;
        if (WinIconMode.INACTIVE == mode) {
            closeColor = Color.GRAY;
        }

        Color lineColor = Color.BLACK;
        Color btnColor = Color.valueOf("#f6f8fa");

        if (theme == Theme.DARK) {
            lineColor = Color.WHITE;
            btnColor = Color.valueOf("#161822");
        }

        if (WinIconMode.HOVER == mode) {
            lineColor = Color.WHITE;
            btnColor = Color.valueOf("#3665b3");
        }

        gc.setLineWidth(2);

        gc.setStroke(lineColor);
        gc.setFill(btnColor);

        double sizex = cnv.getHeight()/4;
        double sizey = cnv.getHeight()/4;

        double centerx = cnv.getWidth()/2;
        double centery = cnv.getHeight()/2;
        switch(type) {
            case CLOSE: {
                gc.setFill(closeColor);
                gc.setStroke(closeStrokeColor);
                gc.fillRect(0,0, cnv.getWidth(), cnv.getHeight());
                gc.strokeLine(centerx - sizex, centery - sizey,
                        centerx + sizex, centery + sizey);
                gc.strokeLine(centerx + sizex, centery - sizey,
                        centerx - sizex, centery + sizey);
                break;
            }
            case MINIMIZE: {
                gc.fillRect(0,0, cnv.getWidth(), cnv.getHeight());
                gc.strokeLine(centerx + sizex, centery + sizey,
                        centerx - sizex, centery + sizey);
                break;
            }
            case MAXIMIZE: {

                double deltay = 2;

                gc.fillRect(0,0, cnv.getWidth(), cnv.getHeight());
                gc.setLineWidth(1);
                gc.strokeRect(centerx -sizex , centery -sizey + deltay,
                        centerx -2, centery);
                gc.setLineWidth(2);
                gc.strokeLine(centerx - sizex + 1, centery - sizey + deltay,
                        centerx + sizex - 1 , centery - sizey + deltay);
                if (isAppMaximised) {
                    gc.setLineWidth(1);
                    gc.strokeLine(centerx - sizex + 2, centery - sizey ,
                            centerx + sizex + 2 , centery - sizey  );
                    gc.strokeLine(centerx + sizex +2 , centery - sizey,
                            centerx + sizex +2 , centery + sizey  );

                }

            }
        };

        WritableImage writableImage = new WritableImage((int) cnv.getWidth(), (int) cnv.getHeight());


        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);

        return cnv.snapshot(sp, writableImage);
    }

}
