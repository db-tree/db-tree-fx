package com.vzhilin.dbview;

import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.db.ContextCache;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;

import java.util.concurrent.ExecutionException;

public class ApplicationContext {
    private final ContextCache contextCache = new ContextCache();
    private final Settings settings;

    public ApplicationContext(Settings settings) {
        this.settings = settings;
    }

    public ContextCache getContextCache() {
        return contextCache;
    }

    public QueryContext getQueryContext(String connectionName) throws ExecutionException {
        ConnectionSettings connection = settings.getConnection(connectionName);
        return new QueryContext(contextCache.getContext(connection.getDriverClass(), connection.getJdbcUrl(), connection.getUsername(), connection.getPassword()), connection);
    }

    public DbContext getQueryContext(String driverClazz, String jdbcUrlText, String usernameText, String password) throws ExecutionException {
        return contextCache.getContext(driverClazz, jdbcUrlText, usernameText, password);
    }
}
