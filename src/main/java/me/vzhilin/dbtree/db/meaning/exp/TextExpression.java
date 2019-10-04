package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.dbtree.db.data.Row;

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
