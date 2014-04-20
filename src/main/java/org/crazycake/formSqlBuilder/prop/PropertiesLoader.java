package org.crazycake.formSqlBuilder.prop;

import java.util.Properties;

import org.crazycake.formSqlBuilder.utils.PropertiesUtils;

/**
 * 用于读取formSqlBuilder.properties的配置
 * @author alex.yang
 *
 */
public class PropertiesLoader {
	
	/**
	 * formSqlBuilder.properties的配置
	 */
	private static Properties properties;
	
	/**
	 * 初始化读取 formSqlBuilder.properties
	 */
	static{
		/**
		 * 加载 formSqlBuilder.properties 配置文件
		 */
		properties = PropertiesUtils.loadProperties();
	}
	
	/**
	 * 根据key 获取value
	 * @param key
	 * @return
	 */
	public static String get(String key){
		return properties.getProperty(key);
	}
}
