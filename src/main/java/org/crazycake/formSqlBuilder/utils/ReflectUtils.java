package org.crazycake.formSqlBuilder.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReflectUtils {
	
	private static Logger logger = LoggerFactory.getLogger(ReflectUtils.class);
	
	/**
	 * 从表单中通过反射获取值
	 * @param form
	 * @param fieldName
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Object getFormValue(Object form, String fieldName) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException{
		Object value =  null;
		Method getter = getGetterByFieldName(form,fieldName);
		value = getter.invoke(form);
		return value;
	}
	
	/**
	 * check whether this field is a transient field
	 * @param form
	 * @param fieldName
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static boolean checkIsTransient(Object form,String fieldName) throws SecurityException, NoSuchMethodException{
		boolean isTransient = false;
		Method getter = getGetterByFieldName(form,fieldName);
		Transient transAnno = getter.getAnnotation(Transient.class);
		if(transAnno != null){
			isTransient = true;
		}
		return isTransient;
	}
	
	/**
	 * get getter method by field name
	 * @param form
	 * @param fieldName
	 * @return
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	public static Method getGetterByFieldName(Object form,String fieldName) throws SecurityException, NoSuchMethodException{
		String getterName = "get" + CamelNameUtils.capitalize(fieldName);
		Method getter = form.getClass().getMethod(getterName);
		return getter;
	}
	
}
