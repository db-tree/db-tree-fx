package com.vzhilin.dbview.test;

import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.mean.MeaningParser;
import com.vzhilin.dbview.db.mean.exp.ParsedTemplate;
import com.vzhilin.dbview.db.schema.Table;
import javafx.beans.property.ListProperty;
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
        String meaningfulValue = qc.getMeanintfulValue(r);
        System.err.println(meaningfulValue);

        String textExpression = "KCAREGNUMBER + ': ' + KCANAME + ' -- ' + KCASTATUSID";
        MeaningParser parser = new MeaningParser();
        ParsedTemplate result = parser.parse(kcaTable, textExpression);

        System.err.println(result.render(r));
    }
}
