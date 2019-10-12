package me.vzhilin.dbtree.ui.tree;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;
import me.vzhilin.dbtree.db.QueryContext;
import me.vzhilin.dbtree.db.meaning.exp.ParsedTemplate;
import me.vzhilin.dbtree.ui.autocomplete.AutoCompletion;
import me.vzhilin.dbtree.ui.autocomplete.row.RowSuggestionProvider;

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
        QueryContext ctx = (QueryContext) row.getContext().getAttribute("query_context");
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
            QueryContext queryContext = (QueryContext) row.getContext().getAttribute("query_context");
            Table table = row.getTable();
            queryContext.setTemplate(table.getSchemaName(), table.getName(), meaningTextField.getText());
            setGraphic(null);
            setTextForItem(row);
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
            setGraphic(null);
            setTextForItem(item);
        }
    }

    private void setTextForItem(Row row) {
        if (row == null) {
            setText("");
            return;
        }

        QueryContext queryContext = (QueryContext) row.getContext().getAttribute("query_context");
        ParsedTemplate pt = queryContext.getParsedTemplate(row.getTable());
        if (pt == null) {
            setText("");
        } else {
            if (pt.isValid()) {
                setText(String.valueOf(pt.render(row).getValue()));
            } else {
                setText(null);
                HBox hBox = new HBox();

                Region r = new Region();
                r.setMaxWidth(16);
                r.setMinWidth(16);
                r.getStyleClass().add("validation-failure");
                hBox.getChildren().add(r);
                Label label = new Label(pt.getError());
                label.getStyleClass().add("validation-message");
                hBox.getChildren().add(label);
                setGraphic(hBox);
            }
        }
    }
}
