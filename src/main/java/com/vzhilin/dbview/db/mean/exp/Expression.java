package com.vzhilin.dbview.db.mean.exp;

import com.vzhilin.dbview.db.data.Row;

public interface Expression {
    ExpressionValue render(Row row);
}
