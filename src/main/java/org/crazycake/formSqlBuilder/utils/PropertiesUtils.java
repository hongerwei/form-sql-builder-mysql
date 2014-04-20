package org.crazycake.formSqlBuilder.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
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
	 */
	public static Hashtable<String, Map<String,Rule>> loadJson(String rulesFolder) {
		
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
						File jsonFile = jsonFiles[i];
						//使用jackson来解析这些文件
						try {
							/*
							 * 获取到整个json文件
							 */
							Map<String, Object> rawData = jacksonMapper.readValue(jsonFile, Map.class);
							
							Iterator<String> it = rawData.keySet().iterator();
							while(it.hasNext()){
								String queryRuleId = it.next();
								
								/*
								 * 单个解析节点，对应的是一组解析规则
								 */
								List<Map<String, String>> parseRules = (List<Map<String, String>>)rawData.get(queryRuleId);
								
								/**
								 * 对应这组解析规则的Rule列表
								 * 此处用LinkedHashMap是为了让hql的拼接顺序是按照配置文件来的
								 */
								Map<String,Rule> rules = new LinkedHashMap<String, Rule>();
								
								for (int j = 0; j < parseRules.size(); j++) {
									
									/**
									 * 获取出单个解析规则
									 */
									Map<String, String> ruleMap = parseRules.get(j);
									
									Rule r = new Rule();
									r.setField(ruleMap.get("field"));
									r.setTargetField(ruleMap.get("targetField"));
									r.setOp(Operator.find(ruleMap.get("op")));
									r.setRel(Relation.find(ruleMap.get("rel")));
									

									/**
									 * 添加到解析规则组
									 */
									rules.put(ruleMap.get("field"),r);
								}
								
								/**
								 * 将这组Rule规则加入到全局Rule规则表
								 */
								queryRules.put(queryRuleId, rules);
							}
							
						} catch (JsonParseException e) {
							logger.error("parse json file error",e);
						} catch (JsonMappingException e) {
							logger.error("parse json file error",e);
						} catch (IOException e) {
							logger.error("parse json file error",e);
						}
					}
				} catch (URISyntaxException e) {
					logger.error("resolve json folder error",e);
				}
		        
		    } 
			
		}
		
		return queryRules;
	}
}
