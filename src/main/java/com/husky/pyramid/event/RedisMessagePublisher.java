package com.husky.pyramid.event;

import com.alibaba.fastjson.JSON;
import com.husky.pyramid.config.GlobalConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * redis chanel message publish
 * @author dengweichang
 */
@Slf4j
@Component
public class RedisMessagePublisher {

	/**
	 * 发布redis事件
	 * @param redisTemplate redis操作类
	 * @param redisMessage	事件对象
	 */
	public static void publish(RedisTemplate<String, Object> redisTemplate, RedisMessage redisMessage) {
		log.info("general clear event publish");
		if (redisTemplate == null || redisMessage == null) {
			return;
		}
		String channel;
		switch (redisMessage.getChannelTypeEnum()) {
			case CLEAR:
				channel = GlobalConfig.CLEAR_TOPIC;
				break;
			case DELETE:
				channel = GlobalConfig.DELETE_TOPIC;
				break;
			default:
				log.error("redis channel不匹配 -{}", JSON.toJSONString(redisMessage));
				return;
		}
		redisTemplate.convertAndSend(channel, redisMessage);
	}
}
