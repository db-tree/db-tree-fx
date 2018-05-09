package com.vzhilin.dbview.db;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public final class ContextCache {
    private final LoadingCache<ContextKey, DbContext> cache;

    public ContextCache() {
        cache =
            CacheBuilder.newBuilder()
                .maximumSize(10)
                .build(new CacheLoader<ContextKey, DbContext>() {
                    @Override
                    public DbContext load(ContextKey dsKey) throws Exception {
                        return new DbContext(dsKey.driverClazz, dsKey.jdbcUrl, dsKey.login, dsKey.password);
                    }
                });
    }

    public DbContext getContext(String driverClazz, String jdbcUrl, String login, String password) throws ExecutionException {
        return cache.get(new ContextKey(driverClazz, jdbcUrl, login, password));
    }

    private final static class ContextKey {
        private final String driverClazz;
        private final String jdbcUrl;
        private final String login;
        private final String password;

        public ContextKey(String driverClazz, String jdbcUrl, String login, String password) {
            this.driverClazz = driverClazz;
            this.jdbcUrl = jdbcUrl;
            this.login = login;
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContextKey contextKey = (ContextKey) o;
            return Objects.equals(driverClazz, contextKey.driverClazz) &&
                    Objects.equals(jdbcUrl, contextKey.jdbcUrl) &&
                    Objects.equals(login, contextKey.login);
        }

        @Override
        public int hashCode() {
            return Objects.hash(driverClazz, jdbcUrl, login);
        }
    }
}
