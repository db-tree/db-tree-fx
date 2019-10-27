package me.vzhilin.dbtree.ui.autocomplete.row;

import me.vzhilin.dbrow.catalog.*;
import me.vzhilin.dbrow.db.Row;
import me.vzhilin.dbtree.ui.ApplicationContext;
import me.vzhilin.dbtree.ui.autocomplete.AutocompletionCell;
import me.vzhilin.dbtree.ui.autocomplete.SuggestionProvider;
import me.vzhilin.dbtree.ui.tree.RenderingHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public final class RowSuggestionProvider implements SuggestionProvider<AutocompletionCell> {
    private final Row row;

    public RowSuggestionProvider(Row row) {
        this.row = row;
    }

    @Override
    public List<AutocompletionCell> suggestions(String line) {
        RowSuggestionContext suggContext = getRowSuggestionContext(line);
        Table table = suggContext.getTable();
        String text = suggContext.getText();

        Set<ForeignKey> usedForeignKeys = new HashSet<>();
        Set<Column> usedColumns = new HashSet<>();
        RenderingHelper renderingHelper = ApplicationContext.get().getRenderingHelper();
        List<AutocompletionCell> cells = new ArrayList<>();

//      FIXME
//        if (table.getPrimaryKey().isPresent()) {
//            for (PrimaryKeyColumn pkc: table.getPrimaryKey().get().getColumns()) {
//                Column column = pkc.getColumn();
//                boolean isFk = !column.getForeignKeys().isEmpty();
//                if (column.getName().startsWith(text) && usedColumns.add(column)) {
//                    cells.add(new AutocompletionCell(column.getName(), true, isFk, suggContext.getRow().get(column)));
//                }
//            }
//        }

        // composite keys
        for (ForeignKey fk: table.getForeignKeys()) {
            if (fk.size() >= 2 && fk.getFkName().startsWith(text) && usedForeignKeys.add(fk)) {
                String textMapping = renderingHelper.renderMapping(fk);
                cells.add(new AutocompletionCell(fk.getFkName() + " " + textMapping, false, true, suggContext.getRow().forwardReference(fk)));
            }
        }

        // one column in multiple foreign keys
        table.getColumns().forEach(new BiConsumer<String, Column>() {
            @Override
            public void accept(String name, Column column) {
                Set<ForeignKey> fks = column.getForeignKeys();
                if (fks.size() > 1) {
                    for (ForeignKey fk: fks) {
                        if (fk.getFkName().startsWith(text) && usedForeignKeys.add(fk)) {
                            cells.add(new AutocompletionCell(fk.getFkName(), false, true, suggContext.getRow().forwardReference(fk)));
                        }
                    }
                }
            }
        });

        // one column in one foreign key
        for (ForeignKey fk: table.getForeignKeys()) {
            if (fk.size() < 2) {
                continue;
            }

            fk.getColumnMapping().forEach(new BiConsumer<UniqueConstraintColumn, ForeignKeyColumn>() {
                @Override
                public void accept(UniqueConstraintColumn primaryKeyColumn, ForeignKeyColumn foreignKeyColumn) {
                    Column column = foreignKeyColumn.getColumn();
                    if (column.getName().startsWith(text)) {
                        cells.add(new AutocompletionCell(fk.getFkName(), false, true, suggContext.getRow().forwardReference(fk)));
                    }
                }
            });
        }

        // remaining columns
        table.getColumns().forEach(new BiConsumer<String, Column>() {
            @Override
            public void accept(String name, Column column) {
                boolean isFk = !column.getForeignKeys().isEmpty();
                boolean isPk = !column.getUniqueConstraints().isEmpty();
                if (!isPk && column.getName().startsWith(text) && usedColumns.add(column)) {
                    Object value = suggContext.getRow().get(column);
                    cells.add(new AutocompletionCell(name, false, isFk, String.valueOf(value)));
                }
            }
        });
        return cells;
    }

    private RowSuggestionContext getRowSuggestionContext(String text) {
        Table currentTable = row.getTable();
        Row currentRow = row;
        if (text.contains(".")) {
            String[] split = text.split("\\.");
            ForeignKey foreignKey = null;
            for (int i = 0; i < split.length; i++) {
//                Map<String, ForeignKey> rs = currentTable.getForeignKeys();
                String name = split[i];
                if (!name.isEmpty()) {
                    if (currentTable.hasForeignKey(name)) {
                        foreignKey = currentTable.getForeignKey(name);
                    } else
                    if (currentTable.hasColumn(name)) {
                        Column column = currentTable.getColumn(name);
                        if (column.getForeignKeys().size() == 1) {
                            foreignKey = column.getForeignKeys().iterator().next();
                        } else {
                            break;
                        }
                    } else {
                        break;
                    }

                    if (currentRow != null) {
                        currentTable = foreignKey.getUniqueConstraint().getTable();
                        currentRow = currentRow.forwardReference(foreignKey);
                    } else {
                        break;
                    }
                }
            }

            text = text.substring(text.lastIndexOf('.') + 1);
        }

        return new RowSuggestionContext(currentRow, currentTable, text);
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
