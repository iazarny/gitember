package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.HighlightProvider;
import com.az.gitember.ui.DiffLineNumberFactory;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.ResourceBundle;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class FileViewController extends BaseFileViewController {

    private CodeArea codeArea;
    private Scene scene;
    private String displayTitle;
    private String fileName;

    public FileViewController() {
        super();
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(DiffLineNumberFactory.get(codeArea, Collections.EMPTY_LIST));
        codeArea.setEditable(false);

        Button button = new Button("Save as ...");

        scene = new Scene(
                new BorderPane(
                        new VirtualizedScrollPane(codeArea),
                        new ToolBar(button),
                        null,null,null
                ),
                1024, 768);
        scene.getStylesheets().add(this.getClass().getResource(Const.KEYWORDS_CSS).toExternalForm());


        button.setOnAction(
                event -> {
                    FileChooser fileChooser = new FileChooser();

                    //Set extension filter
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Any files (*.*)", "*.*");
                    fileChooser.getExtensionFilters().add(extFilter);

                    //Show save file dialog
                    File file = fileChooser.showSaveDialog(scene.getWindow());

                    if (file != null) {

                        try {
                            Files.copy(Paths.get(fileName), file.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
        );

    }

    public void openFile(String fileName, String displayTitle) throws IOException {

        this.displayTitle = displayTitle;
        this.fileName = fileName;

        fillCodeArea(codeArea, fileName);
        codeArea.moveTo(0);
        codeArea.selectRange(0, 0);

        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle(displayTitle);
        stage.show();
    }


}
