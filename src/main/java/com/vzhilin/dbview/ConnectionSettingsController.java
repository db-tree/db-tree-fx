package com.vzhilin.dbview;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vzhilin.dbview.autocomplete.AutoCompletion;
import com.vzhilin.dbview.autocomplete.table.DbSuggestionProvider;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.schema.Table;
import com.vzhilin.dbview.settings.LookupTreeNode;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
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
    private TableView<Template> templateTable;

    @FXML
    private TreeTableView<LookupTreeNode> lookupTreeView;

    @FXML
    private Label testMessageLabel;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private DbContext currentContext;

    private ConnectionSettings settings;

    private void updateContext() {
        try {
            currentContext = new DbContext(driverClass.getText(), jdbcUrl.getText(), username.getText(), password.getText());
        } catch (SQLException e) {
            LOG.error(e, e);
        }
    }

    private DbContext getContext() {
        if (currentContext == null) {
            updateContext();
        }

        return currentContext;
    }

    @FXML
    private void initialize() {
        initTemplateTable();
        initLookupTree();
    }

    private void initLookupTree() {
        lookupTreeView.setEditable(true);
        lookupTreeView.showRootProperty().setValue(false);

        TreeTableColumn<LookupTreeNode, Boolean> enabledColumn = new TreeTableColumn<>("Enabled");
        TreeTableColumn<LookupTreeNode, String> tableColumn = new TreeTableColumn<>("Column");
        tableColumn.setMinWidth(400);
        lookupTreeView.getColumns().add(tableColumn);
        lookupTreeView.getColumns().add(enabledColumn);

        enabledColumn.setCellValueFactory(param -> param.getValue().getValue().includedProperty());

        enabledColumn.setEditable(true);
        enabledColumn.setCellFactory(param -> {
            CheckBoxTreeTableCell<LookupTreeNode, Boolean> cell = new CheckBoxTreeTableCell<>();
            cell.setEditable(true);
            return cell;
        });
        tableColumn.setCellValueFactory(param -> param.getValue().getValue().tableProperty());
//
//        LookupTreeNode kcaNode = new LookupTreeNode("OESO_KCA");
//        kcaNode.includedProperty().setValue(true);
//
//        TreeItem<LookupTreeNode> root = new TreeItem<>(kcaNode);
//        lookupTreeView.setRoot(root);
//
//        root.getChildren().add(new TreeItem<>(new LookupTreeNode("KCAID")));
//        root.getChildren().add(new TreeItem<>(new LookupTreeNode("KCAREGNUMBER")));
//        root.getChildren().add(new TreeItem<>(new LookupTreeNode("KCANAME")));
    }

    private void initTemplateTable() {
        templateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        templateTable.setEditable(true);

        ObservableList<TableColumn<Template, ?>> columns = templateTable.getColumns();
        TableColumn<Template, String> tableColumn = new TableColumn<>("Table");
        TableColumn<Template, String> templateColumn = new TableColumn<>("Meaningful");
        columns.add(tableColumn);
        columns.add(templateColumn);

        tableColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        templateColumn.setCellValueFactory(new PropertyValueFactory<>("template"));
        templateColumn.setCellFactory(param -> new MeaningTableCell());
    }

    @FXML
    private void onTestButton() throws SQLException {
        testMessageLabel.setTextFill(Color.BLACK);
        testMessageLabel.setText("Connecting...");

        executor.execute(() -> {
            try {
                DbContext ctx = new DbContext(driverClass.getText(), jdbcUrl.getText(), username.getText(), password.getText());
                Platform.runLater(() -> {
                    testMessageLabel.setTextFill(Color.DARKGREEN);
                    testMessageLabel.setText("OK");

                    refreshTemplates(ctx.getSchema().allTableNames());
                    refreshLookupTree(ctx.getSchema().allTables());
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
                TreeItem<LookupTreeNode> newItem = new TreeItem<>(new LookupTreeNode(tableName));
                nodeMap.put(tableName, newItem);
                lookupTreeView.getRoot().getChildren().add(newItem);
            }

            Map<String, TreeItem<LookupTreeNode>> cols = Maps.newLinkedHashMap();
            nodeMap.get(tableName).getChildren().forEach(table -> cols.put(table.getValue().tableProperty().getValue(), table));

            for (String columnName: tableMap.get(tableName).getColumns()) {
                if (!cols.containsKey(columnName)) {
                    boolean included = columnName.equals(tableMap.get(tableName).getPk());
                    settings.addLookupableColumn(tableName, columnName, included);

                    LookupTreeNode newNode = new LookupTreeNode(columnName);
                    newNode.includedProperty().set(included);
                    TreeItem<LookupTreeNode> newItem = new TreeItem<>(newNode);
                    cols.put(columnName, newItem);

                    nodeMap.get(tableName).getChildren().add(newItem);
                }
            }
        }
    }

    private void refreshTemplates(Set<String> schemaTables) {
        ObservableList<Template> items = templateTable.getItems();
        Set<String> existingTables = items.stream().map(Template::getTableName).collect(toSet());
        for (String name: Sets.difference(schemaTables, existingTables)) {
            items.add(new Template(name, ""));
        }
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
        templateTable.itemsProperty().bindBidirectional(settings.templatesProperty());

        ChangeListener<String> changeListener = (observable, oldValue, newValue) -> updateContext();
        connectionName.textProperty().addListener(changeListener);
        driverClass.textProperty().addListener(changeListener);
        jdbcUrl.textProperty().addListener(changeListener);
        username.textProperty().addListener(changeListener);
        password.textProperty().addListener(changeListener);

        bindTree(settings.getLookupableColumns());

        this.settings = settings;
    }

    private void bindTree(Map<String, Map<String, BooleanProperty>> settings) {
        TreeItem<LookupTreeNode> root = new TreeItem<>(new LookupTreeNode("ROOT"));
        ObservableList<TreeItem<LookupTreeNode>> ch = root.getChildren();

        for (String table: settings.keySet()) {
            TreeItem<LookupTreeNode> tableNode = new TreeItem<>(new LookupTreeNode(table));
            ch.add(tableNode);

            Map<String, BooleanProperty> columns = settings.get(table);

            for (String column: columns.keySet()) {
                BooleanProperty val = columns.get(column);

                LookupTreeNode newNode = new LookupTreeNode(column);
                TreeItem<LookupTreeNode> columnNode = new TreeItem<>(newNode);
                tableNode.getChildren().add(columnNode);

                newNode.includedProperty().bindBidirectional(val);
            }
        }

        lookupTreeView.setRoot(root);
    }

    private class MeaningTableCell extends TextFieldTableCell<Template, String> {
        private TextField textField;
        private AutoCompletion autoCompletion;

        public MeaningTableCell() {
            super(new DefaultStringConverter());
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (isEditing()) {
                textField = new TextField();
                textField.setOnAction(e -> cancelEdit());

                Template template = (Template) getTableRow().getItem();
                textField.textProperty().bindBidirectional(template.templateProperty());

                Table table = getContext().getSchema().getTable(template.getTableName());
                DbSuggestionProvider kcaProvider = new DbSuggestionProvider(table);
                autoCompletion = new AutoCompletion(kcaProvider, textField);
            }

            MeaningTableCell cell = this;
            cell.setText(null);

            cell.setGraphic(textField);
            textField.selectAll();

            // requesting focus so that key input can immediately go into the
            // TextField (see RT-28132)
            textField.requestFocus();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            MeaningTableCell cell = this;
            cell.setText(textField.getText());
            cell.setGraphic(null);

            textField = null;
            autoCompletion.unbind();
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            MeaningTableCell cell = this;

            if (cell.isEmpty()) {
                cell.setText(null);
                cell.setGraphic(null);
            } else {
                if (cell.isEditing()) {
                    cell.setText(null);
                    cell.setGraphic(textField);
                }
            }
        }
    }
}
