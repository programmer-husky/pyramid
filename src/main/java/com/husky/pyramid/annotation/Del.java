package com.husky.pyramid.annotation;

import java.lang.annotation.*;

/**
 * 删除缓存注解
 * @author programmer_husky
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Del {

	/**
	 * 缓存空间名称
	 * @return	缓存空间名称
	 */
	String cacheName() default "";

	/**
	 * 缓存标识
	 * @return	cacheName下的唯一值
	 */
	String key() default "";

	/**
	 * key解析类型是否为集合
	 * @return	true/false
	 */
	boolean collection() default false;
}
