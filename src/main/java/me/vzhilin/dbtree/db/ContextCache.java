package me.vzhilin.dbtree.db;

import com.google.common.cache.*;
import com.google.common.util.concurrent.ListenableFuture;
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
                        return new DbContext(dsKey.driverClazz, dsKey.jdbcUrl, dsKey.login, dsKey.password, dsKey.pattern, dsKey.schemas);
                    }
                });
    }

    public DbContext getContext(String driverClazz, String jdbcUrl, String login, String password, String pattern, Set<String> schemas) throws ExecutionException {
        ContextKey key = new ContextKey(driverClazz, jdbcUrl, login, password, pattern, schemas);
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

    private final static class ContextKey {
        private final String driverClazz;
        private final String jdbcUrl;
        private final String login;
        private final String password;
        private final String pattern;
        private final Set<String> schemas;

        public ContextKey(String driverClazz, String jdbcUrl, String login, String password, String pattern, Set<String> schemas) {
            this.driverClazz = driverClazz;
            this.jdbcUrl = jdbcUrl;
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
                    jdbcUrl.equals(that.jdbcUrl) &&
                    login.equals(that.login) &&
                    password.equals(that.password) &&
                    pattern.equals(that.pattern) &&
                    schemas.equals(that.schemas);
        }

        @Override
        public int hashCode() {
            return Objects.hash(driverClazz, jdbcUrl, login, password, pattern, schemas);
        }
    }
}
