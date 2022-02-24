package com.husky.pyramid.event;

import com.alibaba.fastjson.JSON;
import com.husky.pyramid.annotation.DelModel;
import com.husky.pyramid.cache.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * redis消息监听器
 * @author programmer_husky
 */
@Component
@Slf4j
public class RedisMessageListener extends MessageListenerAdapter {

	@Resource
	private CacheManager cacheManager;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		log.info("redis listener -{},pattern-{}", JSON.toJSONString(message), new String(pattern));
		super.onMessage(message, pattern);
		RedisMessage redisMessage = JSON.parseObject(message.getBody(), RedisMessage.class);
		String channel = new String(message.getChannel());
		String cacheKey = redisMessage.getCacheKey();
		if (StringUtils.isEmpty(cacheKey)) {
			return;
		}
		cacheManager.generalClear(new DelModel(redisMessage.getCacheName(), cacheKey, false), false);
//		if (cacheKeyList.size() == 1) {
//
//		} else {
//			cacheManager.generalClear(new DelModel(redisMessage.getCacheName(), cacheKeyList, true), false);
//		}
	}
}
