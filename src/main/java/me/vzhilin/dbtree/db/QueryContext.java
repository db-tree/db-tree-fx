package me.vzhilin.dbtree.db;

import com.google.common.collect.Maps;
import javafx.beans.property.StringProperty;
import me.vzhilin.catalog.Catalog;
import me.vzhilin.catalog.Schema;
import me.vzhilin.catalog.Table;
import me.vzhilin.db.Row;
import me.vzhilin.dbtree.db.meaning.MeaningParser;
import me.vzhilin.dbtree.db.meaning.exp.ParsedTemplate;
import me.vzhilin.dbtree.db.meaning.exp.exceptions.ParseException;
import me.vzhilin.dbtree.ui.conf.ConnectionSettings;
import me.vzhilin.dbtree.ui.conf.Template;
import org.apache.log4j.Logger;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;

/**
 * Контекст выполнения запроса
 */
public final class QueryContext implements Closeable {
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

    public String getTemplate(String schemaName, String name) {
        return getTemplateProperty(schemaName, name).getValue();
    }

    public StringProperty getTemplateProperty(String schemaName, String name) {
        Optional<Template> first = findTemplate(name);
        if (!first.isPresent()) {
            Template newTemplate = new Template(schemaName, name, "");
            connectionSettings.templatesProperty().add(newTemplate);
            return newTemplate.templateProperty();
        }
        return first.get().templateProperty();
    }

    public ParsedTemplate getParsedTemplate(Table table) {
        if (parsedTemplates.containsKey(table)) {
            parsedTemplates.get(table);
        }

        Optional<Template> maybeTemplate = findTemplate(table.getName());
        if (maybeTemplate.isPresent()) {
            String template = maybeTemplate.get().getTemplate();
            if (template == null || template.isEmpty()) {
                return null;
            }

            MeaningParser parser = new MeaningParser();
            try {
                ParsedTemplate ex = parser.parse(table, template);
                parsedTemplates.put(table, ex);
                return ex;
            } catch (ParseException e) {
                LOG.error(e, e);
            }
        }

        return null;
    }

    public String getMeaningfulValue(Row row) {
        ParsedTemplate pt = getParsedTemplate(row.getTable());
        if (pt != null) {
            return String.valueOf(pt.render(row));
        } else {
            return "";
        }
    }

    public ConnectionSettings getSettings() {
        return connectionSettings;
    }

    public void setTemplate(String schema, String name, String template) {
        getTemplateProperty(schema, name).setValue(template);
        Catalog catalog = dbContext.getCatalog();
        parsedTemplates.remove(catalog.getSchema(schema).getTable(name));
    }

    private void parseTemplates() {
        MeaningParser parser = new MeaningParser();
        Catalog schema = getDbContext().getCatalog();
        for (Template t: this.connectionSettings.templatesProperty()) {
            String value = t.getTemplate();
            if (!value.isEmpty() && schema.hasTable(t.getSchemaName(), t.getTableName())) {
                try {
                    Schema s = schema.getSchema(t.getSchemaName());
                    Table table = s.getTable(t.getTableName());
                    parsedTemplates.put(table, parser.parse(table, value));
                } catch (ParseException e) {
                    LOG.error(e, e);
                }
            }
        }
    }

    private Optional<Template> findTemplate(String name) {
        return connectionSettings.templatesProperty().stream().filter(t -> t.getTableName().equals(name)).findFirst();
    }

    @Override
    public void close() {
//        dd.close();
    }
}
