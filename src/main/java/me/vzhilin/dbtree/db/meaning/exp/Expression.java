package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.dbtree.db.data.Row;

public interface Expression {
    ExpressionValue render(Row row);
}
