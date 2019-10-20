package me.vzhilin.dbtree.ui.conf;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import javafx.collections.ObservableList;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class SettingsCopy {
    private final Settings copy;

    // original --> modified
    private final BiMap<ConnectionSettings, ConnectionSettings> connSettingsMapping = HashBiMap.create();
    private final Settings original;

    public SettingsCopy(Settings original) {
        this.original = original;
        this.copy = new Settings();

        for (ConnectionSettings cs: original.getConnections()) {
            ConnectionSettings newCs = new ConnectionSettings(cs);
            copy.getConnections().add(newCs);

            connSettingsMapping.put(cs, newCs);
        }
    }

    public void apply() {
        for (Map.Entry<ConnectionSettings, ConnectionSettings> e: connSettingsMapping.entrySet()) {
            ConnectionSettings orig = e.getKey();
            ConnectionSettings modif = e.getValue();

            writeConnection(orig, modif);
            writeTemplates(orig, modif);
            writeToStringExp(orig, modif);
        }

        ObservableList<ConnectionSettings> originalConnections = original.getConnections();
        ObservableList<ConnectionSettings> dirtyConnections = copy.getConnections();

        Set<ConnectionSettings> newValues =
            Sets.difference(Sets.newHashSet(dirtyConnections), Sets.newHashSet(connSettingsMapping.values()));
        originalConnections.addAll(newValues);

        Set<ConnectionSettings> removedValues =
            Sets.difference(Sets.newHashSet(connSettingsMapping.values()), Sets.newHashSet(dirtyConnections));

        for (ConnectionSettings removed: removedValues) {
            originalConnections.remove(connSettingsMapping.inverse().get(removed));
        }
    }

    private void writeToStringExp(ConnectionSettings orig, ConnectionSettings modif) {
        orig.setLookupableColumns(modif.getLookupableColumns());
    }

    private void writeTemplates(ConnectionSettings orig, ConnectionSettings modif) {
        Map<TableKey, Template> origMap = new LinkedHashMap<>();
        orig.templatesProperty().forEach(t -> origMap.put(t.getTableKey(), t));

        Map<TableKey, Template> dirtyMap = new LinkedHashMap<>();
        modif.templatesProperty().forEach(t -> dirtyMap.put(t.getTableKey(), t));
        origMap.entrySet().removeIf(e -> !dirtyMap.containsKey(e.getKey()));

        dirtyMap.forEach(new BiConsumer<TableKey, Template>() {
            @Override
            public void accept(TableKey tableKey, Template template) {
                if (!origMap.containsKey(tableKey)) {
                    origMap.put(tableKey, template);
                } else {
                    Template e = origMap.get(tableKey);
                    e.templateProperty().set(template.templateProperty().getValue());
                }
            }
        });
    }

    private void writeConnection(ConnectionSettings orig, ConnectionSettings modif) {
        orig.connectionNameProperty().set(modif.getConnectionName());
        orig.driverClassProperty().set(modif.getDriverClass());
        orig.jdbcUrlProperty().set(modif.getJdbcUrl());
        orig.usernameProperty().set(modif.getUsername());
        orig.passwordProperty().set(modif.getPassword());
        orig.tableNamePatternProperty().set(modif.getTableNamePattern());
    }

    public Settings getCopy() {
        return copy;
    }
}
