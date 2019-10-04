package me.vzhilin.dbview.db.mean.exp;

import me.vzhilin.dbview.db.data.Row;

import java.util.List;

public final class ConcatExpression implements Expression {
    private final List<Expression> expressions;

    public ConcatExpression(List<Expression> expressionList) {
        this.expressions = expressionList;
    }

    @Override
    public ExpressionValue render(Row row) {
        StringBuilder sb = new StringBuilder();
        for (Expression exp: expressions) {
            sb.append(String.valueOf(exp.render(row)));
        }

        return new ExpressionValue(sb.toString());
    }
}
