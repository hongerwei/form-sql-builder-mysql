package org.crazycake.formSqlBuilder.model;

import java.util.ArrayList;
import java.util.List;

public class QueryNode {
	
	private String field;
	private String op;
	private String rel;
	/**
	 * only used for wildcardTargetField=true
	 */
	private String sourceField;
	
	private List<QueryNode> members = new ArrayList<QueryNode>();
	
	/**
	 * used for one single node
	 * @param field
	 * @param op
	 * @param rel
	 */
	public QueryNode(String field,String op,String rel){
		this.field = field;
		this.op = op;
		this.rel = rel;
	}
	
	/**
	 * used for wildcard matched node with wildcardTargetField=true
	 * @param field
	 * @param op
	 * @param rel
	 * @param sourceField
	 */
	public QueryNode(String field,String op,String rel, String sourceField){
		this.field = field;
		this.op = op;
		this.rel = rel;
		this.sourceField = sourceField;
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

	public String getSourceField() {
		return sourceField;
	}

	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}
}
