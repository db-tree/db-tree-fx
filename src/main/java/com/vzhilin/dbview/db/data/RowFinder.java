package com.vzhilin.dbview.db.data;

import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.schema.Schema;
import com.vzhilin.dbview.db.schema.Table;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;

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
		List<Row> result = new LinkedList<>();

		if (data.matches("\\d+")) {
			result.addAll(findPk(data));
		}

		return result;
	}
	
	private List<Row> findByQuery(String data) throws SQLException {
		List<BigDecimal> idList = ctx.getRunner().query(data, new ColumnListHandler<BigDecimal>(1));
		List<Row> result = new LinkedList<>();
		for (BigDecimal id : idList) {
			result.addAll(findPk(id.toString()));
		}
		
		return result;
	}
//
//
	private Row fetchRow(Table table, BigDecimal id) {
		return new Row(queryContext, table, id.longValue());
	}

//	private List<Row> findPts(String data) throws SQLException {
//		String template = "select ptsid from oeso_pts join oeso_invnum using(invnumid) where ptsinvnumber = :1 or ptsserialnumber = :1";
//		QueryRunner runner = new QueryRunner(ds);
//		Table table = schema.getTable("OESO_PTS");
//		return FluentIterable
//				.from(runner.query(template, new ColumnListHandler<BigDecimal>(1), data, data))
//				.transform(id -> fetchRow(table, id)).toList();
//	}
//
//	private List<Row> findDoc(String data) throws SQLException {
//		String template = "select docid from oeso_doc where docnumber = :1";
//		QueryRunner runner = new QueryRunner(ds);
//		Table table = schema.getTable("OESO_DOC");
//		return FluentIterable
//			.from(runner.query(template, new ColumnListHandler<BigDecimal>(1), data))
//			.transform(id -> fetchRow(table, id)).toList();
//	}
//
//	private List<Row> findKca(String data) throws SQLException {
//		String template = "select kcaid from oeso_kca where kcaregnumber = :1";
//		QueryRunner runner = new QueryRunner(ds);
//		Table table = schema.getTable("OESO_KCA");
//		return FluentIterable
//			.from(runner.query(template, new ColumnListHandler<BigDecimal>(1), data))
//			.transform(id -> fetchRow(table, id)).toList();
//	}

	public List<Row> findPk(String pk) throws SQLException {
		String template = "SELECT '%1$s' as tableName, %2$s as PK FROM %1$s where %2$s = :1";
		
		String query = FluentIterable
                .from(schema.allTables())
                .transform((t) -> String.format(template, t.getName(), t.getPk())).join(Joiner.on(" UNION ALL "));

		Object[] params = Lists.newArrayList(Iterators.limit(Iterators.cycle(pk), schema.allTables().size())).toArray();
		List<RowFinderResult> beanList = ctx.getRunner().query(query, new BeanListHandler<>(RowFinderResult.class), params);

		LinkedList<Row> list = new LinkedList<Row>();
		for (RowFinderResult bean : beanList) {
			list.add(fetchRow(schema.getTable(bean.getTableName()), new BigDecimal(bean.getPk())));
		}
		return list;
	}
}
