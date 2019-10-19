package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.dbrow.db.Row;

public interface Expression {
    ExpressionValue render(Row row);
}
