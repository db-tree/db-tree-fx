package me.vzhilin.dbtree.ui.conf;

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
import java.io.PrintWriter;
import java.util.ArrayList;

//import org.hildan.fxgson.FxGson;

public class Settings {
    private final static Logger LOG = Logger.getLogger(Settings.class);
    private final static String OS = System.getProperty("os.name").toLowerCase();

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
            File configFile = new File(getFolder(), ".db-tree.json");
            File tempFile = new File(getFolder(), ".db-tree.json~");

            PrintWriter pw = new PrintWriter(tempFile);
            pw.write(FxGson.coreBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create().toJson(this));
            pw.close();
            configFile.delete();
            tempFile.renameTo(configFile);
        } catch (FileNotFoundException e) {
            LOG.error(e, e);
        }
    }

    public ConnectionSettings getConnection(String connectionName) {
        return connections.filtered(c -> c.getConnectionName().equals(connectionName)).get(0);
    }

    private File getFolder() {
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
