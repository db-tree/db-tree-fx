package me.vzhilin.dbtree.ui.autocomplete;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.List;

public final class AutoCompletion {
    private static final double TITLE_HEIGHT = 28;
    private final ListView<AutocompletionCell> suggestionList = new ListView<>();
    private final Popup popup = new Popup();
    private final SuggestionProvider<AutocompletionCell> provider;
    private final TextField node;

    public AutoCompletion(SuggestionProvider<AutocompletionCell> suggestionProvider, TextField node) {
        this.node = node;
        this.provider = suggestionProvider;
        popup.getContent().add(suggestionList);
        suggestionList.setPrefWidth(500);
        suggestionList.setCellFactory(param -> new DefaultListCell());
        EventHandler<KeyEvent> handler = ke -> {
            TextField local = getNode();
            switch (ke.getCode()) {
                case ESCAPE:
                    suggestionList.getSelectionModel().clearSelection();
                    popup.hide();
                    break;
                case ENTER:
                    AutocompletionCell selectedItem = suggestionList.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        SuggestionHelper helper = new SuggestionHelper(local.getText(), local.getCaretPosition());
                        node.setText(helper.select(selectedItem.getColumn()));
                        node.positionCaret(local.getText().length());
                        popup.hide();
                    }
                    break;
                default:
                    break;
            }
        };
        suggestionList.setOnKeyPressed(handler);
        node.focusedProperty().addListener(focusedListener);
        node.textProperty().addListener(stringChangeListener);
    }

    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> {
        if (!newValue) {
            popup.hide();
        }
    };

    private final ChangeListener<String> stringChangeListener = (observable, oldValue, newValue) -> {
        TextField local = getNode();

        int caretPosition = local.getCaretPosition();
        if (length(oldValue) < length(newValue)) {
            ++caretPosition;
        }

        if (length(oldValue) > length(newValue)) {
            --caretPosition;
        }

        if (caretPosition < 0) {
            popup.hide();
            return;
        }

        caretPosition = Math.min(caretPosition, local.getText().length());

        SuggestionHelper helper = new SuggestionHelper(local.getText(), caretPosition);
        List<AutocompletionCell> list = getProvider().suggestions(helper.word());

        ObservableList<AutocompletionCell> items = suggestionList.getItems();
        items.clear();
        items.addAll(list);
        // select first row
        suggestionList.getSelectionModel().select(0);
        double value = 23 * list.size() + 5;
        if (popup.getHeight() != value) {
            popup.setHeight(value + 5);
            suggestionList.setPrefHeight(value);
        }

        if (list.isEmpty()) {
            popup.hide();
        } else if (!popup.isShowing()) {
            Scene scene = local.getScene();
            Window parent = scene.getWindow();
            popup.show(parent,
                    parent.getX() + local.localToScene(0, 0).getX() +
                            scene.getX(),
                    parent.getY() + local.localToScene(0, 0).getY() +
                            scene.getY() + TITLE_HEIGHT);
        }
    };

    private SuggestionProvider<AutocompletionCell> getProvider() {
        return provider;
    }

    private TextField getNode() {
        return node;
    }

    public void unbind() {
        node.focusedProperty().removeListener(focusedListener);
        node.textProperty().removeListener(stringChangeListener);
    }

    private int length(String string) {
        return string == null ? 0 : string.length();
    }

    private class DefaultListCell extends ListCell<AutocompletionCell> {
        @Override
        protected void updateItem(AutocompletionCell item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hBox = new HBox();
                hBox.setSpacing(5);
                ObservableList<Node> ch = hBox.getChildren();
                Region r2 = new Region();
                r2.setMaxWidth(26);
                r2.setMinWidth(26);
                if (item.isFk()) {
                    r2.getStyleClass().add("fk-region");
                } else
                if (item.isPk()) {
                    r2.getStyleClass().add("pk-region");
                }
                ch.add(r2);
                Label fieldLabel = new Label(item.getColumn());
                ch.add(fieldLabel);
                Region r = new Region();
                ch.add(r);

                Label valueLabel = new Label(String.valueOf(item.getValue()));
                ch.add(valueLabel);
                valueLabel.setMaxWidth(300);

                HBox.setHgrow(r, Priority.ALWAYS);
                setGraphic(hBox);
            }
        }
    }
}


