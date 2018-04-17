package com.vzhilin.dbview.db;

import com.google.common.collect.Maps;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.mean.MeaningParser;
import com.vzhilin.dbview.db.mean.exp.Expression;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public final class QueryContext {
    private final DbContext dbContext;
    private final ConnectionSettings connectionSettings;
    private final Map<String, String> templates;
    private final Map<String, Expression> parsedTemplates = Maps.newLinkedHashMap();

    public QueryContext(DbContext dbContext, ConnectionSettings connectionSettings) {
        this.dbContext = dbContext;
        this.connectionSettings = connectionSettings;

        this.templates = connectionSettings
                .templatesProperty()
                .stream()
                .collect(toMap(Template::getTableName, Template::getTemplate));

        parseTemplates();
    }

    private void parseTemplates() {
        MeaningParser parser = new MeaningParser();
        for (Map.Entry<String, String> e: templates.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {
                parsedTemplates.put(e.getKey(), parser.parse(e.getValue()));
            }
        }

    }

    public DbContext getDbContext() {
        return dbContext;
    }

    public String getTemplate(String name) {
        return templates.get(name);
    }

    public String getMeanintfulValue(Row row) {
        String tableName = row.getTable().getName();
        if (parsedTemplates.containsKey(tableName)) {
            return String.valueOf(parsedTemplates.get(tableName).render(row));
        } else {
            return "";
        }
    }
}
