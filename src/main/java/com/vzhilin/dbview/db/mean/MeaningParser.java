package com.vzhilin.dbview.db.mean;

import com.google.common.collect.Lists;
import com.vzhilin.dbview.antlr4.MeaningfulBaseVisitor;
import com.vzhilin.dbview.antlr4.MeaningfulLexer;
import com.vzhilin.dbview.antlr4.MeaningfulParser;
import com.vzhilin.dbview.db.mean.exp.*;
import com.vzhilin.dbview.db.mean.exp.exceptions.ColumnNotFound;
import com.vzhilin.dbview.db.mean.exp.exceptions.NotForeignKey;
import com.vzhilin.dbview.db.mean.exp.exceptions.ParseException;
import com.vzhilin.dbview.db.schema.Table;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.antlr.v4.runtime.CharStreams.fromString;

/**
 * Парсер
 */
public final class MeaningParser {
    public ParsedTemplate parse(Table table, String textLine) {
        try {
            MeaningfulLexer lexer = new MeaningfulLexer(fromString(textLine));
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            MeaningfulParser parser = new MeaningfulParser(tokenStream);
            MeaningfulParser.ProgramContext tree = parser.program();
            MeaningfulBaseVisitor<Expression> visitor = new DefaultVisitor(table);
            return new ParsedTemplate(table, textLine, visitor.visit(tree));
        } catch (ParseException e) {
            return new ParsedTemplate(table, textLine, e.getMessage());
        }
    }

    public final static class DefaultVisitor extends MeaningfulBaseVisitor<Expression> {
        private final Table table;

        public DefaultVisitor(Table table) {
            this.table = table;
        }

        @Override
        public Expression visitProgram(MeaningfulParser.ProgramContext ctx) {
            if (ctx.getChildCount() == 1) {
                return visit(ctx.getChild(0));
            }

            List<Expression> expressions = Lists.newArrayList();
            for (ParseTree c: ctx.children) {
                Expression e = visit(c);
                if (e != null) {
                    expressions.add(e);
                }
            }

            return new ConcatExpression(expressions);
        }

        @Override
        public Expression visitSimple_column(MeaningfulParser.Simple_columnContext ctx) {
            String columnName = ctx.getText();
            if (!table.hasColumn(columnName)) {
                throw new ColumnNotFound(table, columnName);
            }

            return new ColumnExpression(columnName);
        }

        @Override
        public Expression visitComplex_column(MeaningfulParser.Complex_columnContext ctx) {
            List<String> columns = ctx.
                    simple_column().
                    stream().
                    map(c -> c.COLUMN_NAME().getText()).
                    collect(Collectors.toList());

            Table local = table;
            Iterator<String> it = columns.iterator();
            while (it.hasNext()) {
                String c = it.next();
                if (!local.hasColumn(c)) {
                    throw new ColumnNotFound(local, c);
                }

                if (it.hasNext() && !local.getRelations().containsKey(c)) {
                    throw new NotForeignKey(local, c);
                }

                if (!c.equals(local.getPk())) {
                    local = local.getRelations().get(c);
                }

            }

            return new ComplexColumnExpression(columns);
        }

        @Override
        public Expression visitString(MeaningfulParser.StringContext ctx) {
            return new TextExpression(ctx.getText());
        }
    }
}
