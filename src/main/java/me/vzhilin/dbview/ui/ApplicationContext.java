package me.vzhilin.dbview.ui;

import me.vzhilin.dbview.db.ContextCache;
import me.vzhilin.dbview.db.DbContext;
import me.vzhilin.dbview.db.QueryContext;
import me.vzhilin.dbview.ui.conf.ConnectionSettings;
import me.vzhilin.dbview.ui.conf.Settings;

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

        queryContext = new QueryContext(contextCache.getContext(connection.getDriverClass(), connection.getJdbcUrl(), connection.getUsername(), connection.getPassword(), connection.getTableNamePattern()), connection);
        return queryContext;
    }

    public DbContext newQueryContext(String driverClazz, String jdbcUrlText, String usernameText, String password, String pattern) throws ExecutionException {
        return contextCache.getContext(driverClazz, jdbcUrlText, usernameText, password, pattern);
    }
}
