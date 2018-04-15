package com.vzhilin.dbview.test;

import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.mean.exp.Expression;
import com.vzhilin.dbview.db.mean.exp.MeaningfulExpression;
import com.vzhilin.dbview.db.schema.Table;
import javafx.beans.property.ListProperty;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Locale;

import static com.google.common.collect.Iterables.getOnlyElement;

public class MeaningfulRendererTest {
    private DbContext ctx;
    private Settings settings;
    private QueryContext qc;

    @Before
    public void setUp() throws SQLException {
        Locale.setDefault(Locale.US);

        ctx = new DbContext("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:XE", "voshod", "voshod");
        settings = readSettings();
        qc = new QueryContext(ctx, getOnlyElement(settings.getConnections()));
    }

    @Test
    public void test() throws SQLException {

        Table kcaTable = ctx.getSchema().getTable("OESO_KCA");
        Row r = new Row(qc, kcaTable, 2190);
        String meaningfulValue = r.meaningfulValue();
        System.err.println(meaningfulValue);

    }

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

    @Test
    public void expressionTest() {
        String e1 = "DICROWCODE + ': ' + DICROWNAME";
        String e2 = "$DOCIDMASTER + ': ' + $DICROWSLAVE";

        Table dicrelationtable = ctx.getSchema().getTable("OESO_DOC_RELATION");
        Table dicrowtable = ctx.getSchema().getTable("OESO_DIC_ROW");

        Row r = new Row(qc, dicrowtable, 101100100002L);
        Expression exp = new ExpressionParser(dicrowtable, e1).parse();
        System.err.println(exp.render(r));
    }


    private interface ExpressionRenderer {
        String render(Row row);
    }

    private MeaningfulExpression getExpresssion() {
        return null;
    }


    private class ExpressionParser {
        private final Table root;
        private final String expression;
        private final Lexer lexer;

        public ExpressionParser(Table root, String expression) {
            this.root = root;
            this.expression = expression;
            this.lexer = new Lexer(expression);
        }

        public Expression parse() {
//            lexer
            return null;
        }
    }

    private final class Lexer {
        private final char[] chars;
        private int pos;

        public Lexer(String line) {
            this.chars = line.toCharArray();
            this.pos = -1;
        }

        public void next() {
            ++pos;
        }

        public char ch() {
            return chars[pos];
        }

        public boolean hasNext() {
            return pos < chars.length;
        }
    }
}

