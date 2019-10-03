package com.vzhilin.dbview.ui.autocomplete.row;

import com.google.common.collect.Lists;
import com.vzhilin.dbview.ui.autocomplete.AutocompletionCell;
import com.vzhilin.dbview.ui.autocomplete.SuggestionProvider;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.schema.Table;

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
        List<String> columns = getColumns(table);
        return columns.stream().filter(t -> t.startsWith(suggContext.getText())).map(column -> {
            boolean isPk = table.getPk().equals(column);
            boolean isFk = table.getRelations().containsKey(column);
            Row row = suggContext.getRow();
            String rightValue = isFk ? table.getRelations().get(column).getName() : row != null ? String.valueOf(row.getField(column)) : null;
            return new AutocompletionCell(column, isPk, isFk, rightValue);
        }).collect(Collectors.toList());
    }

    private RowSuggestionContext getRowSuggestionContext(String text) {
        Table current = row.getTable();
        Row currentRow = row;
        if (text.contains(".")) {
            String[] split = text.split("\\.");
            for (int i = 0; i < split.length; i++) {
                Map<String, Table> rs = current.getRelations();
                String name = split[i];
                if (!rs.containsKey(name)) {
                    break;
                }

                current = rs.get(name);

                if (currentRow != null && currentRow.references().containsKey(name)) {
                    currentRow = currentRow.references().get(name);
                } else {
                    currentRow = null;
                }
            }

            text = text.substring(text.lastIndexOf('.') + 1);
        }

        return new RowSuggestionContext(currentRow, current, text);
    }

    private List<String> getColumns(Table finalCurrent) {
        List<String> result = Lists.newArrayList();
        result.add(finalCurrent.getPk());
        result.addAll(finalCurrent.getRelations().keySet());
        result.addAll(finalCurrent.getColumns().stream().
            filter(col -> !col.equals(finalCurrent.getPk()) && !finalCurrent.getRelations().containsKey(col)).collect(Collectors.toList()));
        return result;
    }

    private final static class RowSuggestionContext {
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
