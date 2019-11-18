package me.vzhilin.dbtree.db;

import me.vzhilin.dbrow.adapter.DatabaseAdapter;
import me.vzhilin.dbrow.adapter.mariadb.MariadbDatabaseAdapter;
import me.vzhilin.dbrow.adapter.oracle.OracleDatabaseAdapter;
import me.vzhilin.dbrow.adapter.postgres.PostgresqlAdapter;
import me.vzhilin.dbrow.catalog.Catalog;
import me.vzhilin.dbrow.catalog.CatalogFilter;
import me.vzhilin.dbrow.catalog.loader.CatalogLoaderFactory;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public final class DbContext implements Closeable {
    private final Connection connection;
    private final DatabaseAdapter adapter;
    private final Catalog catalog;
    private final QueryRunner runner;
    private final static Pattern MATCH_ANY = Pattern.compile(".*");
    private final static Map<String, DatabaseInfo> DATABASES = new HashMap<>();

    static {
        DATABASES.put("org.postgresql.Driver", new DatabaseInfo(
        "jdbc:postgresql://%s:%s:%s", new PostgresqlAdapter(), "5432"
        ));

        DATABASES.put("oracle.jdbc.OracleDriver", new DatabaseInfo(
        "jdbc:oracle:thin:@%s:%s:%s", new OracleDatabaseAdapter(), "1521"
        ));

        DATABASES.put("org.mariadb.jdbc.Driver", new DatabaseInfo(
        "jdbc:mariadb://%s:%s:%s", new MariadbDatabaseAdapter(), "3306"
        ));
    }

    public DbContext(String driverClazz,
                     String host,
                     String port,
                     String database,
                     String login,
                     String password,
                     String pattern,
                     Set<String> schemas) throws SQLException {

        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClazz);
        ds.setUrl(getUrl(driverClazz, host, port, database));
        ds.setUsername(login);
        ds.setPassword(password);
        connection = ds.getConnection();
        connection.setReadOnly(true);

        runner = new WrappedQueryRunner(connection);
        adapter = chooseAdapter(driverClazz);
        schemas = chooseSchemas(schemas);
        Pattern compiledPattern = (pattern == null || pattern.isEmpty()) ? MATCH_ANY : Pattern.compile(pattern);
        CatalogLoaderFactory factory = new CatalogLoaderFactory();
        catalog = factory.getLoader(ds).load(ds, new PatternTableFilter(schemas, compiledPattern));
    }

    private String getUrl(String driverClazz, String host, String port, String database) {
        return DATABASES.get(driverClazz).getUrl(host, port, database);
    }

    private Set<String> chooseSchemas(Set<String> schemas) throws SQLException {
        if (schemas.isEmpty()) {
            schemas = Collections.singleton(adapter.defaultSchema(connection));
        }
        return schemas;
    }

    private DatabaseAdapter chooseAdapter(String driverClazz) {
        return DATABASES.get(driverClazz).adapter;
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

    private final static class DatabaseInfo {
        private final String jdbcUrl;
        private final DatabaseAdapter adapter;
        private final String defaultPort;

        public DatabaseInfo(String jdbcUrl, DatabaseAdapter adapter, String defaultPort) {
            this.jdbcUrl = jdbcUrl;
            this.adapter = adapter;
            this.defaultPort = defaultPort;
        }

        public String getUrl(String host, String port, String database) {
            boolean portIsBlank = port == null || port.isEmpty();
            return String.format(jdbcUrl, host, portIsBlank ? defaultPort : port, database);
        }
    }
}
