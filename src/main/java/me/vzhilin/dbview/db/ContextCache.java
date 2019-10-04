package me.vzhilin.dbview.db;

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
                        return new DbContext(dsKey.driverClazz, dsKey.jdbcUrl, dsKey.login, dsKey.password, dsKey.pattern);
                    }
                });
    }

    public DbContext getContext(String driverClazz, String jdbcUrl, String login, String password, String pattern) throws ExecutionException {
        return cache.get(new ContextKey(driverClazz, jdbcUrl, login, password, pattern));
    }

    private final static class ContextKey {
        private final String driverClazz;
        private final String jdbcUrl;
        private final String login;
        private final String password;
        private final String pattern;

        public ContextKey(String driverClazz, String jdbcUrl, String login, String password, String pattern) {
            this.driverClazz = driverClazz;
            this.jdbcUrl = jdbcUrl;
            this.login = login;
            this.password = password;
            this.pattern = pattern;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContextKey contextKey = (ContextKey) o;
            return Objects.equals(driverClazz, contextKey.driverClazz) &&
                    Objects.equals(jdbcUrl, contextKey.jdbcUrl) &&
                    Objects.equals(login, contextKey.login) &&
                    Objects.equals(pattern, contextKey.pattern);
        }

        @Override
        public int hashCode() {
            return Objects.hash(driverClazz, jdbcUrl, login);
        }
    }
}
