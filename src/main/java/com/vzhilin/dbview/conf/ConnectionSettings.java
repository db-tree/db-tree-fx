package com.vzhilin.dbview.conf;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class ConnectionSettings {
    private final StringProperty connectionName = new SimpleStringProperty();
    private final StringProperty driverClass = new SimpleStringProperty();
    private final StringProperty jdbcUrl = new SimpleStringProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty password = new SimpleStringProperty();

    private final ListProperty<Template> templates = new SimpleListProperty<Template>(FXCollections.observableArrayList(new ArrayList<>()));

    public ConnectionSettings() {
    }

    public ConnectionSettings(ConnectionSettings cs) {
        connectionName.set(cs.getConnectionName());
        driverClass.set(cs.getDriverClass());
        jdbcUrl.set(cs.getJdbcUrl());
        username.set(cs.getUsername());
        password.set(cs.getPassword());

        for (Template t: cs.templates) {
            templates.add(new Template(t.getTableName(), t.getTemplate()));
        }
    }

    public String getConnectionName() {
        return connectionName.get();
    }

    public StringProperty connectionNameProperty() {
        return connectionName;
    }

    public String getDriverClass() {
        return driverClass.get();
    }

    public StringProperty driverClassProperty() {
        return driverClass;
    }

    public String getJdbcUrl() {
        return jdbcUrl.get();
    }

    public StringProperty jdbcUrlProperty() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username.get();
    }

    public StringProperty usernameProperty() {
        return username;
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public ObservableList<Template> getTemplates() {
        return templates.get();
    }

    public ListProperty<Template> templatesProperty() {
        return templates;
    }

    @Override public String toString() {
        return connectionName.get();
    }
}

