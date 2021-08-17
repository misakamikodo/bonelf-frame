package com.bonelf.frame.core.dict.annotation;

import com.bonelf.frame.core.dict.enums.DictType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 用于支持 Map、泛型 等不确定类型的字段翻译
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:45
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictWrapper {
	/**
	 * 字典类型
	 * @return
	 */
	DictType type();

	/**
	 * 翻译字段 xx.xx.xx
	 * @return
	 */
	String fieldSeq();

	/**
	 * 注解信息:
	 * 示例
	 * db		: value=example_status;
	 * enums	: value=com.bonelf.FooEnum;
	 * func		: value=com.bonelf.Func;method=foo
	 * remote	: value=http://api.bonelf.com?exp=hello;queryKey=keys;toMapMethod=com.boenlf.Func#foo
	 * table	: table=bnf_xx;key=id;val=name;sql=select id..;
	 */
	String payload();

	/**
	 * 是否使用定时的缓存
	 * （不会实时修改，但是能减小数据库服务压力，提高访问速度）
	 */
	boolean cached() default true;

	/**
	 * 在前端解析时返回对应枚举值 不设置代表不返回
	 */
	String nameSuffix() default "Name";
}
