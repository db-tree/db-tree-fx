package me.vzhilin.dbtree.ui.conf;

import com.google.common.collect.Maps;
import javafx.beans.property.*;
import javafx.collections.FXCollections;

import java.util.*;

/**
 * Настройки подключений
 */
public final class ConnectionSettings {
    /** Имя подключения */
    private final StringProperty connectionName = new SimpleStringProperty();

    /** Драйвер */
    private final StringProperty driverClass = new SimpleStringProperty();

    /** Адрес подключения */
    private final StringProperty host = new SimpleStringProperty();

    /** connection port */
    private final StringProperty port = new SimpleStringProperty();

    /** database name */
    private final StringProperty database = new SimpleStringProperty();

    /** Имя пользователя */
    private final StringProperty username = new SimpleStringProperty();

    /** Пароль */
    private final transient StringProperty password = new SimpleStringProperty();

    /** regexp для имен таблиц */
    private final StringProperty tableNamePatternProperty = new SimpleStringProperty();

    /** Осмысленные значения */
    private final ListProperty<Template> templates = new SimpleListProperty<Template>(FXCollections.observableArrayList(new ArrayList<>()));

    /** Колонки для поиска */
    private final Map<ColumnKey, BooleanProperty> lookupableColumns = Maps.newLinkedHashMap();

    private final Set<String> schemas = new HashSet<>();
 // ->
    public ConnectionSettings() {
    }

    public ConnectionSettings(ConnectionSettings cs) {
        connectionName.set(cs.getConnectionName());
        driverClass.set(cs.getDriverClass());
        host.set(cs.getHost());
        port.set(cs.getPort());
        database.set(cs.getDatabase());
        username.set(cs.getUsername());
        password.set(cs.getPassword());
        tableNamePatternProperty.set(cs.getTableNamePattern());
        schemas.addAll(cs.schemas);

        for (Template t: cs.templates) {
            templates.add(new Template(t.getSchemaName(), t.getTableName(), t.getTemplate()));
        }

        lookupableColumns.putAll(cs.lookupableColumns);
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public String getPort() {
        return port.get();
    }

    public StringProperty portProperty() {
        return port;
    }

    public String getDatabase() {
        return database.get();
    }

    public StringProperty databaseProperty() {
        return database;
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

    public Set<String> getSchemas() {
        return Collections.unmodifiableSet(schemas);
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

