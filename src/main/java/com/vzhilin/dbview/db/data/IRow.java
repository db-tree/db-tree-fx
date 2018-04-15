package com.vzhilin.dbview.db.data;

import java.util.Map;

import com.vzhilin.dbview.db.schema.Table;

public interface IRow {
    /**
     * @return RowTable
     */
    Table getTable();

    /**
     * @return references
     */
    Map<String, Row> references();

    /**
     * @return inverseReferences
     */
    Map<Map.Entry<Table, String>, Long> inverseReferencesCount();

    /**
     * @param column field
     * @return field
     */
    Object getField(String column);

    /**
     *
     * @return
     */
    String meaningfulValue();
}
