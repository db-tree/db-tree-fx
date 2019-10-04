package me.vzhilin.dbview.db.schema;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Загружает из базы информацию о таблицах
 */
public final class SchemaLoader {
    /** логгер */
    private static final Logger LOGGER = Logger.getLogger(SchemaLoader.class);
    private final Pattern pattern;
    private final DataSource ds;

    public SchemaLoader(DataSource ds, String pattern) {
        this.ds = ds;
        this.pattern = Pattern.compile(pattern == null || pattern.trim().isEmpty() ? ".*" : pattern);
    }

    /**
     * Загружает из базы информацию о таблицах
     * @return Schema
     * @throws SQLException ошибка SQL
     */
    public Schema load() throws SQLException {
        Connection conn = null;

        try {
            conn = ds.getConnection();
            String user = conn.getMetaData().getUserName();
            Schema sc = new Schema();
            DatabaseMetaData md = conn.getMetaData();
            List<String> tables = loadTablesList(sc, md, user);

            fillColumns(sc, md, user);
            fillRelations(sc, md, tables, user);

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

    private void fillRelations(Schema sc, DatabaseMetaData md, List<String> tables, String user) throws SQLException {
        for (String table: tables) {
            if (!sc.hasTable(table)) {
                LOGGER.warn("таблица не найдена: " + table);
                continue;
            }

            final Table tb = sc.getTable(table);

            ResultSet rs = md.getImportedKeys(null, user, table);
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

    private void fillColumns(Schema sc, DatabaseMetaData md, String user) throws SQLException {
        ResultSet rs = md.getColumns(null, user, null, null);
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

    private List<String> loadTablesList(Schema sc, DatabaseMetaData md, String user) throws SQLException {
        List<String> tables = Lists.newLinkedList();
        ResultSet rs = md.getTables(null, user, null, null);
        while (rs.next()) {
            if ("TABLE".equals(rs.getString(4))) {
                final String name = rs.getString(3);

                if (pattern.matcher(name).matches()) {
                    tables.add(name);
                }
            }
        }
        rs.close();

        int tablesCount = 0;
        for (String table: tables) {
            rs = md.getPrimaryKeys(null, user, table);
            if (rs.next()) {
                String pkName = rs.getString(4);
                sc.addTable(new Table(table, pkName, tablesCount++));
            }
            rs.close();
        }
        return tables;
    }
}
