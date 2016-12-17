package com.az.gitember;

import com.az.gitember.misc.HighlightProvider;
import com.az.gitember.ui.DiffLineNumberFactory;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class FileViewController extends BaseFileViewController {

    private CodeArea codeArea;

    public FileViewController() {

        super();
        codeArea = new CodeArea();

        /*IntFunction<Node> numberFactory = DiffLineNumberFactory.get(codeArea);
        //IntFunction<Node> arrowFactory = new ArrowFactory(codeArea);
        IntFunction<Node> graphicFactory = line -> {
            HBox hbox = new HBox(
                    numberFactory.apply(line)
                    //arrowFactory.apply(line)
            );
            hbox.setAlignment(Pos.CENTER_LEFT);
            hbox.toFront();
            return hbox;
        };*/
        codeArea.setParagraphGraphicFactory(DiffLineNumberFactory.get(codeArea, Collections.EMPTY_LIST));
        codeArea.setEditable(false);
    }

    public void openFile(String fileName, String displayTitle) throws IOException {


        fillCodeArea(codeArea, fileName);
        //  need to wait for https://github.com/TomasMikula/RichTextFX/pull/398
        //  and https://github.com/TomasMikula/RichTextFX/issues/289
        //codeArea.setParagraphStyle(10, new ArrayList<String>() {{  add("-rtfx-background-color: coral");  add("-fx-background-color: coral");  }} );


        Scene scene = new Scene(new StackPane(new VirtualizedScrollPane(codeArea)), 1024, 768);
        scene.getStylesheets().add(this.getClass().getResource("/styles/keywords.css").toExternalForm());

        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(displayTitle);
        stage.show();
    }


}
