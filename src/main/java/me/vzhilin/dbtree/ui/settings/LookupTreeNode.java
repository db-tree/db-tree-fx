package me.vzhilin.dbtree.ui.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public final class LookupTreeNode {
    private final StringProperty tableOrColumnProperty = new SimpleStringProperty();
    private final StringProperty schemaProperty = new SimpleStringProperty();
    private final BooleanProperty includedProperty = new SimpleBooleanProperty();
    private final boolean isTable;

    public LookupTreeNode(String schemaName, String tableName, boolean isTable) {
        tableOrColumnProperty.set(tableName);
        schemaProperty.set(schemaName);
        this.isTable = isTable;
    }

    public ObservableValue<String> tableProperty() {
        return tableOrColumnProperty;
    }

    public StringProperty schemaProperty() {
        return schemaProperty;
    }

    public BooleanProperty includedProperty() {
        return includedProperty;
    }

    public boolean isTable() {
        return isTable;
    }
}
