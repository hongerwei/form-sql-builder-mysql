package org.crazycake.formSqlBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.Table;

import org.crazycake.formSqlBuilder.annotation.DefaultSort;
import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.Sort;
import org.crazycake.formSqlBuilder.model.SqlAndParams;
import org.crazycake.formSqlBuilder.prop.RuleSchemeLoader;
import org.crazycake.formSqlBuilder.ruleGenerator.DefaultRuleSchemeGenerator;
import org.crazycake.formSqlBuilder.ruleGenerator.IRuleSchemeGenerator;
import org.crazycake.formSqlBuilder.utils.CamelNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将表单
 * @author Administrator
 *
 */
public class FormSqlBuilder {
	
	private static Logger logger = LoggerFactory.getLogger(FormSqlBuilder.class);

	/**
	 * 要查询的表名
	 */
	private String tableName;

	/**
	 * 排序的条件
	 */
	private List<Sort> sorts = new ArrayList<Sort>();

	/**
	 * 单页最大记录数
	 */
	private int rows;
	
	/**
	 * 当前到第几页
	 */
	private int page;
	
	/**
	 * 查询表单
	 */
	private Object form;
	
	/**
	 * 动态传入的ruleScheme，如果有这个，优先使用
	 * 如果没有就使用 IRuleSchemeGenerator 创建
	 * 如果没有IRuleSchemeGenerator，就在HbJsonRuleScheme里面找
	 */
	private Map<String, Rule> ruleScheme;
	
	/**
	 * ruleScheme生成器
	 * 如果没有 IRuleSchemeGenerator 就在 HbJsonRuleScheme里面找
	 */
	private IRuleSchemeGenerator ruleSchemeGenerator;
	
	/**
	 * json文件里面的 rule scheme (映射规则) 的id
	 */
	private String ruleId;
	
	/**
	 * 添加排序
	 * @param sorts
	 * @return
	 */
	public FormSqlBuilder addSort(Sort sort){
		this.sorts.add(sort);
		return this;
	}

	/**
	 *  添加翻页
	 * @param pageCount 单页最大记录数
	 * @param pageNum 当前到第几页
	 * @return
	 */
	public FormSqlBuilder addLimit(int page, int rows){
		this.rows = rows;
		this.page = page;
		return this;
	}
	
	
	/**
	 * 获取查询规则的MAP queryRule
	 * @return
	 */
	private Map<String, Rule> generateRuleScheme() {
		Map<String, Rule> ruleScheme;
		if(this.ruleSchemeGenerator != null){
			//如果有配置ruleSchemeGenerator，就用这个生成
			ruleScheme = this.ruleSchemeGenerator.generateRuleScheme(this.form);
		}else if(ruleId != null && !"".equals(ruleId)){
			//如果有配置 ruleId
			ruleScheme = RuleSchemeLoader.get(ruleId);
		}else if(RuleSchemeLoader.get("*") != null){
			//如果有配置全局解析规则
			ruleScheme = RuleSchemeLoader.get("*");
		}else{
			//如果全部没有就采用默认的DefaultRuleSchemeGenerator
			DefaultRuleSchemeGenerator defaultRuleSchemeGenerator = new DefaultRuleSchemeGenerator();
			ruleScheme = defaultRuleSchemeGenerator.generateRuleScheme(this.form);
		}
		return ruleScheme;
	}

	/**
	 * 构建出的PreparedStatement对象专门用于统计行数
	 * @param session
	 * @return
	 * @throws SQLException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws HqlBuildException
	 */
	public SqlAndParams buildCount() throws FormIsNullException, SQLException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException{
		if(this.form == null){
			throw new FormIsNullException("form cannot be null!");
		}
		
		//如果表名没有设置：1.根据注解获取表名 2.根据类名自动猜测
		if(tableName == null){
			tableName = guessTableName(form);
		}
				
		/*
		 * 1. 生成查询规则
		 */
		ruleScheme = generateRuleScheme();
		
		/*
		 * 2. 生成sql语句和参数列表
		 */
		SqlAndParams sqlAndParams = SqlGenerator.generateCountSql(this.form, ruleScheme,tableName);
		
		String sql = sqlAndParams.getSql();
		/**
		 * 4. 添加Sort条件
		 */
		sql = SqlGenerator.appendSort(sql,this.form,this.sorts);
		
		/**
		 * 5. 添加分页条件
		 */
		sql = SqlGenerator.appendPage(sql,this.page,this.rows);
		
		logger.debug("sql: " + sql);
		
		sqlAndParams.setSql(sql);
		
		return sqlAndParams;
	}

