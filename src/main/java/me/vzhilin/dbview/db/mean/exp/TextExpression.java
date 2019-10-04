package me.vzhilin.dbview.db.mean.exp;

import me.vzhilin.dbview.db.data.Row;

public class TextExpression implements Expression {
    private String text;

    public TextExpression(String text) {
        this.text = text;
    }

    @Override
    public ExpressionValue render(Row row) {
        return new ExpressionValue(text.substring(1, text.length() - 1));
    }
}
