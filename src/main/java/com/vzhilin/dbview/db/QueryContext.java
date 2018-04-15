package com.vzhilin.dbview.db;

import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Template;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public final class QueryContext {
    private final DbContext dbContext;
    private final ConnectionSettings connectionSettings;
    private final Map<String, String> templates;

    public QueryContext(DbContext dbContext, ConnectionSettings connectionSettings) {
        this.dbContext = dbContext;
        this.connectionSettings = connectionSettings;

        this.templates = connectionSettings
                .templatesProperty()
                .stream()
                .collect(toMap(Template::getTableName, Template::getTemplate));
    }

    public DbContext getDbContext() {
        return dbContext;
    }

    public String getTemplate(String name) {
        return templates.get(name);
    }
}
