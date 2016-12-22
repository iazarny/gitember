package com.az.gitember;

import com.az.gitember.misc.Const;
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
        codeArea.setParagraphGraphicFactory(DiffLineNumberFactory.get(codeArea, Collections.EMPTY_LIST));
        codeArea.setEditable(false);
    }

    public void openFile(String fileName, String displayTitle) throws IOException {
        fillCodeArea(codeArea, fileName);
        codeArea.moveTo(0);
        codeArea.selectRange(0, 0);
        Scene scene = new Scene(new StackPane(new VirtualizedScrollPane(codeArea)), 1024, 768);
        scene.getStylesheets().add(this.getClass().getResource(Const.KEYWORDS_CSS).toExternalForm());
        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(displayTitle);
        stage.show();
    }


}
