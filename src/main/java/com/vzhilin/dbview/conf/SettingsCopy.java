package com.vzhilin.dbview.conf;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;

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
            Sets.difference(newHashSet(dirtyConnections), newHashSet(connSettingsMapping.values()));
        originalConnections.addAll(newValues);

        Set<ConnectionSettings> removedValues =
            Sets.difference(newHashSet(connSettingsMapping.values()), newHashSet(dirtyConnections));

        for (ConnectionSettings removed: removedValues) {
            originalConnections.remove(connSettingsMapping.inverse().get(removed));
        }
    }

    private void writeMeaningful(ConnectionSettings orig, ConnectionSettings modif) {
        Map<String, Map<String, BooleanProperty>> om = orig.getLookupableColumns();
        Map<String, Map<String, BooleanProperty>> mm = modif.getLookupableColumns();

        for (String table: Sets.union(om.keySet(), mm.keySet())) {
            if (!om.containsKey(table)) {
                om.put(table, Maps.newLinkedHashMap());
            }

            Map<String, BooleanProperty> oom = om.get(table);
            Map<String, BooleanProperty> mmm = mm.get(table);

            for (String column: Sets.union(oom.keySet(), mmm.keySet())) {
                if (!oom.containsKey(column)) {
                    oom.put(column, new SimpleBooleanProperty());
                }

                oom.get(column).set(mmm.get(column).getValue());
            }
        }
    }

    private void writeTemplates(ConnectionSettings orig, ConnectionSettings modif) {
        Map<String, Template> origMap =
            orig.templatesProperty().stream().collect(Collectors.toMap(Template::getTableName, t -> t));

        Map<String, Template> dirtyMap =
                modif.templatesProperty().stream().collect(Collectors.toMap(Template::getTableName, t -> t));

        for (String name: Sets.union(origMap.keySet(), dirtyMap.keySet())) {
            if (origMap.containsKey(name)) {
                origMap.get(name).templateProperty().setValue(dirtyMap.get(name).getTemplate());
            } else {
                orig.templatesProperty().add(new Template(name, dirtyMap.get(name).getTemplate()));
            }
        }
    }

    private void writeConnection(ConnectionSettings orig, ConnectionSettings modif) {
        orig.connectionNameProperty().set(modif.getConnectionName());
        orig.driverClassProperty().set(modif.getDriverClass());
        orig.jdbcUrlProperty().set(modif.getJdbcUrl());
        orig.usernameProperty().set(modif.getUsername());
        orig.passwordProperty().set(modif.getPassword());
    }

    public Settings getCopy() {
        return copy;
    }
}
