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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.eclipse.jgit.storage.file.WindowCacheConfig;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
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


    @Override
    public void start(Stage stage) throws IOException {

        Context.readSettings();

        if ("Dark".equals(Context.settingsProperty.get().getTheme())) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }

        LogManager.getLogManager().readConfiguration(App.class.getResourceAsStream("/log.properties"));
        (new WindowCacheConfig()).install();


        Rectangle2D rectangle2D = Screen.getPrimary().getVisualBounds();
        int minus = 20;
        double width = rectangle2D.getWidth() - minus;
        double height = rectangle2D.getHeight() - minus;

        scene = new Scene(loadFXML(Const.View.MAIN).getFirst(), width, height);
        stage.setScene(scene);
        stage.getIcons().add(new Image(this.getClass().getResourceAsStream(Const.ICON)));
        stage.setTitle(Const.APP_NAME);
        stage.show();

        App.stage = stage;
        App.app = this;

        LookAndFeelSet.init(Context.settingsProperty.get().getTheme());
        scene.getStylesheets().add(App.class.getResource(LookAndFeelSet.DEFAULT_CSS).toExternalForm());

        stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean onHidden, Boolean onShown) {
                if (onShown
                        && Const.View.WORKING_COPY.equals(Context.mainPaneName.getValueSafe())
                        && isAllowUpdateByTime()) {
                    Platform.runLater(() -> Context.updateStatusIfNeed(null));
                }
            }
        });

        stage.setOnCloseRequest(
                event -> {
                    Platform.exit();
                }
        );
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

    public static Pair<Parent, Object> loadFXMLToNewStage(final String fxmlFileName, final String windowTitle) throws IOException {
        final Pair<Parent, Object> pair = loadFXML(fxmlFileName);
        final Stage newStage = new Stage();
        final Scene newScene = new Scene(pair.getFirst());

        newStage.getIcons().add(new Image(App.class.getResourceAsStream(Const.ICON)));
        newStage.setScene(newScene);
        newStage.show();
        newStage.setTitle(windowTitle);
        newScene.getStylesheets().add(App.class.getResource(LookAndFeelSet.DEFAULT_CSS).toExternalForm());
        return pair;
    }

    public static Pair<Parent, Object> loadFXML(String fxml) throws IOException { //TODO refactor handle exception here
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent parent = fxmlLoader.load();
        Object controller = fxmlLoader.getController();
        return new Pair<>(parent, controller);
    }

    public static Stage getStage() {
        return stage;
    }

    public static Scene getScene() {
        return scene;
    }


    public static void main(String[] args) {

        launch();
    }

    private boolean isAllowUpdateByTime() {
        boolean allowUpdateByTime = false;
        LocalDateTime lastUpdate = Context.lastUpdate.get();
        if (lastUpdate != null) {
            Duration duration = Duration.between(lastUpdate, LocalDateTime.now());
            if (duration.toSeconds() > 60) {
                allowUpdateByTime = true;
            }
        }
        return allowUpdateByTime;
    }


}