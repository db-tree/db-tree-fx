package com.vzhilin.dbview.db.mean.exp;

import com.google.common.collect.Iterables;
import com.vzhilin.dbview.db.data.Row;

import java.util.List;

public class ComplexColumnExpression implements Expression {
    private final List<String> columns;

    public ComplexColumnExpression(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public String render(Row row) {
        Row current = row;

        for (String column: columns) {
            current = current.references().get(column);
            if (current == null) {
                return null;
            }
        }

        return new ColumnExpression(Iterables.getLast(columns)).render(current);
    }
}
