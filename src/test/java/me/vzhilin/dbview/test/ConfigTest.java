package me.vzhilin.dbview.test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import javafx.beans.property.ListProperty;
import me.vzhilin.dbview.ui.conf.ConnectionSettings;
import me.vzhilin.dbview.ui.conf.Settings;
import me.vzhilin.dbview.ui.conf.Template;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.IOException;

public class ConfigTest {
    public static void main(String... argv) {
        new ConfigTest().start();
    }

    private Settings readSettings() {
        Settings settings = new Settings();
        ConnectionSettings ra00c000 = new ConnectionSettings();
        ra00c000.addLookupableColumn("OESO_KCA", "KCAREGNUMBER");

        ra00c000.driverClassProperty().set("oracle.jdbc.OracleDriver");
        ra00c000.jdbcUrlProperty().set("jdbc:oracle:thin:@localhost:1521:XE");
        ra00c000.usernameProperty().set("voshod");
        ra00c000.passwordProperty().set("voshod");

        ListProperty<Template> templates = ra00c000.templatesProperty();
        templates.add(new Template("OESO_KCA", "KCANAME"));
        templates.add(new Template("OESO_PTS", "PTSID"));


        ra00c000.connectionNameProperty().set("RA00C000");
        settings.connectionsProperty().add(ra00c000);
        return settings;
    }

    private void start() {
        Gson gson = FxGson.create();

        Settings settings = readSettings();

        try {
            Files.write(gson.toJson(settings).getBytes(Charsets.UTF_8), new File("db-tree.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
