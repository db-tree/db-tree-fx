package me.vzhilin.dbtree.ui.tree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import me.vzhilin.dbrow.catalog.Column;
import me.vzhilin.dbrow.catalog.ForeignKey;
import me.vzhilin.dbrow.catalog.Table;
import me.vzhilin.dbrow.db.Row;

import java.util.LinkedList;
import java.util.List;
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
                    RenderingHelper.ForeignKeyRow e = new RenderingHelper().renderForeignKey(row, foreignKey);
                    TreeTableNode refNode = new TreeTableNode(e.cols, e.vals, ref);
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

    public Row getRow() {
        return row;
    }
}
