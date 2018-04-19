package com.vzhilin.dbview.autocomplete;

import com.google.common.collect.Lists;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.List;

public class AutoCompletion {
    private static final double TITLE_HEIGHT = 28;
    private final SuggestionProvider provider;

    public AutoCompletion(SuggestionProvider suggestionProvider) {
        this.provider = suggestionProvider;
    }

    public void bind(TextField node) {
        List<String> vs = Lists.newArrayList("aaa", "Bbbb", "Ccccc");

        Popup popup = new Popup();
        ListView<String> suggestionList = new ListView<String>();
        popup.getContent().add(suggestionList);

        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                popup.hide();
            }
        });

        suggestionList.setOnKeyPressed(ke -> {
            switch (ke.getCode()) {
                case ENTER:
                    String selectedItem = suggestionList.getSelectionModel().getSelectedItem();
                    if (selectedItem != null) {
                        SuggestionHelper helper = new SuggestionHelper(node.getText(), node.getCaretPosition());

                        node.setText(helper.select(selectedItem));

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
            List<String> list = provider.suggestions(helper.word());

            suggestionList.getItems().clear();
            suggestionList.getItems().addAll(list);

            if (list.isEmpty()) {
                popup.hide();
            } else {
                Window parent = node.getScene().getWindow();
                double value = 23.125 * list.size() + 5;
                popup.show(parent,
                        parent.getX() + node.localToScene(0, 0).getX() +
                                node.getScene().getX(),
                        parent.getY() + node.localToScene(0, 0).getY() +
                                node.getScene().getY() + TITLE_HEIGHT);

                popup.setHeight(value);
                suggestionList.setPrefHeight(value);
            }
        });
    }

    private int length(String string) {
        return string == null ? 0 : string.length();
    }
}