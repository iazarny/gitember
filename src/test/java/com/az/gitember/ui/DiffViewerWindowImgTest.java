package com.az.gitember.ui;

import com.az.gitember.service.ExtensionMap;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiffViewerWindowImgTest {

    @Test
    void pngAndJpegAreImages() {
        assertTrue(ExtensionMap.isImage("photo.png"));
        assertTrue(ExtensionMap.isImage("dir/shot.JPEG"));
        assertFalse(ExtensionMap.isImage("readme.md"));
    }

    @Test
    void describeComparison_identicalImages() {
        BufferedImage a = solid(8, 8, Color.RED);
        BufferedImage b = solid(8, 8, Color.RED);
        assertTrue(DiffViewerWindowImg.pixelsEqual(a, b));
        String text = DiffViewerWindowImg.describeComparison(a, b);
        assertTrue(text.contains("8\u00d78"));
        assertTrue(text.contains("Identical"));
    }

    @Test
    void describeComparison_differentPixelsAndSize() {
        BufferedImage red = solid(8, 8, Color.RED);
        BufferedImage blue = solid(8, 8, Color.BLUE);
        assertFalse(DiffViewerWindowImg.pixelsEqual(red, blue));
        assertTrue(DiffViewerWindowImg.describeComparison(red, blue).contains("Different"));

        BufferedImage larger = solid(16, 8, Color.RED);
        assertTrue(DiffViewerWindowImg.describeComparison(red, larger).contains("Different size"));
    }

    @Test
    void describeComparison_missingSide() {
        BufferedImage img = solid(2, 2, Color.BLACK);
        assertTrue(DiffViewerWindowImg.describeComparison(img, null).contains("Missing on one side"));
        assertTrue(DiffViewerWindowImg.describeComparison(null, null).contains("No images"));
    }

    private static BufferedImage solid(int w, int h, Color color) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }
}
