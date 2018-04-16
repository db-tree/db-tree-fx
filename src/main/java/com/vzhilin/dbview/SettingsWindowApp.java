package com.vzhilin.dbview;

import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.conf.Template;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SettingsWindowApp extends Application {
    @Override public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/settings-dialog.fxml"));

        Parent root = loader.load();
        SettingsController controller = loader.getController();
        controller.setMainApp(this);
        Settings settings = readSettings();
        controller.setSettings(settings);


        Scene scene = new Scene(root, 800, 800);
        scene.getStylesheets().add
            (getClass().getResource("/styles/connection-settings.css").toExternalForm());

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
    }

    private Settings readSettings() {
        Settings settings = new Settings();
        ConnectionSettings ra00c000 = new ConnectionSettings();
        ra00c000.templatesProperty().add(new Template("OESO_KCA", "KCAID"));
        ra00c000.templatesProperty().add(new Template("OESO_PTS", "PTSID"));
        ConnectionSettings rt00c000 = new ConnectionSettings();

        ra00c000.connectionNameProperty().set("RA00C000");
        rt00c000.connectionNameProperty().set("RT00C000");
        settings.connectionsProperty().add(ra00c000);
        settings.connectionsProperty().add(rt00c000);
        return settings;
    }
}
