package me.vzhilin.dbtree.db;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import me.vzhilin.dbtree.ui.ApplicationContext;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public final class ContextCache {
    private final LoadingCache<ContextKey, DbContext> cache;

    public ContextCache() {
        cache =
            CacheBuilder.newBuilder()
                .maximumSize(10)
                .removalListener((RemovalListener<ContextKey, DbContext>) n -> n.getValue().close())
                .build(new CacheLoader<ContextKey, DbContext>() {
                    @Override
                    public DbContext load(ContextKey dsKey) throws Exception {
                        return new DbContext(dsKey.driverClazz, dsKey.host, dsKey.port, dsKey.database, dsKey.login, dsKey.password, dsKey.pattern, dsKey.schemas);
                    }
                });
    }

    public DbContext getContext(
            String driverClazz,
            String host,
            String port,
            String database,
            String login,
            String password,
            String pattern,
            Set<String> schemas) throws ExecutionException {

        ContextKey key = new ContextKey(
            driverClazz,
            host,
            port,
            database,
            login,
            password,
            pattern,
            schemas
        );

        DbContext dbContext = cache.get(key);
        try {
            if (dbContext != null && dbContext.getConnection().isClosed()) {
                cache.refresh(key);
                return cache.get(key);
            }
        } catch (SQLException e) {
            ApplicationContext.get().getLogger().log("Database Error", e);
            return null;
        }
        return dbContext;
    }

    public DbContext getIfPresent(String driverClazz,
                                  String host,
                                  String port,
                                  String database,
                                  String login,
                                  String password,
                                  String pattern,
                                  Set<String> schemas) {

        ContextKey key = new ContextKey(driverClazz, host, port, database, login, password, pattern, schemas);
        return cache.getIfPresent(key);
    }

    private final static class ContextKey {
        private final String driverClazz;
        private final String host;
        private final String port;
        private final String database;
        private final String login;
        private final String password;
        private final String pattern;
        private final Set<String> schemas;

        public ContextKey(String driverClazz,
                          String host,
                          String port,
                          String database,
                          String login,
                          String password,
                          String pattern,
                          Set<String> schemas) {

            this.driverClazz = driverClazz;
            this.host = host;
            this.port = port;
            this.database = database;
            this.login = login;
            this.password = password;
            this.pattern = pattern;
            this.schemas = schemas;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContextKey that = (ContextKey) o;
            return driverClazz.equals(that.driverClazz) &&
                    host.equals(that.host) &&
                    port.equals(that.port) &&
                    database.equals(that.database) &&
                    login.equals(that.login) &&
                    Objects.equals(pattern, that.pattern) &&
                    Objects.equals(schemas, that.schemas);
        }

        @Override
        public int hashCode() {
            return Objects.hash(driverClazz, host, port, database, login, pattern, schemas);
        }
    }
}
