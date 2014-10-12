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

import org.crazycake.formSqlBuilder.model.QueryNode;
import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.Sort;
import org.crazycake.formSqlBuilder.model.SqlAndParams;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.utils.ReflectUtils;
import org.crazycake.formSqlBuilder.utils.RuleMatchUtils;
import org.crazycake.utils.CamelNameUtils;

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
				sql += " ORDER BY ";
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
		sql = sql + " LIMIT " + ((page-1)*rows) + "," + rows;
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
	public SqlAndParams generateSqlAndParams(Object form, Map<String, Rule> ruleScheme,String tableName) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException{
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM " + tableName + " ");
		
		SqlAndParams sqlAndParams = generateQueryBody(form, ruleScheme, sql);
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
	public SqlAndParams generateCountSqlAndParams(Object form, Map<String, Rule> ruleScheme,String tableName) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException{
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT count(1) FROM " + tableName + " ");
		
		SqlAndParams sqlAndParams = generateQueryBody(form, ruleScheme, sql);
		
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
	private SqlAndParams generateQueryBody(Object form, Map<String, Rule> ruleScheme, StringBuffer sql) throws IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException {
		
		List<Object> params = new ArrayList<Object>();
		
		//pick field
		List<QueryNode> collectedResult = pickFieldWithRule(form, ruleScheme);
		
		//use collectedResult to generate sql and params
		int paramCounter = 0;
		for(QueryNode queryNode : collectedResult){
			//if queryNode is a group
			if(queryNode.getMembers().size()>0){
				
				if(paramCounter > 0){
					sql.append(queryNode.getRel() + " ");
				}else{
					sql.append("WHERE ");
				}
				
				sql.append("( ");
				List<QueryNode> memberNodes = queryNode.getMembers();
				for(int i=0;i<memberNodes.size();i++){
					QueryNode node = memberNodes.get(i);
					if(i != 0){
						sql.append(node.getRel() + " ");
					}
					sql.append(generateSql(form, node));
					addParams(params, node);
				}
				sql.append(") ");
				
				paramCounter++;
			}else{
				//a single query node
				if(paramCounter > 0){
					sql.append(queryNode.getRel() + " ");
				}else{
					sql.append("WHERE ");
				}
				
				sql.append(generateSql(form, queryNode));
				addParams(params, queryNode);
				
				paramCounter++;
			}
		}
		
		//create sqlAndParams
		SqlAndParams sqlAndParams = new SqlAndParams();
		sqlAndParams.setParams(params.toArray());
		sqlAndParams.setSql(sql.toString());

		//return it!
		return sqlAndParams;
	}
	
	/**
	 * add params
	 * @param params
	 * @param queryNode
	 */
	private void addParams(List<Object> params, QueryNode queryNode) {
		if(queryNode.getValue() !=null && queryNode.getValue().getClass().getName().endsWith(".ArrayList")){
			//if the value is a list, for example: in operator
			List<Object> vlist = (List)queryNode.getValue();
			for(Object v:vlist){
				params.add(v);
			}
		}else{
			params.add(queryNode.getValue());
		}
	}
	
	/**
	 * generate sql
	 * @param form
	 * @param node
	 * @return
	 * @throws NoSuchMethodException
	 */
	private String generateSql(Object form, QueryNode node) throws NoSuchMethodException {
		String sql = "";
		if(Operator.IN.getSql().equals(node.getOp()) || Operator.NOT_IN.getSql().equals(node.getOp())){
			List<Object> vlist = (List)node.getValue();
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for(int i=0;i<vlist.size();i++){
				if(i!=0){
					sb.append(",");
				}
				sb.append("?");
			}
			sb.append(")");
			
			sql = ReflectUtils.guessColumnName(form,node.getField()) + " " + node.getOp() + " " + sb.toString() + " ";
		}else{
			sql = ReflectUtils.guessColumnName(form,node.getField()) + " " + node.getOp() + " ? ";
		}
		return sql;
	}
	
	/**
	 * 从form中获取value
	 * if sourceField has value then use sourceField to getValue
	 * if not use field to getValue
	 * @param form
	 * @param queryNode
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
//	private Object getValue(Object form,QueryNode queryNode) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
//		String sourceField = queryNode.getSourceField();
//		if(sourceField != null && !"".equals(sourceField)){
//			return ReflectUtils.getValue(form,sourceField);
//		}else{
//			return ReflectUtils.getValue(form,queryNode.getField());
//		}
//	}
	
	/**
	 * pick field from class with rule
	 * @param form
	 * @param ruleScheme
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private List<QueryNode> pickFieldWithRule(Object form, Map<String, Rule> ruleScheme) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		//遍历all fields to make a field map
		List<Field> fieldList = createFieldList(form);
		
		//query node list to save the collect result
		List<QueryNode> collectedResult = new ArrayList<QueryNode>();
		
		//遍历rule，用rule来collect field map. Once a field been collected , it will be removed from field list
		Iterator<Map.Entry<String, Rule>> ruleIt = ruleScheme.entrySet().iterator();
		while(ruleIt.hasNext()){
			Map.Entry<String, Rule> ruleEntry = ruleIt.next();
			Rule rule = ruleEntry.getValue();
			
			//scan the field map and try to collect field
			List<QueryNode> pickResult = pickFields(fieldList, rule, form);
			collectedResult.addAll(pickResult);
		}
		
		return collectedResult;
	}
	
	/**
	 * pick field without : 1. no getter 2. no value
	 * @param form
	 * @return
	 */
	private List<Field> createFieldList(Object form){
		List<Field> fieldList = new ArrayList<Field>();
		
		Field[] allFields = form.getClass().getDeclaredFields();
		
		for(Field field : allFields){
			
			//check if hasGetter
			Method getter = null;
			try {
				getter = ReflectUtils.getGetterByFieldName(form, field.getName());
			} catch (Exception e) {
				continue;
			}
			
			//check this field whether it has value
			Object value = null;
			try {
				value = getter.invoke(form);
			} catch (Exception e) {
				continue;
			}
			
			if(value != null){
				fieldList.add(field);
			}
		}
		
		return fieldList;
	}
	
	/**
	 * 用一个rule从field列表里面检索出符合条件的 queryNode
	 * @param fieldList
	 * @param rule
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private List<QueryNode> pickFields(List<Field> fieldList, Rule rule, Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		
		List<QueryNode> pickResult = new ArrayList<QueryNode>();
		
		if(rule.getMembers() != null && rule.getMembers().size()>0){
			//it's a group
			List<Rule> members = rule.getMembers();
			List<QueryNode> queryNodeMembers = new ArrayList<QueryNode>();
			for(Rule member:members){
				List<QueryNode> queryNodes = pickFieldsWithSingleRule(fieldList,member,form);
				queryNodeMembers.addAll(queryNodes);
			}
			if(queryNodeMembers.size()>0){
				QueryNode groupQueryNode = new QueryNode(queryNodeMembers, rule.getRel().getSql());
				pickResult.add(groupQueryNode);
			}
		}else{
			//it's a single node
			List<QueryNode> queryNodes = pickFieldsWithSingleRule(fieldList,rule,form);
			pickResult.addAll(queryNodes);
		}
		
		return pickResult;
	}
	
	/**
	 * pick fields with this rule
	 * @param fieldList
	 * @param rule
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private List<QueryNode> pickFieldsWithSingleRule(List<Field> fieldList, Rule rule,Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		List<QueryNode> pickResult = new ArrayList<QueryNode>();
		
		Iterator<Field> it = fieldList.iterator();
		while(it.hasNext()){
			Field field = it.next();
			
			QueryNode queryNode = matchRule(field,rule, form);
			
			if(queryNode != null){
				pickResult.add(queryNode);
				it.remove();
			}
		}
		return pickResult;
	}
	
	/**
	 * try to match field with this rule
	 * @param field
	 * @param rule
	 * @return
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 */
	private QueryNode matchRule(Field field,Rule rule, Object form) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		QueryNode queryNode = null;
		
		String fieldExpr = rule.getField();
		if(fieldExpr.contains("*")){
			//wildcard match
			queryNode = RuleMatchUtils.wildcardMatch(field, rule, form);
		}else if(fieldExpr.contains(":")){
			//full name match
			queryNode = RuleMatchUtils.fullnameMatch(field, rule, form);
		}else{
			//short nama match
			queryNode = RuleMatchUtils.shortnameMatch(field, rule, form);
		}
		
		return queryNode;
		
	}

	
	
	
}
