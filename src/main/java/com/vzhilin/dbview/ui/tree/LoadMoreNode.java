package com.vzhilin.dbview.ui.tree;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;

public class LoadMoreNode extends TreeItem<TreeTableNode> {
    private final EventHandler<ActionEvent> clickAction;

    public LoadMoreNode(EventHandler<ActionEvent> clickAction) {
        this.clickAction = clickAction;
    }

    public EventHandler<ActionEvent> onClickAction() {
        return clickAction;
    }
}
