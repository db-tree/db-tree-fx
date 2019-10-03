package com.vzhilin.dbview.ui.tree;

import com.vzhilin.dbview.ui.TreeTableNode;
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
