package com.vzhilin.dbview.db.data;

public class RowFinderResult {
	private String tableName;
	private String pk;
	
	public RowFinderResult() {
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getPk() {
		return pk;
	}

	public void setPk(String pk) {
		this.pk = pk;
	}
	
	@Override
	public String toString() {
		return String.format("[%s %s]", tableName, pk);
	}
}