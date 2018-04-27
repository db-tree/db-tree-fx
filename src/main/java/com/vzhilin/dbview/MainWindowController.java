package com.vzhilin.dbview;

import com.google.common.collect.Iterables;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.data.RowFinder;
import com.vzhilin.dbview.db.schema.Table;
import com.vzhilin.dbview.tree.ToOneNode;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;

public class MainWindowController {
    @FXML
    private ComboBox<ConnectionSettings> cbConnection;

    @FXML
    private TreeTableView<TreeTableNode> treeTable;

    @FXML
    private TreeTableColumn<TreeTableNode, String> itemColumn;

    @FXML
    private TreeTableColumn<TreeTableNode, String> valueColumn;

    @FXML
    private TreeTableColumn<TreeTableNode, Row> meaningfulValueColumn;

    @FXML
    private Button findButton;

    @FXML
    private TextField textField;

    /**
     *
     */
    private Property<DbContext> ctxProperty;
    private Settings settings;

    private Stage ownerWindow;

    @FXML
    private void initialize() {
        treeTable.setEditable(true);
//        treeTable.setFixedCellSize(30);
        itemColumn.setCellValueFactory(v -> v.getValue().getValue().itemColumnProperty());
        valueColumn.setCellValueFactory(v -> v.getValue().getValue().valueColumnProperty());
//        valueColumn.setCellValueFactory(v -> v.getValue().getValue().meaningfulValueColumnProperty());
        meaningfulValueColumn.setCellValueFactory(v -> v.getValue().getValue().meaningfulValueColumnProperty());

//        meaningfulValueColumn.setCellFactory(v -> new TextFieldTreeTableCell<>());
        meaningfulValueColumn.setCellFactory(param -> new TreeTableMeaningCell());
        meaningfulValueColumn.setEditable(true);

        treeTable.setShowRoot(false);

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
    }

    @FXML
    private void onFindAction() throws SQLException {
        TreeItem<TreeTableNode> newRoot = new TreeItem<>(new TreeTableNode("ROOT", "ROOT", null));
        QueryContext queryContext = new QueryContext(ctxProperty.getValue(), cbConnection.getValue());
        for (Row r: new RowFinder(queryContext).find(textField.getText())) {
            Table table = r.getTable();

            TreeTableNode newNode = new TreeTableNode(table.getName(), String.valueOf(r.getField(table.getPk())), r);
            newRoot.getChildren().add(new ToOneNode(r, newNode));
        }

        treeTable.setRoot(newRoot);
        if (newRoot.getChildren().size() == 1) {
            newRoot.getChildren().get(0).setExpanded(true);
        }
    }

    @FXML
    private void onConfigAction() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/settings-dialog.fxml"));
        Parent root = loader.load();
        SettingsController controller = loader.getController();
        controller.setMainWinController(this);

        Stage stage = new Stage();
        stage.initOwner(ownerWindow);
        stage.initModality(Modality.WINDOW_MODAL);
        setIcon(stage);
        controller.setSettings(settings);
        Scene scene = new Scene(root, 800, 800);
        scene.getStylesheets().add
                (getClass().getResource("/styles/connection-settings.css").toExternalForm());
        stage.setTitle("Settings");
        stage.setScene(scene);

        stage.show();
    }


    private void setIcon(Stage stage) {
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/logo.png")));
    }

    public void setCtx(Property<DbContext> ctxProperty) {
        this.ctxProperty = ctxProperty;
    }

    public void show(Row r) {
        Table table = r.getTable();
        ToOneNode root = new ToOneNode(r, new TreeTableNode(table.getName(), String.valueOf(r.getField(table.getPk())), r));
        treeTable.setRoot(root);
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
}
