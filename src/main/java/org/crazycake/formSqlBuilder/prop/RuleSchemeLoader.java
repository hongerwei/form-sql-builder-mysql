package org.crazycake.formSqlBuilder.prop;

import java.util.Hashtable;
import java.util.Map;

import org.crazycake.formSqlBuilder.exception.FormSqlBuilderParseException;
import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.utils.PropertiesUtils;


/**
 * 读取hqlbuilder.properties配置的 rules_folder （默认是hqlbuilder）文件夹下的所有.json文件
 * @author alex.yang
 *
 */
public class RuleSchemeLoader {
	
	/**
	 * 映射的json文件集合
	 */
	private static Hashtable<String, Map<String,Rule>> ruleSchemes = new Hashtable<String, Map<String,Rule>>();
	
	static{
		/**
		 * 加载查询规则的json文件夹
		 */
		String rulesFolder = (String)PropertiesLoader.get("rules_folder");
		try {
			ruleSchemes = PropertiesUtils.loadJson(rulesFolder);
		} catch (FormSqlBuilderParseException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String,Rule> get(String ruleSchemeId){
		return ruleSchemes.get(ruleSchemeId);
	}
}
