package me.vzhilin.dbview.db.mean.exp;

import me.vzhilin.dbview.db.data.Row;

public interface Expression {
    ExpressionValue render(Row row);
}
