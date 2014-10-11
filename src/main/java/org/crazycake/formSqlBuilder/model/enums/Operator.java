package org.crazycake.formSqlBuilder.model.enums;

/**
 *  每个运算符都有一个缩写
	以下是缩写对应的操作
	eq -> equal
	nq -> not_equal
	in -> in (,为分隔符)
	nn -> not_in
	lk -> like
	nk -> not_like
	gt就是great_than
	lt就是less_than
	gteq就是great_than_or_equal_to
	lteq就是less_than_or_equal_to
	
	默认是lk
 * @author alex.yang
 *
 */
public enum Operator {
	
	EQUAL("eq","="),
	LESS_THAN("lt","<"),
	GREAT_THAN("gt",">"),
	NOT_EQUAL("nq","<>"),
	LIKE("lk","like"),
	NOT_LIKE("nk","not like"),
	IN("in","in"),
	NOT_IN("Nn","not in"),
	LESS_THAN_OR_EQUAL_TO("lteq","<="),
	GREAT_THAN_OR_EQUAL_TO("gteq",">=");
	
	private String code;
	private String sql;
	
	private Operator(String code,String sql){
		this.code = code;
		this.sql = sql;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getSql() {
		return sql;
	}
	
	/**
	 * 根据缩写获取
	 * @param abbr
	 * @return
	 */
	public static Operator find(String code){
		Operator result = Operator.LIKE;
		Operator[] ops = Operator.values();
		for(Operator op:ops){
			if(op.getSql().equals(code)){
				result = op;
				break;
			}
		}
		return result;
	}
}
