package com.vzhilin.dbview.ui.conf;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Settings {
    private final static Logger LOG = Logger.getLogger(Settings.class);

    private final ListProperty<ConnectionSettings> connections = new SimpleListProperty<ConnectionSettings>(FXCollections.observableArrayList(new ArrayList<>()));

    public ObservableList<ConnectionSettings> getConnections() {
        return connections.get();
    }

    public ListProperty<ConnectionSettings> connectionsProperty() {
        return connections;
    }

    public synchronized void save() {
        try {
            PrintWriter pw = new PrintWriter("db-tree.json~");
            pw.write(FxGson.create().toJson(this));
            pw.close();

            File tempFile = new File("db-tree.json~");
            File configFile = new File("db-tree.json");
            configFile.delete();
            tempFile.renameTo(configFile);
        } catch (FileNotFoundException e) {
            LOG.error(e, e);
        }
    }

    public ConnectionSettings getConnection(String connectionName) {
        return connections.filtered(c -> c.getConnectionName().equals(connectionName)).get(0);
    }
}
