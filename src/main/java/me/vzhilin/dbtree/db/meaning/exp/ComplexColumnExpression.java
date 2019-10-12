package me.vzhilin.dbtree.db.meaning.exp;

import me.vzhilin.db.Row;

import java.util.List;

public class ComplexColumnExpression implements Expression {
    private final List<String> columns;

    public ComplexColumnExpression(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public ExpressionValue render(Row row) {
        Row current = row;
//        for (Iterator<String> iterator = columns.iterator(); iterator.hasNext(); ) {
//            String column = iterator.next();
//            if (current == null) {
//                return null;
//            }
//
//            Map<ForeignKey, Row> refs = current.forwardReferences();
//            if (refs.containsKey(column)) {
//                current = refs.get(column);
//            } else if (!iterator.hasNext()) {
//                return new ExpressionValue(current.getField(column));
//            } else {
//                return new ExpressionValue("");
//            }
//        }
//        return new ExpressionValue(current);
        return new ExpressionValue("");
    }
}
