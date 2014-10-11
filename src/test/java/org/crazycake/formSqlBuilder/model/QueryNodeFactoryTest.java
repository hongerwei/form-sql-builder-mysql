package org.crazycake.formSqlBuilder.model;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.crazycake.formSqlBuilder.testvo.Person;

import static org.hamcrest.CoreMatchers.*;

import org.junit.Test;

public class QueryNodeFactoryTest {

	@Test
	public void testCreateQueryNode() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Person p = new Person("rose", 23, "xiamen", 1);
		Rule r = new Rule("String:name", Operator.EQUAL, Relation.AND);
		QueryNode node = QueryNodeFactory.createQueryNode("name", r, p);
		
		assertThat(node.getField(), is("name"));
		assertThat(node.getOp(),is("="));
		assertThat(node.getRel(),is("AND"));
		assertThat((String)node.getValue(),is("rose"));
	}
	
	@Test
	public void testCreateQueryNodeWithWildcard() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Person p = new Person("rose", 23, "xiamen", 1);
		p.setBirthdayFrom("1980-01-01");
		Rule r = new Rule("String:*From", Operator.GREAT_THAN, Relation.AND);
		r.setWildcardTargetField(true);
		QueryNode node = QueryNodeFactory.createQueryNode("birthdayFrom", r, p);
		
		assertThat(node.getField(), is("birthday"));
		assertThat(node.getOp(),is(">"));
		assertThat(node.getRel(),is("AND"));
		assertThat((String)node.getValue(),is("1980-01-01"));
	}
	
	@Test
	public void testCreateQueryNodeWithIn() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		
		Person p = new Person("rose", 23, "xiamen", 1);
		p.setSelectedRoles("admin,normaluser");
		Rule r = new Rule("String:selected*", Operator.IN, Relation.AND);
		r.setWildcardTargetField(true);
		QueryNode node = QueryNodeFactory.createQueryNode("selectedRoles", r, p);
		
		assertThat(node.getField(), is("roles"));
		assertThat(node.getOp(),is("in"));
		assertThat(node.getRel(),is("AND"));
		ArrayList expect = new ArrayList();
		expect.add("admin");
		expect.add("normaluser");
		assertThat((ArrayList)node.getValue(),is(expect));
		
	}
	

}
