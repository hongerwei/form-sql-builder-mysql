package org.crazycake.formSqlBuilder.ruleGenerator;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.Column;

import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.crazycake.formSqlBuilder.prop.PropertiesLoader;
import org.crazycake.formSqlBuilder.utils.CamelNameUtils;

/**
 * 默认的规则方案实现
 * @author alex.yang
 *
 */
public class DefaultRuleSchemeGenerator implements IRuleSchemeGenerator {

	/**
	 * 直接从一个form对象生成queryRule
	 * @param form
	 * @return
	 */
	@Override
	public Map<String, Rule> generateRuleScheme(Object form){
		
		/**
		 * 遍历form所有字段
		 */
		Map<String, Rule> queryRule = new TreeMap<String, Rule>();
		Field[] fields = form.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i].getName();
			
			//如果等于serialVersionUID就跳过
			if("serialVersionUID".equals(fieldName)){
				continue;
			}
						
			Rule r = getDefaultRule(fields[i]);
			queryRule.put(fieldName, r);
		}
		return queryRule;
	}
	
	/**
	 * 判断某个字段是否是忽略字段
	 * @param ifields
	 * @param fieldName
	 * @param isIgnored
	 * @return
	 */
	private boolean isIgnoredField(String[] ifields, String fieldName) {
		boolean isIgnored = false;
		for (int j = 0; j < ifields.length; j++) {
			String ifield = ifields[j];
			if(ifield.equals(fieldName)){
				isIgnored = true;
				break;
			}
		}
		return isIgnored;
	}
	
	/**
	 * 获取基本rule，根据field的类型做不同的判断
	 * @param field
	 * @return
	 */
	private Rule getDefaultRule(Field field){
		Rule r = null;
		if("java.lang.String".equals(field.getType().getName())){
			//如果是字符串类型的
			r = getStringRule(field);
		}else{
			//不是字符串类型的用eq
			r = getNoStringRule(field);
		}
		
		return r;
	}
	
	/**
	 * 获取字符串类型的基本rule
	 * @param fieldName
	 * @return
	 */
	private Rule getStringRule(Field field){
		Rule r = new Rule();
		//普通字段
		r.setField(field.getName());
		//对所有不设条件的字段默认是like操作
		r.setOp(Operator.LIKE);
		r.setRel(Relation.OR);
		return r;
	}

	/**
	 * 猜测字段名
	 * @param field
	 * @return
	 */
	private String guessColumnName(Field field) {
		String colName = null;
		Column col = field.getAnnotation(Column.class);
		if(col != null){
			colName = col.name();
		}
		if(colName == null){
			colName = CamelNameUtils.camel2underscore(field.getName());
		}
		return colName;
	}
	
	/**
	 * 获取非字符串类型的基本rule
	 * @param fieldName
	 * @return
	 */
	private Rule getNoStringRule(Field field){
		Rule r = new Rule();
		//普通字段
		r.setField(field.getName());
		//根据属性猜测列名：1. 如果有Column注解就用注解，2. 如果没有就用命名猜测
		String colName = guessColumnName(field);

		//对所有不设条件的字段默认是like操作
		r.setOp(Operator.EQUAL);
		r.setRel(Relation.AND);
		return r;
	}
}
