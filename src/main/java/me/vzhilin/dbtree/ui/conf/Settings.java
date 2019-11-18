package me.vzhilin.dbtree.ui.conf;

import com.github.javakeyring.BackendNotSupportedException;
import com.github.javakeyring.Keyring;
import com.github.javakeyring.PasswordAccessException;
import com.google.common.base.Joiner;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;
import org.hildan.fxgson.FxGson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;

public class Settings {
    private static final String DB_TREE_JSON_TEMP = ".db-tree.json~";
    private final static Logger LOG = Logger.getLogger(Settings.class);
    private final static String OS = System.getProperty("os.name").toLowerCase();
    private static final String DB_TREE_JSON = ".db-tree.json";
    private static final String APP_ID = "me.vzhilin.dbtree";

    private Dimensions mainWindow;
    private Dimensions settingsWindow;

    private final DoubleProperty dividerPosition = new SimpleDoubleProperty();
    private final ListProperty<ConnectionSettings> connections = new SimpleListProperty<ConnectionSettings>(FXCollections.observableArrayList(new ArrayList<>()));

    public ObservableList<ConnectionSettings> getConnections() {
        return connections.get();
    }

    public Double getDividerPosition() {
        return dividerPosition.get();
    }

    public DoubleProperty dividerPositionProperty() {
        return dividerPosition;
    }

    public ListProperty<ConnectionSettings> connectionsProperty() {
        return connections;
    }

    public synchronized void save() {
        File folder = getOrCreateFolder();

        savePasswords(folder);
        saveData(folder);
    }

    private void saveData(File folder) {
        try {
            File configFile = new File(folder, DB_TREE_JSON);
            File tempFile = new File(folder, DB_TREE_JSON_TEMP);

            PrintWriter pw = new PrintWriter(tempFile);
            pw.write(FxGson.coreBuilder().setPrettyPrinting().enableComplexMapKeySerialization().create().toJson(this));
            pw.close();
            configFile.delete();
            tempFile.renameTo(configFile);
        } catch (FileNotFoundException e) {
            LOG.error(e, e);
        }
    }

    private void savePasswords(File folder) {
        Keyring keyring;
        try {
            keyring = Keyring.create();
        } catch (BackendNotSupportedException e) {
            throw new RuntimeException(e);
        }

        try {
            for (ConnectionSettings conn: connections) {
                keyring.setPassword(APP_ID, conn.getConnectionName(), conn.getPassword());
            }
        } catch (PasswordAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static File getOrCreateFolder() {
        File folder = getFolder();
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                LOG.error("unable to create folder: " + folder.getAbsolutePath());
            }
        }
        return folder;
    }

    public ConnectionSettings getConnection(String connectionName) {
        return connections.filtered(c -> c.getConnectionName().equals(connectionName)).get(0);
    }

    public static Settings readSettings() {
        try {
            File folder = getOrCreateFolder();
            Settings settings = readData(folder);
            readPasswords(settings);
            return settings;
        } catch (IOException e) {
            LOG.error(e, e);
        }
        return new Settings();
    }

    private static Settings readData(File folder) throws IOException {
        File configFile = new File(folder, ".db-tree.json");
        return FxGson.create().fromJson(Joiner.on("").join(Files.readAllLines(configFile.toPath())), Settings.class);
    }

    private static void readPasswords(Settings settings) {
        Keyring keyring;
        try {
            keyring = Keyring.create();
        } catch (BackendNotSupportedException e) {
            throw new RuntimeException(e);
        }

        try {
            for (ConnectionSettings conn: settings.getConnections()) {
                String password = keyring.getPassword(APP_ID, conn.getConnectionName());
                conn.passwordProperty().set(password);
            }
        } catch (PasswordAccessException e) {
            LOG.error(e, e);
        }
    }

    private static File getFolder() {
        if (OS.contains("win")) {
            return new File(new File(System.getenv("APPDATA")), "DBTree");
        } else
        if (OS.contains("mac")) {
            return new File(new File(System.getenv("HOME")), "Library/Preferences/" + APP_ID);
        } else
        if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")){
            return new File(System.getenv("HOME"));
        } else {
            return new File(".");
        }
    }

    public Dimensions getMainWindow() {
        return mainWindow;
    }

    public Dimensions getSettingsWindow() {
        return settingsWindow;
    }

    public void setMainWindowWidth(int width) {
        if (mainWindow == null) {
            mainWindow = new Dimensions();
        }

        mainWindow.width = width;
    }

    public void setMainWindowHeight(int height) {
        if (mainWindow == null) {
            mainWindow = new Dimensions();
        }

        mainWindow.height = height;
    }

    public void setSettingsWindowWidth(int width) {
        if (settingsWindow == null) {
            settingsWindow = new Dimensions();
        }

        settingsWindow.width = width;
    }

    public void setSettingsWindowHeight(int height) {
        if (settingsWindow == null) {
            settingsWindow = new Dimensions();
        }

        settingsWindow.height = height;
    }

    public void setSettingsWindow(Dimensions settingsWindow) {
        this.settingsWindow = settingsWindow;
    }

    public void setMainWindow(Dimensions mainWindow) {
        this.mainWindow = mainWindow;
    }

    public static class Dimensions {
        public int width;
        public int height;
    }
}
