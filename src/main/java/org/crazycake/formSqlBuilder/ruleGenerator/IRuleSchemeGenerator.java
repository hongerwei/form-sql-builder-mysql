package org.crazycake.formSqlBuilder.ruleGenerator;

import java.util.Map;

import org.crazycake.formSqlBuilder.model.Rule;

/**
 * 规则方案接口
 * 所有规则方案都要实现该接口
 * 直接传入一个规则方案类是另一种规则生成方法（相对于用json定义规则方案而言）
 * 优先级方面 直接传RuleScheme > 传 IRuleSchemeGenerator > json配置，如果全部没有就采用默认的DefaultRuleSchemeGenerator
 * 使用规则方案可以自动化根据某个规律生成规则，比如默认的规则方案是这样的：
 * 1. 检测带From字段是否有去除From后缀的字段存在，如果有将 col 设置为 去除From后的原字段
 * 比如： 同时存在 purOrderDateFrom 和 purOrderDate 字段，则对purOrderDateFrom生成以下规则
 * {
			"field":"purOrderDateFrom",
			"col":"purOrderDate",
			"op":">",
			"rel":"and"
	}
	
	带To也做同样的处理
	
	2. 有值的字段用lk生成规则
 * @author alex.yang
 *
 */
public interface IRuleSchemeGenerator {
	
	/**
	 * 生成规则映射表
	 * @return
	 */
	public Map<String, Rule> generateRuleScheme(Object form);
}
