package com.vzhilin.dbview;

import com.google.common.collect.Lists;
import com.vzhilin.dbview.autocomplete.AutoCompletion;
import com.vzhilin.dbview.autocomplete.AutocompletionCell;
import com.vzhilin.dbview.autocomplete.SuggestionProvider;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.schema.Schema;
import com.vzhilin.dbview.db.schema.Table;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class AutocompleteController {
    private static final double TITLE_HEIGHT = 28;
    @FXML
    private TextField autocompleteField;

    private Scene scene;
    private DbContext ctx;

    @FXML
    private void initialize() {

    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }


    public void setContext(DbContext ctx) {
        this.ctx = ctx;
        DbSuggestionProvider kcaProvider = new DbSuggestionProvider(/* */null);
        new AutoCompletion(kcaProvider).bind(autocompleteField);
    }

    public static class DbSuggestionProvider implements SuggestionProvider<AutocompletionCell> {
        private final Table root;
        private final Row row;

        public DbSuggestionProvider(Row row) {
            this.root = row.getTable();
            this.row = row;
        }

        @Override
        public List<AutocompletionCell> suggestions(String text) {
            Table current = root;
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

            final String finalText = text;
            Table finalCurrent = current;
            Row finalCurrentRow = currentRow;

            List<String> columns = getColumns(finalCurrent);
            return columns.stream().filter(t -> t.startsWith(finalText)).map(column -> {
                boolean isPk = finalCurrent.getPk().equals(column);
                boolean isFk = finalCurrent.getRelations().containsKey(column);
                String rightValue = isFk ? finalCurrent.getRelations().get(column).getName() : finalCurrentRow != null ? String.valueOf(finalCurrentRow.getField(column)) : null;
                return new AutocompletionCell(column, isPk, isFk, rightValue);
            }).collect(Collectors.toList());
        }

        private List<String> getColumns(Table finalCurrent) {
            List<String> result = Lists.newArrayList();
            result.add(finalCurrent.getPk());
            result.addAll(finalCurrent.getRelations().keySet());
            result.addAll(finalCurrent.getColumns().stream().
                filter(col -> !col.equals(finalCurrent.getPk()) && !finalCurrent.getRelations().containsKey(col)).collect(Collectors.toList()));
            return result;
        }
    }
}
