package org.crazycake.formSqlBuilder.exception;

public class FormSqlBuilderParseException extends Exception {
	
	private String msg;
	
	public FormSqlBuilderParseException(String msg){
		super(msg);
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
