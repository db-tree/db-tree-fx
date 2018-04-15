package com.vzhilin.dbview.db.mean.exp;

import com.vzhilin.dbview.db.data.Row;

final class ConcatExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public ConcatExpression(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String render(Row row) {
        return left.render(row) + right.render(row);
    }
}
