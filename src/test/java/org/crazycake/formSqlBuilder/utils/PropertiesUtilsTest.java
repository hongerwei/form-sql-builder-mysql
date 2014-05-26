package org.crazycake.formSqlBuilder.utils;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.crazycake.formSqlBuilder.exception.FormSqlBuilderParseException;
import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.junit.Test;

public class PropertiesUtilsTest {
	
	@Test
	public void testLoadJson() throws FormSqlBuilderParseException{
		Hashtable<String, Map<String, Rule>> ruleMap = PropertiesUtils.loadJson("formSqlRules");
		
		//activeStatus
		Map<String, Rule> ruleScheme = ruleMap.get("groupSearch");
		Rule r = ruleScheme.get("Integer:activeStatus");
		assertThat("activeStatus op should be =", r.getOp(), is(Operator.EQUAL));
		
		//group
		Rule group0 = ruleScheme.get("_group0");
		assertThat("group0 should not be null",group0,is(notNullValue()));
		
		//group0 should have and rel
		assertThat("group0 should not be null",group0.getRel(),is(Relation.AND));
		
		//group members
		assertThat("group0 should have 2 members",group0.getMembers().size(),is(2));
		
		//group member: String:*
		assertThat("group0 should have a String:* member",group0.getMembers().get(0).getField(),is("String:*"));
		
		//group member: String:* op should be like
		assertThat("group0 should have a String:* and its op should be like",group0.getMembers().get(0).getOp(),is(Operator.LIKE));
		
		List<String> fieldList = new ArrayList<String>();
		Iterator<Map.Entry<String, Rule>> ruleIt = ruleScheme.entrySet().iterator();
		while(ruleIt.hasNext()){
			Map.Entry<String, Rule> entry = ruleIt.next();
			String field = entry.getKey();
			fieldList.add(field);
		}
		
		//Integer:* should be after _group0
		assertThat("Integer:* should be after _group0",fieldList.indexOf("Integer:*") > fieldList.indexOf("_group0"),is(true));
	}
	
	@Test(expected=FormSqlBuilderParseException.class)
	public void testExceptionJson() throws FormSqlBuilderParseException{
		PropertiesUtils.loadJson("exceptionRules");
	}
}
