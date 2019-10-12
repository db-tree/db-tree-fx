package me.vzhilin.dbtree.ui.tree;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import me.vzhilin.catalog.PrimaryKey;
import me.vzhilin.catalog.PrimaryKeyColumn;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
            PrimaryKey pk = table.getPrimaryKey().get();
            Map.Entry<String, String> e = toString(r, pk);
            TreeTableNode newNode = new TreeTableNode(e.getKey(), e.getValue(), r);
            ch.add(new ToOneNode(r, newNode));
        }
    }

    private Map.Entry<String, String> toString(Row r, PrimaryKey pk) {
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();
        pk.getColumns().forEach(new Consumer<PrimaryKeyColumn>() {
            @Override
            public void accept(PrimaryKeyColumn primaryKeyColumn) {
                values.add(String.valueOf(r.get(primaryKeyColumn.getColumn())));
                columns.add(primaryKeyColumn.getName());
            }
        });
        String vsText = Joiner.on(',').join(values);
        String csText = Joiner.on(',').join(columns);
        return Maps.immutableEntry(csText, vsText);
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