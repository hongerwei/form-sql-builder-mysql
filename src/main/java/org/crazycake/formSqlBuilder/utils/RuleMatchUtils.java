package org.crazycake.formSqlBuilder.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.crazycake.formSqlBuilder.model.QueryNode;
import org.crazycake.formSqlBuilder.model.QueryNodeFactory;
import org.crazycake.formSqlBuilder.model.Rule;

public class RuleMatchUtils {
	
	/**
	 * match field with wildcard expression
	 * @param field
	 * @param rule
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	public static QueryNode wildcardMatch(Field field, Rule rule, Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		QueryNode node = null;
		String fieldExpr = rule.getField();
		
		if("*".equals(fieldExpr)){
			//match!
			node = QueryNodeFactory.createQueryNode(field.getName(), rule, form);
		}
		
		if(fieldExpr.contains(":")){
			String[] temp = rule.getField().split(":");
			String typeExpr = temp[0];
			String nameExpr = temp[1];
			
			if(matchType(typeExpr,field) && matchWildcardName(nameExpr,field.getName())){
				//match!
				node = createNodeAfterMatch(field, rule, nameExpr, form);
			}
			
		}else{
			if(matchWildcardName(fieldExpr,field.getName())){
				//match!
				node = createNodeAfterMatch(field, rule, fieldExpr, form);
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
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private static QueryNode createNodeAfterMatch(Field field, Rule rule, String fieldExpr, Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return QueryNodeFactory.createQueryNode(field.getName(), rule, form);
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
	 * fullname like String:name
	 * @param field
	 * @param rule
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	public static QueryNode fullnameMatch(Field field, Rule rule, Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		
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
		QueryNode node = QueryNodeFactory.createQueryNode(field.getName(), rule, form);
		return node;
	}
	
	/**
	 * shorname is a field name
	 * @param field
	 * @param rule
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	public static QueryNode shortnameMatch(Field field, Rule rule, Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		String fieldExpr = rule.getField();
		if(!field.getName().equals(fieldExpr)){
			return null;
		}
				
		//match!
		QueryNode node = QueryNodeFactory.createQueryNode(field.getName(), rule, form);
		return node;
		
	}
}
