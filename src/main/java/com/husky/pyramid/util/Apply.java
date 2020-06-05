package com.husky.pyramid.util;

/**
 * 类似{@link Runnable}
 * 调用指定的方法体
 *
 * @author dengweichang
 */
@FunctionalInterface
public interface Apply {

	/**
	 * 调用指定的方法块
	 */
	void apply();
}
