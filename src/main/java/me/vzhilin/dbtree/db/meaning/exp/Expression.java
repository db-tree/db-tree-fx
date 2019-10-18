package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.db.Row;

public interface Expression {
    ExpressionValue render(Row row);
}
