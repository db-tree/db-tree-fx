package me.vzhilin.dbtree.ui.settings;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import me.vzhilin.catalog.Catalog;
import me.vzhilin.catalog.Table;
import me.vzhilin.dbtree.db.DbContext;
import me.vzhilin.dbtree.db.meaning.MeaningParser;
import me.vzhilin.dbtree.db.meaning.exp.ParsedTemplate;
import me.vzhilin.dbtree.ui.ApplicationContext;
import me.vzhilin.dbtree.ui.autocomplete.AutoCompletion;
import me.vzhilin.dbtree.ui.autocomplete.table.DbSuggestionProvider;
import me.vzhilin.dbtree.ui.conf.ConnectionSettings;
import me.vzhilin.dbtree.ui.conf.Template;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toSet;

public class ConnectionSettingsController {
    private final static Logger LOG = Logger.getLogger(ConnectionSettingsController.class);

    @FXML
    private TextField connectionName;

    @FXML
    private TextField driverClass;

    @FXML
    private TextField jdbcUrl;

    @FXML
    private TextField username;

    @FXML
    private TextField password;

    @FXML
    private TextField tableNamePattern;

    @FXML
    private TableView<Template> templateTable;

    @FXML
    private TreeTableView<LookupTreeNode> lookupTreeView;

    @FXML
    private Label testMessageLabel;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private ConnectionSettings settings;
    private ApplicationContext appContext;


    private DbContext getContext() {
        try {
            return appContext.newQueryContext(driverClass.getText(), jdbcUrl.getText(), username.getText(), password.getText(), tableNamePattern.getText());
        } catch (ExecutionException e) {
            LOG.error(e, e);
        }

        return null;
    }

    @FXML
    private void initialize() {
        initTemplateTable();
        initLookupTree();
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
        private final String table;
        private final StringProperty text;

        public TemplateCell(Template template) {
            this.table = template.getTableName();
            this.text = template.templateProperty();
        }

        public String getText() {
            return text.get();
        }

        public String getTable() {
            return table;
        }
    }

