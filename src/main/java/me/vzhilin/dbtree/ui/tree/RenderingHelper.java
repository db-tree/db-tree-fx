package me.vzhilin.dbtree.ui.tree;

import com.google.common.base.Joiner;
import me.vzhilin.catalog.Column;
import me.vzhilin.catalog.ForeignKey;
import me.vzhilin.catalog.ForeignKeyColumn;
import me.vzhilin.catalog.PrimaryKeyColumn;
import me.vzhilin.db.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class RenderingHelper {
    public ForeignKeyRow renderForeignKey(Row row, ForeignKey fk) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        fk.getColumnMapping().forEach(new BiConsumer<PrimaryKeyColumn, ForeignKeyColumn>() {
            @Override
            public void accept(PrimaryKeyColumn primaryKeyColumn, ForeignKeyColumn foreignKeyColumn) {
                Column column = foreignKeyColumn.getColumn();
                columns.add(column.getName());
                values.add(String.valueOf(row.get(column)));
            }
        });

        Joiner j = Joiner.on(',');
        String cols = j.join(columns);
        String vals = j.join(values);
        if (fk.size() > 1) {
            cols = "<" + cols + ">";
            vals = "<" + vals + ">";
        }
        return new ForeignKeyRow(cols, vals);
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
