package me.vzhilin.dbtree.db.tostring.exp;

import me.vzhilin.dbrow.catalog.ForeignKey;
import me.vzhilin.dbrow.db.Row;
import me.vzhilin.dbtree.ui.ApplicationContext;
import me.vzhilin.dbtree.ui.tree.RenderingHelper;

public class ForeignKeyExpression implements Expression {
    private final ForeignKey foreignKey;

    public ForeignKeyExpression(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }

    @Override
    public ExpressionValue render(Row row) {
        RenderingHelper renderingHelper = ApplicationContext.get().getRenderingHelper();
        Row ref = row.forwardReference(foreignKey);
        return new ExpressionValue(ref == null ? "" : renderingHelper.renderKey(ref.getKey()));
    }
}
