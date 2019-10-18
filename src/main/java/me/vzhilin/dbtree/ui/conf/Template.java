package me.vzhilin.dbtree.ui.conf;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class Template {
    private final StringProperty schemaName = new SimpleStringProperty();
    private final StringProperty tableName = new SimpleStringProperty();
    private final StringProperty template = new SimpleStringProperty();

    public Template(String schema, String table, String template) {
        schemaName.set(schema);
        tableName.set(table);
        this.template.set(template);
    }

    public TableKey getTableKey() {
        return new TableKey(new SchemaKey(schemaName.getValue()), tableName.getValue());
    }

    public String getSchemaName() {
        return schemaName.get();
    }

    public StringProperty schemaNameProperty() {
        return schemaName;
    }

    public String getTableName() {
        return tableName.get();
    }

    public StringProperty tableNameProperty() {
        return tableName;
    }

    public String getTemplate() {
        return template.get();
    }

    public StringProperty templateProperty() {
        return template;
    }
}
