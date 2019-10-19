package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.dbrow.catalog.Column;
import me.vzhilin.dbrow.db.Row;

public final class ColumnExpression implements Expression {
    private final Column column;

    public ColumnExpression(Column column) {
        this.column = column;
    }

    @Override
    public ExpressionValue render(Row row) {
//        row.get
//        if (row.getTable().getRelations().containsKey(column)) {
//            return new ExpressionValue(row.references().get(column));
//        } else {
//            return new ExpressionValue(row.getField(column));
//        }

        return new ExpressionValue(row.get(column));
    }
}
