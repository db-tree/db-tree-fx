package com.vzhilin.dbview.db;

import java.sql.SQLException;

import com.vzhilin.dbview.db.schema.Schema;
import com.vzhilin.dbview.db.schema.SchemaLoader;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;

public final class DbContext {
    private Schema schema;
    private QueryRunner runner;

    public DbContext(String driverClazz, String jdbcUrl, String login, String password) throws SQLException {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClazz);
        ds.setUrl(jdbcUrl);
        ds.setUsername(login);
        ds.setPassword(password);

        runner = new QueryRunner(ds);
        schema = new SchemaLoader().load(ds);
    }

    public Schema getSchema() {
        return schema;
    }

    public QueryRunner getRunner() {
        return runner;
    }
}
