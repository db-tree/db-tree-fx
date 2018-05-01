package com.vzhilin.dbview.tree;

import com.vzhilin.dbview.TreeTableNode;
import com.vzhilin.dbview.db.data.Row;

public final class LeafNode extends BasicTreeItem {
    public LeafNode(Row r, String column) {
        setValue(new TreeTableNode(column, String.valueOf(r.getField(column)), null));
    }
}
