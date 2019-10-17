package me.vzhilin.dbtree.ui;

import com.google.common.collect.Iterables;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import me.vzhilin.adapter.DatabaseAdapter;
import me.vzhilin.catalog.Catalog;
import me.vzhilin.catalog.CatalogFilter;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;
import me.vzhilin.db.RowContext;
import me.vzhilin.dbtree.db.DbContext;
import me.vzhilin.dbtree.db.QueryContext;
import me.vzhilin.dbtree.ui.conf.*;
import me.vzhilin.dbtree.ui.settings.SettingsController;
import me.vzhilin.dbtree.ui.tree.CountNode;
import me.vzhilin.dbtree.ui.tree.Paging;
import me.vzhilin.dbtree.ui.tree.TreeTableMeaningCell;
import me.vzhilin.dbtree.ui.tree.TreeTableNode;
import me.vzhilin.search.CountOccurences;
import me.vzhilin.search.SearchInTable;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;

public class MainWindowController {
    private final static Logger LOG = Logger.getLogger(MainWindowController.class);

    @FXML
    private ComboBox<ConnectionSettings> cbConnection;

    @FXML
    private TreeTableView<TreeTableNode> treeTable;

    @FXML
    private TreeTableColumn<TreeTableNode, String> itemColumn;

    @FXML
    private TreeTableColumn<TreeTableNode, String> tableColumn;

    @FXML
    private TreeTableColumn<TreeTableNode, String> valueColumn;

    @FXML
    private TreeTableColumn<TreeTableNode, Row> meaningfulValueColumn;

    @FXML
    private TextField textField;

    @FXML
    private ToggleButton showLog;

    @FXML
    private SplitPane splitPane;

    @FXML
    private TextArea logView = new TextArea();

    private Settings settings;

    private Stage ownerWindow;

    /** Контекст приложения */
    private ApplicationContext appContext;

    @FXML
    private void initialize() {
        treeTable.setRowFactory(param -> new TreeTableRow<>());
        treeTable.setEditable(true);
        treeTable.setShowRoot(false);
        treeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        itemColumn.setCellValueFactory(v -> v.getValue().getValue().itemColumnProperty());
        itemColumn.setSortable(false);
        itemColumn.setCellFactory(param -> new ItemTreeTableCell());

        tableColumn.setCellFactory(param -> new TableTreeTableCell());
        tableColumn.setSortable(false);
        tableColumn.setEditable(false);
        tableColumn.setCellValueFactory(v -> v.getValue().getValue().tableColumnProperty());

        valueColumn.setCellValueFactory(v -> v.getValue().getValue().valueColumnProperty());
        valueColumn.setSortable(false);
        valueColumn.setEditable(true);
        valueColumn.setStyle( "-fx-alignment: CENTER-RIGHT;");
        valueColumn.setCellFactory(param -> new TextFieldTreeTableCell<>());
        meaningfulValueColumn.setCellValueFactory(v -> v.getValue().getValue().meaningfulValueColumnProperty());
        meaningfulValueColumn.setCellFactory(param -> new TreeTableMeaningCell() {
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                settings.save();
            }
        });
        meaningfulValueColumn.setSortable(false);
        meaningfulValueColumn.setEditable(true);

