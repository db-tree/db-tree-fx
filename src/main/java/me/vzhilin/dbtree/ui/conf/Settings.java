package me.vzhilin.dbtree.ui.conf;

import com.google.common.base.Joiner;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;

//import org.hildan.fxgson.FxGson;

public class Settings {
    public static final String DB_TREE_JSON_TEMP = ".db-tree.json~";
    private final static Logger LOG = Logger.getLogger(Settings.class);
    private final static String OS = System.getProperty("os.name").toLowerCase();
    public static final String DB_TREE_JSON = ".db-tree.json";

    private final DoubleProperty dividerPosition = new SimpleDoubleProperty();
    private final ListProperty<ConnectionSettings> connections = new SimpleListProperty<ConnectionSettings>(FXCollections.observableArrayList(new ArrayList<>()));


    public ObservableList<ConnectionSettings> getConnections() {
        return connections.get();
    }

    public double getDividerPosition() {
        return dividerPosition.get();
    }

    public DoubleProperty dividerPositionProperty() {
        return dividerPosition;
    }

    public ListProperty<ConnectionSettings> connectionsProperty() {
        return connections;
    }

    public synchronized void save() {
        try {
            File folder = getOrCreateFolder();
            File configFile = new File(folder, DB_TREE_JSON);
            File tempFile = new File(folder, DB_TREE_JSON_TEMP);

            PrintWriter pw = new PrintWriter(tempFile);
            pw.write(FxGson.coreBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create().toJson(this));
            pw.close();
            configFile.delete();
            tempFile.renameTo(configFile);
        } catch (FileNotFoundException e) {
            LOG.error(e, e);
        }
    }

    private static File getOrCreateFolder() {
        File folder = getFolder();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                LOG.error("unable to create folder: " + folder.getAbsolutePath());
            }
        }
        return folder;
    }

    public ConnectionSettings getConnection(String connectionName) {
        return connections.filtered(c -> c.getConnectionName().equals(connectionName)).get(0);
    }

    public static Settings readSettings() {
        try {
            File folder = getOrCreateFolder();
            File configFile = new File(folder, ".db-tree.json");
            return FxGson.create().fromJson(Joiner.on("").join(Files.readAllLines(configFile.toPath())), Settings.class);
        } catch (IOException e) {
            LOG.error(e, e);
        }
        return new Settings();
    }

    private static File getFolder() {
        if (OS.contains("win")) {
            return new File("%APPDATA%\\DBTree");
        } else
        if (OS.contains("mac")) {
            return new File("~/Library/Preferences/me.vzhilin.dbtree");
        } else
        if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")){
            return new File("~");
        } else {
            return new File(".");
        }
    }
}
