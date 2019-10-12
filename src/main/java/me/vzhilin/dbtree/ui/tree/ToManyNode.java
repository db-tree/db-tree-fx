package me.vzhilin.dbtree.ui.tree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import me.vzhilin.catalog.ForeignKey;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;

import java.util.Iterator;

public final class ToManyNode extends BasicTreeItem {
    private final Row row;
    private final ForeignKey relation;
    private boolean wasLoaded;

    public ToManyNode(ForeignKey fk, long count, Row row) {
        this.row = row;
        relation = fk;
        Table tb = fk.getTable();
        setValue(new TreeTableNode(String.format("(+) %s::%s (%d)", tb.getName(),  fk.getFkAsString(), count), "", null));
    }

    @Override public boolean isLeaf() {
        return false;
    }

    @Override public ObservableList<TreeItem<TreeTableNode>> getChildren() {
        if (!wasLoaded) {
            wasLoaded = true;

            Iterator<Row> it = row.backwardReference(relation).iterator();
            new Paging().addNodes(it,  super.getChildren());
        }

        return super.getChildren();
    }

    public Row getRow() {
        return row;
    }
}
