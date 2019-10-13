package me.vzhilin.dbtree.db.meaning.exp;

import com.google.common.collect.Iterables;
import me.vzhilin.catalog.Column;
import me.vzhilin.catalog.ForeignKey;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;

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

            Map<ForeignKey, Row> refs = current.forwardReferences();
            Table table = current.getTable();
            ForeignKey fk = table.getForeignKeys().get(column);
            if (fk != null && refs.containsKey(fk)) {
                current = refs.get(fk);
                continue;
            } else
            if (table.hasColumn(column)) {
                Column c = table.getColumn(column);
                if (c.getForeignKeys().size() == 1) {
                    fk = Iterables.getOnlyElement(c.getForeignKeys());
                    if (fk.size() == 1) {
                        current = refs.get(fk);
                        continue;
                    }
                }
            }

//            if (refs.containsKey(column)) {
//                current = refs.get(column);
//            } else
            if (!iterator.hasNext()) {
                return new ExpressionValue(current.get(column));
            } else {
                return new ExpressionValue("");
            }
        }
        return new ExpressionValue(current);
    }
}
