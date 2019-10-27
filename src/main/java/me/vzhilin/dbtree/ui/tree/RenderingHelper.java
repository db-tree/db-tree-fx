package me.vzhilin.dbtree.ui.tree;

import com.google.common.base.Joiner;
import me.vzhilin.dbrow.catalog.Column;
import me.vzhilin.dbrow.catalog.ForeignKey;
import me.vzhilin.dbrow.catalog.ForeignKeyColumn;
import me.vzhilin.dbrow.catalog.UniqueConstraintColumn;
import me.vzhilin.dbrow.db.ObjectKey;
import me.vzhilin.dbrow.db.Row;
import me.vzhilin.dbtree.ui.util.ToStringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public final class RenderingHelper {
    private final ToStringConverter conv;

    public RenderingHelper(ToStringConverter conv) {
        this.conv = conv;
    }

    public ForeignKeyRow renderForeignKey(Row row, ForeignKey fk) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        fk.getColumnMapping().forEach(new BiConsumer<UniqueConstraintColumn, ForeignKeyColumn>() {
            @Override
            public void accept(UniqueConstraintColumn primaryKeyColumn, ForeignKeyColumn foreignKeyColumn) {
                Column column = foreignKeyColumn.getColumn();
                columns.add(column.getName());
                values.add(conv.toString(row.get(column)));
            }
        });

        Joiner j = Joiner.on(", ");
        String cols = j.join(columns);
        String vals = j.join(values);
        if (fk.size() > 1) {
            cols = "<" + cols + ">";
            vals = "<" + vals + ">";
        }
        return new ForeignKeyRow(cols, vals);
    }

    public String renderMapping(ForeignKey fk) {
        List<String> fkColumns = new ArrayList<>();
        List<String> pkColumns = new ArrayList<>();
        fk.getColumnMapping().forEach(new BiConsumer<UniqueConstraintColumn, ForeignKeyColumn>() {
            @Override
            public void accept(UniqueConstraintColumn primaryKeyColumn, ForeignKeyColumn foreignKeyColumn) {
                fkColumns.add(foreignKeyColumn.getColumn().getName());
                pkColumns.add(primaryKeyColumn.getColumn().getName());
            }
        });

        String fkString = "(" + Joiner.on(", ").join(fkColumns)+ ")";
        String pkString = Joiner.on(", ").join(pkColumns);
        return fkString + "->" + fk.getUniqueConstraint().getTable().getName() + "(" + pkString + ")";
    }

    public String renderKey(ObjectKey key) {
        List<String> vs = new ArrayList<>();
        key.forEach((pkc, value) -> vs.add(pkc.getName() + "=" + conv.toString(value)));
        return Joiner.on(", ").join(vs);
    }

    public String toString(Object value) {
        return conv.toString(value);
    }

    public static final class ForeignKeyRow {
        public final String cols;
        public final String vals;
        public ForeignKeyRow(String cols, String vals) {
            this.cols = cols;
            this.vals = vals;
        }
    }
}
