package com.bonelf.frame.core.dict.constraints;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * 枚举字典
 * </p>
 * @author bonelf
 * @since 2020/10/11 17:45
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RemoteDict {
	/**
	 *  地址url 使用restTemplate Bean cloud支持loadBalance
	 *  http://xxx/xx/xx
	 */
	String value();

	/**
	 * 查询ID 将传递数组类型
	 * keys=1,2,3
	 * @return
	 */
	String queryKey() default "keys";

	/**
	 * 结果集处理静态方法 类全名.方法名 只有方法取当前类，不设置则默认接口返回的就是Map
	 * 方法传入结果 返回Map&lt;Object, String>
	 * @return
	 */
	String toMapMethod() default "";

	/**
	 * 在前端解析时返回对应枚举值 不设置代表不返回
	 */
	String nameSuffix() default "Name";

	/**
	 * 是否使用定时的缓存
	 * （不会实时修改，但是能减小数据库服务压力，提高访问速度）
	 */
	boolean cached() default true;
}
