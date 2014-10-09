package org.crazycake.formSqlBuilder.utils;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.crazycake.formSqlBuilder.testvo.Person;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

public class ReflectUtilsTest {

	@Test
	public void testGetFormValue() throws SecurityException, IllegalArgumentException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		
		Person form = new Person("lily", 12, "newyork", 1);
		Object v = ReflectUtils.getFormValue(form, "city");
		
		assertThat((String)v,is("newyork"));
	}

	@Test
	public void testCheckIsTransient() throws SecurityException, NoSuchMethodException {
		Person form = new Person("lily", 12, "newyork", 1);
		form.setPassword("123");
		
		boolean y = ReflectUtils.checkIsTransient(form, "password");
		
		assertThat(y,is(true));
	}

	@Test
	public void testGetGetterByFieldName() throws SecurityException, NoSuchMethodException {
		Person form = new Person("lily", 12, "newyork", 1);
		form.setPassword("123");
		
		Method getter = ReflectUtils.getGetterByFieldName(form, "city");
		
		assertThat(getter.getName(),is("getCity"));
	}

	@Test
	public void testGuessColumnName() throws SecurityException, NoSuchMethodException {
		Person form = new Person("lily", 12, "newyork", 1);
		
		String c = ReflectUtils.guessColumnName(form, "className");
		
		assertThat(c,is("class_name"));
	}

	@Test
	public void testGetValue() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Person form = new Person("lily", 12, "newyork", 1);
		
		String v = (String) ReflectUtils.getValue(form, "name");
		
		assertThat(v,is("lily"));
	}

}
