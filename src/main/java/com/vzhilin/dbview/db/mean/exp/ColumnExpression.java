package com.vzhilin.dbview.db.mean.exp;

import com.vzhilin.dbview.db.data.Row;

public class ColumnExpression implements Expression {
    private final String column;

    public ColumnExpression(String column) {
        this.column = column;
    }

    @Override
    public String render(Row row) {
        return String.valueOf(row.getField(column));
    }
}
