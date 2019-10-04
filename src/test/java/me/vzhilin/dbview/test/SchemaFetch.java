package me.vzhilin.dbview.test;

import me.vzhilin.dbview.db.DbContext;
import me.vzhilin.dbview.db.schema.Schema;

import java.sql.SQLException;
import java.util.Locale;

public class SchemaFetch {
    public static void main(String... argv) throws SQLException {
        new SchemaFetch().start();
    }

    private void start() throws SQLException {
        Locale.setDefault(Locale.US);
        DbContext ctx = new DbContext("oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:XE", "hr", "hr", "");
        Schema schema = ctx.getSchema();
        schema.prettyPrint(System.out);
    }
}
