package me.vzhilin.dbtree.ui.autocomplete.table;

import com.google.common.collect.Lists;
import me.vzhilin.dbtree.db.schema.Table;
import me.vzhilin.dbtree.ui.autocomplete.AutocompletionCell;
import me.vzhilin.dbtree.ui.autocomplete.SuggestionProvider;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DbSuggestionProvider implements SuggestionProvider<AutocompletionCell> {
    private final Table root;

    public DbSuggestionProvider(Table table) {
        this.root = table;
    }

    @Override
    public List<AutocompletionCell> suggestions(String text) {
        DbSuggestionContext suggContext = getRowSuggestionContext(text);
        Table table = suggContext.getTable();
        List<String> columns = getColumns(table);
        return columns.stream().filter(t -> t.startsWith(suggContext.getText())).map(column -> {
            boolean isPk = table.getPk().equals(column);
            boolean isFk = table.getRelations().containsKey(column);
            String rightValue = isFk ? table.getRelations().get(column).getName() : "";
            return new AutocompletionCell(column, isPk, isFk, rightValue);
        }).collect(Collectors.toList());
    }

    private DbSuggestionContext getRowSuggestionContext(String text) {
        Table current = root;
        if (text.contains(".")) {
            String[] split = text.split("\\.");
            for (int i = 0; i < split.length; i++) {
                Map<String, Table> rs = current.getRelations();
                String name = split[i];
                if (!rs.containsKey(name)) {
                    break;
                }
                current = rs.get(name);
            }
            text = text.substring(text.lastIndexOf('.') + 1);
        }

        return new DbSuggestionContext(current, text);
    }

    private List<String> getColumns(Table finalCurrent) {
        List<String> result = Lists.newArrayList();
        result.add(finalCurrent.getPk());
        result.addAll(finalCurrent.getRelations().keySet());
        result.addAll(finalCurrent.getColumns().stream().
            filter(col -> !col.equals(finalCurrent.getPk()) && !finalCurrent.getRelations().containsKey(col)).collect(Collectors.toList()));
        return result;
    }

    private final static class DbSuggestionContext {
        private final Table table;
        private final String text;

        private DbSuggestionContext(Table table, String text) {
            this.table = table;
            this.text = text;
        }

        public Table getTable() {
            return table;
        }

        public String getText() {
            return text;
        }
    }
}
