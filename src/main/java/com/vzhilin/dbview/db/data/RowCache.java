package com.vzhilin.dbview.db.data;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.SetMultimap;
import com.vzhilin.dbview.db.schema.Table;
import org.apache.commons.dbutils.QueryRunner;

import java.math.BigDecimal;
import java.util.Map;

public class RowCache {
    private final SetMultimap<Table, IRow> shadeRows = LinkedHashMultimap.create();
    private final QueryRunner runner;

    public RowCache(QueryRunner runner) {
        this.runner = runner;
    }

    public IRow getRow(Table table, BigDecimal pk) {
//        runner.query(String.format()"SELECT * FROM %s WHERE %s = %s")
        return new IRow() {

            @Override public Table getTable() {
                return table;
            }

            @Override public Map<String, Row> references() {
                return null;
            }

            @Override public Map<Map.Entry<Table, String>, Long> inverseReferencesCount() {
                return null;
            }

            @Override public Object getField(String column) {
                return null;
            }

            @Override
            public String meaningfulValue() {
                return null;
            }
        };
    }

    private final static class Key {
        private final Table table;
        private final BigDecimal pk;

        private Key(Table table, BigDecimal pk) {
            this.table = table;
            this.pk = pk;
        }
    }
}
