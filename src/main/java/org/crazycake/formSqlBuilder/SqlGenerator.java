package org.crazycake.formSqlBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;

import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.Sort;
import org.crazycake.formSqlBuilder.model.SqlAndParams;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.utils.CamelNameUtils;
import org.crazycake.formSqlBuilder.utils.ReflectUtils;

/**
 * hibernate query对象的工具类
 * @author alex.yang
 *
 */
public class SqlGenerator {
	
	/**
	 * 往sql上拼接排序条件
	 * @param hql
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public static String appendSort(String sql,Object form,List<Sort> sorts) throws SecurityException, NoSuchMethodException{
		if(sorts == null){
			return sql;
		}
		
		for (int i = 0; i < sorts.size(); i++) {
			if(i!=0){
				sql += ",";
			}else{
				sql += " order by ";
			}
			Sort sort = sorts.get(i);
			
			//将sort字段转成数据库列名
			String getterName =  "get" + CamelNameUtils.capitalize(sort.getSort());
			Method getter = form.getClass().getMethod(getterName);			
			String sortCol = "";
			Column colAnno = getter.getAnnotation(Column.class);
			if(colAnno != null){
				sortCol = colAnno.name();
			}else{
				sortCol = CamelNameUtils.camel2underscore(sort.getSort());
			}
			
			sql += sortCol + " " + sort.getOrder().getSql();
		}
		return sql;
	}
	/**
	 * 设置查询范围
	 * 如果 page=-1 或者 rows=-1 都表示不限定范围
	 * @param query
	 * @param page 从1开始
	 * @param rows
	 */
	public static String appendPage(String sql,int page,int rows){
		if(rows == 0 || page == 0){
			return sql;
		}
		if(rows == -1 || page == -1){
			return sql;
		}
		sql = sql + " limit " + ((page-1)*rows) + "," + rows;
		return sql;
	}
	
	/**
	 * 使用criterionResult生成 Query对象
	 * @param session
	 * @param cr
	 * @return
	 * @throws SQLException 
	 */
	public static PreparedStatement generatePs(Connection conn,String sql,Object[] params) throws SQLException {
		
		//生成query对象
		PreparedStatement ps = conn.prepareStatement(sql);
		
		//遍历values并设置值
		for(int i=0; i<params.length; i++){
			ps.setObject((i+1), params[i]);
		} 
		return ps;
	}
	
	/**
	 * 生成sql语句和参数列表
	 * @param form
	 * @param ruleScheme
	 * @param tableName
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	public static SqlAndParams generateSql(Object form, Map<String, Rule> ruleScheme,String tableName) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException{
		
		StringBuffer sql = new StringBuffer();
		sql.append("select * from " + tableName + " ");
		
		List<Object> params = generateParams(form, ruleScheme, sql);
		
		SqlAndParams sqlAndParams = new SqlAndParams(sql.toString(), params.toArray());
		return sqlAndParams;
	}
	
	/**
	 * 生成count语句和参数列表
	 * @param form
	 * @param ruleScheme
	 * @param tableName
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	public static SqlAndParams generateCountSql(Object form, Map<String, Rule> ruleScheme,String tableName) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException{
		
		StringBuffer sql = new StringBuffer();
		sql.append("select count(1) from " + tableName + " ");
		
		List<Object> params = generateParams(form, ruleScheme, sql);
		
		SqlAndParams sqlAndParams = new SqlAndParams(sql.toString(), params.toArray());
		return sqlAndParams;
	}
	
	/**
	 * 构建参数列表和sql
	 * @param form
	 * @param ruleScheme
	 * @param sql
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 */
	private static List<Object> generateParams(Object form,
			Map<String, Rule> ruleScheme, StringBuffer sql) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException {
		List<Object> params = new ArrayList<Object>();
		
		/**
		 * 遍历Form对象所有属性
		 * 顺序是映射文件里面从上到下
		 */
		Field[] fields = form.getClass().getDeclaredFields();
		
		//参数计数器
		int paramCount = 0;
		for(int i=0;i<fields.length;i++){
			Field field = fields[i];
			String fieldName = field.getName();
			//如果是 serialVersionUID 属性就跳过
			if("serialVersionUID".equals(fieldName)){
				continue;
			}
			
			/**
			 * 获取值对象
			 */
			Object value = ReflectUtils.getFormValue(form, fieldName);
			
			/**
			 * 如果 value == null 表示本次查询不对该条件限制
			 */
			if(value==null){
				continue;
			}
			
			//获取该field对应的规则
			Rule rule = getRuleByField(ruleScheme, field);
			if(rule==null){
				continue;
			}
			
			//如果是第一个参数就加where
			if(paramCount == 0){
				sql.append("where ");
			}
			
			//如果不是第一个参数就加关系符号(and 或者 or)
			if(paramCount != 0){
				sql.append(rule.getRel().getSql() + " ");
			}
			String targetField = rule.getTargetField();
			if(targetField == null || "".equals(targetField)){
				targetField = rule.getField();
			}
			sql.append(guessColumnName(form,targetField) + " " + rule.getOp().getSql() + " ? ");
			
			if(rule.getOp()==Operator.LIKE && value instanceof String){
				//等于操作并且类型是String的要把值前后加%
				params.add("%" + value + "%");
			}else{
				params.add(value);
			}
			
			paramCount++;
		}
		return params;
	}
	
