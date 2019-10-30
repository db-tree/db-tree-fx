package me.vzhilin.dbtree.ui.tree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import me.vzhilin.dbrow.catalog.ForeignKey;
import me.vzhilin.dbrow.catalog.Table;
import me.vzhilin.dbrow.db.Row;

import java.util.Iterator;

public final class ToManyNode extends BasicTreeItem {
    private final Row row;
    private final ForeignKey relation;
    private boolean wasLoaded;

    public ToManyNode(ForeignKey fk, long count, Row row) {
        this.row = row;
        relation = fk;
        Table tb = fk.getTable();
        TreeTableNode ttn = new TreeTableNode(String.format("%s (%d)", fk.getFkAsString(), count), "", null);
        ttn.tableColumnProperty().set(tb.getName());
        setValue(ttn);
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
