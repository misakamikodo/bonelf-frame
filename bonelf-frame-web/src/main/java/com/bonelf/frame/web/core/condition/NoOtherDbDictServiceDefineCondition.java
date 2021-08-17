package com.bonelf.frame.web.core.condition;

import com.bonelf.frame.web.core.dict.service.DbDictService;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * 没有其他bean
 * @deprecated bean的属性都变空了
 * @author bonelf
 * @date 2021/6/15 23:05
 */
@Deprecated
public class NoOtherDbDictServiceDefineCondition implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, @NonNull AnnotatedTypeMetadata annotatedTypeMetadata) {
		if (conditionContext.getBeanFactory() != null) {
			return conditionContext.getBeanFactory().getBeansOfType(DbDictService.class).size() == 0;
		} else {
			return true;
		}
	}
}