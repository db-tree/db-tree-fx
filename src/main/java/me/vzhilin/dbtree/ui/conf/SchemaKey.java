package me.vzhilin.dbtree.ui.conf;

import java.util.Objects;

public final class SchemaKey {
    private final String schemaName;

    public SchemaKey(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchemaKey schemaKey = (SchemaKey) o;
        return schemaName.equals(schemaKey.schemaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaName);
    }
}
