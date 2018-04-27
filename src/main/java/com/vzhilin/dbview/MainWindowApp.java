package com.vzhilin.dbview;

import com.google.common.base.Joiner;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.data.Row;
import javafx.application.Application;
import javafx.beans.property.ListProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.hildan.fxgson.FxGson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Locale;

import static com.google.common.collect.Iterables.getOnlyElement;

public class MainWindowApp extends Application {
    @Override public void start(Stage stage) throws Exception {
        Locale.setDefault(Locale.US);
        setIcon(stage);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/main-window.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 800);
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
        Settings settings = readSettings();
        controller.setSettings(settings);

        try {
            DbContext ctx = new DbContext("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:XE", "voshod", "voshod");
            QueryContext queryContext = new QueryContext(ctx, getOnlyElement(settings.getConnections()));
//            Row r = new Row(queryContext, ctx.getSchema().getTable("OESO_KCA"), 2190);

            Property<DbContext> currentContext = new SimpleObjectProperty<>();
            currentContext.setValue(ctx);

            controller.setCtx(currentContext);
//            controller.show(r);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Settings readSettings() {
        try {
            return FxGson.create().fromJson(Joiner.on("").join(Files.readAllLines(Paths.get("db-tree.json"))), Settings.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
