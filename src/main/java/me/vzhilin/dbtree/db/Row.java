package me.vzhilin.dbtree.db;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import me.vzhilin.dbtree.db.schema.Table;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;

import static java.lang.String.format;

/**
 * Строка таблицы
 */
public class Row {
    /** Логгер */
    private final static Logger LOG = Logger.getLogger(Row.class);
    private static final String COUNT = "SELECT COUNT(1) as C FROM %s WHERE %s = %d";
    private static final String ROW = "SELECT %s from %s where %s = ?";

    /** Контекст БД */
    private final DbContext ctx;

    /** Контекст запроса */
    private final QueryContext queryContext;

    /** Таблица */
    private final Table table;

    /** Первичный ключ*/
    private final long pk;

    /** Колонки */
    private Map<String, Object> rowData = null;

    /** Прямые ссылки */
    private Map<String, Row> references = null;

    /** Обратные ссылки */
    private Multimap<Map.Entry<Table, String>, Row> invRows = null;

    /** Количество обратных ссылок */
    private Map<Map.Entry<Table, String>, Long> invReferencesCount = null;

    /**
     * Строка таблицы
     * @param queryContext контекст запрос
     * @param table таблица
     * @param pk первичный ключ
     */
    public Row(QueryContext queryContext, Table table, long pk) {
        this.queryContext = queryContext;
        this.ctx = queryContext.getDbContext();
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
                String query = format(ROW, columns, table.getName(), table.getPk());
                rowData = getRunner().query(query, new MapHandler(), pk);
            }
        } catch (SQLException ex) {
            LOG.error(ex, ex);
        }
    }

    protected QueryRunner getRunner() {
        return ctx.getRunner();
    }

    /**
     * Column name --> Row
     * @return
     * @throws SQLException
     */
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

    /**
     * @return table -&gt; FK column -&gt; count
     */
    public Map<Map.Entry<Table, String>, Long> reverseReferencesCount() {
        try {
            if (invReferencesCount == null) {
                invReferencesCount = Maps.newLinkedHashMap();

                for (Map.Entry<Table, String> e: table.getBackRelations().entries()) {
                    Table table = e.getKey();
                    String tableName = table.getName();
                    String query = format(COUNT, tableName, e.getValue(), pk);

                    for (Map<String, Object> m: getRunner().query(query, new MapListHandler())) {
                        long count = ((BigDecimal) m.get("C")).longValue();
                        invReferencesCount.put(e, count);
                    }
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex, ex);
        }

        return invReferencesCount;
    }

    public Iterable<Row> getInverseReference(Map.Entry<Table, String> relation) {
        return queryContext.getDataDigger().reverseReferences(this, relation.getKey(), relation.getValue());
    }

    @Override public String toString() {
        return table + ": " + pk;
    }

    public Table getTable() {
        return table;
    }

    public Object getField(String column) {
        ensureLoaded();
        return rowData.get(column);
    }

    public long getPk() {
        return pk;
    }
}
