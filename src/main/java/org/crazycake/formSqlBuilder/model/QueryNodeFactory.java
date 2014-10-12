package org.crazycake.formSqlBuilder.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.utils.ReflectUtils;

public class QueryNodeFactory {
	
	/**
	 * Create queryNode 
	 * @param field
	 * @param rule
	 * @param form
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public static QueryNode createQueryNode(String sourceField, Rule rule, Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		
		//if operator is IN
		Object v = ReflectUtils.getValue(form, sourceField);
		if(rule.getOp()==Operator.IN || rule.getOp()==Operator.NOT_IN){
			String vs = (String)v;
			String[] varray = vs.split(",");
			ArrayList<Object> vlist = new ArrayList<Object>();
			for(String vstr:varray){
				vlist.add(vstr);
			}
			v = vlist;
		}
		
		//if wildcardTargetField is been assigned
		String targetField = "";
		if(rule.getWildcardTargetField()){
			
			String[] temp = rule.getField().split(":");
			String nameExpr = temp[1];
			
			targetField = getWildcardTargetField(nameExpr, sourceField);
		}else if(rule.getTargetField()!=null){
			
			targetField = rule.getTargetField();
		}else{
			
			targetField = sourceField;
		}
		
		//create QueryNode
		QueryNode node = new QueryNode(targetField,rule.getOp().getSql(),rule.getRel().getSql(), v);
		return node;
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
			//turn the first character to lower case
			String firstCharacter = targetField.substring(0,1).toLowerCase();
			targetField = firstCharacter + targetField.substring(1, targetField.length());
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
}
