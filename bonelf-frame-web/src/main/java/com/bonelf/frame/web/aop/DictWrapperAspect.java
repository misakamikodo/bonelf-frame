package com.bonelf.frame.web.aop;

import com.bonelf.frame.core.dict.annotation.DictWrapper;
import com.bonelf.frame.core.dict.annotation.DictWrappers;
import com.bonelf.frame.core.domain.Result;
import com.bonelf.frame.web.core.dict.decorator.DictWrapperDictDecorator;
import com.bonelf.frame.web.core.dict.domain.BatchDictFieldHolder;
import com.bonelf.frame.web.core.dict.service.DbDictService;
import com.bonelf.frame.web.core.dict.service.RemoteDictService;
import com.bonelf.frame.web.core.dict.service.TableDictService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * Result Cost填充
 * @author Bonelf
 **/
@Aspect
@Component
@Slf4j
public class DictWrapperAspect {
	@Autowired(required = false)
	private DbDictService dbDictService;
	@Autowired(required = false)
	private TableDictService tableDictService;
	@Autowired(required = false)
	private RemoteDictService remoteDictService;

	@Pointcut("@annotation(dictWrappers)")
	public void pointCut(DictWrappers dictWrappers) {
	}

	@Pointcut("@annotation(dictWrapper)")
	public void pointCutSingle(DictWrapper dictWrapper) {
	}

	@Around(value = "pointCut(dictWrappers)", argNames = "pjp,dictWrappers")
	public Object doAround(ProceedingJoinPoint pjp, DictWrappers dictWrappers) throws Throwable {
		Object result = pjp.proceed();
		this.parseDictText(result, dictWrappers);
		return result;
	}

	@Around(value = "pointCutSingle(dictWrapper)", argNames = "pjp,dictWrapper")
	public Object doAroundSingle(ProceedingJoinPoint pjp, DictWrapper dictWrapper) throws Throwable {
		Object result = pjp.proceed();
		this.parseDictText(result, dictWrapper);
		return result;
	}

	private void parseDictText(Object result, Annotation dictWrapper) {
		List<BatchDictFieldHolder<Annotation>> wrapperFields = new ArrayList<>();
		if(result instanceof Result){
			wrapperFields.add(new BatchDictFieldHolder<>(((Result<?>)result).getResult(), dictWrapper));
		} else {
			wrapperFields.add(new BatchDictFieldHolder<>(result, dictWrapper));
		}
		new DictWrapperDictDecorator(dbDictService,
				tableDictService,
				remoteDictService, wrapperFields).decorate();
	}

}
