package me.vzhilin.dbtree.ui.tree;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import me.vzhilin.dbrow.db.Row;

public final class TreeTableNode {
    private StringProperty itemColumnProperty = new SimpleStringProperty();
    private StringProperty valueColumnProperty = new SimpleStringProperty();
    private StringProperty tableColumnProperty = new SimpleStringProperty();
    private SimpleObjectProperty<Row> meaningfulColumnProperty = new SimpleObjectProperty<>();

    public TreeTableNode(String column, String value, Row row) {
        itemColumnProperty.set(column);
        valueColumnProperty.set(value);
        tableColumnProperty.set(row == null ? "" : row.getTable().getName());
        meaningfulColumnProperty.set(row);
    }

    public StringProperty itemColumnProperty() {
        return itemColumnProperty;
    }

    public StringProperty valueColumnProperty() {
        return valueColumnProperty;
    }

    public ObservableValue<Row> meaningfulValueColumnProperty() {
        return meaningfulColumnProperty;
    }

    public StringProperty tableColumnProperty() {
        return tableColumnProperty;
    }
}
