package com.vzhilin.dbview.db.mean.exp;

import com.vzhilin.dbview.db.data.Row;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ComplexColumnExpression implements Expression {
    private final List<String> columns;

    public ComplexColumnExpression(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public ExpressionValue render(Row row) {
        Row current = row;

        for (Iterator<String> iterator = columns.iterator(); iterator.hasNext(); ) {

            String column = iterator.next();
            if (current == null) {
                return null;
            }

            Map<String, Row> refs = current.references();

            if (refs.containsKey(column)) {
                current = refs.get(column);
            } else if (!iterator.hasNext()) {
                return new ExpressionValue(current.getField(column));
            } else {
                throw new RuntimeException("expected: last value");
            }
        }

        return new ExpressionValue(current);
    }
}
