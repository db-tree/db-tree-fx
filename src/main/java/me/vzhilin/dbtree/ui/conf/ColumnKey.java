package me.vzhilin.dbtree.ui.conf;

import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

public final class ColumnKey {
    private final SimpleStringProperty schema;
    private final SimpleStringProperty table;
    private final SimpleStringProperty column;

    public ColumnKey(String schema, String table, String column) {
        this.schema = new SimpleStringProperty(schema);
        this.table = new SimpleStringProperty(table);
        this.column = new SimpleStringProperty(column);
    }

    public TableKey getTableKey() {
        return new TableKey(new SchemaKey(schema.get()), table.get());
    }

    public String getSchema() {
        return schema.get();
    }

    public String getTable() {
        return table.get();
    }

    public String getColumn() {
        return column.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnKey columnKey = (ColumnKey) o;
        return schema.get().equals(columnKey.schema.get()) &&
                table.get().equals(columnKey.table.get()) &&
                column.get().equals(columnKey.column.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema.get(), table.get(), column.get());
    }
}
