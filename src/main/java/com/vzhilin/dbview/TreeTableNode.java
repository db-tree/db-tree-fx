package com.vzhilin.dbview;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class TreeTableNode {
    private StringProperty itemColumnProperty = new SimpleStringProperty();
    private StringProperty valueColumnProperty = new SimpleStringProperty();
    private StringProperty meaningfulColumnProperty = new SimpleStringProperty();

    public TreeTableNode(String column, String value, String meaningfulValue) {
        itemColumnProperty.set(column);
        valueColumnProperty.set(value);
        meaningfulColumnProperty.set(meaningfulValue);
    }

    public ObservableValue<String> itemColumnProperty() {
        return itemColumnProperty;
    }

    public ObservableValue<String> valueColumnProperty() {
        return valueColumnProperty;
    }

    public ObservableValue<String> meaningfulValueColumnProperty() {
        return meaningfulColumnProperty;
    }
}
