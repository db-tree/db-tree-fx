package me.vzhilin.dbtree.ui.tree;

import com.google.common.base.Joiner;
import me.vzhilin.dbrow.catalog.UniqueConstraint;
import me.vzhilin.dbrow.db.Row;

import java.util.Set;

public final class UniqueConstratintNode extends BasicTreeItem {
    private final Row row;
    private final UniqueConstraint uc;

    public UniqueConstratintNode(Row row, UniqueConstraint uc) {
        this.row = row;
        this.uc = uc;

        TreeTableNode ttn = buildNode(uc);
        setValue(ttn);
    }

    private TreeTableNode buildNode(UniqueConstraint uc) {
        TreeTableNode ttn = new TreeTableNode("", "", null);
        ttn.tableColumnProperty().set(uc.getTable().getName());
        Set<String> columns = uc.getColumnNames();
        String joinedColumns = Joiner.on(", ").join(columns);

        if (columns.size() > 1) {
            joinedColumns = "(" + joinedColumns + ")";
        }
        ttn.itemColumnProperty().set(joinedColumns);
        return ttn;
    }
}
