package org.crazycake.formSqlBuilder.ruleGenerator;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import org.crazycake.formSqlBuilder.SqlGenerator;
import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.crazycake.formSqlBuilder.prop.PropertiesLoader;


/**
 * jxc 的 hqlBuilder默认rule方案
 * 方案内容：
 * 1. 检测带From字段是否有去除From后缀的字段存在，如果有将 col 设置为 去除From后的原字段
 * 比如： 同时存在 purOrderDateFrom 和 purOrderDate 字段，则对purOrderDateFrom生成以下规则
 * {
			"field":"purOrderDateFrom",
			"col":"purOrderDate",
			"op":">",
			"rel":"and"
	}
	
	带To也做同样的处理
	
	2. 有值的字段用lk生成规则
 * @author alex.yang
 *
 */
public class JxcRuleSchemeGenerator implements IRuleSchemeGenerator{
	
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
		Map<String, Rule> ruleScheme = new TreeMap<String, Rule>();
		Field[] fields = form.getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String fieldName = field.getName();
						
			//如果是 serialVersionUID 就跳过
			if("serialVersionUID".equals(fieldName)){
				continue;
			}
			
			Rule r = null;
			if(fieldName.endsWith("From")){
				//如果带From
				String originalFieldName = fieldName.substring(0, fieldName.lastIndexOf("From"));
				Field originalField = getField(fields, originalFieldName);
				if(originalField != null){
					r = new Rule();
					r.setField(fieldName);
					r.setOp(Operator.GREAT_THAN);
					r.setRel(Relation.AND);
				}
			}
			
			if(r == null && fieldName.endsWith("To")){
				//如果带To
				String originalFieldName = fieldName.substring(0, fieldName.lastIndexOf("To"));
				Field originalField = getField(fields, originalFieldName);
				if(originalField!=null){
					r = new Rule();
					r.setField(fieldName);
					r.setOp(Operator.LESS_THAN);
					r.setRel(Relation.AND);
				}
			}
			
			if(r == null){
				//普通字段
				r = getDefaultRule(field);
			}
			
			
			ruleScheme.put(fieldName, r);
		}
		return ruleScheme;
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
		r.setRel(Relation.AND);
		return r;
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

		//对所有不设条件的字段默认是like操作
		r.setOp(Operator.EQUAL);
		r.setRel(Relation.AND);
		return r;
	}
	
	/**
	 * 获取某字段
	 * @param fieldName
	 * @return
	 */
	private Field getField(Field[] fields,String fieldName){
		Field found = null;
		for(Field f:fields){
			if(f.getName().equals(fieldName)){
				found = f;
			}
		}
		return found;
	}
	

}
