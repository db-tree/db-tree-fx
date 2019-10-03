package me.vzhilin.dbtree.db.meaning.exp;


import me.vzhilin.dbtree.db.Row;

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
        if (isRow) {
            return String.valueOf(row.getPk());
        } else {
            return String.valueOf(value);
        }
    }
}
