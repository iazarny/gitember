package com.az.gitember.os.lin;

import com.az.gitember.os.IconFactory;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class IconFactoryImpl implements IconFactory {



    @Override
    public Image createImage(WinIconType type, WinIconMode mode, Theme theme, boolean isAppMaximised) {

        double width = 20;
        double height = 20;



        Canvas cnv = new Canvas(width, height);
        cnv.getGraphicsContext2D().setImageSmoothing(false);

        GraphicsContext gc = cnv.getGraphicsContext2D();
        //gc.fillOval(0, 0, cnv.getWidth(), cnv.getHeight());


        Color lineColor = Color.DARKGRAY;
        Color btnColor = Color.LIGHTGRAY;

        if (theme == Theme.DARK) {
            lineColor = Color.GRAY;
            btnColor = Color.DARKGRAY;
        }

        double sizex = cnv.getHeight()/4;
        double sizey = cnv.getHeight()/4;

        double centerx = cnv.getWidth()/2;
        double centery = cnv.getHeight()/2;

        gc.setFill(btnColor);
        gc.setStroke(lineColor);
        gc.setLineWidth(2);

        gc.fillOval(0,0, cnv.getWidth(), cnv.getHeight());


        if (WinIconMode.HOVER == mode) {
            gc.setFill(Color.WHITE);
            gc.setStroke(Color.BLACK);
        }

        switch(type) {
            case CLOSE: {

                gc.strokeLine(centerx - sizex + 1, centery - sizey+1,
                        centerx + sizex - 1, centery + sizey - 1);
                gc.strokeLine(centerx + sizex-1, centery - sizey,
                        centerx - sizex + 1, centery + sizey);
                break;
            }
            case MINIMIZE: {
                gc.strokeLine(centerx + sizex, centery + sizey,
                        centerx - sizex, centery + sizey);
                break;
            }
            case MAXIMIZE: {
                double deltay = 2;


                gc.setLineWidth(1);
                gc.strokeRect(centerx -sizex + 1, centery -sizey + deltay,
                        centerx -1, centery - 1);
                gc.setLineWidth(2);
                gc.strokeLine(centerx - sizex +1, centery - sizey + deltay,
                        centerx + sizex -1 , centery - sizey + deltay);

                if (isAppMaximised) {

                    gc.setLineWidth(1);
                    gc.strokeLine(centerx - sizex + 2, centery - sizey -1,
                            centerx + sizex + 3 , centery - sizey -1 );
                    gc.strokeLine(centerx + sizex +3 , centery - sizey,
                            centerx + sizex +3 , centery + sizey -1  );

                }

            }
        };

        WritableImage writableImage = new WritableImage((int) cnv.getWidth(), (int) cnv.getHeight());


        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);

        return cnv.snapshot(sp, writableImage);
    }

}
