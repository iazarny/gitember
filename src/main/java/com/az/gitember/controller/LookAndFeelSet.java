package com.az.gitember.controller;

import javafx.scene.paint.Color;
import jfxtras.styles.jmetro.Style;

public class LookAndFeelSet {

    public static String PIECHART_LEGENF_STYLE = "-fx-fill: lightgray";
    public static Style THEME_NAME = Style.LIGHT;
    public static Color BRANCH_NAME_COLOR = Color.color(0.1627451F, 0.1627451F, 0.1627451F);
    public static final Color[] historyPlotCommitRenderedColors = new Color[]{
            Color.rgb(255, 0, 0), Color.rgb(0, 255, 0), Color.rgb(229, 229, 0), Color.rgb(177, 178, 255), Color.rgb(255, 0, 255), Color.rgb(0, 255, 255),
            Color.rgb(206, 0, 0), Color.rgb(0, 187, 0), Color.rgb(0, 187, 187), Color.rgb(133, 133, 255), Color.rgb(217, 0, 190), Color.rgb(0, 197, 197),
            Color.rgb(159, 0, 0), Color.rgb(0, 159, 0), Color.rgb(136, 136, 0), Color.rgb(71, 71, 255), Color.rgb(173, 0, 151), Color.rgb(0, 155, 155),
            Color.rgb(99, 0, 0), Color.rgb(0, 99, 0), Color.rgb(83, 83, 0), Color.rgb(0, 0, 255), Color.rgb(129, 0, 113), Color.rgb(0, 114, 114),
    };
    public static String KEYWORDS_CSS = "/styles/keywords.css";
    public static String DEFAULT_CSS = "/styles/styles.css";
    public static String FOUND_ROW = "-fx-font-weight: bold; -fx-background-color: #97c9f0";

    public static Color DIFF_COLOR;

    public static Color DIFF_STROKE_COLOR;

    public static Color DIFF_FILL_COLOR;

    public static void init(String themeMode) {
        if ("Dark".equalsIgnoreCase(themeMode)) {
            PIECHART_LEGENF_STYLE = "-fx-fill: lightgray";
            THEME_NAME = Style.DARK;
            BRANCH_NAME_COLOR = Color.LIGHTGRAY;
            KEYWORDS_CSS = "/styles/keywords_dark.css";
            DEFAULT_CSS = "/styles/styles_dark.css";
            FOUND_ROW = "-fx-font-weight: bold; -fx-background-color: #004f8b";
            DIFF_COLOR = Color.valueOf("#203e20"); //rgb(106, 206, 159);
            DIFF_STROKE_COLOR = DIFF_COLOR.deriveColor(1, 1, 1, 0.9);
            DIFF_FILL_COLOR = Color.valueOf("#203e20"); //Color.rgb(106, 206, 159);


        } else {
            PIECHART_LEGENF_STYLE = "";
            THEME_NAME = Style.LIGHT;
            BRANCH_NAME_COLOR = Color.color(0.1627451F, 0.1627451F, 0.1627451F);
            KEYWORDS_CSS = "/styles/keywords.css";
            DEFAULT_CSS = "/styles/styles.css";
            FOUND_ROW = "-fx-font-weight: bold; -fx-background-color: #97c9f0";
            DIFF_COLOR = Color.valueOf("#97fa97") ;//rgb(106, 206, 159);
            DIFF_STROKE_COLOR = DIFF_COLOR.deriveColor(1, 1, 1, 0.3);
            DIFF_FILL_COLOR = Color.valueOf("#97fa97") ;// DIFF_COLOR.deriveColor(1, 1, 1, 0.15);

        }
    }

}
