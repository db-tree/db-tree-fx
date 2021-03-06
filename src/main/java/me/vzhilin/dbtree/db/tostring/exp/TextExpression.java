package me.vzhilin.dbtree.db.tostring.exp;

import me.vzhilin.dbrow.db.Row;

public final class TextExpression implements Expression {
    private String text;

    public TextExpression(String text) {
        this.text = text;
    }

    @Override
    public ExpressionValue render(Row row) {
        return new ExpressionValue(text.substring(1, text.length() - 1));
    }
}
