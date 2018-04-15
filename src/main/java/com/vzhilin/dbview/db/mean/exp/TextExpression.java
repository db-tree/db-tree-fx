package com.vzhilin.dbview.db.mean.exp;

import com.vzhilin.dbview.db.data.Row;

public class TextExpression implements Expression {
    private String text;

    public TextExpression(String text) {
        this.text = text;
    }

    @Override
    public String render(Row row) {
        return text;
    }
}
