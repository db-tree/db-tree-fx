package com.vzhilin.dbview.db;

import com.google.common.collect.Maps;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.mean.MeaningParser;
import com.vzhilin.dbview.db.mean.exp.Expression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public final class QueryContext {
    private final DbContext dbContext;
    private final ConnectionSettings connectionSettings;
    private final Map<String, StringProperty> templates;
    private final Map<String, Expression> parsedTemplates = Maps.newLinkedHashMap();

    public QueryContext(DbContext dbContext, ConnectionSettings connectionSettings) {
        this.dbContext = dbContext;
        this.connectionSettings = connectionSettings;

        this.templates = connectionSettings
                .templatesProperty()
                .stream()
                .collect(toMap(Template::getTableName, Template::templateProperty));

        parseTemplates();
    }

    private void parseTemplates() {
        MeaningParser parser = new MeaningParser();
        for (Map.Entry<String, StringProperty> e: templates.entrySet()) {
            String value = e.getValue().getValue();
            if (!value.isEmpty()) {
                parsedTemplates.put(e.getKey(), parser.parse(value));
            }
        }
    }

    public DbContext getDbContext() {
        return dbContext;
    }

    public String getTemplate(String name) {
        return getTemplateProperty(name).getValue();
    }

    public StringProperty getTemplateProperty(String name) {
        if (!templates.containsKey(name)) {
            templates.put(name, new SimpleStringProperty());
        }

        return templates.get(name);
    }

    public String getMeanintfulValue(Row row) {
        String tableName = row.getTable().getName();
        if (parsedTemplates.containsKey(tableName)) {
            return String.valueOf(parsedTemplates.get(tableName).render(row));
        } else
        if (templates.containsKey(tableName)) {
            String template = templates.get(tableName).getValue();
            if (template == null || template.isEmpty()) {
                return "";
            }

            MeaningParser parser = new MeaningParser();
            parsedTemplates.put(tableName, parser.parse(template));
            return String.valueOf(parsedTemplates.get(tableName).render(row));
        } else {
            return "";
        }
    }

    public void setTemplate(String name, String template) {
        getTemplateProperty(name).setValue(template);
        parsedTemplates.remove(name);
    }
}
