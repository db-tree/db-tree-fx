package com.vzhilin.dbview.autocomplete;

import com.google.common.collect.Maps;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class AutoCompletion {
    private static final double TITLE_HEIGHT = 28;
    private final SuggestionProvider<AutocompletionCell> provider;

    public AutoCompletion(SuggestionProvider<AutocompletionCell> suggestionProvider) {
        this.provider = suggestionProvider;
    }

    public void bind(TextField node) {
        Popup popup = new Popup();

        ListView<AutocompletionCell> suggestionList = new ListView<>();
        popup.getContent().add(suggestionList);
        suggestionList.setCellFactory(new Callback<ListView<AutocompletionCell>, ListCell<AutocompletionCell>>() {
            @Override
            public ListCell<AutocompletionCell> call(ListView<AutocompletionCell> param) {
                ListCell<AutocompletionCell> cell = new DefaultListCell<AutocompletionCell>();
                return cell;
            }
        });

        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                popup.hide();
            }
        });

        suggestionList.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case ENTER:
                    AutocompletionCell selectedItem = suggestionList.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        SuggestionHelper helper = new SuggestionHelper(node.getText(), node.getCaretPosition());

                        node.setText(helper.select(selectedItem.getColumn()));
                        node.positionCaret(node.getText().length());
                        popup.hide();
                    }
                    break;
            }
        });

        node.textProperty().addListener((observable, oldValue, newValue) -> {
            int caretPosition = node.getCaretPosition();
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

            caretPosition = Math.min(caretPosition, node.getText().length());

            SuggestionHelper helper = new SuggestionHelper(node.getText(), caretPosition);
            List<AutocompletionCell> list = provider.suggestions(helper.word());

            ObservableList<AutocompletionCell> items = suggestionList.getItems();
            items.clear();
            items.addAll(list);
            // select first row
            suggestionList.getSelectionModel().select(0);

            if (list.isEmpty()) {
                popup.hide();
            } //else
//            if (!popup.isShowing()){
                Window parent = node.getScene().getWindow();
                double value = 31.25 * list.size() + 5;
                popup.show(parent,
                        parent.getX() + node.localToScene(0, 0).getX() +
                                node.getScene().getX(),
                        parent.getY() + node.localToScene(0, 0).getY() +
                                node.getScene().getY() + TITLE_HEIGHT);

                if (popup.getHeight() != value) {
                    popup.setHeight(value);
                    suggestionList.setPrefHeight(value);
                }
//            }
        });
    }

    private int length(String string) {
        return string == null ? 0 : string.length();
    }

    private class DefaultListCell<T> extends ListCell<AutocompletionCell> {
        private final Map<AutocompletionCell, Button> butts = Maps.newLinkedHashMap();

        @Override
        protected void updateItem(AutocompletionCell item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                HBox hBox = new HBox();
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
                ch.add(new Label(item.getColumn()));
                Region r = new Region();
                ch.add(r);

                ch.add(new Label(String.valueOf(item.getValue())));

                HBox.setHgrow(r, Priority.ALWAYS);
//                setText(item == null ? "null" : item.getColumn());

//                if (!butts.containsKey(item)) {
//                    Button butt = new Button(item.getColumn());
//                    butt.setMaxHeight(20);
//                    butts.put(item, butt);
//                }
//
//                setGraphic(butts.get(item));
                setGraphic(hBox);
            }
        }
    }
}


