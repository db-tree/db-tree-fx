package me.vzhilin.dbtree.db;

import me.vzhilin.adapter.DatabaseAdapter;
import me.vzhilin.adapter.mariadb.MariadbDatabaseAdapter;
import me.vzhilin.adapter.oracle.OracleDatabaseAdapter;
import me.vzhilin.adapter.postgres.PostgresqlAdapter;
import me.vzhilin.catalog.Catalog;
import me.vzhilin.catalog.CatalogFilter;
import me.vzhilin.catalog.CatalogLoader;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

public final class DbContext implements Closeable {
    private final Connection connection;
    private final DatabaseAdapter adapter;
    private final Catalog catalog;
    private final QueryRunner runner;
    private final static Pattern MATCH_ANY = Pattern.compile(".*");

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
        schemas = chooseSchemas(schemas);
        Pattern compiledPattern = (pattern == null || pattern.isEmpty()) ? MATCH_ANY : Pattern.compile(pattern);
        catalog = new CatalogLoader(adapter).load(ds, new PatternTableFilter(schemas, compiledPattern));
    }

    private Set<String> chooseSchemas(Set<String> schemas) throws SQLException {
        if (schemas.isEmpty()) {
            schemas = Collections.singleton(adapter.defaultSchema(connection));
        }
        return schemas;
    }

    private DatabaseAdapter chooseAdapter(String driverClazz) {
        switch (driverClazz) {
            case "org.postgresql.Driver":
                return new PostgresqlAdapter();
            case "oracle.jdbc.OracleDriver":
                return new OracleDatabaseAdapter();
            case "org.mariadb.jdbc.Driver":
                return new MariadbDatabaseAdapter();
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

    private final static class PatternTableFilter implements CatalogFilter {
        private final Set<String> schemas;
        private final Pattern compiledPattern;

        private PatternTableFilter(Set<String> schemas, Pattern compiledPattern) {
            this.schemas = schemas;
            this.compiledPattern = compiledPattern;
        }

        @Override
        public boolean acceptSchema(String schemaName) {
            return schemas.contains(schemaName);
        }

        @Override
        public boolean acceptTable(String schemaName, String tableName) {
            return schemas.contains(schemaName) && compiledPattern.matcher(tableName).matches();
        }

        @Override
        public boolean acceptColumn(String schema, String table, String column) {
            return true;
        }
    }
}
