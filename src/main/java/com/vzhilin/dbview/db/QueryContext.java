package com.vzhilin.dbview.db;

import com.google.common.collect.Maps;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.mean.MeaningParser;
import com.vzhilin.dbview.db.mean.exp.Expression;
import com.vzhilin.dbview.db.mean.exp.ParsedTemplate;
import com.vzhilin.dbview.db.mean.exp.exceptions.ParseException;
import com.vzhilin.dbview.db.schema.Table;
import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Optional;

/**
 * Контекст выполнения запроса
 */
public final class QueryContext {
    /** Логгер */
    private final static Logger LOG = Logger.getLogger(QueryContext.class);

    /** Контекст подключения к БД */
    private final DbContext dbContext;

    /** Настройки подключения */
    private final ConnectionSettings connectionSettings;

    /** table --> exp */
    private final Map<Table, ParsedTemplate> parsedTemplates = Maps.newLinkedHashMap();

    public QueryContext(DbContext dbContext, ConnectionSettings connectionSettings) {
        this.dbContext = dbContext;
        this.connectionSettings = connectionSettings;
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
            connectionSettings.templatesProperty().add(newTemplate);
            return newTemplate.templateProperty();
        }
        return first.get().templateProperty();
    }

    public String getMeanintfulValue(Row row) {
        if (parsedTemplates.containsKey(row.getTable())) {
            return String.valueOf(parsedTemplates.get(row.getTable()).render(row));
        }
        Optional<Template> maybeTemplate = findTemplate(row.getTable().getName());
        if (maybeTemplate.isPresent()) {
            String template = maybeTemplate.get().getTemplate();
            if (template == null || template.isEmpty()) {
                return "";
            }

            MeaningParser parser = new MeaningParser();
            try {
                ParsedTemplate ex = parser.parse(row.getTable(), template);
                parsedTemplates.put(row.getTable(), ex);
                return String.valueOf(parsedTemplates.get(row.getTable()).render(row));
            } catch (ParseException e) {
                LOG.error(e, e);
            }

            return "";
        } else {
            return "";
        }
    }

    public ConnectionSettings getSettings() {
        return connectionSettings;
    }

    public void setTemplate(String name, String template) {
        getTemplateProperty(name).setValue(template);
        parsedTemplates.remove(name);
    }

    private void parseTemplates() {
        MeaningParser parser = new MeaningParser();
        for (Template t: connectionSettings.templatesProperty()) {
            String value = t.getTemplate();
            if (!value.isEmpty()) {
                try {
                    parsedTemplates.put(getDbContext().getSchema().getTable(t.getTableName()), parser.parse(dbContext.getSchema().getTable(t.getTableName()), value));
                } catch (ParseException e) {
                    LOG.error(e, e);
                }
            }
        }
    }

    private Optional<Template> findTemplate(String name) {
        return connectionSettings.templatesProperty().stream().filter(t -> t.getTableName().equals(name)).findFirst();
    }
}
