package com.vzhilin.dbview.tree;

import com.vzhilin.dbview.TreeTableNode;
import javafx.scene.control.TreeItem;

public class BasicTreeItem extends TreeItem<TreeTableNode> {
    public BasicTreeItem() {
        addEventHandler(branchExpandedEvent(), event -> {
            if (getChildren().size() == 1) {
                getChildren().get(0).setExpanded(true);
            }
        });
    }
}
