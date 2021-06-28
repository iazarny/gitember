package com.az.gitember.service;

import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.LangDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.transformation.SortedList;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GitemberUtil {

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String formatDate(Date date) {
        return simpleDateFormat.format(date);
    }

    public static Date intToDate(int time) {
        return new Date(1000L * time);
    }

    public static String getMimeType(Path path) throws IOException {
        String mimeType = Files.probeContentType(path);
        if (StringUtils.isBlank(mimeType)) {
            mimeType = URLConnection.guessContentTypeFromName(path.toString());
        }
        return mimeType;
    }


    public static IsClass is(String str) {

        return new IsClass(str);

    }

    public static class IsClass {

        private final String obj;

        public IsClass(String obj) {
            this.obj = obj;
        }

        public boolean oneOf(String... objes) {
            return Arrays.stream(objes).filter(s -> s.equals(obj)).findFirst().isPresent();
        }
    }


    public static LangDefinition[] getKeywordsDefinition(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        LangDefinition[] rez ;
        try {
            rez = objectMapper.readValue(json, LangDefinition[].class);
        } catch (IOException e) {
            rez = new LangDefinition[0];
            e.printStackTrace();
        }
        return rez;
    }

    public static Map<String, Integer> topValues(Map<String, Integer> toFilter, int topQty) {
        return toFilter.entrySet().stream()
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .limit(topQty)
                .collect(Collectors.toMap(
                        stringIntegerEntry -> stringIntegerEntry.getKey(),
                        stringIntegerEntry -> stringIntegerEntry.getValue()));
    }

    //TODO return styleclass
    public static Color getDiffColor(Edit delta) {
        switch (delta.getType()) {
            case INSERT: return LookAndFeelSet.DIFF_FILL_COLOR_INSERT;
            case DELETE: return LookAndFeelSet.DIFF_FILL_COLOR_DELETE;
            case REPLACE: return  LookAndFeelSet.DIFF_FILL_COLOR_REPLACE;
            case EMPTY: return  LookAndFeelSet.DIFF_FILL_COLOR_EMPTY;
        }
        return  LookAndFeelSet.DIFF_FILL_COLOR_INSERT;
    }


}
