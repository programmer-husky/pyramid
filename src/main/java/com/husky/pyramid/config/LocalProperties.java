package com.husky.pyramid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 本地缓存配置
 * @author dengweichang
 */
@ConfigurationProperties(prefix = "pyramid.local")
@Data
public class LocalProperties {

	/**
	 * 是否使用本地缓存
	 */
	private boolean enable;

	/**
	 * 初始容量
	 */
	private long capacity = 100;

	/**
	 * 缓存有效时间
	 * time unit seconds
	 */
	private long cacheSeconds = 60 * 5;
}
