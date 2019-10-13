package me.vzhilin.dbtree.ui.conf;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.Set;

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
            writeMeaningful(orig, modif);
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

    private void writeMeaningful(ConnectionSettings orig, ConnectionSettings modif) {
        orig.setLookupableColumns(modif.getLookupableColumns());
    }

    private void writeTemplates(ConnectionSettings orig, ConnectionSettings modif) {
//        Map<String, Template> origMap =
//            orig.templatesProperty().stream().collect(Collectors.toMap(Template::getTableName, t -> t));
//
//        Map<String, Template> dirtyMap =
//                modif.templatesProperty().stream().collect(Collectors.toMap(Template::getTableName, t -> t));
//
//        Set<String> forRemove = Sets.newLinkedHashSet();
//        for (String name: Sets.union(origMap.keySet(), dirtyMap.keySet())) {
//            if (origMap.containsKey(name)) {
//                if (dirtyMap.containsKey(name)) {
//                    origMap.get(name).templateProperty().setValue(dirtyMap.get(name).getTemplate());
//                } else {
//                    forRemove.add(name);
//                }
//            } else {
//                orig.templatesProperty().add(new Template(name, dirtyMap.get(name).getTemplate()));
//            }
//        }
//
//        for (String name: forRemove) {
//            origMap.remove(name);
//        }
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
