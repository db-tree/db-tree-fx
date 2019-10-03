package com.vzhilin.dbview.ui.tree;

import com.vzhilin.dbview.db.data.Row;

public final class LeafNode extends BasicTreeItem {
    private final String column;

    public LeafNode(Row r, String column) {
        this.column = column;
        setValue(new TreeTableNode(column, String.valueOf(r.getField(column)), null));
    }

    public String getColumn() {
        return column;
    }
}
