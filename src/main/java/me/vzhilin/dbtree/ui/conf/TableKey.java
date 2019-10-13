package me.vzhilin.dbtree.ui.conf;

import java.util.Objects;

public final class TableKey {
    private final SchemaKey schemaKey;
    private final String tableName;

    public TableKey(SchemaKey schemaKey, String tableName) {
        this.schemaKey = schemaKey;
        this.tableName = tableName;
    }

    public SchemaKey getSchemaKey() {
        return schemaKey;
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableKey tableKey = (TableKey) o;
        return schemaKey.equals(tableKey.schemaKey) &&
                tableName.equals(tableKey.tableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaKey, tableName);
    }
}
