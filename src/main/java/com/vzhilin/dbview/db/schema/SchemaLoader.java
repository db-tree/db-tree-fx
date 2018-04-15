package com.vzhilin.dbview.db.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.log4j.Logger;

/**
 * Загружает из базы информацию о таблицах
 */
public final class SchemaLoader {
    /** логгер */
    private static final Logger LOGGER = Logger.getLogger(SchemaLoader.class);

    /** Схема БД */
    private static final String VOSHOD = "VOSHOD";

    public SchemaLoader() { }

    /**
     * Загружает из базы информацию о таблицах
     * @param ds DataSource
     * @return Schema
     * @throws SQLException ошибка SQL
     */
    public Schema load(DataSource ds) throws SQLException {
        Connection conn = null;

        try {
            conn = ds.getConnection();
            Schema sc = new Schema();
            DatabaseMetaData md = conn.getMetaData();

            Set<String> patterns = Sets.newHashSet("OESO_%");
            for (String pattern: patterns) {
                List<String> tables = loadTablesList(sc, md, pattern);

                fillColumns(sc, md, pattern);
                fillRelations(sc, md, tables);
            }

            return sc;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error(e, e);
                }
            }
        }
    }

    private void fillRelations(Schema sc, DatabaseMetaData md, List<String> tables) throws SQLException {
        for (String table: tables) {
            if (!sc.hasTable(table)) {
                LOGGER.warn("таблица не найдена: " + table);
                continue;
            }

            final Table tb = sc.getTable(table);

            ResultSet rs = md.getImportedKeys(null, VOSHOD, table);
            while (rs.next()) {
                String pktableName = rs.getString(3);
                String fkcolumnName = rs.getString(8);

                if (!sc.hasTable(pktableName)) {
                    LOGGER.warn("таблица не найдена: " + pktableName);
                    continue;
                }

                if (!fkcolumnName.equals(tb.getPk())) {
                    final Table pkTable = sc.getTable(pktableName);
                    tb.addRelation(fkcolumnName, pkTable);
                    pkTable.addReverseRelation(tb, fkcolumnName);
                }
            }

            rs.close();
        }
    }

    private void fillColumns(Schema sc, DatabaseMetaData md, String pattern) throws SQLException {
        ResultSet rs = md.getColumns(null, VOSHOD, pattern, null);
        while (rs.next()) {
            String table = rs.getString(3);
            String name = rs.getString(4);
            String dataType = rs.getString(6);

            if (sc.hasTable(table)) {
                sc.getTable(table).addColumn(name, dataType);
            }
        }
        rs.close();
    }

    private List<String> loadTablesList(Schema sc, DatabaseMetaData md, String pattern) throws SQLException {
        List<String> tables = Lists.newLinkedList();
        ResultSet rs = md.getTables(null, VOSHOD, pattern, null);
        while (rs.next()) {
            if ("TABLE".equals(rs.getString(4))) {
                final String name = rs.getString(3);
                tables.add(name);
            }
        }
        rs.close();

        int tablesCount = 0;
        for (String table: tables) {
            rs = md.getPrimaryKeys(null, VOSHOD, table);
            if (rs.next()) {
                String pkName = rs.getString(4);
                sc.addTable(new Table(table, pkName, tablesCount++));
            }
            rs.close();
        }
        return tables;
    }
}
