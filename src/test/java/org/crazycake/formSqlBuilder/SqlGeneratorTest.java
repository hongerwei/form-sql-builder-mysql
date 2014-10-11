package org.crazycake.formSqlBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.SqlAndParams;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.crazycake.formSqlBuilder.testvo.Person;
import org.junit.Test;

public class SqlGeneratorTest {

	@Test
	public void testGenerateSqlAndParams(){
		Person form = new Person("michael",28,"miami",1);
		form.setBirthdayFrom("2000-01-01");
		
		Map<String, Rule> ruleScheme = new LinkedHashMap<String, Rule>();
		Rule activeStatusRule = new Rule();
		activeStatusRule.setField("Integer:activeStatus");
		activeStatusRule.setOp(Operator.EQUAL);
		activeStatusRule.setRel(Relation.AND);
		ruleScheme.put(activeStatusRule.getField(), activeStatusRule);
		
		Rule birthdayFromRule = new Rule();
		birthdayFromRule.setField("*:*From");
		birthdayFromRule.setOp(Operator.GREAT_THAN);
		birthdayFromRule.setRel(Relation.AND);
		birthdayFromRule.setWildcardTargetField(true);
		ruleScheme.put("*:*From", birthdayFromRule);
		
		Rule group0Rule = new Rule();
		group0Rule.setField("_group0");
		group0Rule.setRel(Relation.AND);
		List<Rule> members = new ArrayList<Rule>();
		Rule stringRule = new Rule();
		stringRule.setField("String:*");
		stringRule.setOp(Operator.LIKE);
		stringRule.setRel(Relation.OR);
		members.add(stringRule);
		
		Rule anyRule  = new Rule();
		anyRule.setField("*:*");
		anyRule.setOp(Operator.EQUAL);
		anyRule.setRel(Relation.OR);
		members.add(anyRule);
		
		group0Rule.setMembers(members);
		
		ruleScheme.put("_group0", group0Rule);
		
		
		
		String tableName = "person";
		
		SqlAndParams sqlAndParams = null;
		try {
			SqlGenerator sqlGenerator = new SqlGenerator();
			sqlAndParams = sqlGenerator.generateSqlAndParams(form, ruleScheme, tableName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String sql = sqlAndParams.getSql();
		
		//check sql syntax
		assertThat("the sql should have right syntax! sql=["+sql+"]",sql.contains("SELECT ") && sql.contains(" FROM ") && sql.contains(" WHERE "),is(true));
		
		//check activeStatus
		assertThat("the sql should have active_status = 1", sql.contains("active_status = ?"), is(true));
		
		//check name
		assertThat("the sql should have name like ? ! sql=["+sql+"]", sql.contains("name like ?"), is(true));
		
		//name and city and age should at one group
		String subSql = sql.substring(sql.indexOf("(")+1, sql.lastIndexOf(")"));
		boolean inOneGroup = subSql.contains("name like ?") && subSql.contains("city like ?") && subSql.contains("age = ?");
		assertThat("name and city and age should at one group! sql=["+sql+"]",inOneGroup,is(true));
		
		//name and city and age should connect with or
		String tempSql = subSql.substring(subSql.lastIndexOf("OR"));
		boolean connectWithOr = subSql.contains("OR ") && tempSql.contains("OR ");
		assertThat("name and city and age should connect with or",connectWithOr,is(true));
		
		//active_status should be out of group
		boolean outOfGroup = !subSql.contains("active_status");
		assertThat("active_status should be out of group",outOfGroup,is(true));
		
		//should have birthday > ?
		assertThat("sql should have birthday > ?",sql.contains("birthday > ?"),is(true));
		
		
		//test values
		Object[] values = sqlAndParams.getParams();
		assertThat("first should be 1",(Integer)values[0],is(1));
		assertThat("second should be 2000-01-01",(String)values[1],is("2000-01-01"));
		assertThat("3th should be michael",(String)values[2],is("michael"));
		assertThat("4th should be miami",(String)values[3],is("miami"));
		assertThat("5th should be 28",(Integer)values[4],is(28));
	}
}
