package me.vzhilin.dbtree.ui.conf;

import com.google.common.collect.Maps;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.util.ArrayList;
import java.util.Collections;
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
    private final Map<ColumnKey, BooleanProperty> lookupableColumns = Maps.newLinkedHashMap();
 // ->
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

        lookupableColumns.putAll(cs.lookupableColumns);
    }

    /**
     * schema --> Table --> column --> isEnabled
     * @return
     */
    public Map<ColumnKey, BooleanProperty> getLookupableColumns() {
        return Collections.unmodifiableMap(lookupableColumns);
    }

    public void setLookupableColumn(String schemaName, String tableName, String columnName, boolean value) {
        ColumnKey key = new ColumnKey(schemaName, tableName, columnName);
        lookupableColumns.computeIfAbsent(key, ck -> new SimpleBooleanProperty()).set(value);
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

    public boolean isLookupable(String schemaName, String tableName, String columnName) {
        ColumnKey key = new ColumnKey(schemaName, tableName, columnName);
        if (!lookupableColumns.containsKey(key)) {
            return false;
        }
        return lookupableColumns.get(key).getValue();
    }

    public StringProperty tableNamePatternProperty() {
        return tableNamePatternProperty;
    }

    public String getTableNamePattern() {
        return tableNamePatternProperty.get();
    }

    public void setLookupableColumns(Map<ColumnKey, BooleanProperty> newValues) {
        this.lookupableColumns.clear();
        this.lookupableColumns.putAll(newValues);
    }
}

