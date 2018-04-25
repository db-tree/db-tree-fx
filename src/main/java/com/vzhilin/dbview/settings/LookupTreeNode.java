package com.vzhilin.dbview.settings;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class LookupTreeNode {
    private final StringProperty tableOrColumnProperty = new SimpleStringProperty();
    private final BooleanProperty includedProperty = new SimpleBooleanProperty();

    public LookupTreeNode(String tableName) {
        tableOrColumnProperty.set(tableName);
    }

    public ObservableValue<String> tableProperty() {
        return tableOrColumnProperty;
    }

    public BooleanProperty includedProperty() {
        return includedProperty;
    }
}
