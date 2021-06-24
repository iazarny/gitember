package com.az.gitember.controller;

import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextBrowserContentAdapterTest {


    @Test
    void reTest() {

        //TODo refactor

        TextBrowserContentAdapter adapter = new TextBrowserContentAdapter("TODO", "java", false, true);
        List<Node> nodes = adapter.lineToTexts("  import zzz;  \"s\"[ zx]{}()", 0);
        List<HBox> textList = new ArrayList(nodes);
        assertEquals(13, textList.size());
        assertEquals("  ", ((Text)textList.get(0).getChildren().get(0)).getText());
        assertEquals("import", ((Text)textList.get(1).getChildren().get(0)).getText());
        assertEquals(" zzz", ((Text)textList.get(2).getChildren().get(0)).getText());
        assertEquals(";", ((Text)textList.get(3).getChildren().get(0)).getText());
        assertEquals("  ", ((Text)textList.get(4).getChildren().get(0)).getText());
        assertEquals("\"s\"", ((Text)textList.get(5).getChildren().get(0)).getText());
        assertEquals("[", ((Text)textList.get(6).getChildren().get(0)).getText());
        assertEquals(" zx", ((Text)textList.get(7).getChildren().get(0)).getText());
        assertEquals("]", ((Text)textList.get(8).getChildren().get(0)).getText());
        assertEquals("{", ((Text)textList.get(9).getChildren().get(0)).getText());
        assertEquals("}", ((Text)textList.get(10).getChildren().get(0)).getText());
        assertEquals("(",((Text)textList.get(11).getChildren().get(0)).getText());
        assertEquals(")", ((Text)textList.get(12).getChildren().get(0)).getText());

    }
}