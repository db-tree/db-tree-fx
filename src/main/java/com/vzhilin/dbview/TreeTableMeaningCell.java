package com.vzhilin.dbview;

import com.vzhilin.dbview.autocomplete.AutoCompletion;
import com.vzhilin.dbview.autocomplete.row.RowSuggestionProvider;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.data.Row;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;

/**
 * Ячейка с "осмысленным значением" для дерева в главном окне
 */
public class TreeTableMeaningCell extends TreeTableCell<TreeTableNode, Row> {
    private TextField meaningTextField;
    private AutoCompletion autoCompletion;

    public TreeTableMeaningCell() {
        itemProperty().addListener((observable, oldValue, newValue) -> setEditable(newValue != null));
    }

    @Override
    public void startEdit() {
        super.startEdit();
        Row row = getItem();
        if (row == null) {
            setText("");
            setGraphic(null);
            return;
        }
        QueryContext ctx = row.getContext();
        if (meaningTextField == null) {
            meaningTextField = new TextField();
            meaningTextField.getStyleClass().add("table-cell");
            meaningTextField.setOnAction(event -> cancelEdit());
            meaningTextField.setText(ctx.getTemplate(row.getTable().getName()));
            RowSuggestionProvider provider = new RowSuggestionProvider(row);
            autoCompletion = new AutoCompletion(provider, meaningTextField);
        }
        setGraphic(meaningTextField);
        setText("");
        meaningTextField.requestFocus();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        Row row = getItem();
        if (row != null) {
            row.getContext().setTemplate(row.getTable().getName(), meaningTextField.getText());

            setGraphic(null);
            setText(row.getContext().getMeanintfulValue(row));
        }

        getTreeTableView().refresh();

        meaningTextField = null;
        if (autoCompletion != null) {
            autoCompletion.unbind();
        }
    }

    @Override
    public void updateItem(Row item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
            setText(null);
        } else {
            if (item != null) {
                setText(item.getContext().getMeanintfulValue(item));
            } else {
                setText(null);
            }

            setGraphic(null);
        }
    }
}
