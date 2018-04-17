package com.vzhilin.dbview.db.mean;

import com.google.common.collect.Lists;
import com.vzhilin.dbview.antlr4.MeaningfulBaseVisitor;
import com.vzhilin.dbview.antlr4.MeaningfulLexer;
import com.vzhilin.dbview.antlr4.MeaningfulParser;
import com.vzhilin.dbview.db.mean.exp.*;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.stream.Collectors;

import static org.antlr.v4.runtime.CharStreams.fromString;

/**
 * Парсер
 */
public final class MeaningParser {
    public Expression parse(String textLine) {
        MeaningfulLexer lexer = new MeaningfulLexer(fromString(textLine));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MeaningfulParser parser = new MeaningfulParser(tokenStream);
        MeaningfulParser.ProgramContext tree = parser.program();
        MeaningfulBaseVisitor<Expression> visitor = new DefaultVisitor();
        return visitor.visit(tree);
    }

    public final static class DefaultVisitor extends MeaningfulBaseVisitor<Expression> {
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
