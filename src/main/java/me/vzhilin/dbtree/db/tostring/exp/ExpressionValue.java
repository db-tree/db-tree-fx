package me.vzhilin.dbtree.db.tostring.exp;
import me.vzhilin.dbrow.db.Row;
import me.vzhilin.dbtree.ui.ApplicationContext;
import me.vzhilin.dbtree.ui.tree.RenderingHelper;

public final class ExpressionValue {
    private final Row row;
    private final Object value;
    private final boolean isRow;

    public ExpressionValue(Object value) {
        isRow  = value instanceof Row;
        this.row = isRow ? (Row) value : null;
        this.value = value;
    }

    public Row getRow() {
        return row;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        RenderingHelper renderingHelper = ApplicationContext.get().getRenderingHelper();
        if (isRow) {
            return renderingHelper.renderKey(row.getKey());
        } else {
            return renderingHelper.toString(value);
        }
    }
}