        cbConnection.setCellFactory(new Callback<ListView<ConnectionSettings>, ListCell<ConnectionSettings>>() {
            @Override
            public ListCell<ConnectionSettings> call(ListView<ConnectionSettings> param) {
                return new ListCell<ConnectionSettings>() {
                    @Override
                    protected void updateItem(ConnectionSettings item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getConnectionName());
                        }
                    }
                };
            }
        });

        showLog.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                ObservableList<Node> items = splitPane.getItems();
                final DoubleProperty positionProperty = settings.dividerPositionProperty();
                if (newValue) {
                    items.add(logView);
                    Double dividerPosition = positionProperty.getValue();
                    splitPane.setDividerPosition(0, dividerPosition == null ? 0.5: dividerPosition);

                    for (SplitPane.Divider dv: splitPane.getDividers()) {
                        dv.positionProperty().addListener(new ChangeListener<Number>() {
                            @Override
                            public void changed(ObservableValue<? extends Number> v, Number oldValue, Number newValue) {
                                positionProperty.setValue(newValue);
                            }
                        });
                    }
                } else {
                    positionProperty.setValue(splitPane.getDividerPositions()[0]);
                    items.remove(logView);
                }
            }
        });
    }

    @FXML
    private void onFindAction() throws ExecutionException {
        TreeItem<TreeTableNode> newRoot = new TreeItem<>(new TreeTableNode("ROOT", "ROOT", null));
        ConnectionSettings connection = cbConnection.getValue();

        QueryContext queryContext = appContext.newQueryContext(connection.getConnectionName());
        ObservableList<TreeItem<TreeTableNode>> ch = newRoot.getChildren();
        CatalogFilter filter = filterFor(queryContext.getSettings().getLookupableColumns());
        DbContext dbContext = queryContext.getDbContext();
        Connection conn = dbContext.getConnection();
        Catalog catalog = dbContext.getCatalog();
        QueryRunner runner = dbContext.getRunner();
        DatabaseAdapter adapter = dbContext.getAdapter();
        RowContext ctx = new RowContext(catalog, adapter, conn, runner);
        ctx.setAttribute("query_context", queryContext);
        final String searchText = textField.getText();
        CountOccurences c = new CountOccurences(ctx, searchText);
        Map<Table, Long> rs = c.count(filter);
        rs.forEach(new BiConsumer<Table, Long>() {
            @Override
            public void accept(Table table, Long count) {
                SearchInTable search = new SearchInTable(ctx, table, searchText);
                Iterable<Row> iter = search.search(filter);
                ch.add(new CountNode(iter, table, count));
            }
        });

        treeTable.setRoot(newRoot);
        if (ch.size() == 1) {
            ch.get(0).setExpanded(true);
        }
    }

    private CatalogFilter filterFor(Map<ColumnKey, BooleanProperty> lookupableColumns) {
        Set<String> schemas = new HashSet<>();
        Set<TableKey> tables = new HashSet<>();
        Set<ColumnKey> columns = new HashSet<>();

        lookupableColumns.forEach((columnKey, booleanProperty) -> {
            if (booleanProperty.getValue()) {
                schemas.add(columnKey.getSchema());
                tables.add(columnKey.getTableKey());
                columns.add(columnKey);
            }
        });
        return new CatalogFilter() {
            @Override
            public boolean acceptSchema(String schema) {
                return schemas.contains(schema);
            }

            @Override
            public boolean acceptTable(String schema, String table) {
                return tables.contains(new TableKey(new SchemaKey(schema), table));
            }

            @Override
            public boolean acceptColumn(String schema, String table, String column) {
                return columns.contains(new ColumnKey(schema, table, column));
            }
        };
    }

    @FXML
    private void onConfigAction() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/settings-dialog.fxml"));
        Parent root = loader.load();
        SettingsController controller = loader.getController();
        controller.setMainWinController(this);
        controller.setAppContext(appContext);
        Stage stage = new Stage();
        stage.initOwner(ownerWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        setIcon(stage);
        controller.setSettings(settings);
        Scene scene = new Scene(root, 800, 500);
        scene.getStylesheets().add(getClass().getResource("/styles/connection-settings.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/styles/autocomplete.css").toExternalForm());
        stage.setTitle("Settings");
        stage.setScene(scene);
        stage.show();
    }

    private void setIcon(Stage stage) {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.png")));
    }

    public void setSettings(Settings settings) {
        this.settings = settings;

        setupCombobox(settings);
    }

    private void setupCombobox(Settings settings) {
        ChangeListener<String> nameChangeListener = (observable1, oldName, newName) -> {
            ConnectionSettings curValue = cbConnection.getValue();

            cbConnection.setValue(null);
            cbConnection.setValue(curValue);
        };

        cbConnection.valueProperty().addListener((observable, oldConn, newConn) -> {
            if (newConn != null) {
                StringProperty newNameProperty = newConn.connectionNameProperty();
                newNameProperty.addListener(nameChangeListener);
            }

            if (oldConn != null) {
                StringProperty oldNameProperty = oldConn.connectionNameProperty();
                if (oldNameProperty != null) {
                    oldNameProperty.removeListener(nameChangeListener);
                }
            }
        });

        cbConnection.setValue(Iterables.getFirst(settings.getConnections(), null));
        cbConnection.itemsProperty().bind(settings.connectionsProperty());
    }

    public void refreshTreeView() {
        treeTable.refresh();
    }

    public void setOwnerWindow(Stage stage) {
        this.ownerWindow = stage;
    }

    public void setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    private final static class TableTreeTableCell extends TreeTableCell<TreeTableNode, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            super.setText(item);
        }
    }

    private static class ItemTreeTableCell extends TreeTableCell<TreeTableNode, String> {
        private final static Pane EMPTY_PANE = new Pane();
        @Override protected void updateItem(String item, boolean empty) {
            TreeItem<TreeTableNode> treeItem = getTreeTableRow().getTreeItem();
            if (item == null || empty) {
                super.setText(null);
            } else {
                if (treeItem instanceof Paging.PagingItem) {
                    Pane hBox = new Pane();
                    ObservableList<Node> ch = hBox.getChildren();
                    Button loadMoreButton = new Button("More...");
                    loadMoreButton.setOnAction((Paging.PagingItem) treeItem);
                    ch.add(loadMoreButton);
                    treeItem.setGraphic(hBox);
                    setText(null);
                } else {
                    setText(item);
                    if (treeItem != null) {
                        treeItem.setGraphic(EMPTY_PANE);
                    }
                }
            }
      }
    }
}
