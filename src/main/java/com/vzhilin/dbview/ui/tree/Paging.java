package com.vzhilin.dbview.ui.tree;

import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.schema.Table;
import com.vzhilin.dbview.ui.tree.LoadMoreNode;
import com.vzhilin.dbview.ui.tree.ToOneNode;
import com.vzhilin.dbview.ui.tree.TreeTableNode;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

public class Paging {
    private final int PAGE_SIZE = 1000;
    public void addNodes(Iterator<Row> it, ObservableList<TreeItem<TreeTableNode>> ch) {
        add(it, ch);

        if (it.hasNext()) {
            AtomicReference<LoadMoreNode> loadMore = new AtomicReference<>();

            LoadMoreNode newNode = new LoadMoreNode(event -> {
                ch.remove(loadMore.get());
                add(it, ch);

                if (it.hasNext()) {
                    ch.add(loadMore.get());
                }
            });
            loadMore.set(newNode);
            newNode.setValue(new TreeTableNode("","", null));
            ch.add(loadMore.get());
        }
    }

    private void add(Iterator<Row> it, ObservableList<TreeItem<TreeTableNode>> ch) {
        for (int i = 0; i < PAGE_SIZE && it.hasNext(); ++i) {
            Row r = it.next();
            Table table = r.getTable();
            TreeTableNode newNode = new TreeTableNode(table.getPk(), String.valueOf(r.getField(table.getPk())), r);
            ch.add(new ToOneNode(r, newNode));
        }
    }
}