	/**
	 * 根据fieldName获取匹配的Rule
	 * @param fieldName
	 * @return
	 */
	private static Rule getRuleByField(Map<String, Rule> ruleScheme,Field field){
		Rule rule = null;
		
		//directly get Rule by fieldName
		rule = ruleScheme.get(field.getName());
		if(rule != null){
			return rule;
		}
		
		//if we can't get Rule directly by fieldName , try regex type
		Iterator<Map.Entry<String, Rule>> it = ruleScheme.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<String, Rule> entry = it.next();
			String wildcardDefined = entry.getKey();
			if(wildcardDefined.indexOf(":") == -1){
				//if wildcardDefined doesn't contain : means it's not a correct format , so pass it
				continue;
			}

			String[] temp = wildcardDefined.split(":");
			
			//rule type
			String typeName = temp[0];
			
			//field match wildcard
			String wildcardExpression = temp[1];
			
			//if type not a "*" or equal field class name it will continue.
			boolean cannotMatchType = checkTypeMatch(field, typeName);
			if(cannotMatchType){
				continue;
			}
			
			//wildcard match begin!
			
			//get target field 
			String targetField = getTargetFieldByWildcard(wildcardExpression,field);
			if("".equals(targetField)){
				//not match!
				continue;
			}
			
			//all match!
			Rule matchedRule = entry.getValue();
			rule = new Rule();
			rule.setField(matchedRule.getField());
			rule.setOp(matchedRule.getOp());
			rule.setRel(matchedRule.getRel());
			if(matchedRule.getTargetField() == null || "".equals(matchedRule.getTargetField())){
				rule.setTargetField(targetField);
			}else{
				rule.setTargetField(matchedRule.getTargetField());
			}
			break;
			
		}
		return rule;
	}
	
	/**
	 * check whether field class name matched type name
	 * @param field
	 * @param typeName
	 * @return
	 */
	private static boolean checkTypeMatch(Field field,String typeName){
		Class fieldClass = field.getType();
		String fieldClassName = fieldClass.getName();
		String fieldClassLastName = fieldClassName.substring(fieldClassName.lastIndexOf(".")+1);
		boolean cannotMatchType = !"*".equals(typeName) && !fieldClassLastName.equals(typeName);
		return cannotMatchType;
	}
	
	/**
	 * get origin field by wildcard and fieldname
	 * @param wildcardExpression
	 * @param fieldName
	 * @return
	 */
	private static String getTargetFieldByWildcard(String wildcardExpression, Field field) {
		String fieldName = field.getName();
		String targetField = "";
		if("*".endsWith(wildcardExpression)){
			//match any word
			targetField = fieldName;
			return targetField;
		}
		
		int wildcardIndex = wildcardExpression.indexOf("*");
		if(wildcardIndex==0){
			//begin of line
			String suffix = wildcardExpression.substring(1);
			if(fieldName.endsWith(suffix)){
				//match!
				int suffixLen = suffix.length();
				targetField = fieldName.substring(0, (fieldName.length()-suffixLen));
			}
		}else if(wildcardIndex==(wildcardExpression.length()-1)){
			//end of line
			String prefix = wildcardExpression.substring(0,wildcardExpression.length()-1);
			if(fieldName.startsWith(prefix)){
				//match!
				targetField = fieldName.substring(prefix.length());
			}
		}else{
			//middle of line
			String[] fixArr = wildcardExpression.split("*");
			String prefix = fixArr[0];
			String suffix = fixArr[1];
			if(fieldName.startsWith(prefix)&&fieldName.endsWith(suffix)){
				//match!
				int suffixLen = suffix.length();
				targetField = fieldName.substring(prefix.length(),(fieldName.length()-suffixLen));
			}
		}
		return targetField;
	}

	
	/**
	 * 猜测字段名
	 * @param field
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws SecurityException 
	 */
	public static String guessColumnName(Object form,String fieldName) throws SecurityException, NoSuchMethodException {
		String colName = "";
		
		//try to get col name from annotation
		Method getter = ReflectUtils.getGetterByFieldName(form,fieldName);
		Column colAnno = getter.getAnnotation(Column.class);
		if(colAnno != null){
			//if this field getter hasAnnotation
			colName = colAnno.name();
		}
		if("".equals(colName)){
			colName = CamelNameUtils.camel2underscore(fieldName);
		}
		return colName;
	}
	
}
