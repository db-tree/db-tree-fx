package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.dbrow.db.Row;

public final class ParsedTemplate {
    private final boolean valid;
    private final Expression expression;
    private final String errorMessage;

    public ParsedTemplate(Expression expression) {
        this.expression = expression;
        this.errorMessage = "";
        this.valid = true;
    }

    public ParsedTemplate(String errorMessage) {
        this.errorMessage = errorMessage;
        this.expression = null;
        this.valid = false;
    }

    public ExpressionValue render(Row row) {
        if (expression != null) {
            return expression.render(row);
        } else {
            return new ExpressionValue(errorMessage);
        }
    }

    public String getError() {
        return errorMessage;
    }

    public boolean isValid() {
        return valid;
    }
}