    private void initTemplateTable() {
        templateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        templateTable.setEditable(true);

        ObservableList<TableColumn<Template, ?>> columns = templateTable.getColumns();
        TableColumn<Template, String> tableColumn = new TableColumn<>("Table");
        TableColumn<Template, TemplateCell> templateColumn = new TableColumn<>("Meaningful");
        columns.add(tableColumn);
        columns.add(templateColumn);
        tableColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        templateColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(new TemplateCell(param.getValue())));
        templateColumn.setCellFactory(param -> new MeaningTableCell());
    }

    @FXML
    private void onTestButton() {
        testMessageLabel.setTextFill(Color.BLACK);
        testMessageLabel.setText("Connecting...");

        executor.execute(() -> {
            try {
                DbContext ctx = new DbContext(driverClass.getText(), jdbcUrl.getText(), username.getText(), password.getText(), tableNamePattern.getText());
                Platform.runLater(() -> {
                    testMessageLabel.setTextFill(Color.DARKGREEN);
                    testMessageLabel.setText("OK");

//                    refreshTemplates(ctx.getSchema().allTableNames());
//                    refreshLookupTree(ctx.getSchema().allTables());
                });
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    testMessageLabel.setTextFill(Color.RED);
                    testMessageLabel.setText(ex.getMessage());
                });
            }
        });
    }

    private void refreshLookupTree(Set<Table> schemaTables) {
        Map<String, Table> tableMap = Maps.newLinkedHashMap();
        schemaTables.forEach(table -> tableMap.put(table.getName(), table));

        Map<String, TreeItem<LookupTreeNode>> nodeMap = Maps.newLinkedHashMap();
        ObservableList<TreeItem<LookupTreeNode>> ch = lookupTreeView.getRoot().getChildren();
        ch.forEach(table -> nodeMap.put(table.getValue().tableProperty().getValue(), table));

        for (String tableName: tableMap.keySet()) {
            if (!nodeMap.containsKey(tableName)) {
                TreeItem<LookupTreeNode> newItem = new TreeItem<>(new LookupTreeNode(tableName, true));
                nodeMap.put(tableName, newItem);
                lookupTreeView.getRoot().getChildren().add(newItem);
            }

            Map<String, TreeItem<LookupTreeNode>> cols = Maps.newLinkedHashMap();
            nodeMap.get(tableName).getChildren().forEach(table -> cols.put(table.getValue().tableProperty().getValue(), table));

//            for (String columnName: tableMap.get(tableName).getColumns()) {
//                if (!cols.containsKey(columnName)) {
//                    boolean included = columnName.equals(tableMap.get(tableName).getPk());
//                    settings.addLookupableColumn(tableName, columnName, included);
//
//                    LookupTreeNode newNode = new LookupTreeNode(columnName, false);
//                    newNode.includedProperty().set(included);
//                    TreeItem<LookupTreeNode> newItem = new TreeItem<>(newNode);
//                    cols.put(columnName, newItem);
//
//                    nodeMap.get(tableName).getChildren().add(newItem);
//                }
//            }
        }

        for (String tableName: Sets.difference(nodeMap.keySet(), tableMap.keySet())) {
            TreeItem<LookupTreeNode> n = nodeMap.get(tableName);
            n.getParent().getChildren().remove(n);

            settings.getLookupableColumns().remove(tableName);
        }
    }

    private void refreshTemplates(Set<String> schemaTables) {
        ObservableList<Template> items = templateTable.getItems();
        Set<String> existingTables = items.stream().map(Template::getTableName).collect(toSet());
        Sets.SetView<String> newTables = Sets.difference(schemaTables, existingTables);
        for (String name: newTables) {
            items.add(new Template("", name, ""));
        }

        Sets.SetView<String> removedTables = Sets.difference(existingTables, schemaTables);
        templateTable.getItems().removeIf(item -> removedTables.contains(item.getTableName()));
    }

    public void setConnectoinName(String connectionName) {
        this.connectionName.setText(connectionName);
    }

    public void bindSettings(ConnectionSettings settings) {
        connectionName.textProperty().bindBidirectional(settings.connectionNameProperty());
        driverClass.textProperty().bindBidirectional(settings.driverClassProperty());
        jdbcUrl.textProperty().bindBidirectional(settings.jdbcUrlProperty());
        username.textProperty().bindBidirectional(settings.usernameProperty());
        password.textProperty().bindBidirectional(settings.passwordProperty());
        tableNamePattern.textProperty().bindBidirectional(settings.tableNamePatternProperty());
        templateTable.itemsProperty().bindBidirectional(settings.templatesProperty());
        bindTree(settings.getLookupableColumns());

        this.settings = settings;
    }

    private void bindTree(Map<String, Map<String, BooleanProperty>> settings) {
        TreeItem<LookupTreeNode> root = new TreeItem<>(new LookupTreeNode("ROOT", true));
        ObservableList<TreeItem<LookupTreeNode>> ch = root.getChildren();

        for (String table: settings.keySet()) {
            TreeItem<LookupTreeNode> tableNode = new TreeItem<>(new LookupTreeNode(table, true));
            ch.add(tableNode);

            Map<String, BooleanProperty> columns = settings.get(table);

            for (String column: columns.keySet()) {
                BooleanProperty val = columns.get(column);

                LookupTreeNode newNode = new LookupTreeNode(column, false);
                TreeItem<LookupTreeNode> columnNode = new TreeItem<>(newNode);
                tableNode.getChildren().add(columnNode);

                newNode.includedProperty().bindBidirectional(val);
            }
        }

        lookupTreeView.setRoot(root);
    }

    private class MeaningTableCell extends TableCell<Template, TemplateCell> {
        private TextField textField;
        private AutoCompletion autoCompletion;

        public MeaningTableCell() {

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
            DbContext context = getContext();
            if (context != null) {
                textField = new TextField();
                textField.setOnAction(e -> cancelEdit());

                Template template = (Template) getTableRow().getItem();
                textField.textProperty().bindBidirectional(template.templateProperty());

                Catalog catalog = context.getCatalog();
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
            setText("");
//            if ("".equals(item.getText())) {
//                setText("");
//            } else {
//                String tableName = item.getTable();
//                if (getContext() != null) {
//                    ParsedTemplate exp = parse(getContext().getSchema().getTable(tableName), item.getText());
//                    if (exp.isValid()) {
//                        setText(item.getText());
//                    } else {
//                        setText(null);
//                        HBox hBox = new HBox();
//
//                        Region r = new Region();
//                        r.setMaxWidth(16);
//                        r.setMinWidth(16);
//                        r.getStyleClass().add("validation-failure");
//                        hBox.getChildren().add(r);
//                        Label label = new Label(exp.getError());
//                        label.getStyleClass().add("validation-message");
//                        hBox.getChildren().add(label);
//                        setGraphic(hBox);
//                    }
//                }
//            }
        }

        private ParsedTemplate parse(Table table, String text) {
            return new MeaningParser().parse(table, text);
        }
    }
}