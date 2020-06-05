package com.husky.pyramid.event;

import com.husky.pyramid.enums.ChannelTypeEnum;
import lombok.Data;
import org.springframework.util.Assert;

import java.io.Serializable;

/**
 * redis同步所用消息
 * @author dengweichang
 */
@Data
public class RedisMessage implements Serializable {

	/**
	 * 通道类型
	 */
	private ChannelTypeEnum channelTypeEnum;

	/**
	 * 缓存名称
	 */
	private String cacheName;

	/**
	 * 缓存唯一标记
	 */
	private String cacheKey;

	public RedisMessage(ChannelTypeEnum channelTypeEnum, String cacheName, String cacheKey) {
		Assert.notNull(channelTypeEnum, "redis消息通道类型未指定");
		Assert.hasLength(cacheName, "redis cache name未指定");
		this.channelTypeEnum = channelTypeEnum;
		this.cacheName = cacheName;
		this.cacheKey = cacheKey;
	}
}
