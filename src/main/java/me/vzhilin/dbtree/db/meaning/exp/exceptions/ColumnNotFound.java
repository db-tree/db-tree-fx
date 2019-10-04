package me.vzhilin.dbtree.db.meaning.exp.exceptions;

import me.vzhilin.dbtree.db.schema.Table;

public final class ColumnNotFound extends ParseException {
    public ColumnNotFound(Table table, String columnName) {
        super(String.format("Column '%s' was not found in table '%s'", columnName, table));
    }
}
