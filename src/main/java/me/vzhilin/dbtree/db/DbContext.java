package me.vzhilin.dbtree.db;

import me.vzhilin.adapter.DatabaseAdapter;
import me.vzhilin.adapter.oracle.OracleDatabaseAdapter;
import me.vzhilin.catalog.Catalog;
import me.vzhilin.catalog.CatalogLoader;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

public final class DbContext implements Closeable {
//    private final Connection connection;
    private final DatabaseAdapter adapter;
    private final Catalog catalog;
    private QueryRunner runner;

    public DbContext(String driverClazz, String jdbcUrl, String login, String password, String pattern) throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClazz);
        ds.setUrl(jdbcUrl);
        ds.setUsername(login);
        ds.setPassword(password);
//        connection = ds.getConnection();

//        runner = new WrappedQueryRunner(connection);
        runner = new QueryRunner(ds);
        adapter = new OracleDatabaseAdapter();
        catalog = new CatalogLoader(adapter).load(ds, "VOSHOD");
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public QueryRunner getRunner() {
        return runner;
    }

    @Override
    public void close() {
//        try {
//            connection.close();
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }

    public Connection getConnection() {
        return null;
//        return connection;
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
