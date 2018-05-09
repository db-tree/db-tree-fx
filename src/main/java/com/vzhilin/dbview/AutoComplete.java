package com.vzhilin.dbview;

import com.vzhilin.dbview.db.DbContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

public class AutoComplete extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        Locale.setDefault(Locale.US);

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/auto-complete.fxml"));

        Parent root = loader.load();

        Scene scene = new Scene(root, 800, 30);
        scene.getStylesheets().add
                (getClass().getResource("/styles/autocomplete.css").toExternalForm());

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();

        AutocompleteController controller = (AutocompleteController) loader.getController();
        controller.setScene(scene);

        DbContext ctx = new DbContext("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:XE", "voshod", "voshod", "");
        controller.setContext(ctx);
    }
}
