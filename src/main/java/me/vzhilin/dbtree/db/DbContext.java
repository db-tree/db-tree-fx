package me.vzhilin.dbtree.db;

import me.vzhilin.adapter.DatabaseAdapter;
import me.vzhilin.adapter.oracle.OracleDatabaseAdapter;
import me.vzhilin.adapter.postgres.PostgresqlAdapter;
import me.vzhilin.catalog.Catalog;
import me.vzhilin.catalog.CatalogLoader;
import me.vzhilin.catalog.filter.AcceptSchema;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;

public final class DbContext implements Closeable {
    private final Connection connection;
    private final DatabaseAdapter adapter;
    private final Catalog catalog;
    private final QueryRunner runner;

    public DbContext(String driverClazz, String jdbcUrl, String login, String password, String pattern, Set<String> schemas) throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClazz);
        ds.setUrl(jdbcUrl);
        ds.setUsername(login);
        ds.setPassword(password);
        connection = ds.getConnection();
        connection.setReadOnly(true);

        runner = new WrappedQueryRunner(connection);
        adapter = chooseAdapter(driverClazz);
        catalog = new CatalogLoader(adapter).load(ds, new AcceptSchema(getSchemas(schemas)));
    }

    private DatabaseAdapter chooseAdapter(String driverClazz) {
        switch (driverClazz) {
            case "org.postgresql.Driver":
                return new PostgresqlAdapter();
            case "oracle.jdbc.OracleDriver":
                return new OracleDatabaseAdapter();
        }
        throw new RuntimeException("unsupported driver: " + driverClazz);
    }

    public DatabaseAdapter getAdapter() {
        return adapter;
    }

    private Set<String> getSchemas(Set<String> schemas) throws SQLException {
        if (schemas.isEmpty()) {
             return Collections.singleton(adapter.defaultSchema(connection));
        } else {
            return schemas;
        }
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public QueryRunner getRunner() {
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
