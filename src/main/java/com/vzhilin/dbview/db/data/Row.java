package com.vzhilin.dbview.db.data;

import com.google.common.base.Joiner;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.schema.Table;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class Row implements IRow {
    private final static Logger LOG = Logger.getLogger(Row.class);
    private final DbContext ctx;
    private final QueryRunner runner;

    private final Table table;
    private final long pk;
    private final QueryContext queryContext;

    private Map<String, Object> rowData = null;
    private Multimap<Map.Entry<Table, String>, Row> invRows = null;

    private Map<String, Row> references = null;
    private Map<Map.Entry<Table, String>, Long> invReferencesCount = null;

    public Row(QueryContext queryContext, Table table, long pk) {
        this.queryContext = queryContext;
        this.ctx = queryContext.getDbContext();
        this.runner = ctx.getRunner();
        this.table = table;
        this.pk = pk;
    }

    public QueryContext getContext() {
        return queryContext;
    }

    private void ensureLoaded() {
        try {
            if (rowData == null) {
                String columns = Joiner.on(',').join(table.getColumns());
                String query = format("SELECT %s from %s where %s = ?", columns, table.getName(), table.getPk());
                rowData = runner.query(query, new MapHandler(), pk);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Column name --> Row
     * @return
     * @throws SQLException
     */
    @Override
    public Map<String, Row> references()  {
        ensureLoaded();

        if (references == null) {
            references = Maps.newLinkedHashMap();
            for (Map.Entry<String, Table> t: table.getRelations().entrySet()) {
                BigDecimal fk = (BigDecimal) rowData.get(t.getKey());

                if (fk != null) {
                    references.put(t.getKey(), new Row(queryContext, t.getValue(), fk.longValue()));
                }
            }
        }

        return references;
    }

    @Override
    public Map<Map.Entry<Table, String>, Long> inverseReferencesCount() {
        try {
            if (invReferencesCount == null) {
                invReferencesCount = Maps.newLinkedHashMap();

                for (Map.Entry<Table, String> e: table.getBackRelations().entries()) {
                    Table table = e.getKey();
                    String tableName = table.getName();
                    String query = format("SELECT COUNT(1) as C FROM %s WHERE %s = %d", tableName, e.getValue(), pk);

                    for (Map<String, Object> m: runner.query(query, new MapListHandler())) {
                        long count = ((BigDecimal) m.get("C")).longValue();
                        invReferencesCount.put(e, count);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return invReferencesCount;
    }

    public Multimap<Map.Entry<Table, String>, Row> inverseReferences()  {
        try {
            if (invRows == null) {
                invRows = LinkedHashMultimap.create();
                List<String> queries = Lists.newArrayList();

                for (Map.Entry<Table, String> e: table.getBackRelations().entries()) {
                    Table table = e.getKey();
                    String tableName = table.getName();
                    queries.add(format("select '%s' as table_name, '%s' as column_name, %s as pk FROM %s WHERE %s = :pm", tableName, e.getValue(), table.getPk(), tableName, e.getValue()));
                }

                String q = Joiner.on(" UNION ALL ").join(queries);
                for (Map<String, Object> m: runner.query(q, new MapListHandler(), Collections.nCopies(queries.size(), pk).toArray())) {
                    String tb = (String) m.get("TABLE_NAME");
                    String col = (String) m.get("COLUMN_NAME");
                    long pk = ((BigDecimal) m.get("PK")).longValue();

                    Table ta = ctx.getSchema().getTable(tb);
                    invRows.put(Maps.immutableEntry(ta, col), new Row(queryContext, ta, pk));
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex, ex);
        }

        return invRows;
    }

    @Override public String toString() {
        return table + ": " + pk;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public Object getField(String column) {
        ensureLoaded();

        return rowData.get(column);
    }

    @Override
    public String meaningfulValue() {
        return queryContext.getMeanintfulValue(this);
    }

    public long getPk() {
        return pk;
    }
}
