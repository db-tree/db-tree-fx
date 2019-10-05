package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.dbtree.db.Row;

public class ColumnExpression implements Expression {
    private final String column;

    public ColumnExpression(String column) {
        this.column = column;
    }

    @Override
    public ExpressionValue render(Row row) {
        if (row.getTable().getRelations().containsKey(column)) {
            return new ExpressionValue(row.references().get(column));
        } else {
            return new ExpressionValue(row.getField(column));
        }
    }
}
