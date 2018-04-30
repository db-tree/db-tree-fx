package com.vzhilin.dbview.db.data;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.vzhilin.dbview.conf.ConnectionSettings;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.schema.Schema;
import com.vzhilin.dbview.db.schema.Table;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class RowFinder {
	private final DbContext ctx;
	private final QueryContext queryContext;
	private Schema schema;

	public RowFinder(QueryContext queryContext) {
		this.queryContext = queryContext;
		this.ctx = queryContext.getDbContext();
		this.schema = ctx.getSchema();
	}
	
	public List<Row> find(String data) throws SQLException {
		boolean isDigit = data.matches("\\d+");
		String template = "SELECT '%1$s' as tableName, %2$s as PK FROM %1$s where %3$s = :1";

		ConnectionSettings settings = queryContext.getSettings();

		// table --> [column]
		List<String> queries = Lists.newArrayList();
		for (String tableName: schema.allTableNames()) {
			Table t = schema.getTable(tableName);
			for (String columnName: t.getColumns()) {
				if (settings.isLookupable(tableName, columnName)) {
					if (isDigit || !t.getPk().equals(columnName)) {
						queries.add(String.format(template, tableName, t.getPk(), columnName));
					}
				}
			}
		}

		String query = Joiner.on(" UNION ALL ").join(queries);

		Object[] params = Lists.newArrayList(Iterators.limit(Iterators.cycle(data), queries.size())).toArray();
		List<RowFinderResult> beanList = ctx.getRunner().query(query, new BeanListHandler<>(RowFinderResult.class), params);

		LinkedList<Row> list = new LinkedList<>();
		for (RowFinderResult bean : beanList) {
			list.add(fetchRow(schema.getTable(bean.getTableName()), new BigDecimal(bean.getPk())));
		}
		return list;
	}


	private Row fetchRow(Table table, BigDecimal id) {
		return new Row(queryContext, table, id.longValue());
	}
}
