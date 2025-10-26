package com.az.gitember.controller;

import com.az.gitember.service.Context;
import javafx.scene.paint.Color;

public class LookAndFeelSet {


    public static int DIALOG_DEFAULT_WIDTH = 600;
    public static String PIECHART_LEGENF_STYLE = "";
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
    public static String CODE_AREA_CSS = "-fx-font-size: 18; -fx-font-family: Source Sans Pro; -fx-background-color: white;";
    public static String CODE_AREA_LINE_NUM_CSS = "-fx-background-color: white; -fx-text-fill: #b3b3b3; -fx-font-family: Source Sans Pro;";


    //for scroll pane
    public static Color DIFF_COLOR_DELETE = Color.valueOf("#ff6666");
    public static Color DIFF_COLOR_INSERT = Color.valueOf("#66ff66");
    public static Color DIFF_COLOR_REPLACE = Color.valueOf("#6666ff");
    public static Color DIFF_COLOR_TEXT =  Color.valueOf("#e0e0e0");
    public static String HISTORY_LABEL_BOX_CSS = "-fx-border-insets: 0 5 0 0px; -fx-background-insets: 0 5 0 0px; -fx-padding: 0 4px; -fx-background-color: #d1e7dd; -fx-border-color: #a3cfbb; -fx-spacing: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px";
    public static String INFO_LABEL = "-fx-font-size: 10px; -fx-border-insets: 0 5 0 0px; -fx-background-insets: 0 5 0 0px; -fx-padding: 0 4px; -fx-background-color: #d1e7dd; -fx-border-color: #a3cfbb; -fx-spacing: 10px; -fx-border-radius: 4px; -fx-background-radius: 4px";
    public static String HISTORY_LABEL_BOX_TXT_CSS = "-fx-text-fill: #0a3622; -fx-font-size: 14px;";
    public static String HISTORY_BOX_CSS = "-fx-background-color: transparent";
    public static String HISTORY_BOX_TXT_CSS = "";
    public static String RESULT_WARNING = "-fx-text-fill: #1a1919; -fx-background-color: #d7d7ea";
    public static String RESULT_ERROR = "-fx-text-fill: #1a1919; -fx-background-color: #EADFE2";

    public static double FONT_SIZE = 23.0;

    public static void init(String themeMode) {

        if ("Dark".equalsIgnoreCase(themeMode)) {
            PIECHART_LEGENF_STYLE = "-fx-fill: lightgray";
            BRANCH_NAME_COLOR = Color.LIGHTGRAY;
            KEYWORDS_CSS = "/styles/keywords_dark.css";
            DEFAULT_CSS = "/styles/styles_dark.css";
            FOUND_ROW = "-fx-font-weight: bold; -fx-background-color: #004f8b";

            CODE_AREA_CSS = "-fx-fill: gray; -fx-background-color: black;  -fx-font-size: 18; -fx-font-family: Source Sans Pro;";
            CODE_AREA_LINE_NUM_CSS = "-fx-background-color: black; -fx-text-fill: #505050; -fx-font-family: Source Sans Pro;";

            DIFF_COLOR_DELETE = Color.valueOf("#ff6666");
            DIFF_COLOR_INSERT = Color.valueOf("#66ff66");
            DIFF_COLOR_REPLACE = Color.valueOf("#118888");
            DIFF_COLOR_TEXT = Color.valueOf("#303030");
            HISTORY_LABEL_BOX_CSS = "-fx-border-insets: 0 5 0 0px; -fx-background-insets: 0 5 0 0px; -fx-padding: 0 4px; -fx-background-color: #404040; -fx-border-color: #a3cfbb; -fx-spacing: 10px;  -fx-border-radius: 4px; -fx-background-radius: 4px";
            INFO_LABEL = "-fx-font-size: 10px; -fx-border-insets: 0 5 0 0px; -fx-background-insets: 0 5 0 0px; -fx-padding: 0 4px; -fx-background-color: #404040; -fx-border-color: #a3cfbb; -fx-spacing: 10px;  -fx-border-radius: 4px; -fx-background-radius: 4px";
            HISTORY_LABEL_BOX_TXT_CSS = "-fx-text-fill: #0a3622; -fx-font-size: 16px;";
            HISTORY_BOX_CSS = "-fx-background-color: transparent";
            HISTORY_BOX_TXT_CSS = "-fx-font-size: 14pt";
            RESULT_WARNING = "-fx-text-fill: white; -fx-background-color: #070969";
            RESULT_ERROR = "-fx-text-fill: white; -fx-background-color: #690707;";
        }

        if (Context.isWindows()) {
            LookAndFeelSet.FONT_SIZE = 27.0;
            DEFAULT_CSS = DEFAULT_CSS.replace("/styles/", "/styles/win/");
        }

        if (Context.isLinux()) {
            LookAndFeelSet.FONT_SIZE = 21;
            DEFAULT_CSS = DEFAULT_CSS.replace("/styles/", "/styles/lin/");
        }

    }
}
