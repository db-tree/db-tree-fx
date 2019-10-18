package me.vzhilin.dbtree.db.meaning;

import com.google.common.collect.Lists;
import me.vzhilin.catalog.Table;
import me.vzhilin.dbtree.antlr4.MeaningfulBaseVisitor;
import me.vzhilin.dbtree.antlr4.MeaningfulLexer;
import me.vzhilin.dbtree.antlr4.MeaningfulParser;
import me.vzhilin.dbtree.db.meaning.exp.*;
import me.vzhilin.dbtree.db.meaning.exp.exceptions.ColumnNotFound;
import me.vzhilin.dbtree.db.meaning.exp.exceptions.ParseException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

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
            parser.setErrorHandler(new DefaultErrorStrategy() {
                @Override
                public void recover(Parser recognizer, RecognitionException e) {
                    throw new ParseCancellationException(e);
                }

                @Override
                public Token recoverInline(Parser recognizer) throws RecognitionException {
                    InputMismatchException e = new InputMismatchException(recognizer);
                    for (ParserRuleContext context = recognizer.getContext(); context != null; context = context.getParent()) {
                        context.exception = e;
                    }

                    throw new ParseCancellationException(e);
                }
            });
            MeaningfulParser.ProgramContext tree = parser.program();
            MeaningfulBaseVisitor<Expression> visitor = new DefaultVisitor(table);
            return new ParsedTemplate(visitor.visit(tree));
        } catch (ParseException e) {
            return new ParsedTemplate(e.getMessage());
        } catch (ParseCancellationException e) {
            return new ParsedTemplate("syntax error");
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
            if (table.hasColumn(columnName)) {
                return new ColumnExpression(table.getColumn(columnName));
            }

            if (table.hasForeignKey(columnName)) {
                return new ForeignKeyExpression(table.getForeignKeys().get(columnName));
            }
            throw new ColumnNotFound(table, columnName);
        }

        @Override
        public Expression visitComplex_column(MeaningfulParser.Complex_columnContext ctx) {
            List<String> columns = ctx.
                    simple_column().
                    stream().
                    map(c -> c.COLUMN_NAME().getText()).
                    collect(Collectors.toList());

//            Table local = table;
//            Iterator<String> it = columns.iterator();
//            while (it.hasNext()) {
//                String c = it.next();
//                if (!local.hasColumn(c)) {
//                    throw new ColumnNotFound(local, c);
//                }
//
//                if (it.hasNext() && !local.hasOnlyForeignKey(c)) {
//                    throw new NotForeignKey(local, c);
//                }
//
//                PrimaryKey pk = local.getPrimaryKey().get();
//                if (pk.hasColumn(c)) { // FIXME composite keys
//                    local = local.g().get(c);
//                }
//            } FIXME VALIDATION

            return new ComplexColumnExpression(columns);
        }

        @Override
        public Expression visitString(MeaningfulParser.StringContext ctx) {
            return new TextExpression(ctx.getText());
        }
    }
}
