package com.az.gitember.controller;

import javafx.scene.paint.Color;

public class LookAndFeelSet {

    public static final double FONT_SIZE = 24.0;
    public static int DIALOG_DEFAULT_WIDTH = 800;
    public static String PIECHART_LEGENF_STYLE = "-fx-fill: lightgray";
    public static Color BRANCH_NAME_COLOR = Color.color(0.1627451F, 0.1627451F, 0.1627451F); //TODO to css
    public static final Color[] historyPlotCommitRenderedColors = new Color[]{
            Color.rgb(255, 0, 0), Color.rgb(0, 255, 0), Color.rgb(229, 229, 0), Color.rgb(177, 178, 255), Color.rgb(255, 0, 255), Color.rgb(0, 255, 255),
            Color.rgb(206, 0, 0), Color.rgb(0, 187, 0), Color.rgb(0, 187, 187), Color.rgb(133, 133, 255), Color.rgb(217, 0, 190), Color.rgb(0, 197, 197),
            Color.rgb(159, 0, 0), Color.rgb(0, 159, 0), Color.rgb(136, 136, 0), Color.rgb(71, 71, 255), Color.rgb(173, 0, 151), Color.rgb(0, 155, 155),
            Color.rgb(99, 0, 0), Color.rgb(0, 99, 0), Color.rgb(83, 83, 0), Color.rgb(0, 0, 255), Color.rgb(129, 0, 113), Color.rgb(0, 114, 114),
    };
    public static String KEYWORDS_CSS = "/styles/keywords.css";
    public static String DEFAULT_CSS = "/styles/styles.css";
    public static String FOUND_ROW = "-fx-font-weight: bold; -fx-background-color: #97c9f0";

    public static String CODE_AREA_CSS = "-fx-fill: gray; -fx-background-color: black; -fx-font: Monospace; -fx-font-size: 20;";
    public static String CODE_AREA_LINE_NUM_CSS = "-fx-background-color: background_color; -fx-text-fill: #b3b3b3; -fx-font: Monospace; -fx-font-size: 20;";

    public static Color DIFF_COLOR_DELETE = Color.valueOf("#ff6666");
    public static Color DIFF_COLOR_INSERT = Color.valueOf("#66ff66");
    public static Color DIFF_COLOR_REPLACE = Color.valueOf("#404040");
    public static Color DIFF_COLOR_TEXT = Color.valueOf("#e0e0e0");

    public static void init(String themeMode) {
        if ("Dark".equalsIgnoreCase(themeMode)) {
            PIECHART_LEGENF_STYLE = "-fx-fill: lightgray";
            BRANCH_NAME_COLOR = Color.LIGHTGRAY;
            KEYWORDS_CSS = "/styles/keywords_dark.css";
            DEFAULT_CSS = "/styles/styles_dark.css";
            FOUND_ROW = "-fx-font-weight: bold; -fx-background-color: #004f8b";
            CODE_AREA_CSS = "-fx-fill: gray; -fx-background-color: black; -fx-font: Monospace; -fx-font-size: 20; ";
            CODE_AREA_LINE_NUM_CSS = "-fx-background-color: background_color;  -fx-text-fill: #505050;";
            DIFF_COLOR_DELETE = Color.valueOf("#ff6666");
            DIFF_COLOR_INSERT = Color.valueOf("#66ff66");
            DIFF_COLOR_REPLACE = Color.valueOf("#e0e0e0");
            DIFF_COLOR_TEXT = Color.valueOf("#505050");
        } else {
            PIECHART_LEGENF_STYLE = "";
            BRANCH_NAME_COLOR = Color.color(0.1627451F, 0.1627451F, 0.1627451F);
            KEYWORDS_CSS = "/styles/keywords.css";
            DEFAULT_CSS = "/styles/styles.css";
            FOUND_ROW = "-fx-font-weight: bold; -fx-background-color: #97c9f0";
            CODE_AREA_CSS = "-fx-font: Monospace; -fx-font-size: 20;";
            CODE_AREA_LINE_NUM_CSS = "-fx-background-color: background_color; -fx-text-fill: #b3b3b3; -fx-font: Monospace; -fx-font-size: 20;";
            DIFF_COLOR_DELETE = Color.valueOf("#ff6666");
            DIFF_COLOR_INSERT = Color.valueOf("#66ff66");
            DIFF_COLOR_REPLACE = Color.valueOf("#404040");
            DIFF_COLOR_TEXT = Color.valueOf("#e0e0e0");

        }
    }
}
