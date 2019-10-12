package me.vzhilin.dbtree.ui.tree;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import me.vzhilin.db.Row;

public final class TreeTableNode {
    private StringProperty itemColumnProperty = new SimpleStringProperty();
    private StringProperty valueColumnProperty = new SimpleStringProperty();
    private SimpleObjectProperty<Row> meaningfulColumnProperty = new SimpleObjectProperty<>();

    public TreeTableNode(String column, String value, Row row) {
        itemColumnProperty.set(column);
        valueColumnProperty.set(value);
        meaningfulColumnProperty.set(row);
    }

    public ObservableValue<String> itemColumnProperty() {
        return itemColumnProperty;
    }

    public ObservableValue<String> valueColumnProperty() {
        return valueColumnProperty;
    }

    public ObservableValue<Row> meaningfulValueColumnProperty() {
        return meaningfulColumnProperty;
    }
}
