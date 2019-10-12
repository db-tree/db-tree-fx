package me.vzhilin.dbtree.ui.conf;

import com.google.common.collect.Maps;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Настройки подключений
 */
public final class ConnectionSettings {
    /** Имя подключения */
    private final StringProperty connectionName = new SimpleStringProperty();

    /** Драйвер */
    private final StringProperty driverClass = new SimpleStringProperty();

    /** Адрес подключения */
    private final StringProperty jdbcUrl = new SimpleStringProperty();

    /** Имя пользователя */
    private final StringProperty username = new SimpleStringProperty();

    /** Пароль */
    private final StringProperty password = new SimpleStringProperty();

    /** regexp для имен таблиц */
    private final StringProperty tableNamePatternProperty = new SimpleStringProperty();

    /** Осмысленные значения */
    private final ListProperty<Template> templates = new SimpleListProperty<Template>(FXCollections.observableArrayList(new ArrayList<>()));

    /** Колонки для поиска */
    private final Map<String, Map<String, BooleanProperty>> lookupableColumns = Maps.newLinkedHashMap();

    public ConnectionSettings() {
    }

    public ConnectionSettings(ConnectionSettings cs) {
        connectionName.set(cs.getConnectionName());
        driverClass.set(cs.getDriverClass());
        jdbcUrl.set(cs.getJdbcUrl());
        username.set(cs.getUsername());
        password.set(cs.getPassword());
        tableNamePatternProperty.set(cs.getTableNamePattern());

        for (Template t: cs.templates) {
            templates.add(new Template(t.getSchemaName(), t.getTableName(), t.getTemplate()));
        }

        for (Map.Entry<String, Map<String, BooleanProperty>> e: cs.lookupableColumns.entrySet()) {
            LinkedHashMap<String, BooleanProperty> m = Maps.newLinkedHashMap();
            lookupableColumns.put(e.getKey(), m);

            for (Map.Entry<String, BooleanProperty> p: e.getValue().entrySet()) {
                m.put(p.getKey(), new SimpleBooleanProperty(p.getValue().get()));
            }
        }
    }

    /**
     * Table --> column --> isEnabled
     * @return
     */
    public Map<String, Map<String, BooleanProperty>> getLookupableColumns() {
        return lookupableColumns;
    }

    public BooleanProperty getLookupableProperty(String tableName, String columnName) {
        if (!lookupableColumns.containsKey(tableName)) {
            lookupableColumns.put(tableName, Maps.newLinkedHashMap());
        }

        Map<String, BooleanProperty> mp = lookupableColumns.get(tableName);
        if (!mp.containsKey(columnName)) {
            mp.put(columnName, new SimpleBooleanProperty());
        }

        return mp.get(columnName);
    }

    public void addLookupableColumn(String tableName, String columnName) {
       addLookupableColumn(tableName, columnName, false);
    }

    public void addLookupableColumn(String tableName, String columnName, boolean value) {
        getLookupableProperty(tableName, columnName).set(value);
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

    public ListProperty<Template> templatesProperty() {
        return templates;
    }

    @Override public String toString() {
        return connectionName.get();
    }

    public boolean isLookupable(String tableName, String columnName) {
        return lookupableColumns.containsKey(tableName) && lookupableColumns.get(tableName).containsKey(columnName) && lookupableColumns.get(tableName).get(columnName).getValue();
    }

    public StringProperty tableNamePatternProperty() {
        return tableNamePatternProperty;
    }

    public String getTableNamePattern() {
        return tableNamePatternProperty.get();
    }
}

