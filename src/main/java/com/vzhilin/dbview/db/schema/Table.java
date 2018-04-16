package com.vzhilin.dbview.db.schema;

import com.google.common.base.Objects;
import com.google.common.collect.*;

import java.util.Map;
import java.util.Set;

/**
 * Таблица
 */
public final class Table {
    /** Имя таблицы */
    private final String name;

    /** первичный ключ */
    private final String pk;

    /** порядковый номер таблицы */
    private final int index;

    /** имя колонки --> колонка */
    private BiMap<String, Column> columns = HashBiMap.create();

    /** имя колонки --> связанная таблица */
    private Map<String, Table> relations = Maps.newLinkedHashMap();

    /** связанная таблца --> колонка ("обратная ссылка") */
    private Multimap<Table, String> backRelations = LinkedHashMultimap.create();

    /**
     * Таблица
     * @param name имя таблицы
     * @param pk первичный ключ
     * @param index номер таблицы
     */
    public Table(String name, String pk, int index) {
        this.name = name;
        this.pk = pk;
        this.index = index;
    }

    /**
     * @return порядковый номер таблицы
     */
    int getIndex() {
        return index;
    }

    /**
     * @return имя таблицы
     */
    public String getName() {
        return name;
    }

    /**
     * Добавляет связанную таблицу
     * @param column колонка
     * @param table таблица
     */
    public void addRelation(String column, Table table) {
        relations.put(column, table);
    }

    /**
     * Обратное отношение
     * @param tableFrom таблица
     * @param columnName колонка
     */
    void addReverseRelation(Table tableFrom, String columnName) {
        backRelations.put(tableFrom, columnName);
    }

    /**
     * reverse relationships
     * @return
     */
    public Multimap<Table, String> getBackRelations() {
        return ImmutableMultimap.copyOf(backRelations);
    }

    /**
     * @return колонка - первичный ключ
     */
    public String getPk() {
        return pk;
    }

    /**
     * @return все колонки таблицы
     */
    public Set<String> getColumns() {
        return columns.keySet();
    }

    /**
     * @return ссылки
     */
    public Map<String, Table> getRelations() {
        return relations;
    }

    /**
     * Добавляет колонку
     * @param columnName имя колонки
     * @param dataType тип
     */
    public void addColumn(String columnName, String dataType) {
        columns.put(columnName, new Column(columnName, columns.size(), dataType));
    }

    /**
     * Удаляет колонку
     * @param column имя колонки
     */
    public void removeColumn(String column) {
        columns.remove(column);
    }

    @Override public String toString() {
        return name;
    }

    /**
     * @param columnName имя колонки
     * @return тип
     */
    public String getDataType(String columnName) {
        return columns.get(columnName).getDataType();
    }

    /**
     * Проверяет, есть ли колонка в таблице
     * @param columnName имя колонки
     * @return true, если в таблице есть колонка
     */
    public boolean hasColumn(String columnName) {
        return columns.containsKey(columnName);
    }

    /**
     * @return колонка типа CLOB
     */
    public String getClobColumn() {
        for (String column: getColumns()) {
            if ("CLOB".equals(getDataType(column))) {
                return column;
            }
        }

        return null;
    }

    public Set<String> getForeignKeyColumns() {
        return relations.keySet();
    }

    /** Колонка */
    private static final class Column {
        /** Имя колонки */
        private final String name;

        /** Порядковый номер */
        private final int index;

        /** Тип колонки */
        private final String dataType;

        /**
         * Колонка
         * @param name имя колонки
         * @param index порядковый номер
         * @param dataType тип
         */
        private Column(String name, int index, String dataType) {
            this.name = name;
            this.index = index;
            this.dataType = dataType;
        }

        /**
         * @return порядковый номер
         */
        public int getIndex() {
            return index;
        }

        /**
         * @return тип
         */
        String getDataType() {
            return dataType;
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Table table = (Table) o;
        return Objects.equal(name, table.name);
    }

    @Override public int hashCode() {
        return Objects.hashCode(name);
    }
}
