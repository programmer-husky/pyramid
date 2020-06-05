package com.husky.pyramid.annotation;

import lombok.Data;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 缓存属性值对象
 * @author dengweichang
 */
@Data
public class PyramidModel {


	/**
	 * 缓存名称，类似group
	 */
	private String cacheName;

	/**
	 * cacheName下的唯一值
	 */
	private Object key;

	/**
	 * key解析类型是否为集合
	 */
	boolean collection;

	/**
	 * @see #collection == true
	 * key集合所在方法参数数组中的的角标
	 * @see ProceedingJoinPoint#getArgs()
	 */
	int collectionArgsIndex;

	/**
	 * 只使用本地缓存
	 */
	boolean onlyLocal;

	/**
	 * 只使用分布式缓存
	 */
	boolean onlyDistributed;

	/**
	 * redis过期时间
	 * -1为永久有效
	 */
	long redisExpiration;

	/**
	 * 本地缓存过期时间
	 * <p>默认60秒
	 */
	long nativeExpiration;

	/**
	 * 缓存刷新时间
	 * <p>缓存刷新时间
	 * -1为用不刷新
	 */
	long refreshTime;

	public PyramidModel(String cacheName, Object key, boolean collection, int collectionArgsIndex, boolean onlyLocal, boolean onlyDistributed,  long redisExpiration, long nativeExpiration, long refreshTime) {
		this.cacheName = cacheName;
		this.key = key;
		this.collection = collection;
		this.onlyLocal = onlyLocal;
		this.onlyDistributed = onlyDistributed;
		if (collection && collectionArgsIndex < 0) {
			collectionArgsIndex = 0;
		}
		this.collectionArgsIndex = collectionArgsIndex;
		this.redisExpiration = redisExpiration;
		this.nativeExpiration = nativeExpiration;
		this.refreshTime = refreshTime;
	}
}
