package me.vzhilin.dbview.db.mean.exp.exceptions;

import me.vzhilin.dbview.db.schema.Table;

public final class NotForeignKey extends ParseException {
    public NotForeignKey(Table table, String column) {
        super(String.format("column '%s' in table '%s' is not a foreign key", column, table.getName()));
    }
}
