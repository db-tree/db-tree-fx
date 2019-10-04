package me.vzhilin.dbview.ui.tree;

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
