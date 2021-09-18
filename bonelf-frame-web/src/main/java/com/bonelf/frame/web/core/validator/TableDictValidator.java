package com.bonelf.frame.web.core.validator;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bonelf.frame.web.core.validator.constraints.TableDictValid;
import com.bonelf.frame.web.mapper.SqlMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * 枚举值校验（微服务不支持）
 * </p>
 * @author bonelf
 * @since 2020/7/9 9:21
 */
@Slf4j
public class TableDictValidator implements ConstraintValidator<TableDictValid, Object> {
	private TableDictValid annotation;
	@Autowired
	private SqlMapper sqlMapper;

	@Override
	public void initialize(TableDictValid constraintAnnotation) {
		this.annotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}
		if ("".equals(annotation.table()) && annotation.tableClazz() == void.class) {
			log.warn("注解@TableDict必须要定义表类或表名之一");
			return false;
		}
		String table = StrUtil.isBlank(annotation.table()) ? annotation.tableClazz().getAnnotation(TableName.class).value() : annotation.table();
		String key = StrUtil.isBlank(annotation.keyColumn()) ?
				getKeyColumnFormAnnoClazz() :
				annotation.keyColumn();
		List<Map<String, Object>> res = sqlMapper.dynamicsQuery(MessageFormat.format("SELECT EXISTS(SELECT 1 FROM {0} WHERE {1}={2}) AS ext",
				table, key, value.toString()));
		return "1".equals(res.get(0).get("ext").toString());
	}

	private String getKeyColumnFormAnnoClazz() {
		if (annotation.tableClazz() == void.class || annotation.tableClazz().getAnnotation(TableName.class) == null) {
			return "id";
		}
		Optional<Field> field = Arrays.stream(annotation.tableClazz().getFields())
				.filter(item -> item.getDeclaredAnnotation(TableId.class) != null).findFirst();
		return field.map(Field::getName).orElse("id");
	}
}
