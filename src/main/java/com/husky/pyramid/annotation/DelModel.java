package com.husky.pyramid.annotation;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 删除缓存标记值对象
 * @author programmer_husky
 */
@Data
@AllArgsConstructor
public class DelModel {
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

}
