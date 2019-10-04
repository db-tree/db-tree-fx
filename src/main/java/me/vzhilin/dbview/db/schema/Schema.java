package me.vzhilin.dbview.db.schema;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Хранит описание структуры БД: таблицы, колонки и ограничения ссылочной целостности
 */
public final class Schema {

    /**
     * Отображение имени таблицы на таблицу
     */
    private final BiMap<String, Table> allTables = HashBiMap.create();

    /**
     * @return все таблицы
     */
    public Set<Table> allTables() {
        return allTables.values();
    }

    /**
     * @return имена таблиц
     */
    public Set<String> allTableNames() {
        return allTables.keySet();
    }

    /**
     * Конструктор по умолчанию
     */
    public Schema() {
    }

    /**
     * Конструктор копирования
     * @param schema прототип
     */
    public Schema(Schema schema) {
        int tableCount = 0;
        for (Table protoTable: schema.allTables()) {
            Table table = new Table(protoTable.getName(), protoTable.getPk(), ++tableCount);
            addTable(table);

            for (String column: protoTable.getColumns()) {
                table.addColumn(column, protoTable.getDataType(column));
            }
        }

        for (Table protoTable: schema.allTables()) {
            for (Map.Entry<String, Table> e: protoTable.getRelations().entrySet()) {
                String column = e.getKey();
                getTable(protoTable.getName()).addRelation(column, getTable(e.getValue().getName()));
            }
        }
    }

    /**
     * Добавляет таблицу
     * @param table таблица
     */
    public void addTable(Table table) {
        Preconditions.checkState(!allTables.containsValue(table));
        allTables.put(table.getName(), table);
    }

    /**
     * Находит таблицу по имени
     * @param tableName имя таблицы
     * @return таблица
     */
    public Table getTable(String tableName) {
        Preconditions.checkState(allTables.containsKey(tableName), "таблица не найдена: " + tableName);
        return allTables.get(tableName);
    }

    /**
     * Проверяем наличие таблицы
     * @param tableName имя таблицы
     * @return true, если в БД есть таблица
     */
    public boolean hasTable(String tableName) {
        return allTables.containsKey(tableName);
    }

    /**
     * Форматированный вывод для отладки
     * @param out поток вывода
     */
    public void prettyPrint(PrintStream out) {
        PrintWriter pw = new PrintWriter(out);

        for (Table tb: allTables.values()) {
            pw.write(tb.getName() + "\n");

            for (String c: tb.getColumns()) {
                pw.write(c + " " + tb.getDataType(c) + "\n");
            }

            for (String c: tb.getRelations().keySet()) {
                pw.write(c + " -> " + tb.getRelations().get(c).getName() + "\n");
            }

            pw.write("\n");
        }

        pw.flush();
    }

    /**
     * Конвертирует имя таблицы (String) в Table
     * @param tableNames имена таблиц
     * @return таблицы
     */
    public Collection<Table> getTables(Collection<String> tableNames) {
        Set<Table> result = Sets.newLinkedHashSet();
        for (String name: tableNames) {
            if (hasTable(name)) {
                result.add(getTable(name));
            }
        }

        return result;
    }
}
