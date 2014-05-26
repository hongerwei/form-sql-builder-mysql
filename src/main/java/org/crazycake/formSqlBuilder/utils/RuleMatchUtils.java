package org.crazycake.formSqlBuilder.utils;

import java.lang.reflect.Field;

import org.crazycake.formSqlBuilder.model.QueryNode;
import org.crazycake.formSqlBuilder.model.Rule;

public class RuleMatchUtils {
	
	/**
	 * match field with wildcard expression
	 * @param field
	 * @param rule
	 * @return
	 */
	public static QueryNode wildcardMatch(Field field, Rule rule){
		QueryNode node = null;
		String fieldExpr = rule.getField();
		
		if("*".endsWith(fieldExpr)){
			//match!
			node = new QueryNode(field.getName(),rule.getOp().getSql(),rule.getRel().getSql());
		}
		
		if(fieldExpr.contains(":")){
			String[] temp = rule.getField().split(":");
			String typeExpr = temp[0];
			String nameExpr = temp[1];
			
			if(matchType(typeExpr,field) && matchWildcardName(nameExpr,field.getName())){
				//match!
				node = createNodeAfterMatch(field, rule, nameExpr);
			}
			
		}else{
			if(matchWildcardName(fieldExpr,field.getName())){
				//match!
				node = createNodeAfterMatch(field, rule, fieldExpr);
			}
		}
		
		return node;
	}

	/**
	 * match 过后创建node
	 * if wildcardTargetField=true 建立一个含有 sourceField 的 queryNode
	 * if not 建立一个不含sourceField的queryNode
	 * @param field
	 * @param rule
	 * @param fieldExpr
	 * @return
	 */
	private static QueryNode createNodeAfterMatch(Field field, Rule rule, String fieldExpr) {
		QueryNode node;
		if(rule.getWildcardTargetField()){
			node = new QueryNode(getWildcardTargetField(fieldExpr, field.getName()),rule.getOp().getSql(),rule.getRel().getSql(),field.getName());
		}else{
			node = new QueryNode(field.getName(),rule.getOp().getSql(),rule.getRel().getSql());
		}
		return node;
	}
	
	/**
	 * match type
	 * @param typeExpr
	 * @param field
	 * @return
	 */
	private static boolean matchType(String typeExpr,Field field){
		if("*".equals(typeExpr)){
			return true;
		}
		String fieldTypeName = field.getType().getName();
		String fieldTypeClassName = fieldTypeName.substring(fieldTypeName.lastIndexOf(".")+1);
		
		return fieldTypeClassName.equalsIgnoreCase(typeExpr);
	}
	
	/**
	 * match field name with wildcard
	 * @param wildcardExpression
	 * @param fieldName
	 * @return
	 */
	private static boolean matchWildcardName(String wildcardExpression, String fieldName){
		int wildcardIndex = wildcardExpression.indexOf("*");
		if(wildcardIndex==0){
			//begin of line
			String suffix = wildcardExpression.substring(1);
			if(fieldName.endsWith(suffix)){
				//match!
				return true;
			}
		}else if(wildcardIndex==(wildcardExpression.length()-1)){
			//end of line
			String prefix = wildcardExpression.substring(0,wildcardExpression.length()-1);
			if(fieldName.startsWith(prefix)){
				return true;
			}
		}else{
			//middle of line
			String[] fixArr = wildcardExpression.split("*");
			String prefix = fixArr[0];
			String suffix = fixArr[1];
			if(fieldName.startsWith(prefix)&&fieldName.endsWith(suffix)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * get wildcard field
	 * @param wildcardExpr
	 * @param fieldName
	 * @return
	 */
	private static String getWildcardTargetField(String wildcardExpr,String fieldName){
		String targetField = "";
		int wildcardIndex = wildcardExpr.indexOf("*");
		if(wildcardIndex==0){
			//begin of line
			String suffix = wildcardExpr.substring(1);
			int suffixLen = suffix.length();
			targetField = fieldName.substring(0, (fieldName.length()-suffixLen));
		}else if(wildcardIndex==(wildcardExpr.length()-1)){
			//end of line
			String prefix = wildcardExpr.substring(0,wildcardExpr.length()-1);
			targetField = fieldName.substring(prefix.length());
		}else{
			//middle of line
			String[] fixArr = wildcardExpr.split("*");
			String prefix = fixArr[0];
			String suffix = fixArr[1];
			//match!
			int suffixLen = suffix.length();
			targetField = fieldName.substring(prefix.length(),(fieldName.length()-suffixLen));
		}
		return targetField;
	}
	
	/**
	 * fullname like String:name
	 * @param field
	 * @param rule
	 * @return
	 */
	public static QueryNode fullnameMatch(Field field, Rule rule){
		
		//full name match
		String[] temp = rule.getField().split(":");
		String typeExpr = temp[0];
		String nameExpr = temp[1];
		if(!field.getName().equals(nameExpr)){
			return null;
		}
		Class type = field.getType();
		String className = type.getName();
		String shortName = className.substring(className.lastIndexOf(".")+1);
		if(!shortName.equalsIgnoreCase(typeExpr)){
			return null;
		}
		
		//match!
		QueryNode node = new QueryNode(field.getName(),rule.getOp().getSql(),rule.getRel().getSql());
		return node;
	}
	
	/**
	 * shorname is a field name
	 * @param field
	 * @param rule
	 * @return
	 */
	public static QueryNode shortnameMatch(Field field, Rule rule){
		String fieldExpr = rule.getField();
		if(!field.getName().equals(fieldExpr)){
			return null;
		}
		
		//match!
		QueryNode node = new QueryNode(field.getName(), rule.getOp().getSql(),rule.getRel().getSql());
		return node;
		
	}
}
