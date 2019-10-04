package me.vzhilin.dbtree.ui.tree;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import me.vzhilin.dbtree.db.data.Row;
import me.vzhilin.dbtree.db.schema.Table;

import java.util.Iterator;

public final class Paging {
    private final static int PAGE_SIZE = 200;

    public void addNodes(Iterator<Row> it, ObservableList<TreeItem<TreeTableNode>> ch) {
        add(it, ch);

        if (it.hasNext()) {
            PagingItem newNode = new PagingItem(ch, it);
            newNode.setValue(new TreeTableNode("","", null));
            ch.add(newNode);
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

    public final class PagingItem extends TreeItem<TreeTableNode> implements EventHandler<ActionEvent> {
        private final ObservableList<TreeItem<TreeTableNode>> ch;
        private final Iterator<Row> it;

        private PagingItem(ObservableList<TreeItem<TreeTableNode>> ch, Iterator<Row> it) {
            this.ch = ch;
            this.it = it;
        }

        @Override
        public void handle(ActionEvent event) {
            ch.remove(this);
            add(it, ch);
            if (it.hasNext()) {
                ch.add(this);
            }
        }
    }
}
