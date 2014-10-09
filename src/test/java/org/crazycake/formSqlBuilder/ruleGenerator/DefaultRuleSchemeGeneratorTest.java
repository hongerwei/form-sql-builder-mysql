package org.crazycake.formSqlBuilder.ruleGenerator;

import static org.junit.Assert.*;

import java.util.Map;

import org.crazycake.formSqlBuilder.model.Rule;
import org.crazycake.formSqlBuilder.model.enums.Operator;
import org.crazycake.formSqlBuilder.model.enums.Relation;
import org.crazycake.formSqlBuilder.testvo.Person;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

public class DefaultRuleSchemeGeneratorTest {

	@Test
	public void testGenerateRuleScheme() {
		DefaultRuleSchemeGenerator g = new DefaultRuleSchemeGenerator();
		Person form = new Person("ted", 22, "xiamen", 1);
		Map<String, Rule> m = g.generateRuleScheme(form);
		
		assertThat(m.get("name").getOp(),CoreMatchers.is(Operator.LIKE));
		
		assertThat(m.get("age").getRel(),CoreMatchers.is(Relation.AND));
	}

}
