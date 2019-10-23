package me.vzhilin.dbtree.ui;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import me.vzhilin.dbrow.adapter.DatabaseAdapter;
import me.vzhilin.dbrow.catalog.Catalog;
import me.vzhilin.dbrow.catalog.CatalogFilter;
import me.vzhilin.dbrow.catalog.Table;
import me.vzhilin.dbrow.db.Row;
import me.vzhilin.dbrow.db.RowContext;
import me.vzhilin.dbrow.search.CountOccurences;
import me.vzhilin.dbrow.search.SearchInTable;
import me.vzhilin.dbtree.db.DbContext;
import me.vzhilin.dbtree.db.QueryContext;
import me.vzhilin.dbtree.ui.conf.*;
import me.vzhilin.dbtree.ui.settings.SettingsController;
import me.vzhilin.dbtree.ui.tree.CountNode;
import me.vzhilin.dbtree.ui.tree.Paging;
import me.vzhilin.dbtree.ui.tree.TreeTableNode;
import me.vzhilin.dbtree.ui.tree.TreeTableStringCell;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.ExecutionException;

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
    private IntegerProperty logErrors = new SimpleIntegerProperty();

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
        meaningfulValueColumn.setCellFactory(param -> new TreeTableStringCell() {
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
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean show) {
                ObservableList<Node> items = splitPane.getItems();
                final DoubleProperty positionProperty = settings.dividerPositionProperty();
                if (show) {
                    logErrors.set(0);
                    showLog.setText("Log");
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
        ConnectionSettings connection = cbConnection.getValue();
        if (connection == null) {
            ObservableList<ConnectionSettings> connections = settings.getConnections();
            if (!connections.isEmpty()) {
                ConnectionSettings c = connections.get(1);
                cbConnection.setValue(c);
                connection = c;
            } else {
                appContext.getLogger().log("not connected");
                return;
            }
        }
        final String searchText = textField.getText();
        treeTable.setPlaceholder(new ProgressIndicator());
        textField.setDisable(true);
        TreeItem<TreeTableNode> newRoot = new TreeItem<>(new TreeTableNode("ROOT", "ROOT", null));
        treeTable.setRoot(newRoot);

        List<TreeItem<TreeTableNode>> countNodes = new ArrayList<>();
        ConnectionSettings finalConnection = connection;
        ApplicationContext.get().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    QueryContext queryContext = appContext.newQueryContext(finalConnection.getConnectionName());
                    CatalogFilter filter = filterFor(queryContext.getSettings().getLookupableColumns());
                    DbContext dbContext = queryContext.getDbContext();
                    Connection conn = dbContext.getConnection();
                    Catalog catalog = dbContext.getCatalog();
                    QueryRunner runner = dbContext.getRunner();
                    DatabaseAdapter adapter = dbContext.getAdapter();
                    RowContext ctx = new RowContext(catalog, adapter, conn, runner);
                    ctx.setAttribute("query_context", queryContext);

                    CountOccurences c = new CountOccurences(ctx, searchText);
                    Map<Table, Long> rs = c.count(filter);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            rs.forEach((table, count) -> {
                                SearchInTable search = new SearchInTable(ctx, table, searchText);
                                Iterable<Row> iter = search.search(filter);
                                countNodes.add(new CountNode(iter, table, count));
                            });

                            ObservableList<TreeItem<TreeTableNode>> ch = newRoot.getChildren();
                            ch.addAll(countNodes);

                            if (ch.size() == 1) {
                                ch.get(0).setExpanded(true);
                            }
                        }
                    });

                } catch (ExecutionException e) {
                    appContext.getLogger().log("error", e);
                } finally {
                    Platform.runLater(() -> {
                        textField.setDisable(false);
                        treeTable.setPlaceholder(new Label());
                    });
                }
            }
        });
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

        ConnectionSettings sett = Iterables.getFirst(settings.getConnections(), null);
        if (sett != null) {
            cbConnection.setValue(sett);
        }
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
        appContext.setLogger(new ApplicationContext.Logger() {
            @Override
            public void log(String message) {
                Platform.runLater(() -> logView.appendText(new Date() + " " + message));
            }

            @Override
            public void log(String message, Throwable ex) {
                Platform.runLater(() -> {
                    if (!showLog.isSelected()) {
                        logErrors.setValue(logErrors.get() + 1);
                        showLog.setText("Log (" + logErrors.get() + ")");
                    }
                    logView.appendText(new Date() + " " + message + "\n" + Throwables.getStackTraceAsString(ex) + "\n");
                });
            }
        });
    }

    public void onAboutAction(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/about.fxml"));
        Parent root = loader.load();
        Stage stage = new Stage();
        stage.initOwner(ownerWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        setIcon(stage);
        Scene scene = new Scene(root);
        stage.setTitle("About");
        stage.setScene(scene);
        stage.show();
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
