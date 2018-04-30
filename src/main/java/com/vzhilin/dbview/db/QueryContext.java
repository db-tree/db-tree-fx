package com.vzhilin.dbview.db;

import com.google.common.collect.Maps;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.mean.MeaningParser;
import com.vzhilin.dbview.db.mean.exp.Expression;
import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;
import java.util.Optional;

public final class QueryContext {
    private final DbContext dbContext;
    private final ConnectionSettings connectionSettings;
    private final Map<String, Expression> parsedTemplates = Maps.newLinkedHashMap();
    private final ListProperty<Template> templates;

    public QueryContext(DbContext dbContext, ConnectionSettings connectionSettings) {
        this.dbContext = dbContext;
        this.connectionSettings = connectionSettings;

        this.templates = connectionSettings.templatesProperty();
        parseTemplates();
    }

    public DbContext getDbContext() {
        return dbContext;
    }

    public String getTemplate(String name) {
        return getTemplateProperty(name).getValue();
    }

    public StringProperty getTemplateProperty(String name) {
        Optional<Template> first = findTemplate(name);
        if (!first.isPresent()) {
            Template newTemplate = new Template(name, "");
            templates.add(newTemplate);

            return newTemplate.templateProperty();
        }

        return first.get().templateProperty();
    }

    public String getMeanintfulValue(Row row) {
        String tableName = row.getTable().getName();
        if (parsedTemplates.containsKey(tableName)) {
            return String.valueOf(parsedTemplates.get(tableName).render(row));
        }

        Optional<Template> maybeTemplate = findTemplate(tableName);
        if (maybeTemplate.isPresent()) {
            String template = maybeTemplate.get().getTemplate();
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

    private void parseTemplates() {
        MeaningParser parser = new MeaningParser();
        for (Template t: templates) {
            String value = t.getTemplate();
            if (!value.isEmpty()) {
                parsedTemplates.put(t.getTableName(), parser.parse(value));
            }
        }
    }

    private Optional<Template> findTemplate(String name) {
        return templates.stream().filter(t -> t.getTableName().equals(name)).findFirst();
    }

    public ConnectionSettings getSettings() {
        return connectionSettings;
    }
}
