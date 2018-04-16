package com.vzhilin.dbview.db.mean.exp;

import com.vzhilin.dbview.db.data.Row;

public class ColumnExpression implements Expression {
    private final String column;

    public ColumnExpression(String column) {
        this.column = column;
    }

    @Override
    public ExpressionValue render(Row row) {
        if (row.getTable().getRelations().containsKey(column)) {
            return new ExpressionValue((Row) row.references().get(column));
        } else {
            return new ExpressionValue(row.getField(column));
        }
    }
}
