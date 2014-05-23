package org.crazycake.formSqlBuilder.model;

import java.util.List;

import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;

public class Rule {
	private String field;
	private Operator op;
	private Relation rel;
	private Relation groupRel;
	private List<Rule> members;
	
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

	public Relation getGroupRel() {
		return groupRel;
	}

	public void setGroupRel(Relation groupRel) {
		this.groupRel = groupRel;
	}

	public List<Rule> getMembers() {
		return members;
	}

	public void setMembers(List<Rule> members) {
		this.members = members;
	}
	
}
