package com.az.gitember.controller;

import com.az.gitember.App;
import com.az.gitember.controller.lang.java.*;
import com.az.gitember.controller.lang.java.impl.Java9ParserVisitorImpl;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class TextBrowser implements Initializable {

    public TextFlow codeArea;
    public ScrollPane scrollPane;
    private String content;
    private String fileName;

    private Java9ParserVisitorImpl visitor = new Java9ParserVisitorImpl();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setText(String text, boolean diff) {

        //TODO setting to fxml
        codeArea.setPrefWidth(Region.USE_COMPUTED_SIZE);
        codeArea.setMinWidth(Region.USE_PREF_SIZE);

        content = text;

        long dt = System.currentTimeMillis();

        Java9Lexer lex = new Java9Lexer( new ANTLRInputStream(content));
        System.out.println(">>>> 1 " + (System.currentTimeMillis() - dt));
        lex.getAllTokens();
        Java9Parser parser = new Java9Parser(new CommonTokenStream(lex));
        System.out.println(">>>> 2 " + (System.currentTimeMillis() - dt));
        ParseTree tree = parser.compilationUnit();
        System.out.println(">>>> 3 " + (System.currentTimeMillis() - dt));
        visitor.visit(tree);
        System.out.println(">>>> 4 " + (System.currentTimeMillis() - dt));

        TextBrowserContentAdapter adapter = new TextBrowserContentAdapter(FilenameUtils.getExtension(fileName), diff, true, visitor.getParsedCode());
        codeArea.getChildren().addAll(
                adapter.getText(text)
        );




    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void saveFile(ActionEvent actionEvent) {

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter anyFilter = new FileChooser.ExtensionFilter("Any files (*.*)", "*.*");
        fileChooser.getExtensionFilters().add(anyFilter);

        if (StringUtils.isNotBlank(FilenameUtils.getExtension(fileName))) {
            String ext = FilenameUtils.getExtension(fileName);
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(ext + " files (*." + ext + ")", "*." + ext);
            fileChooser.getExtensionFilters().add(extFilter);
        }

        fileChooser.setTitle("Save file");

        //Show save file dialog
        File file = fileChooser.showSaveDialog(App.getScene().getWindow());

        if (file != null) {

            try {
                Files.write(file.toPath(), this.content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
