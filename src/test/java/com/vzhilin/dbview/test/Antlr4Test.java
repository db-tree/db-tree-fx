package com.vzhilin.dbview.test;

import com.google.common.collect.Lists;
import com.vzhilin.dbview.antlr4.MeaningfulBaseVisitor;
import com.vzhilin.dbview.antlr4.MeaningfulLexer;
import com.vzhilin.dbview.antlr4.MeaningfulParser;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.conf.Settings;
import com.vzhilin.dbview.conf.Template;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.mean.exp.*;
import com.vzhilin.dbview.db.schema.Table;
import javafx.beans.property.ListProperty;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.getOnlyElement;
import static org.antlr.v4.runtime.CharStreams.fromString;

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

        ctx = new DbContext("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:XE", "voshod", "voshod");
        settings = readSettings();
        qc = new QueryContext(ctx, getOnlyElement(settings.getConnections()));
    }

    @Test
    public void test() {
        Table kcaTable = ctx.getSchema().getTable("OESO_KCA");
        Row r = new Row(qc, kcaTable, 2190);
        String meaningfulValue = r.meaningfulValue();
        System.err.println(meaningfulValue);

        MeaningfulLexer lexer = new MeaningfulLexer(fromString("KCAREGNUMBER + ': ' + KCANAME + ' -- ' + KCASTATUSID.DICROWCODE"));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MeaningfulParser parser = new MeaningfulParser(tokenStream);


        MeaningfulParser.ProgramContext tree = parser.program();
//        System.err.println(tree.toStringTree(parser));


        MeaningfulBaseVisitor<Expression> visitor = new DefaultVisitor();

        Expression result = visitor.visit(tree);
        System.err.println(result);

        System.err.println(result.render(r));
    }

    private static class DefaultVisitor extends MeaningfulBaseVisitor<Expression> {
        @Override
        public Expression visitProgram(MeaningfulParser.ProgramContext ctx) {
            if (ctx.getChildCount() == 1) {
                return visit(ctx.getChild(0));
            }

            List<Expression> expresions = Lists.newArrayList();
            for (ParseTree c: ctx.children) {
                Expression e = visit(c);
                if (e != null) {
                    expresions.add(e);
                }
            }

            return new ConcatExpression(expresions);
        }

        @Override
        public Expression visitExp(MeaningfulParser.ExpContext ctx) {
            return super.visitExp(ctx);
        }

        @Override
        public Expression visitEv_exp(MeaningfulParser.Ev_expContext ctx) {
            return super.visitEv_exp(ctx);
        }

        @Override
        public Expression visitSimple_column(MeaningfulParser.Simple_columnContext ctx) {
            return new ColumnExpression(ctx.getText());
        }

        @Override
        public Expression visitComplex_column(MeaningfulParser.Complex_columnContext ctx) {
            return new ComplexColumnExpression(ctx.
                    simple_column().
                    stream().
                    map(c -> c.COLUMN_NAME().getText()).
                    collect(Collectors.toList()));
        }

        @Override
        public Expression visitString(MeaningfulParser.StringContext ctx) {
            return new TextExpression(ctx.getText());
        }

    }
}
