package me.vzhilin.dbtree.db.tostring.exp.exceptions;

import me.vzhilin.dbrow.catalog.Table;

public final class ColumnNotFound extends ParseException {
    public ColumnNotFound(Table table, String columnName) {
        super(String.format("Column '%s' was not found in table '%s'", columnName, table));
    }
}
