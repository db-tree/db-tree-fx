package me.vzhilin.dbtree.test;

import javafx.beans.property.ListProperty;
import me.vzhilin.dbtree.db.DbContext;
import me.vzhilin.dbtree.db.QueryContext;
import me.vzhilin.dbtree.db.Row;
import me.vzhilin.dbtree.db.meaning.MeaningParser;
import me.vzhilin.dbtree.db.meaning.exp.ParsedTemplate;
import me.vzhilin.dbtree.db.schema.Table;
import me.vzhilin.dbtree.ui.conf.ConnectionSettings;
import me.vzhilin.dbtree.ui.conf.Settings;
import me.vzhilin.dbtree.ui.conf.Template;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Locale;

import static com.google.common.collect.Iterables.getOnlyElement;

public class Antlr4Test {
    private DbContext ctx;
    private Settings settings;
    private QueryContext qc;


    private Settings readSettings() {
        Settings settings = new Settings();
        ConnectionSettings ra00c000 = new ConnectionSettings();
        ra00c000.driverClassProperty().set("oracle.jdbc.OracleDriver");
        ra00c000.jdbcUrlProperty().set("jdbc:oracle:thin:@localhost:1521:XE");
        ra00c000.usernameProperty().set("voshod");
        ra00c000.passwordProperty().set("voshod");

        ListProperty<Template> templates = ra00c000.templatesProperty();
        templates.add(new Template("OESO_KCA", "KCANAME"));
        templates.add(new Template("OESO_PTS", "PTSID"));


        ra00c000.connectionNameProperty().set("RA00C000");
        settings.connectionsProperty().add(ra00c000);
        return settings;
    }

    @Before
    public void setUp() throws SQLException {
        Locale.setDefault(Locale.US);

        ctx = new DbContext("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:XE", "voshod", "voshod", "");
        settings = readSettings();
        qc = new QueryContext(ctx, getOnlyElement(settings.getConnections()));
    }

    @Test
    public void test() {
        Table kcaTable = ctx.getSchema().getTable("OESO_KCA");
        Row r = new Row(qc, kcaTable, 2190);
        String meaningfulValue = qc.getMeaningfulValue(r);
        System.err.println(meaningfulValue);

        String textExpression = "KCAREGNUMBER + ': ' + KCANAME + ' -- ' + KCASTATUSID";
        MeaningParser parser = new MeaningParser();
        ParsedTemplate result = parser.parse(kcaTable, textExpression);

        System.err.println(result.render(r));
    }
}
