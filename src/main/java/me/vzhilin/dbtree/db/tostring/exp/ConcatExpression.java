package me.vzhilin.dbtree.db.tostring.exp;

import me.vzhilin.dbrow.db.Row;

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
            sb.append(exp.render(row));
        }

        return new ExpressionValue(sb.toString());
    }
}
