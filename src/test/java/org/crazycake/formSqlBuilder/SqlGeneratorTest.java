package org.crazycake.formSqlBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
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
		Object form = new Person("michael",28,"miami",1);
		
		
		Map<String, Rule> ruleScheme = new HashMap<String, Rule>();
		Rule activeStatusRule = new Rule();
		activeStatusRule.setField("Integer:activeStatus");
		activeStatusRule.setOp(Operator.EQUAL);
		activeStatusRule.setRel(Relation.AND);
		ruleScheme.put(activeStatusRule.getField(), activeStatusRule);
		
		Rule group0Rule = new Rule();
		group0Rule.setField("_group0");
		group0Rule.setGroupRel(Relation.AND);
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
			sqlAndParams = SqlGenerator.generateSqlAndParams(form, ruleScheme, tableName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String sql = sqlAndParams.getSql();
		
		//check activeStatus
		assertThat("the sql should have active_status = 1", sql.contains("active_status = ?"), is(true));
		
		//check name
		assertThat("the sql should have name like ?", sql.contains("name like ?"), is(true));
		
		//name and city and age should at one group
		String subSql = sql.substring(sql.indexOf("(")+1, sql.lastIndexOf(")"));
		boolean inOneGroup = subSql.contains("name like ?") && subSql.contains("city like ?") && subSql.contains("age = ?");
		assertThat("name and city and age should at one group",inOneGroup,is(true));
		
		//name and city and age should connect with or
		String tempSql = subSql.substring(subSql.lastIndexOf("or"));
		boolean connectWithOr = subSql.contains("or ") && tempSql.contains("or ");
		assertThat("name and city and age should connect with or",connectWithOr,is(true));
		
		//active_status should be out of group
		boolean outOfGroup = !subSql.contains("active_status");
		assertThat("active_status should be out of group",outOfGroup,is(true));
		
	}
}
