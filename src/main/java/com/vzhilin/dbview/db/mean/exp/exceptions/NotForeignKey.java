package com.vzhilin.dbview.db.mean.exp.exceptions;

import com.vzhilin.dbview.db.schema.Table;

public class NotForeignKey extends ParseException {

    public NotForeignKey(Table table, String column) {
        super(String.format("column + '%s' in table '%s' is not a foreign key", column, table.getName()));
    }
}
