package com.vzhilin.dbview.conf;

import java.util.ArrayList;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Settings {
    private final ListProperty<ConnectionSettings> connections = new SimpleListProperty<ConnectionSettings>(FXCollections.observableArrayList(new ArrayList<>()));

    public ObservableList<ConnectionSettings> getConnections() {
        return connections.get();
    }

    public ListProperty<ConnectionSettings> connectionsProperty() {
        return connections;
    }
}
