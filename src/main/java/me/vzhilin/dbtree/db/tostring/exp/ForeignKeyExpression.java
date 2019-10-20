package me.vzhilin.dbtree.db.tostring.exp;

import me.vzhilin.dbrow.catalog.ForeignKey;
import me.vzhilin.dbrow.db.Row;
import me.vzhilin.dbtree.ui.tree.RenderingHelper;

public class ForeignKeyExpression implements Expression {
    private final ForeignKey foreignKey;

    public ForeignKeyExpression(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }

    @Override
    public ExpressionValue render(Row row) {
        Row ref = row.forwardReference(foreignKey);
        return new ExpressionValue(ref == null ? "" : new RenderingHelper().renderKey(ref.getKey()));
    }
}
