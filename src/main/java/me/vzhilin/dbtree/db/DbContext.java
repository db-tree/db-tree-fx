package me.vzhilin.dbtree.db;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import me.vzhilin.dbtree.db.schema.Schema;
import me.vzhilin.dbtree.db.schema.SchemaFactory;
import me.vzhilin.dbtree.db.schema.Table;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import javax.sql.DataSource;
import java.io.Closeable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static java.lang.String.format;

public final class DbContext implements Closeable {
    private static final String ROW = "SELECT %s from %s where %s = ?";
    private static final String COUNT = "SELECT COUNT(1) as C FROM %s WHERE %s = %d";

    private final Connection connection;
    private Schema schema;
    private QueryRunner runner;

    public DbContext(String driverClazz, String jdbcUrl, String login, String password, String pattern) throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClazz);
        ds.setUrl(jdbcUrl);
        ds.setUsername(login);
        ds.setPassword(password);
        connection = ds.getConnection();

        runner = new WrappedQueryRunner(connection);
        schema = new SchemaFactory(ds, pattern).load();
    }

    public Schema getSchema() {
        return schema;
    }

    private QueryRunner getRunner() {
        return runner;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public Map<String, Object> fetchColumns(Row row) throws SQLException {
        Table table = row.getTable();
        String columns = Joiner.on(',').join(table.getColumns());
        String query = format(ROW, columns, table.getName(), table.getPk());
        return getRunner().query(query, new MapHandler(), row.getPk());
    }

    public Map<Map.Entry<Table, String>, Long> getReverseReferencesCount(Row row) throws SQLException {
        Map<Map.Entry<Table, String>, Long> invReferencesCount = Maps.newLinkedHashMap();
        for (Map.Entry<Table, String> e: row.getTable().getBackRelations().entries()) {
            Table table = e.getKey();
            String tableName = table.getName();
            String query = format(COUNT, tableName, e.getValue(), row.getPk());
            for (Map<String, Object> m: getRunner().query(query, new MapListHandler())) {
                long count = ((BigDecimal) m.get("C")).longValue();
                invReferencesCount.put(e, count);
            }
        }
        return invReferencesCount;
    }

    private final static class WrappedQueryRunner extends QueryRunner {
        private final Connection conn;

        private WrappedQueryRunner(Connection connection) {
            this.conn = connection;
        }

        @Override
        public <T> T query(String sql, ResultSetHandler<T> rsh, Object... params) throws SQLException {
            return super.query(conn, sql, rsh, params);
        }

        @Override
        public <T> T query(String sql, ResultSetHandler<T> rsh) throws SQLException {
            return super.query(conn, sql, rsh);
        }

        @Override
        public DataSource getDataSource() {
            throw new UnsupportedOperationException();
        }
    }
}
