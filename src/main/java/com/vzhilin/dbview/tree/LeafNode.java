package com.vzhilin.dbview.tree;

import com.vzhilin.dbview.TreeTableNode;
import com.vzhilin.dbview.db.data.Row;

import javafx.scene.control.TreeItem;

public final class LeafNode extends TreeItem<TreeTableNode> {
    public LeafNode(Row r, String column) {
        setValue(new TreeTableNode(column, String.valueOf(r.getField(column)), ""));
    }
}
