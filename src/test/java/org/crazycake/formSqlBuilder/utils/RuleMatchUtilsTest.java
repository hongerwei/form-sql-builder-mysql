package org.crazycake.formSqlBuilder.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.crazycake.formSqlBuilder.model.QueryNode;
import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.crazycake.formSqlBuilder.testvo.Person;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class RuleMatchUtilsTest {

	@Test
	public void testWildcardMatch() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException{
		
		Person tommy = new Person("tommy", 18, "new york", 0);
		tommy.setAfterThat("2011-12-12");
		
		Rule rule1 = new Rule();
		rule1.setField("integer:activeStatus");
		rule1.setOp(Operator.EQUAL);
		rule1.setRel(Relation.AND);
		
		//integer
		QueryNode queryNode = RuleMatchUtils.fullnameMatch(pickField(tommy,"activeStatus"), rule1, tommy);
		assertThat("activeStatus should be matched!", queryNode, is(notNullValue()));
		
		//Integer
		Rule capitalRule = new Rule("Integer:activeStatus", Operator.EQUAL, Relation.AND);
		QueryNode capitalQueryNode = RuleMatchUtils.fullnameMatch(pickField(tommy,"activeStatus"), capitalRule, tommy);
		assertThat("activeStatus should be matched!", capitalQueryNode, is(notNullValue()));
		
		//after* -> afterThat
		Rule wildcardBeginRule = new Rule("*:after*", Operator.EQUAL, Relation.AND);
		QueryNode afterQueryNode = RuleMatchUtils.wildcardMatch(pickField(tommy,"afterThat"), wildcardBeginRule, tommy);
		assertThat("afterThat should be matched!", afterQueryNode, is(notNullValue()));
		
		//*From -> BirthdayFrom
		Rule wildcardEndRule = new Rule("*:*From", Operator.EQUAL, Relation.AND);
		QueryNode wildcardEndQueryNode = RuleMatchUtils.wildcardMatch(pickField(tommy,"birthdayFrom"), wildcardEndRule, tommy);
		assertThat("birthdayFrom should be matched!", wildcardEndQueryNode, is(notNullValue()));

	}
	
	private Field pickField(Object form,String fieldName){
		Field[] fields = form.getClass().getDeclaredFields();
		for(Field f:fields){
			if(f.getName().equals(fieldName)){
				return f;
			}
		}
		return null;
	}
}
