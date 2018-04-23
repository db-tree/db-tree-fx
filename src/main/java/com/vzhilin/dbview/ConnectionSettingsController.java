package com.vzhilin.dbview;

import com.google.common.collect.Sets;
import com.vzhilin.dbview.autocomplete.AutoCompletion;
import com.vzhilin.dbview.autocomplete.table.DbSuggestionProvider;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.schema.Table;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.paint.Color;
import javafx.util.converter.DefaultStringConverter;
import org.apache.log4j.Logger;

import java.sql.SQLException;
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
    private Label testMessageLabel;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private DbContext currentContext;

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
        templateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        templateTable.setEditable(true);

        ObservableList<TableColumn<Template, ?>> columns = templateTable.getColumns();
        TableColumn<Template, String> tableColumn = new TableColumn<>("Table");
        TableColumn<Template, String> templateColumn = new TableColumn<>("Meaningful");
        columns.add(tableColumn);
        columns.add(templateColumn);

        tableColumn.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        templateColumn.setCellValueFactory(new PropertyValueFactory<>("template"));
        templateColumn.setCellFactory(param -> {
            return new MeaningTableCell();
        });


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
                });

                refreshTemplates(ctx.getSchema().allTableNames());
            } catch (SQLException ex) {
                Platform.runLater(() -> {
                    testMessageLabel.setTextFill(Color.RED);
                    testMessageLabel.setText(ex.getMessage());
                });
            }
        });
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
    }

    private class MeaningTableCell extends TextFieldTableCell<Template, String> {
        private TextField textField;
        public MeaningTableCell() {
            super(new DefaultStringConverter());
        }

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                textField = new TextField();
                textField.setOnAction(e -> cancelEdit());

                Template template = (Template) getTableRow().getItem();
                textField.textProperty().bindBidirectional(template.templateProperty());

                Table table = getContext().getSchema().getTable(template.getTableName());
                DbSuggestionProvider kcaProvider = new DbSuggestionProvider(table);
                new AutoCompletion(kcaProvider).bind(textField);
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
