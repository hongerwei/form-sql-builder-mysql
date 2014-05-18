package org.crazycake.formSqlBuilder.model.enums;

public enum Relation {
	AND(1,"AND"),OR(2,"OR");
	
	private int code;
	private String sql;
	
	private Relation(int c,String s){
		code = c;
		sql = s;
	}
	
	public int getCode() {
		return code;
	}
	
	public String getSql(){
		return sql;
	}
	
	/**
	 * 根据短语获取Relation对象
	 * @param abbr
	 * @return
	 */
	public static Relation find(String abbr){
		Relation ralation = Relation.AND;
		Relation[] rels = Relation.values();
		for(Relation rel:rels){
			if(rel.getSql().equalsIgnoreCase(abbr)){
				ralation = rel;
				break;
			}
		}
		return ralation;
	}
}
