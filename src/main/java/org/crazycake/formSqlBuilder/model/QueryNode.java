package org.crazycake.formSqlBuilder.model;

import java.util.ArrayList;
import java.util.List;

public class QueryNode {
	
	private String field;
	private String op;
	private String rel;
	private Object value;
	
	private List<QueryNode> members = new ArrayList<QueryNode>();
	
	/**
	 * used for one single node
	 * @param field
	 * @param op
	 * @param rel
	 */
	public QueryNode(String field,String op,String rel,Object value){
		this.field = field;
		this.op = op;
		this.rel = rel;
		this.value = value;
	}
	
	/**
	 * used for group node
	 * @param members
	 * @param rel
	 */
	public QueryNode(List<QueryNode> members,String rel){
		this.members = members;
		this.rel = rel;
	}
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}
	public String getRel() {
		return rel;
	}
	public void setRel(String rel) {
		this.rel = rel;
	}

	public List<QueryNode> getMembers() {
		return members;
	}

	public void setMembers(List<QueryNode> members) {
		this.members = members;
	}
	
	public void addMember(QueryNode member){
		this.members.add(member);
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
