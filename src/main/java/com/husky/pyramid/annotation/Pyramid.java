package com.husky.pyramid.annotation;

import com.husky.pyramid.config.DistributedProperties;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.annotation.*;

/**
 * 二级缓存注解
 * @author dengweichang
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Pyramid {

	/**
	 * 缓存空间名称
	 * @return	缓存空间名称
	 */
	String cacheName() default "";

	/**
	 * 缓存标识
	 * <p>
	 *     值不可为空，
	 *     若为集合，集合不可为空
	 * </p>
	 * @return	cacheName下的唯一值
	 */
	String key() default "";

	/**
	 * key解析类型是否为集合
	 * @return	true/false
	 */
	boolean collection() default false;

	/**
	 * @see #collection() == true
	 * @return	key 集合所在方法参数数组中的的角标
	 * @see ProceedingJoinPoint#getArgs()
	 */
	int collectionArgsIndex() default 0;

	/**
	 * 是否只使用本地缓存
	 */
	boolean onlyLocal() default false;

	/**
	 * 是否仅使用分布式缓存
	 */
	boolean onlyDistributed() default false;

	/**
	 * redis过期时间
	 * 默认值{@link DistributedProperties#getCacheExpiration()}
	 * -1为永久有效
	 * @return	seconds
	 */
	long redisExpiration() default -1023L;

	/**
	 * 本地缓存过期时间
	 * <p>默认10分
	 * @return	seconds
	 */
	long nativeExpiration() default 600;

	/**
	 * 缓存刷新时间
	 * <p>缓存刷新时间
	 * -1为用不刷新
	 * @return	seconds
	 */
	long refreshTime() default -1;
}
