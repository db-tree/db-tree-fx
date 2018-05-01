package com.vzhilin.dbview.tree;

import com.vzhilin.dbview.TreeTableNode;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.schema.Table;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.Collection;
import java.util.Map;

public final class ToManyNode extends BasicTreeItem {
    private final Row row;
    private final Table key;
    private final Map.Entry<Table, String> relation;
    private boolean wasLoaded;

    public ToManyNode(Map.Entry<Table, String> tm, long count, Row row) {
        this.row = row;
        relation = tm;
        key = tm.getKey();
        setValue(new TreeTableNode(String.format("(+) %s::%s (%d)", key.getName(), tm.getValue(), count), "", null));
    }

    @Override public boolean isLeaf() {
        return false;
    }

    @Override public ObservableList<TreeItem<TreeTableNode>> getChildren() {
        if (!wasLoaded) {
            wasLoaded = true;

            ObservableList<TreeItem<TreeTableNode>> ch = super.getChildren();
            Collection<Row> rs = row.inverseReferences().get(relation);

            rs.forEach(r -> {
                Table tb = r.getTable();
                ch.add(new ToOneNode(r, new TreeTableNode(tb.getPk(), String.valueOf(r.getField(tb.getPk())), r)));
            });
        }

        return super.getChildren();
    }
}
