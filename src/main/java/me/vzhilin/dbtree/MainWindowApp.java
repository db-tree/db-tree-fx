package me.vzhilin.dbtree;

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

import java.util.Locale;

public class MainWindowApp extends Application {
    private final static Logger LOG = Logger.getLogger(MainWindowApp.class);
    private Settings settings;

    static {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("prism.lcdtext", "false");
        }
    }

    @Override public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(true);
        BasicConfigurator.configure();
        Locale.setDefault(Locale.US);
        setIcon(stage);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/main-window.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1024, 768);
        ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.add(getClass().getResource("/styles/connection-settings.css").toExternalForm());
        stylesheets.add(getClass().getResource("/styles/autocomplete.css").toExternalForm());
        stylesheets.add(getClass().getResource("/styles/main-window.css").toExternalForm());
        stage.setTitle("DB Tree");
        stage.setScene(scene);

        MainWindowController controller = loader.getController();
        load(controller);
        controller.setOwnerWindow(stage);
        if (settings.getMainWindow() != null) {
            Settings.Dimensions dimension = settings.getMainWindow();
            stage.setWidth(dimension.width);
            stage.setHeight(dimension.height);
        }

        stage.widthProperty().addListener((v, ov, nv) -> settings.setMainWindowWidth(nv.intValue()));
        stage.heightProperty().addListener((v, ov, nv) -> settings.setMainWindowHeight(nv.intValue()));
        stage.show();

    }

    private void setIcon(Stage stage) {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.png")));
    }

    private void load(MainWindowController controller) {
        settings = Settings.readSettings();
        controller.setSettings(settings);
        controller.setAppContext(new ApplicationContext(settings));
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        settings.save();
        Platform.exit();
        System.exit(0); // workaround JavaFX exit bug on GNU/Linux
    }
}
