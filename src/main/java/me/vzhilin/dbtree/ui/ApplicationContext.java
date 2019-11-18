package me.vzhilin.dbtree.ui;

import me.vzhilin.dbtree.db.ContextCache;
import me.vzhilin.dbtree.db.DbContext;
import me.vzhilin.dbtree.db.QueryContext;
import me.vzhilin.dbtree.ui.conf.ConnectionSettings;
import me.vzhilin.dbtree.ui.conf.Settings;
import me.vzhilin.dbtree.ui.tree.RenderingHelper;
import me.vzhilin.dbtree.ui.util.ToStringConverter;

import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ApplicationContext {
    private final ContextCache contextCache = new ContextCache();
    private final Settings settings;
    private QueryContext queryContext;
    private final static ThreadLocal<ApplicationContext> CURRENT = new ThreadLocal<>();
    private Logger logger;
    private final ExecutorService queryExecutor;
    private final ToStringConverter toStringConverter = new ToStringConverter();
    private final RenderingHelper renderingHelper = new RenderingHelper(toStringConverter);

    public ApplicationContext(Settings settings) {
        this.settings = settings;
        CURRENT.set(this);
        Thread.UncaughtExceptionHandler handler = (t, e) -> logger.log("Uncaught Exception", e);
        Thread.setDefaultUncaughtExceptionHandler(handler);

         queryExecutor = Executors.newSingleThreadExecutor(r -> {
             Thread thread = new Thread(r);
             thread.setDaemon(true);
             thread.setUncaughtExceptionHandler(handler);
             return thread;
         });
    }

    public static ApplicationContext get() {
        return CURRENT.get();
    }

    public synchronized QueryContext newQueryContext(String connectionName) throws ExecutionException {
        ConnectionSettings connection = settings.getConnection(connectionName);

        if (queryContext != null) {
            queryContext.close();
        }

        String driverClass = connection.getDriverClass();
        String host = connection.getHost();
        String port = connection.getPort();
        String database = connection.getDatabase();
        String name = connection.getUsername();
        String pass = connection.getPassword();
        String pattern = connection.getTableNamePattern();
        Set<String> schemas = connection.getSchemas();

        DbContext context = contextCache.getContext(driverClass, host, port, database, name, pass, pattern, schemas);
        queryContext = new QueryContext(context, connection);
        return queryContext;
    }

    public DbContext newQueryContext(String driverClazz,
                                     String host,
                                     String port,
                                     String database,
                                     String usernameText,
                                     String password,
                                     String pattern,
                                     Set<String> schemas) throws ExecutionException {

        return contextCache.getContext(driverClazz,
                host,
                port,
                database,
                usernameText,
                password,
                pattern,
                schemas);
    }

    public DbContext getIfPresent(String driverClazz,
                                  String host,
                                  String port,
                                  String database,
                                  String usernameText, String password, String pattern, Set<String> schemas) {

        return contextCache.getIfPresent(driverClazz, host, port, database, usernameText, password, pattern, schemas);
    }

    public synchronized Logger getLogger() {
        return logger;
    }

    public synchronized void setLogger(Logger logger) {
        this.logger = logger;
    }

    public ExecutorService getQueryExecutorService() {
        return queryExecutor;
    }

    public RenderingHelper getRenderingHelper() {
        return renderingHelper;
    }

    public ToStringConverter getToStringConverter() {
        return toStringConverter;
    }

    public interface Logger {
        void log(String message);
        void log(String message, Throwable ex);
    }
}
