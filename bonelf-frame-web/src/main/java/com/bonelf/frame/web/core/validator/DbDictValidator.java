package com.bonelf.frame.web.core.validator;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bonelf.frame.web.core.validator.constraints.DbDictValid;
import com.bonelf.frame.web.domain.entity.SysDictItem;
import com.bonelf.frame.web.mapper.SysDictItemMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * <p>
 * 枚举值校验（微服务不支持）
 * </p>
 * @author bonelf
 * @since 2020/7/9 9:21
 */
public class DbDictValidator implements ConstraintValidator<DbDictValid, Object> {
	private DbDictValid annotation;
	@Autowired
	private SysDictItemMapper sysDictItemMapper;

	@Override
	public void initialize(DbDictValid constraintAnnotation) {
		this.annotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		if (sysDictItemMapper.selectCount(Wrappers.<SysDictItem>lambdaQuery()
				.eq(SysDictItem::getItemValue, value.toString()).eq(SysDictItem::getDictId, annotation.dictId())) > 0) {
			return true;
		}
		return false;
	}
}
