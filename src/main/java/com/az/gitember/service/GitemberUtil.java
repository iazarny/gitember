package com.az.gitember.service;

import com.az.gitember.data.LangDefinition;
import com.az.gitember.data.Pair;
import com.az.gitember.data.Side;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.javafx.StackedFontIcon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class GitemberUtil {


    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static SimpleDateFormat simpleDateOnlyFormat = new SimpleDateFormat("yyyy-MM-dd");



    public static String formatDateOnly(Date date) {
        return simpleDateOnlyFormat.format(date);
    }

    public static String formatDate(Date date) {
        return simpleDateFormat.format(date);
    }

    public static Date intToDate(int time) {
        return new Date(1000L * time);
        //https://stackoverflow.com/questions/12608610/how-do-you-get-the-author-date-and-commit-date-from-a-jgit-revcommit
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

    public static String getDiffSyleClass(Edit delta, String prefix) {
        return getDiffSyleClass(delta.getType(), prefix);
    }

    public static String getDiffSyleClass(Edit.Type type, String prefix) {
        switch (type) {
            case INSERT: return prefix + "-new";
            case DELETE: return prefix + "-deleted";
            default : return prefix + "-modified";
        }
    }

    public static Pair<ArrayList<String>, ArrayList<Edit.Type>> getLines(final String content, final EditList diffList, final Side side) {
        ArrayList<String> original = getLines(content);
        ArrayList<String> lines = new ArrayList<>(original.size());
        ArrayList<Edit.Type> types = new ArrayList<>(original.size());
        for (int i = 0; i < original.size(); i++) {
            Edit edit = getDiffAtLine(diffList, side, i);
            Edit.Type replaceType = null;

            if (edit != null) {
                Edit.Type type = edit.getType();
                if (type == Edit.Type.DELETE) {
                    Pair<Integer, Integer> pos = getPosition(side.opposite(), edit );
                    int emtyLineToAdd = pos.getSecond() - pos.getFirst();
                    if (emtyLineToAdd > 0) {
                        System.out.println("Side " +  side + " D Need to add lines " + pos);
                        addEmptyLines(lines, types, type, emtyLineToAdd);
                    }
                } else if (type == Edit.Type.INSERT) {
                    Pair<Integer, Integer> pos = getPosition(side.opposite(), edit );
                    int emtyLineToAdd = pos.getSecond() - pos.getFirst();
                    if (emtyLineToAdd > 0) {
                        System.out.println("Side " +  side + " i Need to add lines " + pos);
                        addEmptyLines(lines, types, type, emtyLineToAdd);
                    }

                 } else if (type == Edit.Type.REPLACE) {
                    replaceType = type;
                }
            }

            final String linetoAdd = original.get(i);
            lines.add(linetoAdd);
            types.add(replaceType);
        }
        return new Pair<>(lines, types);
    }

    private static void addEmptyLines(ArrayList<String> lines, ArrayList<Edit.Type> types, Edit.Type type, int emtyLineToAdd) {
        for (int j = 0; j < emtyLineToAdd; j++) {
            lines.add("");
            types.add(type);
        }
    }

    private static Edit getDiffAtLine(final EditList diffList, final Side side, final int lineIdx) {

        for (int i = 0; i < diffList.size(); i++) {

            Edit edit = diffList.get(i);

            if (side == Side.A && lineIdx >= edit.getBeginA() && lineIdx <= edit.getEndA()) {
                return edit;
            } else if (side == Side.B && lineIdx >= edit.getBeginB() && lineIdx <= edit.getEndB()) {
                return edit;
            }

        }
        return  null;

    }

    private static Pair<Integer, Integer> getPosition(Side side, Edit edit) {
        if (side == Side.A) {
            return new Pair<>(edit.getBeginA(), edit.getEndA());
        } else {
            return new Pair<>(edit.getBeginB(), edit.getEndB());
        }
    }

    public static StackedFontIcon create(final FontIcon fontIcon) {
        final StackedFontIcon stackedFontIcon = new StackedFontIcon();
        stackedFontIcon.setStyle("-fx-icon-color: text_color");
        stackedFontIcon.getChildren().add(fontIcon);
        return stackedFontIcon;
    }

    public static ArrayList<String> getLines(final String content) {
        return (ArrayList<String>) new BufferedReader(new StringReader(content))
                .lines()
                .collect(Collectors.toList());
    }


    public static Object getField(Object obj, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = null;
        privateStringField = obj.getClass().getDeclaredField(fieldName);
        privateStringField.setAccessible(true);
        return privateStringField.get(obj);
    }

}
