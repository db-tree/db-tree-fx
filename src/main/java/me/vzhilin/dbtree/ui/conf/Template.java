package me.vzhilin.dbtree.ui.conf;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class Template {
    private final StringProperty tableName = new SimpleStringProperty();
    private final StringProperty template = new SimpleStringProperty();

    public Template(String table, String template) {
        tableName.set(table);
        this.template.set(template);
    }

    public String getTableName() {
        return tableName.get();
    }

    public String getTemplate() {
        return template.get();
    }

    public StringProperty templateProperty() {
        return template;
    }
}
