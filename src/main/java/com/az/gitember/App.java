package com.az.gitember;

import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import com.az.gitember.controller.LookAndFeelSet;
import com.az.gitember.data.Const;
import com.az.gitember.data.Pair;
import com.az.gitember.service.Context;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.eclipse.jgit.storage.file.WindowCacheConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * JavaFX App
 */
public class App extends Application {

    private final static Logger log = Logger.getLogger(App.class.getName());

    private static Scene scene;
    private static Stage stage;
    private static App app;
    private static ReadOnlyBooleanProperty isFocused;

    // Store command-line arguments for processing in start()
    private static List<String> commandLineArgs;

    @Override
    public void init() {
        // Capture command-line arguments before JavaFX starts
        commandLineArgs = getParameters().getRaw();
    }

    @Override
    public void start(Stage stage) throws IOException {

        // Check if two file arguments were provided for diff mode
        if (commandLineArgs != null && commandLineArgs.size() == 2) {
            if (handleDiffMode(stage, commandLineArgs.get(0), commandLineArgs.get(1))) {
                return; // Successfully opened diff viewer, don't start main app
            }
        } else if (commandLineArgs != null && commandLineArgs.size() > 0 && !commandLineArgs.isEmpty()) {
            // Invalid number of arguments, print usage and exit
            printUsage();
            Platform.exit();
            return;
        }

        // Normal application startup

        Font font = Font.loadFont(App.class.getResourceAsStream("/sourcesanspro/SourceSansPro-Regular.otf"), 40);

        Context.readSettings();

        if ("Dark".equals(Context.settingsProperty.get().getTheme())) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }

        LogManager.getLogManager().readConfiguration(App.class.getResourceAsStream("/log.properties"));
        (new WindowCacheConfig()).install();


        Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
        int minus = 10;
        double width = rectangle2D.getWidth() - minus;
        double height = rectangle2D.getHeight() - minus;


        App.isFocused = stage.focusedProperty();
        App.stage = stage;
        App.app = this;

        scene = new Scene(loadFXML(Const.View.MAIN).getFirst(), width, height);

        scene.setFill(Color.TRANSPARENT);
        //stage.setMaximized(true);
        stage.setScene(scene);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream(Const.ICON)));
        stage.setTitle(Const.APP_NAME);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.show();

        LookAndFeelSet.init(Context.settingsProperty.get().getTheme());
        scene.getStylesheets().add(App.class.getResource(LookAndFeelSet.DEFAULT_CSS).toExternalForm());

        stage.setOnCloseRequest(
                event -> {
                    Platform.exit();
                }
        );

        ResizeHelper.addResizeListener(stage);
    }

    @Override
    public void stop() throws Exception {
        log.log(Level.INFO, "Gitemeber is stopped");
        Context.saveSettings();
        Context.getGitRepoService().shutdown();
        super.stop();
    }

    public static HostServices getShell() {
        return App.app.getHostServices();
    }

    public static Pair<Parent, Object> loadFXMLToNewStage(final String fxmlFileName, final String windowTitle)  {
        final Pair<Parent, Object> pair = loadFXML(fxmlFileName);
        Window currentWindow = scene.getWindow();
        final Stage newStage = new Stage();
        final Scene newScene = new Scene(pair.getFirst());
        newStage.getIcons().add(new Image(App.class.getResourceAsStream(Const.ICON)));
        newStage.setScene(newScene);
        newScene.getWindow().setX(currentWindow.getX() + 50);
        newScene.getWindow().setY(currentWindow.getY() + 50);
        newStage.show();
        newStage.setTitle(windowTitle);
        newScene.getStylesheets().add(App.class.getResource(LookAndFeelSet.DEFAULT_CSS).toExternalForm());
        return pair;
    }

    public static Pair<Parent, Object> loadFXML(String fxml)  { //TODO refactor handle exception here
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
            Parent parent = fxmlLoader.load();
            Object controller = fxmlLoader.getController();
            return new Pair<>(parent, controller);
        } catch (Exception e) {
            Context.getMain().showResult("Cannot load FXML", e.getMessage(), Alert.AlertType.ERROR);
            throw new RuntimeException(e);
        }

    }

    public static Stage getStage() {
        return stage;
    }

    public static Scene getScene() {
        return scene;
    }

    public static ReadOnlyBooleanProperty getIsFocused() {
        return isFocused;
    }

    /**
     * Handle diff mode when two files are provided as command-line arguments
     * @return true if diff viewer was successfully opened, false if files are invalid
     */
    private boolean handleDiffMode(Stage stage, String file1Path, String file2Path) {
        File file1 = new File(file1Path);
        File file2 = new File(file2Path);

        // Validate both files exist
        if (!file1.exists() || !file1.isFile()) {
            System.err.println("Error: First file does not exist or is not a file: " + file1Path);
            printUsage();
            Platform.exit();
            return false;
        }

        if (!file2.exists() || !file2.isFile()) {
            System.err.println("Error: Second file does not exist or is not a file: " + file2Path);
            printUsage();
            Platform.exit();
            return false;
        }

        try {
            // Initialize minimal theme for diff viewer
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            LookAndFeelSet.init("Light");

            // Create diff viewer window
            App.stage = stage;
            App.app = this;

            Pair<Parent, Object> pair = App.loadFXMLToNewStage(Const.View.FILE_DIFF,
                    "Diff: " + file1.getName() + " ↔ " + file2.getName());

            // Apply styles
            pair.getFirst().getStylesheets().add(
                    App.class.getResource(LookAndFeelSet.DEFAULT_CSS).toExternalForm());
            pair.getFirst().getStylesheets().add(
                    App.class.getResource(LookAndFeelSet.KEYWORDS_CSS).toExternalForm());

            // Get the diff controller and load the files
            com.az.gitember.controller.diff.DiffController diffController =
                    (com.az.gitember.controller.diff.DiffController) pair.getSecond();

            // Load the files for comparison using Platform.runLater to ensure UI is ready
            Platform.runLater(() -> {
                try {
                    diffController.loadFilesForDiff(file1.getAbsolutePath(), file2.getAbsolutePath());
                    System.out.println("Diff viewer opened successfully");
                    System.out.println("  Left:  " + file1.getAbsolutePath());
                    System.out.println("  Right: " + file2.getAbsolutePath());
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Error loading files for diff", e);
                    System.err.println("Error loading files for diff: " + e.getMessage());
                    Platform.exit();
                }
            });

            return true;

        } catch (Exception e) {
            System.err.println("Error opening diff viewer: " + e.getMessage());
            Platform.exit();
            return false;
        }
    }

    /**
     * Print usage information for command-line mode
     */
    private void printUsage() {
        System.out.println();
        System.out.println("Gitember - Git Repository Manager");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  gitember2                    - Start the full application");
        System.out.println("  gitember2 <file1> <file2>    - Show diff between two files");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  gitember2");
        System.out.println("  gitember2 file1.txt file2.txt");
        System.out.println("  gitember2 /path/to/old.java /path/to/new.java");
        System.out.println("On mac use open -a \"gitember2\" ...");
    }

    public static void main(String[] args) {
        launch(args);
    }

}