package me.vzhilin.dbtree.db.tostring.exp;

import me.vzhilin.dbrow.db.Row;

public interface Expression {
    ExpressionValue render(Row row);
}
