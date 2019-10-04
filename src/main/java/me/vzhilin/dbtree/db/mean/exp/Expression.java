package me.vzhilin.dbtree.db.mean.exp;

import me.vzhilin.dbtree.db.data.Row;

public interface Expression {
    ExpressionValue render(Row row);
}
