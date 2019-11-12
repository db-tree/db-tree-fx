package me.vzhilin.dbtree.ui.settings;

import com.google.common.collect.Sets;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import me.vzhilin.dbrow.catalog.Catalog;
import me.vzhilin.dbrow.catalog.Column;
import me.vzhilin.dbrow.catalog.Table;
import me.vzhilin.dbtree.db.DbContext;
import me.vzhilin.dbtree.db.tostring.Parser;
import me.vzhilin.dbtree.db.tostring.exp.ParsedTemplate;
import me.vzhilin.dbtree.ui.ApplicationContext;
import me.vzhilin.dbtree.ui.autocomplete.AutoCompletion;
import me.vzhilin.dbtree.ui.autocomplete.table.DbSuggestionProvider;
import me.vzhilin.dbtree.ui.conf.*;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ConnectionSettingsController {
    private final static Logger LOG = Logger.getLogger(ConnectionSettingsController.class);

    @FXML
    private TextField connectionName;

    @FXML
    private ComboBox<String> driverClass;

    @FXML
    private TextField jdbcUrl;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private TextField tableNamePattern;

    @FXML
    private TextField schemas;

    @FXML
    private TableView<Template> templateTable;

    @FXML
    private TreeTableView<LookupTreeNode> lookupTreeView;

    @FXML
    private Label testMessageLabel;

    private ConnectionSettings settings;
    private ApplicationContext appContext;

    private final Executor executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread th = new Thread(r);
            th.setDaemon(true);
            return th;
        }
    });

    private DbContext localContext;

    private DbContext getContext() {
        try {
            String driver = driverClass.getValue();
            String url = jdbcUrl.getText();
            String username = this.username.getText();
            String pass = password.getText();
            String pattern = tableNamePattern.getText();
            Set<String> schemas = parseSchemas(this.schemas.getText());
            return appContext.newQueryContext(driver, url, username, pass, pattern, schemas);
        } catch (ExecutionException e) {
            ApplicationContext.get().getLogger().log("Database error", e);
        }

        return null;
    }

    @FXML
    private void initialize() {
        ObservableList<String> driverList =
            FXCollections.observableArrayList("oracle.jdbc.OracleDriver", "org.postgresql.Driver", "org.mariadb.jdbc.Driver");
        driverClass.setItems(driverList);

        lookupTreeView.setPlaceholder(new ProgressIndicator());
        templateTable.setPlaceholder(new ProgressIndicator());

        executor.execute(new Runnable() {
            DbContext dbContext = null;

            @Override
            public void run() {
                try {
                    dbContext = getContext();
                } finally {
                    Platform.runLater(() -> {
                        localContext = dbContext;
                        lookupTreeView.setPlaceholder(new Label());
                        templateTable.setPlaceholder(new Label());
                        initTemplateTable();
                        initLookupTree();
                    });
                }
            }
        });
    }

    private void initLookupTree() {
        lookupTreeView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        lookupTreeView.setEditable(true);
        lookupTreeView.showRootProperty().setValue(false);

        TreeTableColumn<LookupTreeNode, Boolean> selectedColumn = new TreeTableColumn<>("Selected");
        selectedColumn.setPrefWidth(80);
        selectedColumn.setMaxWidth(80);
        selectedColumn.setMinWidth(60);
        TreeTableColumn<LookupTreeNode, String> tableColumn = new TreeTableColumn<>("Column");

        lookupTreeView.getColumns().add(tableColumn);
        lookupTreeView.getColumns().add(selectedColumn);

        selectedColumn.setCellValueFactory(param -> param.getValue().getValue().includedProperty());

        selectedColumn.setEditable(true);
        selectedColumn.setCellFactory(param -> {
            CheckBoxTreeTableCell<LookupTreeNode, Boolean> cell = new CheckBoxTreeTableCell<LookupTreeNode, Boolean> () {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);

                    TreeTableRow<LookupTreeNode> row = getTreeTableRow();
                    if (row != null && row.getTreeItem() != null) {
                        LookupTreeNode v = row.getTreeItem().getValue();
                        if (v.isTable()) {
                            setGraphic(null);
                        }
                    }
                }
            };
            cell.setEditable(true);
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        tableColumn.setCellValueFactory(param -> param.getValue().getValue().tableProperty());
    }

    public void setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    public final class TemplateCell {
        private final String schema;
        private final String table;
        private final StringProperty text;

        public TemplateCell(Template template) {
            this.table = template.getTableName();
            this.schema = template.getSchemaName();
            this.text = template.templateProperty();
        }

        public String getText() {
            return text.get();
        }

        public String getTable() {
            return table;
        }

        public String getSchema() {
            return schema;
        }
    }

    private void initTemplateTable() {
        templateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        templateTable.setEditable(true);

        ObservableList<TableColumn<Template, ?>> columns = templateTable.getColumns();
        TableColumn<Template, String> tableColumn = new TableColumn<>("Table");
        TableColumn<Template, TemplateCell> templateColumn = new TableColumn<>("ToString");
        columns.add(tableColumn);
        columns.add(templateColumn);
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        templateColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(new TemplateCell(param.getValue())));
        templateColumn.setCellFactory(param -> new ToStringTableCell());
    }

    @FXML
    private void onTestButton() {
        testMessageLabel.setTextFill(Color.BLACK);
        testMessageLabel.setText("Connecting...");

        executor.execute(() -> {
            try {
                String driverClazz = driverClass.getValue();
                String url = jdbcUrl.getText();
                String name = username.getText();
                String pass = password.getText();
                String pattern = tableNamePattern.getText();
                DbContext ctx = new DbContext(driverClazz, url, name, pass, pattern, parseSchemas(schemas.getText()));
                Platform.runLater(() -> {
                    testMessageLabel.setTextFill(Color.DARKGREEN);
                    testMessageLabel.setText("OK");

                    localContext = ctx;
                    Set<Table> allTables = new HashSet<>();
                    Catalog catalog = ctx.getCatalog();
                    catalog.forEachTable(allTables::add);

                    refreshTemplates(catalog, allTables);
                    refreshLookupTree(catalog, allTables);
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    testMessageLabel.setTextFill(Color.RED);
                    testMessageLabel.setText(ex.getMessage());
                });
            }
        });
    }

    private Set<String> parseSchemas(String text) {
        Scanner sc = new Scanner(text);
        sc.useDelimiter(",");

        Set<String> result = new HashSet<>();
        while (sc.hasNext()) {
            result.add(sc.next().trim());
        }
        return result;
    }

    private void refreshLookupTree(Catalog catalog, Set<Table> schemaTables) {
        Map<ColumnKey, Boolean> schemaEnabled = getColumnsFromSchema(schemaTables);
        Map<ColumnKey, Boolean> nowEnabled = new HashMap<>();
        for (TreeItem<LookupTreeNode> schemas: lookupTreeView.getRoot().getChildren()) {
            for (TreeItem<LookupTreeNode> tables: schemas.getChildren()) {
                for (TreeItem<LookupTreeNode> columns: tables.getChildren()) {
                    LookupTreeNode node = columns.getValue();
                    String schemaName = node.schemaProperty().get();
                    String tableName = tables.getValue().tableProperty().getValue();
                    String columnName = node.tableProperty().getValue();
                    boolean isIncluded = node.includedProperty().get();
                    nowEnabled.put(new ColumnKey(schemaName, tableName, columnName), isIncluded);
                }
            }
        }

        Set<ColumnKey> removedColumns = Sets.difference(nowEnabled.keySet(), schemaEnabled.keySet());
        Set<ColumnKey> addedColumns = Sets.difference(schemaEnabled.keySet(), nowEnabled.keySet());

        addedColumns.forEach(new Consumer<ColumnKey>() {
            @Override
            public void accept(ColumnKey columnKey) {
                TreeItem<LookupTreeNode> tableNode = findOrAddTable(columnKey.getTableKey());
                String schemaName = columnKey.getTableKey().getSchemaKey().getSchemaName();
                String columnName = columnKey.getColumn();
                String tableName = columnKey.getTableKey().getTableName();
                LookupTreeNode columnNode = new LookupTreeNode(schemaName, columnName, false);
                TreeItem<LookupTreeNode> item = new TreeItem<>(columnNode);
                Boolean included = schemaEnabled.get(columnKey);
                columnNode.includedProperty().set(included);
                tableNode.getChildren().add(item);

                settings.setLookupableColumn(schemaName, tableName, columnName, included);
            }
        });

        removedColumns.forEach(new Consumer<ColumnKey>() {
            @Override
            public void accept(ColumnKey columnKey) {
                removeColumn(columnKey);
            }
        });
    }

    private void removeColumn(ColumnKey columnKey) {
        TreeItem<LookupTreeNode> rootItem = lookupTreeView.getRoot();
        String schemaName = columnKey.getTableKey().getSchemaKey().getSchemaName();
        String tableName = columnKey.getTableKey().getTableName();

        ObservableList<TreeItem<LookupTreeNode>> schemas = rootItem.getChildren();
        Optional<TreeItem<LookupTreeNode>> maybeSchema = find(schemas, schemaName);
        if (!maybeSchema.isPresent()) {
            return;
        }

        ObservableList<TreeItem<LookupTreeNode>> tables = maybeSchema.get().getChildren();
        Optional<TreeItem<LookupTreeNode>> maybeTable = find(tables, tableName);
        if (!maybeTable.isPresent()) {
            return;
        }

        ObservableList<TreeItem<LookupTreeNode>> columns = maybeTable.get().getChildren();
        Optional<TreeItem<LookupTreeNode>> maybeColumn = find(columns, columnKey.getColumn());
        if (!maybeColumn.isPresent()) {
            return;
        }

        columns.remove(maybeColumn.get());
        if (columns.isEmpty()) {
            tables.remove(maybeTable.get());

            if (tables.isEmpty()) {
                schemas.remove(maybeSchema.get());
            }
        }
    }

    private TreeItem<LookupTreeNode> findOrAddTable(TableKey tableKey) {
        TreeItem<LookupTreeNode> rootItem = lookupTreeView.getRoot();
        String schemaName = tableKey.getSchemaKey().getSchemaName();
        String tableName = tableKey.getTableName();

        TreeItem<LookupTreeNode> schemaItem = findOrAdd(rootItem.getChildren(), schemaName, schemaName);
        return findOrAdd(schemaItem.getChildren(), schemaName, tableName);
    }

    private TreeItem<LookupTreeNode> findOrAdd(ObservableList<TreeItem<LookupTreeNode>> children, String schemaName, String name) {
        Optional<TreeItem<LookupTreeNode>> maybe = find(children, name);
        if (maybe.isPresent()) {
            return maybe.get();
        } else {
            TreeItem<LookupTreeNode> newItem = new TreeItem<>(new LookupTreeNode(schemaName, name, true));
            children.add(newItem);
            return newItem;
        }
    }

    private Optional<TreeItem<LookupTreeNode>> find(ObservableList<TreeItem<LookupTreeNode>> children, String name) {
        return children.stream().filter(ltni -> name.equals(ltni.getValue().tableProperty().getValue())).findFirst();
    }

    private Map<ColumnKey, Boolean> getColumnsFromSchema(Set<Table> schemaTables) {
        Map<ColumnKey, Boolean> result = new HashMap<>();
        schemaTables.forEach(table -> table.getColumns().forEach(new BiConsumer<String, Column>() {
            @Override
            public void accept(String name, Column column) {
                boolean selected = !column.getUniqueConstraints().isEmpty();
                result.put(new ColumnKey(table.getSchemaName(), table.getName(), name), selected);
            }
        }));
        return result;
    }

    private void refreshTemplates(Catalog catalog, Set<Table> schemaTables) {
        ObservableList<Template> items = templateTable.getItems();
        Set<Table> existingTables = new HashSet<>();
        Set<Template> removedTemplates = new HashSet<>();
        items.forEach(template -> {
            String schemaName = template.getSchemaName();
            String tableName = template.getTableName();
            if (catalog.hasTable(schemaName, tableName)) {
                existingTables.add(catalog.getSchema(schemaName).getTable(tableName));
            } else {
                removedTemplates.add(template);
            }
        });

        Sets.SetView<Table> newTables = Sets.difference(schemaTables, existingTables);
        for (Table t: newTables) {
            items.add(new Template(t.getSchemaName(), t.getName(), ""));
        }

        templateTable.getItems().removeAll(removedTemplates);
    }

    public void setConnectoinName(String connectionName) {
        this.connectionName.setText(connectionName);
    }

    public void bindSettingsToUI(ConnectionSettings settings) {
        connectionName.textProperty().bindBidirectional(settings.connectionNameProperty());
        driverClass.valueProperty().bindBidirectional(settings.driverClassProperty());
        jdbcUrl.textProperty().bindBidirectional(settings.jdbcUrlProperty());
        username.textProperty().bindBidirectional(settings.usernameProperty());
        password.textProperty().bindBidirectional(settings.passwordProperty());
        tableNamePattern.textProperty().bindBidirectional(settings.tableNamePatternProperty());
        templateTable.itemsProperty().bindBidirectional(settings.templatesProperty());
        fillTree(settings.getLookupableColumns());

        this.settings = settings;
    }

    // schema -> table -> column -> isIncluded
    private void fillTree(Map<ColumnKey, BooleanProperty> includedColumns) {
        TreeItem<LookupTreeNode> root = new TreeItem<>(new LookupTreeNode("ROOT", "ROOT", true));
        // schema -> table -> column
        Map<SchemaKey, TreeItem<LookupTreeNode>> schemaNodes = new HashMap<>();
        Map<TableKey, TreeItem<LookupTreeNode>> tableNodes = new HashMap<>();

        includedColumns.forEach(new BiConsumer<ColumnKey, BooleanProperty>() {
            @Override
            public void accept(ColumnKey columnKey, BooleanProperty val) {
                TreeItem<LookupTreeNode> schemaNode = schemaNodes.computeIfAbsent(columnKey.getTableKey().getSchemaKey(), schemaKey -> {
                    LookupTreeNode node = new LookupTreeNode(schemaKey.getSchemaName(), schemaKey.getSchemaName(), true);
                    TreeItem<LookupTreeNode> item = new TreeItem<>(node);
                    root.getChildren().add(item);
                    return item;
                });


                TreeItem<LookupTreeNode> tableNode = tableNodes.computeIfAbsent(columnKey.getTableKey(), tableKey -> {
                    LookupTreeNode node = new LookupTreeNode(tableKey.getSchemaKey().getSchemaName(), tableKey.getTableName(), true);
                    TreeItem<LookupTreeNode> item = new TreeItem<>(node);
                    schemaNode.getChildren().add(item);
                    return item;
                });


                SchemaKey schemaKey = columnKey.getTableKey().getSchemaKey();
                LookupTreeNode newNode = new LookupTreeNode(schemaKey.getSchemaName(), columnKey.getColumn(), false);
                TreeItem<LookupTreeNode> columnNode = new TreeItem<>(newNode);
                tableNode.getChildren().add(columnNode);
                newNode.includedProperty().bindBidirectional(val);
            }
        });

        lookupTreeView.setRoot(root);
        if (root.getChildren().size() == 1) {
            root.getChildren().get(0).setExpanded(true);
        }
    }

    private class ToStringTableCell extends TableCell<Template, TemplateCell> {
        private TextField textField;
        private AutoCompletion autoCompletion;

        public ToStringTableCell() {

        }

        @Override
        public void updateIndex(int i) {
            if (isEditing()) {
                cancelEdit();
            }

            super.updateIndex(i);
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (isEditing()) {
                bindTextField();
            }

            setText(null);

            if (textField != null) {
                setGraphic(textField);
                textField.selectAll();

                // requesting focus so that key input can immediately go into the
                // TextField (see RT-28132)
                textField.requestFocus();
            }
        }

        private void bindTextField() {
            if (localContext != null) {
                textField = new TextField();
                textField.setOnAction(e -> cancelEdit());

                Template template = getTableRow().getItem();
                textField.textProperty().bindBidirectional(template.templateProperty());

                Catalog catalog = localContext.getCatalog();
                Table table = catalog.getSchema(template.getSchemaName()).getTable(template.getTableName());
                DbSuggestionProvider kcaProvider = new DbSuggestionProvider(table);
                autoCompletion = new AutoCompletion(kcaProvider, textField);
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setGraphic(null);
            setTextForItem(getItem());

            if (textField != null) {
                textField = null;
                autoCompletion.unbind();
            }
        }

        @Override
        public void updateItem(TemplateCell item, boolean empty) {
            super.updateItem(item, empty);

            if (isEmpty()) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    setText(null);
                    setGraphic(textField);
                } else {
                    setGraphic(null);
                    setTextForItem(item);
                }
            }
        }

        private void setTextForItem(TemplateCell item) {
            if ("".equals(item.getText())) {
                setText("");
            } else {
                setText(item.getText());

                String tableName = item.getTable();
                String schemaName = item.getSchema();
                if (localContext != null) {
                    Catalog catalog = localContext.getCatalog();
                    Table table = catalog.getSchema(schemaName).getTable(tableName);
                    if (table != null) {
                        ParsedTemplate exp = parse(table, item.getText());
                        if (exp.isValid()) {
                            setText(item.getText());
                        } else {
                            setText(null);
                            HBox hBox = new HBox();

                            Region r = new Region();
                            r.setMaxWidth(16);
                            r.setMinWidth(16);
                            r.getStyleClass().add("validation-failure");
                            hBox.getChildren().add(r);
                            Label label = new Label(exp.getError());
                            label.getStyleClass().add("validation-message");
                            hBox.getChildren().add(label);
                            setGraphic(hBox);
                        }
                    }
                }
            }
        }

        private ParsedTemplate parse(Table table, String text) {
            return new Parser().parse(table, text);
        }
    }
}
