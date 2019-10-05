package me.vzhilin.dbtree.db;

import com.google.common.collect.Maps;
import javafx.beans.property.StringProperty;
import me.vzhilin.dbtree.db.meaning.MeaningParser;
import me.vzhilin.dbtree.db.meaning.exp.ParsedTemplate;
import me.vzhilin.dbtree.db.meaning.exp.exceptions.ParseException;
import me.vzhilin.dbtree.db.schema.Schema;
import me.vzhilin.dbtree.db.schema.Table;
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
    private final DataDigger dd;

    public QueryContext(DbContext dbContext, ConnectionSettings connectionSettings) {
        this.dbContext = dbContext;
        this.connectionSettings = connectionSettings;
        this.dd = new DataDigger(this);
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

    public void setTemplate(String name, String template) {
        getTemplateProperty(name).setValue(template);
        parsedTemplates.remove(dbContext.getSchema().getTable(name));
    }

    private void parseTemplates() {
        MeaningParser parser = new MeaningParser();
        Schema schema = getDbContext().getSchema();
        for (Template t: this.connectionSettings.templatesProperty()) {
            String value = t.getTemplate();
            if (!value.isEmpty() && schema.hasTable(t.getTableName())) {
                try {
                    parsedTemplates.put(schema.getTable(t.getTableName()), parser.parse(dbContext.getSchema().getTable(t.getTableName()), value));
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
        dd.close();
    }

    public DataDigger getDataDigger() {
        return dd;
    }
}
