package me.vzhilin.dbview.db.mean.exp.exceptions;

import me.vzhilin.dbview.db.schema.Table;

public class ColumnNotFound extends ParseException {
    public ColumnNotFound(Table table, String columnName) {
        super(String.format("Column '%s' was not found in table '%s'", columnName, table));
    }
}