	/**
	 * 使用session 构建出query对象
	 * 最核心的方法
	 * @param session
	 * @param isCount
	 * @return
	 * @throws SQLException 
	 * @throws FormIsNullException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws NoSuchMethodException 
	 * @throws IllegalArgumentException 
	 * @throws NoSuchFieldException 
	 * @throws SecurityException 
	 * @throws HqlBuildException
	 */
	public SqlAndParams build() throws SQLException, FormIsNullException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SecurityException, NoSuchFieldException{
		
		if(this.form == null){
			throw new FormIsNullException("form cannot be null!");
		}
		
		//如果表名没有设置：1.根据注解获取表名 2.根据类名自动猜测
		if(tableName == null){
			tableName = guessTableName(form);
		}
		
		/*
		 * 1. 生成查询规则
		 */
		ruleScheme = generateRuleScheme();
		
		//如果没有设置排序用默认排序
		useDefaultSortIfNotSet();
		
		/*
		 * 2. 生成sql语句和参数列表
		 */
		SqlAndParams sqlAndParams = SqlGenerator.generateSql(this.form, ruleScheme,tableName);
		
		String sql = sqlAndParams.getSql();
		/**
		 * 4. 添加Sort条件
		 */
		sql = SqlGenerator.appendSort(sql,this.form,this.sorts);
		
		/**
		 * 5. 添加分页条件
		 */
		sql = SqlGenerator.appendPage(sql,this.page,this.rows);
		
		logger.debug("sql: " + sql);
		sqlAndParams.setSql(sql);
		
		return sqlAndParams;
	}

	/**
	 * 如果没有设置排序字段用默认排序
	 * @throws NoSuchMethodException
	 */
	private void useDefaultSortIfNotSet() throws NoSuchMethodException {
		if(this.sorts == null){
			List<Sort> sortsList = new ArrayList<Sort>();
			Field[] fields = form.getClass().getDeclaredFields();
			for(Field f:fields){
				String getterName = "get" + CamelNameUtils.capitalize(f.getName());
				Method getter = form.getClass().getMethod(getterName);
				DefaultSort defaultSortAnno = getter.getAnnotation(DefaultSort.class);
				if(defaultSortAnno!=null){
					boolean asc = defaultSortAnno.asc();
					String orderStr = "";
					if(asc){
						orderStr = "asc";
					}else{
						orderStr = "desc";
					}
					Sort sort = new Sort(f.getName(), orderStr);
					sortsList.add(sort);
				}
			}
			
			this.sorts = sortsList;
		}
	}

	/**
	 * 通过form类猜测表名
	 * @param form
	 * @return
	 */
	private String guessTableName(Object form) {
		String tableName = "";
		Table tableAnno = form.getClass().getAnnotation(Table.class);
		if(tableAnno != null){
			tableName = tableAnno.name();
		}else{
			String className = form.getClass().getName();
			String camelName = className.substring(className.lastIndexOf(".")+1);
			tableName = CamelNameUtils.camel2underscore(camelName);
		}
		
		return tableName;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Object getForm() {
		return form;
	}

	public void setForm(Object form) {
		this.form = form;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public IRuleSchemeGenerator getRuleSchemeGenerator() {
		return ruleSchemeGenerator;
	}

	public void setRuleSchemeGenerator(IRuleSchemeGenerator ruleSchemeGenerator) {
		this.ruleSchemeGenerator = ruleSchemeGenerator;
	}

	public Map<String, Rule> getRuleScheme() {
		return ruleScheme;
	}

	public void setRuleScheme(Map<String, Rule> ruleScheme) {
		this.ruleScheme = ruleScheme;
	}
	
}
