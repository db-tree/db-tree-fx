package me.vzhilin.dbview.ui.tree;

import me.vzhilin.dbview.db.data.Row;
import me.vzhilin.dbview.db.schema.Table;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.Iterator;
import java.util.Map;

public final class ToManyNode extends BasicTreeItem {
    private final Row row;
    private final Map.Entry<Table, String> relation;
    private boolean wasLoaded;

    public ToManyNode(Map.Entry<Table, String> tm, long count, Row row) {
        this.row = row;
        relation = tm;
        Table key = tm.getKey();
        setValue(new TreeTableNode(String.format("(+) %s::%s (%d)", key.getName(), tm.getValue(), count), "", null));
    }

    @Override public boolean isLeaf() {
        return false;
    }

    @Override public ObservableList<TreeItem<TreeTableNode>> getChildren() {
        if (!wasLoaded) {
            wasLoaded = true;

            Iterator<Row> it = row.getInverseReference(relation).iterator();
            new Paging().addNodes(it,  super.getChildren());
        }

        return super.getChildren();
    }

    public Map.Entry<Table, String> getRelation() {
        return relation;
    }

    public Row getRow() {
        return row;
    }
}
