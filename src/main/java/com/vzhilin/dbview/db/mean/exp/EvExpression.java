package com.vzhilin.dbview.db.mean.exp;

import com.vzhilin.dbview.db.data.Row;

public class EvExpression implements Expression {
    private final Expression child;

    public EvExpression(Expression child) {
        this.child = child;
    }

    @Override
    public ExpressionValue render(Row row) {
        return child.render(row);
    }
}
