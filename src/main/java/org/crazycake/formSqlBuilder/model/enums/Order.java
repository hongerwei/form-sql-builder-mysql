package org.crazycake.formSqlBuilder.model.enums;

public enum Order {
	ASC(1,"asc"),DESC(2,"desc");
	
	private int code;
	private String sql;
	
	private Order(int c,String sql){
		code = c;
		this.sql = sql;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getSql(){
		return this.sql;
	}
}
