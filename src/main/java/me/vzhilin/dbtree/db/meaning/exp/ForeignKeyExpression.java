package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.catalog.ForeignKey;
import me.vzhilin.db.Row;
import me.vzhilin.dbtree.ui.tree.RenderingHelper;

import java.util.ArrayList;
import java.util.List;

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
