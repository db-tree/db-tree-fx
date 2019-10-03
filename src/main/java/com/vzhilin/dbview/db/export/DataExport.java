package com.vzhilin.dbview.db.export;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.vzhilin.dbview.ui.tree.TreeTableNode;
import com.vzhilin.dbview.db.schema.Table;
import com.vzhilin.dbview.ui.tree.LeafNode;
import com.vzhilin.dbview.ui.tree.ToManyNode;
import com.vzhilin.dbview.ui.tree.ToOneNode;
import javafx.scene.control.TreeItem;

import java.util.*;

public class DataExport {
    private final StringBuilder joins;

    public DataExport() {
        joins = new StringBuilder();
    }

    public String export(List<TreeItem<TreeTableNode>> nodes) {
        Set<Constraint> constraints = Sets.newLinkedHashSet();
        Set<Column> columns = Sets.newLinkedHashSet();

        Map<TreeItem<TreeTableNode>, List<TreeItem<TreeTableNode>>> ns = Maps.newLinkedHashMap();

        for (TreeItem<TreeTableNode> node: nodes) {
            TreeItem<TreeTableNode> n = node;
            List<TreeItem<TreeTableNode>> path = Lists.newArrayList();
            if (n instanceof LeafNode) {
                n = n.getParent();
            }
            if (n instanceof ToOneNode && n.getParent() instanceof ToOneNode) {
                n = n.getParent();
            }

            while (n.getParent() != null) {
                path.add(n);
                n = n.getParent();
            }
            Collections.reverse(path);
            ns.put(node, path);
        }

        ToOneNode rootNode = (ToOneNode) Iterables.getFirst(ns.values().iterator().next(), null);
        int aliasCount = 1;

        Map<TreeItem<TreeTableNode>, String> aliases = Maps.newLinkedHashMap();
        for (Map.Entry<TreeItem<TreeTableNode>, List<TreeItem<TreeTableNode>>> e: ns.entrySet()) {
            Iterator<TreeItem<TreeTableNode>> it = e.getValue().iterator();

            ToOneNode root = (ToOneNode) it.next();
            aliases.put(root, "t0");

            while (it.hasNext()) {
                TreeItem<TreeTableNode> n = it.next();
                if (!aliases.containsKey(n)) {
                    if (n instanceof ToOneNode) {
                        ToOneNode node = (ToOneNode) n;
                        String parentProp = n.getValue().itemColumnProperty().getValue();
                        String parentAlias = aliases.get(n.getParent());

                        if (node.getParent() instanceof ToManyNode) {
                            TreeItem<TreeTableNode> toMany = node.getParent();
                            constraints.add(new Constraint(aliases.get(toMany), node.getRow().getTable().getPk(), node.getRow().getPk()));
                            aliases.put(n, aliases.get(toMany));
                        } else {
                            String alias = "t" + aliasCount++;
                            aliases.put(n, alias);

                            joins.append(String.format("JOIN %s %s ON %s.%s = %s.%s\n", node.getRow().getTable(), alias, parentAlias, parentProp, alias, node.getRow().getTable().getPk()));
                        }
                    } else
                    if (n instanceof ToManyNode){
                        String alias = "t" + aliasCount++;
                        aliases.put(n, alias);

                        ToManyNode toMany = (ToManyNode) n;

                        Map.Entry<Table, String> rel = toMany.getRelation();
                        String parentProp = rel.getValue();
                        String parentAlias = aliases.get(n.getParent());

//                        constraints.add(new Constraint(aliases.get(toMany), rel.getKey().getPk(), toMany.getRow().getPk()));
                        joins.append(String.format("JOIN %s %s ON %s.%s = %s.%s\n", rel.getKey().getName(), alias, alias, parentProp, parentAlias, toMany.getRow().getTable().getPk()));
                    }
                }
            }
        }

        for (TreeItem<TreeTableNode> node: nodes) {
            TreeItem<TreeTableNode> n = node;

            if (n instanceof LeafNode) {
                columns.add(new Column(aliases.get((ToOneNode) n.getParent()), ((LeafNode) n).getColumn()));
            } else if (n instanceof ToOneNode) {
                TreeItem<TreeTableNode> parent = n.getParent();

                if (parent instanceof ToOneNode) {
                    columns.add(new Column(aliases.get(parent), n.getValue().itemColumnProperty().getValue()));
                } else {
                    columns.add(new Column(aliases.get(n.getParent()), ((ToOneNode) n).getRow().getTable().getPk()));
                }
            } else {
                ToManyNode toMany = (ToManyNode) n;
                columns.add(new Column(aliases.get(toMany), toMany.getRelation().getKey().getPk()));
            }

            continue;
        }

        for (Map.Entry<TreeItem<TreeTableNode>, List<TreeItem<TreeTableNode>>> e: ns.entrySet()) {
            constraints.add(new Constraint(aliases.get(rootNode), rootNode.getRow().getTable().getPk(), rootNode.getRow().getPk()));
        }

        joins.append("WHERE ").append(Joiner.on(" AND ").join(constraints));
        String cs = Joiner.on(", ").join(columns);
        String value = "SELECT " + cs + " FROM " + rootNode.getRow().getTable() + " t0\n" + joins.toString();
        System.err.println(value);
        return value;
    }

    private final static class Column {
        private final String alias;
        private final String column;

        public Column(String alias, String column) {
            this.alias = alias;
            this.column = column;
        }

        @Override
        public String toString() {
            return alias + "." + column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Column column1 = (Column) o;
            return Objects.equals(alias, column1.alias) &&
                    Objects.equals(column, column1.column);
        }

        @Override
        public int hashCode() {
            return Objects.hash(alias, column);
        }
    }

    private final static class Constraint {
        private final String alias;
        private final String column;
        private final Object value;

        public Constraint(String alias, String column, Object value) {
            this.alias = alias;
            this.column = column;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s.%s = %s", alias, column, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Constraint that = (Constraint) o;
            return Objects.equals(alias, that.alias) &&
                    Objects.equals(column, that.column) &&
                    Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(alias, column, value);
        }
    }
}
