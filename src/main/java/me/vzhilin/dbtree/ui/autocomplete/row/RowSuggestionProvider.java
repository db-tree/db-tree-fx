package me.vzhilin.dbtree.ui.autocomplete.row;

import com.google.common.collect.Iterables;
import me.vzhilin.catalog.Column;
import me.vzhilin.catalog.ForeignKey;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;
import me.vzhilin.dbtree.ui.autocomplete.AutocompletionCell;
import me.vzhilin.dbtree.ui.autocomplete.SuggestionProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class RowSuggestionProvider implements SuggestionProvider<AutocompletionCell> {
    private final Row row;

    public RowSuggestionProvider(Row row) {
        this.row = row;
    }

    @Override
    public List<AutocompletionCell> suggestions(String text) {
        RowSuggestionContext suggContext = getRowSuggestionContext(text);
        Table table = suggContext.getTable();
        Collection<Column> columns = table.getColumns().values();
        return columns.stream().filter(t -> t.getName().startsWith(suggContext.getText())).map(column -> {
            boolean isPk = column.getPrimaryKey().isPresent();
            boolean isFk = !column.getForeignKeys().isEmpty();
            Row row = suggContext.getRow();
            String rightValue;
            if (isFk) {
                rightValue = String.valueOf(row.forwardReferences().get(Iterables.getOnlyElement(column.getForeignKeys()))); // FIXME
            } else if (row != null) {
                rightValue = String.valueOf(row.get(column));
            } else {
                rightValue = "";
            }

            return new AutocompletionCell(column.getName(), isPk, isFk, rightValue);
        }).collect(Collectors.toList());
    }

    private RowSuggestionContext getRowSuggestionContext(String text) {
        Table current = row.getTable();
        Row currentRow = row;
        if (text.contains(".")) {
            String[] split = text.split("\\.");
            ForeignKey next = null;
            for (int i = 0; i < split.length; i++) {
                Map<String, ForeignKey> rs = current.getForeignKeys();
                String name = split[i];
                if (rs.containsKey(name)) {
                    current = rs.get(name).getPkTable();
                } else
                if (current.hasColumn(name)) {
                    Column column = current.getColumn(name);
                    if (column.getForeignKeys().size() == 1) {
                        next = column.getForeignKeys().iterator().next();
                        current = next.getPkTable();
                    } else {
                        break;
                    }
                } else {
                    break;
                }

                if (currentRow != null && next != null) {
                    currentRow = currentRow.forwardReferences().get(next);
                } else {
                    next = null;
                    currentRow = null;
                }
            }

            text = text.substring(text.lastIndexOf('.') + 1);
        }

        return new RowSuggestionContext(currentRow, current, text);
    }
    private final class RowSuggestionContext {
        private final Row row;
        private final Table table;
        private final String text;

        private RowSuggestionContext(Row row, Table table, String text) {
            this.row = row;
            this.table = table;
            this.text = text;
        }

        public Row getRow() {
            return row;
        }

        public Table getTable() {
            return table;
        }

        public String getText() {
            return text;
        }
    }
}
