package me.vzhilin.dbtree.ui.tree;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;

public class CountNode extends BasicTreeItem {
    private final Table table;
    private final Long count;
    private final Iterable<Row> iter;
    private boolean loaded = false;

    public CountNode(Iterable<Row> iter, Table table, Long count) {
        this.iter = iter;
        this.table = table;
        this.count = count;
        setValue(new TreeTableNode(table.getName() + " (" + count + ")", "", null));
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public ObservableList<TreeItem<TreeTableNode>> getChildren() {
        if (!loaded) {
            loaded = true;
            new Paging().addNodes(iter.iterator(), super.getChildren());
        }
        return super.getChildren();
    }
}
