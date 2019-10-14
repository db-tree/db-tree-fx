package me.vzhilin.dbtree.ui;

import me.vzhilin.dbtree.db.ContextCache;
import me.vzhilin.dbtree.db.DbContext;
import me.vzhilin.dbtree.db.QueryContext;
import me.vzhilin.dbtree.ui.conf.ConnectionSettings;
import me.vzhilin.dbtree.ui.conf.Settings;

import java.util.Set;
import java.util.concurrent.ExecutionException;

public final class ApplicationContext {
    private final ContextCache contextCache = new ContextCache();
    private final Settings settings;
    private QueryContext queryContext;

    public ApplicationContext(Settings settings) {
        this.settings = settings;
    }

    public QueryContext newQueryContext(String connectionName) throws ExecutionException {
        ConnectionSettings connection = settings.getConnection(connectionName);

        if (queryContext != null) {
            queryContext.close();
        }

        String driverClass = connection.getDriverClass();
        String jdbcUrl = connection.getJdbcUrl();
        String name = connection.getUsername();
        String pass = connection.getPassword();
        String pattern = connection.getTableNamePattern();
        Set<String> schemas = connection.getSchemas();
        queryContext = new QueryContext(contextCache.getContext(driverClass, jdbcUrl, name, pass, pattern, schemas), connection);
        return queryContext;
    }

    public DbContext newQueryContext(String driverClazz, String jdbcUrlText, String usernameText, String password, String pattern, Set<String> schemas) throws ExecutionException {
        return contextCache.getContext(driverClazz, jdbcUrlText, usernameText, password, pattern, schemas);
    }
}
