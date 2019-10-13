package me.vzhilin.dbtree.ui.conf;

import java.util.Objects;

public final class ColumnKey {
    private final TableKey tableKey;
    private final String column;

    public ColumnKey(TableKey tableKey, String column) {
        this.tableKey = tableKey;
        this.column = column;
    }

    public TableKey getTableKey() {
        return tableKey;
    }

    public String getColumnName() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnKey columnKey = (ColumnKey) o;
        return tableKey.equals(columnKey.tableKey) &&
                column.equals(columnKey.column);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableKey, column);
    }
}
