package me.vzhilin.dbtree.ui.tree;

import me.vzhilin.dbrow.db.Row;

public final class LeafNode extends BasicTreeItem {
    private final String column;

    public LeafNode(Row r, String column, String textValue) {
        this.column = column;

        setValue(new TreeTableNode(column, textValue, null));
    }

    public String getColumn() {
        return column;
    }
}
