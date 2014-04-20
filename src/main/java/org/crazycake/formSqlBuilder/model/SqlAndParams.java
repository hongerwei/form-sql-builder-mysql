package org.crazycake.formSqlBuilder.model;

import java.util.Arrays;

public class SqlAndParams {
	
	private String sql;
	
	private Object[] params;
	
	public SqlAndParams(){
		
	}
	
	public SqlAndParams(String sql,Object[] params){
		this.sql = sql;
		this.params = params;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	@Override
	public String toString() {
		return "SqlAndParams [sql=" + sql + ", params="
				+ Arrays.toString(params) + "]";
	}
	
}
