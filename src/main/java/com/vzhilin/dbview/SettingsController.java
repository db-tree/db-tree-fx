package com.vzhilin.dbview;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.conf.SettingsCopy;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Set;

public class SettingsController {
    private final static Logger LOG = Logger.getLogger(SettingsController.class);

    @FXML
    private TreeView<SettingNode> settingView;

    @FXML
    private StackPane stackPane;

    @FXML
    private Button okButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button addButton;

    @FXML
    private Button removeButton;

    @FXML
    private Button copyButton;

    private Settings settings;

    private SettingsCopy settingsCopy;

    private SettingsWindowApp mainApp;

    private TreeItem<SettingNode> rootNode;
    private TreeItem<SettingNode> connectionsNode;
    private MainWindowController mainController;
    private ApplicationContext appContext;


    public SettingsController() {
    }

    @FXML
    private void initialize() {
        rootNode = new TreeItem<>(new RootNode());
        connectionsNode = new TreeItem<>(new ConnectionsNode());;

        rootNode.getChildren().add(connectionsNode);
        settingView.setRoot(rootNode);

        rootNode.setExpanded(true);
        connectionsNode.setExpanded(true);


        // -----------------
        ReadOnlyObjectProperty<TreeItem<SettingNode>> selectedItemProperty = settingView.getSelectionModel().selectedItemProperty();
        selectedItemProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                addButton.setDisable(true);
                removeButton.setDisable(true);
                copyButton.setDisable(true);

                return;
            }

            SettingNode v = newValue.getValue();
            addButton.setDisable(!(v instanceof ConnectionsNode));
            removeButton.setDisable(!(v instanceof ConnectionSettingNode));
            copyButton.setDisable(!(v instanceof ConnectionSettingNode));
        });

        selectedItemProperty.addListener(new ChangeListener<TreeItem<SettingNode>>() {
            @Override public void changed(ObservableValue<? extends TreeItem<SettingNode>> observable,
                    TreeItem<SettingNode> oldValue, TreeItem<SettingNode> newValue) {

                stackPane.getChildren().clear();

                if (newValue == null) {
                    return;
                }

                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("/connection-settings-pane.fxml"));

                try {
                    SettingNode value = newValue.getValue();
                    if (value instanceof ConnectionSettingNode) {
                        ConnectionSettings settings = ((ConnectionSettingNode) value).getSettings();

                        Parent root = loader.load();
                        ConnectionSettingsController controller = loader.getController();
                        controller.setAppContext(appContext);
                        controller.setConnectoinName(newValue.getValue().toString());
                        controller.bindSettings(settings);

                        settings.connectionNameProperty().set(newValue.getValue().toString());
                        stackPane.getChildren().add(root);
                    }

                } catch (IOException e) {
                    LOG.error(e, e);
                }
            }
        });

        setupButtons();
    }

    private TreeItem<SettingNode> newItem(ConnectionSettings settings) {
        TreeItem<SettingNode> item = new TreeItem<>(new ConnectionSettingNode(settings));
        settings.connectionNameProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                item.valueProperty().setValue(new ConnectionSettingNode(settings));
            }
        });

        return item;
    }

    @FXML
    public void onAddButton() {
        ConnectionSettings newConnection = new ConnectionSettings();
        newConnection.connectionNameProperty().set("New connection");
        settings.connectionsProperty().add(newConnection);

        ObservableList<TreeItem<SettingNode>> ch = connectionsNode.getChildren();
        TreeItem<SettingNode> newTreeNode = ch.get(ch.size() - 1);
        settingView.getSelectionModel().select(newTreeNode);
    }

    @FXML
    public void onRemoveButton() {
        TreeItem<SettingNode> selectedItem = settingView.getSelectionModel().getSelectedItem();
        settings.getConnections().remove(((ConnectionSettingNode) selectedItem.getValue()).getSettings());
    }

    @FXML
    public void onCopyButton() {
        Set<String> names = Sets.newHashSet();
        settings.getConnections().forEach(c -> names.add(c.getConnectionName()));

        TreeItem<SettingNode> selectedItem = settingView.getSelectionModel().getSelectedItem();
        ConnectionSettings s = ((ConnectionSettingNode) selectedItem.getValue()).getSettings();

        ConnectionSettings copy = new ConnectionSettings(s);
        settings.getConnections().add(copy);
        int i = 1;
        while (names.contains(copy.getConnectionName())) {
            copy.connectionNameProperty().set(s.getConnectionName() + " (" + i + ")");
        }

        String newName = copy.getConnectionName();
        ObservableList<TreeItem<SettingNode>> ch = selectedItem.getParent().getChildren();
        settingView.getSelectionModel().select(ch.filtered(n -> newName.equals(((ConnectionSettingNode) n.getValue())
            .settings.getConnectionName())).get(0));
    }

    @FXML
    public void onOkButton() {
        settingsCopy.apply();
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
        mainController.refreshTreeView();
        settings.save();
    }

    @FXML
    public void onCancelButton() {
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    private void setupButtons() {
        addButton.setDisable(true);
        removeButton.setDisable(true);
        copyButton.setDisable(true);
    }

    public void setMainApp(SettingsWindowApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setSettings(Settings src) {
        this.settingsCopy = new SettingsCopy(src);
        this.settings = settingsCopy.getCopy();

        settings.connectionsProperty().addListener((ListChangeListener<ConnectionSettings>) c -> {
            while (c.next()) {
                for (ConnectionSettings newSettings: c.getAddedSubList()) {
                    connectionsNode.getChildren().add(newItem(newSettings));
                }

                for (ConnectionSettings removedSettings: c.getRemoved()) {
                    for (TreeItem<SettingNode> ch: connectionsNode.getChildren()) {
                        if (((ConnectionSettingNode) ch.getValue()).getSettings() == removedSettings) {
                            connectionsNode.getChildren().remove(ch);
                            break;
                        }
                    }
                }
            }
        });

        for (ConnectionSettings cs: settings.getConnections()) {
            connectionsNode.getChildren().add(newItem(cs));
        }


        TreeItem<SettingNode> first = Iterables.getFirst(connectionsNode.getChildren(), null);
        if (first != null) {
            settingView.getSelectionModel().select(first);
        }
    }

    public void setMainWinController(MainWindowController mainWindowController) {
        this.mainController = mainWindowController;
    }

    public void setAppContext(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    private static class SettingNode {

    }

    private static class RootNode extends SettingNode {
        @Override public String toString() {
            return "Settings";
        }
    }

    private static class ConnectionsNode extends SettingNode {
        @Override public String toString() {
            return "Connections";
        }
    }

    private static class ConnectionSettingNode extends SettingNode {
        private final ConnectionSettings settings;

        public ConnectionSettingNode(ConnectionSettings settings) {
            this.settings = settings;
        }

        @Override public String toString() {
            return settings.getConnectionName();
        }

        public ConnectionSettings getSettings() {
            return settings;
        }
    }
}
