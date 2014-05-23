package org.crazycake.formSqlBuilder.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.crazycake.formSqlBuilder.FormSqlBuilder;
import org.crazycake.formSqlBuilder.exception.FormSqlBuilderParseException;
import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 配置加载工具类
 * @author alex.yang
 *
 */
public class PropertiesUtils {
	
	private static Logger logger = LoggerFactory.getLogger(PropertiesUtils.class);
	
	/**
	 * 加载hqlbuilder.properties的配置文件
	 */
	public static Properties loadProperties() {
		/**
		 * 初始化加载配置文件
		 */
		Properties hqlprop = new Properties();
		hqlprop.setProperty("rules_folder", "formSqlRules");
		InputStream in = FormSqlBuilder.class.getClassLoader().getResourceAsStream("formSqlBuilder.properties");
		try {
			hqlprop.load(in);
			in.close();
		} catch (Throwable e){
			logger.info("Maybe formSqlBuilder.properties doesn't exist... But nevermind!");
		}
		return hqlprop;
	}
	
	/**
	 * 加载配置的json文件
	 * @throws FormSqlBuilderParseException 
	 */
	public static Hashtable<String, Map<String,Rule>> loadJson(String rulesFolder) throws FormSqlBuilderParseException {
		
		Hashtable<String, Map<String,Rule>> queryRules = new Hashtable<String, Map<String,Rule>>();
		
		/**
		 * 初始化加载json映射文件
		 * 通过配置获取映射文件所在文件夹
		 */
		if(rulesFolder != null){
			URL dirUrl = FormSqlBuilder.class.getClassLoader().getResource(rulesFolder);
			if (dirUrl != null && dirUrl.getProtocol().equals("file")) {
		        //获取到映射文件夹
				try {
					File rulesFolderFile = new File(dirUrl.toURI());
					
					//获取映射文件列表
					File[] jsonFiles = rulesFolderFile.listFiles();
					
					ObjectMapper jacksonMapper = new ObjectMapper();
					for (int i = 0; i < jsonFiles.length; i++) {
						parseJsonFile(queryRules, jsonFiles, jacksonMapper, i);
					}
				} catch (URISyntaxException e) {
					logger.error("resolve json folder error",e);
				}
		        
		    } 
			
		}
		
		return queryRules;
	}

	/**
	 * 解析单个文件
	 * @param queryRules
	 * @param jsonFiles
	 * @param jacksonMapper
	 * @param i
	 * @throws FormSqlBuilderParseException 
	 */
	private static void parseJsonFile(Hashtable<String, Map<String, Rule>> queryRules, File[] jsonFiles, ObjectMapper jacksonMapper, int i) throws FormSqlBuilderParseException {
		File jsonFile = jsonFiles[i];
		//使用jackson来解析这些文件
		try {
			/*
			 * 获取到整个json文件
			 */
			Map<String, Object> rawData = jacksonMapper.readValue(jsonFile, Map.class);
			
			Iterator<String> it = rawData.keySet().iterator();
			while(it.hasNext()){
				//解析一个rule scheme
				parseRuleScheme(queryRules, rawData, it);
			}
			
		} catch (JsonParseException e) {
			logger.error("parse json file error",e);
		} catch (JsonMappingException e) {
			logger.error("parse json file error",e);
		} catch (IOException e) {
			logger.error("parse json file error",e);
		}
	}

	/**
	 * 解析rule scheme
	 * @param queryRules
	 * @param rawData
	 * @param it
	 * @throws FormSqlBuilderParseException 
	 */
	private static void parseRuleScheme(Hashtable<String, Map<String, Rule>> queryRules, Map<String, Object> rawData, Iterator<String> it) throws FormSqlBuilderParseException {
		String queryRuleId = it.next();
		
		/*
		 * 单个解析节点，对应的是一组解析规则
		 */
		List<Map<String, Object>> parseRules = (List<Map<String, Object>>)rawData.get(queryRuleId);
		
		/**
		 * 对应这组解析规则的Rule列表
		 * 此处用LinkedHashMap是为了让hql的拼接顺序是按照配置文件来的
		 */
		Map<String,Rule> rules = new LinkedHashMap<String, Rule>();
		
		//group counter
		int groupCounter = 0;
		
		for (int j = 0; j < parseRules.size(); j++) {
			
			//解析单个rule节点
			/**
			 * 获取出单个解析规则
			 */
			Map<String, Object> ruleMap = parseRules.get(j);
			Rule r = null;
			
			if(ruleMap.get("field") != null && !"".equals((String)ruleMap.get("field"))){
				//it should be a rule node
				r = parseOneRule(ruleMap);
				
			}else if(ruleMap.get("members") != null && !"".equals(ruleMap.get("members"))){
				
				//it should be a group
				r = parseOneGroup(ruleMap,groupCounter);
				groupCounter++;
			}else{
				return;
			}

			/**
			 * 添加到解析规则组
			 */
			rules.put(r.getField(),r);
		}
		
		/**
		 * 将这组Rule规则加入到全局Rule规则表
		 */
		queryRules.put(queryRuleId, rules);
	}

	
	/**
	 * 解析单个rule
	 * @return
	 * @throws FormSqlBuilderParseException 
	 */
	private static Rule parseOneRule(Map<String, Object> ruleMap) throws FormSqlBuilderParseException{
		Rule r = new Rule();
		String field = (String)ruleMap.get("field");
		//validate some field error
		validateFieldExpr(field);
		r.setField(field);
		r.setTargetField((String)ruleMap.get("targetField"));
		r.setOp(Operator.find((String)ruleMap.get("op")));
		r.setRel(Relation.find((String)ruleMap.get("rel")));
		return r;
	}

	/**
	 * validate some obvious errors
	 * @param field
	 * @throws FormSqlBuilderParseException
	 */
	private static void validateFieldExpr(String field) throws FormSqlBuilderParseException {
		if(field.startsWith("int:")){
			throw new FormSqlBuilderParseException("Use Integer instead of int! field: "+field);
		}else if(field.startsWith("boolean:")){
			throw new FormSqlBuilderParseException("Use Integer Boolean of boolean! field: "+field);
		}else if(field.startsWith("double:")){
			throw new FormSqlBuilderParseException("Use Integer Double of double! field: "+field);
		}
	}
	
	/**
	 * 解析一个组
	 * @param ruleMap
	 * @param groupCounter
	 * @return
	 * @throws FormSqlBuilderParseException
	 */
	private static Rule parseOneGroup(Map<String, Object> ruleMap, int groupCounter) throws FormSqlBuilderParseException{
		Rule r = new Rule();
		r.setField("_group"+groupCounter);
		r.setRel(Relation.find((String)ruleMap.get("rel")));
		List<Rule> memberList = new ArrayList<Rule>();
		List<Map<String, Object>> members = (List<Map<String, Object>>)ruleMap.get("members");
		for(Map<String, Object> member:members){				
			memberList.add(parseOneRule(member));
		}
		r.setMembers(memberList);
		return r;
	}
}
