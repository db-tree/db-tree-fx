package com.vzhilin.dbview.tree;

import com.vzhilin.dbview.TreeTableNode;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.schema.Table;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class ToOneNode extends TreeItem<TreeTableNode> {
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

            for (String column : tb.getColumns()) {
                Object value = row.getField(column);

                if (value != null) {
                    if (tb.getRelations().containsKey(column)) {
                        Row refRow = this.row.references().get(column);
                        Table refTable = refRow.getTable();
                        complexNodes.add(new ToOneNode(refRow, new TreeTableNode(
                                column, String.valueOf(refRow.getField(refTable.getPk())), refRow.meaningfulValue())));
                    } else {
                        simpleNodes.add(new LeafNode(row, column));
                    }
                }
            }

            Map<Map.Entry<Table, String>, Long> irc = row.inverseReferencesCount();
            for (Map.Entry<Table, String> tm : row.getTable().getBackRelations().entries()) {
                long count = irc.get(tm);

                if (count > 0) {
                    TreeItem<TreeTableNode> toManyNode = new ToManyNode(tm, count, row);
                    relationNodes.add(toManyNode);
                }
            }

            ObservableList<TreeItem<TreeTableNode>> ch = super.getChildren();
            ch.addAll(complexNodes);
            ch.addAll(relationNodes);
            ch.addAll(simpleNodes);
        }

        return super.getChildren();
    }
}
