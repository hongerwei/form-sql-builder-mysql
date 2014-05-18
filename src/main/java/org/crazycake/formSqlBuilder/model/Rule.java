package org.crazycake.formSqlBuilder.model;

import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;

public class Rule {
	private String field;
	private Operator op;
	private Relation rel;
	
	private String targetField;
	
	public Rule(){}
	
	public Rule(String field,Operator op,Relation rel){
		this.field = field;
		this.op = op;
		this.rel = rel;
	}
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}

	public Operator getOp() {
		return op;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

	public Relation getRel() {
		return rel;
	}

	public void setRel(Relation rel) {
		this.rel = rel;
	}

	public String getTargetField() {
		return targetField;
	}

	public void setTargetField(String targetField) {
		this.targetField = targetField;
	}
	
}
