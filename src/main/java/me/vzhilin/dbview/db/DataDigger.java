package me.vzhilin.dbview.db;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import me.vzhilin.dbview.db.data.Row;
import me.vzhilin.dbview.db.schema.Schema;
import me.vzhilin.dbview.db.schema.Table;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.lang.String.format;

public class DataDigger implements Closeable {
    private final static Logger LOG = Logger.getLogger(DataDigger.class);
    private final QueryContext queryContext;
    private final Schema schema;
    private final List<RowIterator> openedIterators = Lists.newArrayList();

    DataDigger(QueryContext ctx) {
        this.queryContext = ctx;
        this.schema = ctx.getDbContext().getSchema();
    }

    public Iterable<Row> find(String text) {
        boolean isDigit = text.matches("\\d+");
        String template = "SELECT '%1$s' as tableName, %2$s as PK FROM %1$s where %3$s = :1";

        // table --> [column]
        List<String> queries = Lists.newArrayList();
        for (String tableName: schema.allTableNames()) {
            Table t = schema.getTable(tableName);
            for (String columnName: t.getColumns()) {
                if (queryContext.getSettings().isLookupable(tableName, columnName)) {
                    if (isDigit || !t.getPk().equals(columnName)) {
                        queries.add(String.format(template, tableName, t.getPk(), columnName));
                    }
                }
            }
        }

        if (queries.isEmpty()) {
            return EmptyIterator::new;
        }

        Object[] params = Lists.newArrayList(Iterators.limit(Iterators.cycle(text), queries.size())).toArray();
        return query(Joiner.on(" UNION ALL ").join(queries), params);
    }

    public Iterable<Row> inverseReferences(Row row, Table table, String column) {
        String query = format("select %s as pk FROM %s WHERE %s = ?", table.getPk(), table.getName(), column);
        RowIterator rowIterator = new RowIterator(queryContext, query, row.getPk()) {
            @Override
            protected Row convertRsToRow(ResultSet rs) throws SQLException {
                BigDecimal pk = rs.getBigDecimal(1);
                return new Row(queryContext, table, pk.longValue());
            }
        };

        openedIterators.add(rowIterator);
        return () -> rowIterator;
    }

    private Iterable<Row> query(String query, Object... params) {
        RowIterator rowIterator = new RowIterator(queryContext, query, params);
        openedIterators.add(rowIterator);
        return () -> rowIterator;
    }

    @Override
    public void close() {
        for (RowIterator it: openedIterators) {
            try {
                it.close();
            } catch (Exception e) {
                LOG.error(e, e);
            }
        }
    }

    private static class RowIterator implements Iterator<Row>, Closeable {
        private final Connection conn;
        private final PreparedStatement st;
        private final ResultSet rs;
        private final QueryContext qc;
        private boolean hasNext;

        public RowIterator(QueryContext qc, String query, Object... params) {
            this.qc = qc;
            try {
                conn = qc.getDbContext().getConnection();
                st = conn.prepareStatement(query);
                for (int i = 0; i < params.length; i++) {
                    st.setObject(i + 1, params[i]);
                }
                rs = st.executeQuery();
                hasNext = rs.next();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public Row next() {
            try {
                if (hasNext) {
                    Row row = convertRsToRow(rs);
                    hasNext = rs.next();
                    return row;
                } else {
                    hasNext = false;
                    close();
                    throw new NoSuchElementException();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        protected Row convertRsToRow(ResultSet rs) throws SQLException {
            String tableName = rs.getString(1);
            BigDecimal pk = (BigDecimal) rs.getObject(2);
            return new Row(qc, qc.getDbContext().getSchema().getTable(tableName), pk.longValue());
        }

        @Override
        public void close() {
            try {
                hasNext = false;
                rs.close();
                st.close();
            } catch (SQLException e) {
                LOG.error(e, e);
            }
        }
    }

    private static class EmptyIterator implements Iterator<Row> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Row next() {
            return null;
        }
    }
}
