package me.vzhilin.dbtree.ui.tree;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import me.vzhilin.catalog.*;
import me.vzhilin.db.Row;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ToOneNode extends BasicTreeItem {
    private boolean loaded = false;

    private final TreeTableNode node;
    private final Row row;

    public ToOneNode(Row row, TreeTableNode treeTableNode) {
        this.row = row;
        this.node = treeTableNode;

        setValue(node);
    }

    @Override public boolean isLeaf() {
        return false;
    }

    @Override public ObservableList<TreeItem<TreeTableNode>> getChildren() {
        if (!loaded) {
            loaded = true;

            Table tb = row.getTable();

            List<TreeItem<TreeTableNode>> complexNodes  = new LinkedList<>();
            List<TreeItem<TreeTableNode>> simpleNodes   = new LinkedList<>();
            List<TreeItem<TreeTableNode>> relationNodes = new LinkedList<>();

            // foreign keys
            row.forwardReferences().forEach(new BiConsumer<ForeignKey, Row>() {
                @Override
                public void accept(ForeignKey foreignKey, Row ref) {
                    Map.Entry<String, String> e = asStringColumn(row, foreignKey);
                    TreeTableNode refNode = new TreeTableNode(e.getKey(), e.getValue(), ref);
                    complexNodes.add(new ToOneNode(ref, refNode));
                }
            });

            // all fields
            tb.getColumns().forEach(new BiConsumer<String, Column>() {
                @Override
                public void accept(String columnName, Column column) {
                    simpleNodes.add(new LeafNode(row, columnName));
                }
            });

            row.backwardReferencesCount().forEach(new BiConsumer<ForeignKey, Number>() {
                @Override
                public void accept(ForeignKey foreignKey, Number number) {
                    long count = number.longValue();
                    if (count > 0) {
                        TreeItem<TreeTableNode> toManyNode = new ToManyNode(foreignKey, count, row);
                        relationNodes.add(toManyNode);
                    }
                }
            });

            ObservableList<TreeItem<TreeTableNode>> ch = super.getChildren();
            ch.addAll(complexNodes);
            ch.addAll(relationNodes);
            ch.addAll(simpleNodes);
        }

        return super.getChildren();
    }

    private Map.Entry<String, String> asStringColumn(Row row, ForeignKey foreignKey) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        foreignKey.getColumnMapping().forEach(new BiConsumer<PrimaryKeyColumn, ForeignKeyColumn>() {
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
        return Maps.immutableEntry(cols, vals);
    }

    public Row getRow() {
        return row;
    }
}
