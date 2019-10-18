package me.vzhilin.dbtree;

import com.google.common.base.Joiner;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import me.vzhilin.dbtree.ui.ApplicationContext;
import me.vzhilin.dbtree.ui.MainWindowController;
import me.vzhilin.dbtree.ui.conf.Settings;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.hildan.fxgson.FxGson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class MainWindowApp extends Application {
    private final static Logger LOG = Logger.getLogger(MainWindowApp.class);
    private Settings settings;

    @Override public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(true);
        BasicConfigurator.configure();
        Locale.setDefault(Locale.US);
        setIcon(stage);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/main-window.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 500);
        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.add(getClass().getResource("/styles/connection-settings.css").toExternalForm());
        stylesheets.add(getClass().getResource("/styles/autocomplete.css").toExternalForm());
        stylesheets.add(getClass().getResource("/styles/main-window.css").toExternalForm());
        stage.setTitle("DB Tree");
        stage.setScene(scene);
        stage.show();
        MainWindowController controller = loader.getController();
        load(controller);
        controller.setOwnerWindow(stage);

    }

    private void setIcon(Stage stage) {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.png")));
    }

    private void load(MainWindowController controller) {
        settings = readSettings();
        controller.setSettings(settings);
        controller.setAppContext(new ApplicationContext(settings));
    }

    private Settings readSettings() {
        try {
            return FxGson.create().fromJson(Joiner.on("").join(Files.readAllLines(Paths.get("db-tree.json"))), Settings.class);
        } catch (IOException e) {
            LOG.error(e, e);
        }
        return new Settings();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        settings.save();
    }
}
