package me.vzhilin.dbview.ui;

import com.google.common.collect.Iterables;
import me.vzhilin.dbview.ui.conf.ConnectionSettings;
import me.vzhilin.dbview.ui.conf.Settings;
import me.vzhilin.dbview.db.QueryContext;
import me.vzhilin.dbview.db.data.Row;
import me.vzhilin.dbview.db.export.DataExport;
import me.vzhilin.dbview.ui.settings.SettingsController;
import com.vzhilin.dbview.ui.tree.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import me.vzhilin.dbview.ui.tree.Paging;
import me.vzhilin.dbview.ui.tree.ToOneNode;
import me.vzhilin.dbview.ui.tree.TreeTableMeaningCell;
import me.vzhilin.dbview.ui.tree.TreeTableNode;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
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
    private TreeTableColumn<TreeTableNode, String> valueColumn;

    @FXML
    private TreeTableColumn<TreeTableNode, Row> meaningfulValueColumn;

    @FXML
    private TextField textField;

    private Settings settings;

    private Stage ownerWindow;

    /** Контекст приложения */
    private ApplicationContext appContext;

    @FXML
    private void initialize() {
        treeTable.setRowFactory(new Callback<TreeTableView<TreeTableNode>, TreeTableRow<TreeTableNode>>() {
            @Override
            public TreeTableRow<TreeTableNode> call(TreeTableView<TreeTableNode> param) {
                TreeTableRow<TreeTableNode> row = new TreeTableRow<TreeTableNode>();

                MenuItem exportItem = new MenuItem("Build query");
                exportItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ObservableList<TreeItem<TreeTableNode>> selectedItems = treeTable.getSelectionModel().getSelectedItems();
                        new DataExport().export(selectedItems);
                    }
                });

                ContextMenu rowMenu = new ContextMenu(exportItem);

                // only display context menu for non-null items:
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                .then(rowMenu)
                                .otherwise((ContextMenu)null));
                return row;
            }
        });

        treeTable.setEditable(true);
        treeTable.setShowRoot(false);
        treeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        itemColumn.setCellValueFactory(v -> v.getValue().getValue().itemColumnProperty());
        itemColumn.setSortable(false);
        itemColumn.setCellFactory(param -> new ItemTreeTableCell());

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
    }

    @FXML
    private void onFindAction() throws SQLException, ExecutionException {
        TreeItem<TreeTableNode> newRoot = new TreeItem<>(new TreeTableNode("ROOT", "ROOT", null));
        ConnectionSettings connection = cbConnection.getValue();

        QueryContext queryContext = appContext.newQueryContext(connection.getConnectionName());
        ObservableList<TreeItem<TreeTableNode>> ch = newRoot.getChildren();

        Iterator<Row> it = queryContext.getDataDigger().find(textField.getText()).iterator();
        new Paging().addNodes(it, ch);

        treeTable.setRoot(newRoot);
        if (ch.size() == 1) {
            ch.get(0).setExpanded(true);
        }
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

    public ApplicationContext getAppContext() {
        return appContext;
    }

    private static class ItemTreeTableCell extends TreeTableCell<TreeTableNode, String> {
        @Override protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);

          if (item == null || empty) {
              super.setText(null);
              super.setGraphic(null);
          } else {
              TreeItem<TreeTableNode> treeItem = getTreeTableRow().getTreeItem();
              if (treeItem instanceof ToOneNode) {
                  HBox hBox = new HBox();
                  ObservableList<Node> ch = hBox.getChildren();
                  ch.add(new Label(item));
                  Region r = new Region();
                  HBox.setHgrow(r, Priority.ALWAYS);
                  ch.add(r);
                  Row row = ((ToOneNode) treeItem).getRow();
                  ch.add(new Label(row.getTable().getName()));

                  setGraphic(hBox);
              } else
              if (treeItem instanceof Paging.PagingItem){
                  HBox hBox = new HBox();
                  ObservableList<Node> ch = hBox.getChildren();
                  Button loadMoreButton = new Button("More...");
                  loadMoreButton.setOnAction((Paging.PagingItem) treeItem);
                  ch.add(loadMoreButton);
                  setGraphic(hBox);
              } else {
                  setGraphic(new Label(item));
              }
          }
      }
    }
}
