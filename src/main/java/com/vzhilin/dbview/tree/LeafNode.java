package com.vzhilin.dbview.tree;

import com.vzhilin.dbview.TreeTableNode;
import com.vzhilin.dbview.db.data.Row;

public final class LeafNode extends BasicTreeItem {
    private final String column;
    private final Row row;

    public LeafNode(Row r, String column) {
        this.column = column;
        this.row = r;
        setValue(new TreeTableNode(column, String.valueOf(r.getField(column)), null));
    }

    public String getColumn() {
        return column;
    }
}
