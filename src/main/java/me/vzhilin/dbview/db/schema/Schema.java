package me.vzhilin.dbview.db.schema;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.PrintStream;
import java.io.PrintWriter;
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
}
