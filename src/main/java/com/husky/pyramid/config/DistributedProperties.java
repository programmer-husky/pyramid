package com.husky.pyramid.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 二级缓存配置信息
 * @author programmer_husky
 */
@ConfigurationProperties(prefix = "pyramid")
@Data
public class DistributedProperties {

	/**
	 * 是否使用二级缓存
	 */
	private boolean enable;

	/**
	 * 缓存前缀
	 */
	private String prefix;

	/**
	 * 是否使用分布式缓存
	 */
	private boolean distributedEnable;

	/**
	 *	缓存节点，便于区分同一个redis下的不同项目
	 */
	private String node;

	/**
	 * 全局默认缓存失效时间
	 * <p>timeUnit seconds</p>
	 */
	private long expiration = 60;

	/**
	 * 缓存有效时间
	 * <p>timeUnit seconds</p>
	 */
	private long cacheExpiration;

}
