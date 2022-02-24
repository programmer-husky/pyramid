package com.husky.pyramid.config;

import com.alibaba.fastjson.parser.ParserConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.Resource;

/**
 * Pyramid global config
 * @author programmer_husky
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties({DistributedProperties.class, LocalProperties.class, CacheProperties.class})
@Conditional(StartCondition.class)
@ComponentScan("com.husky.pyramid")
@Slf4j
public class GlobalConfig implements ApplicationListener<ApplicationReadyEvent> {

	@Resource
	private DistributedProperties distributedProperties;

	@Resource
	private LocalProperties localProperties;

	/**
	 * redis删除主题
	 */
	public static String DELETE_TOPIC = "redis:cache:delete:topic:";

	/**
	 * redis清空主题
	 */
	public static String CLEAR_TOPIC = "redis:cache:generalClear:topic:";


	/**
	 * redis缓存监听容齐
	 * @param redisConnectionFactory	redis连接工厂
	 * @param messageListener	redis消息监听器
	 * @return	redis监听
	 */
	@Bean
	public RedisMessageListenerContainer redisContainer(RedisConnectionFactory redisConnectionFactory, MessageListenerAdapter messageListener) {
		DELETE_TOPIC = DELETE_TOPIC + distributedProperties.getNode();
		CLEAR_TOPIC = CLEAR_TOPIC + distributedProperties.getNode();
		final RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(redisConnectionFactory);
		container.addMessageListener(messageListener, new ChannelTopic(DELETE_TOPIC));
		log.info("监听redis-{}", DELETE_TOPIC);
		container.addMessageListener(messageListener, new ChannelTopic(CLEAR_TOPIC));
		log.info("监听redis-{}", CLEAR_TOPIC);
		return container;
	}

	/**
	 * 指定redis template 序列化工具
	 * key and hash key -> StringRedisSerializer
	 * value and hash value -> FastJsonRedisSerializer
	 * @param redisConnectionFactory	redis连接工厂
	 * @return	redis template
	 */
	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
		ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(valueSerializer);
		redisTemplate.setHashValueSerializer(valueSerializer);
		redisTemplate.afterPropertiesSet();
		return redisTemplate;
	}


	/**
	 * Handle an application event.
	 *
	 * @param event the event to respond to
	 */
	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		log.info("******************************************");
		log.info("     PYRAMID缓存开启");
		log.info("     redis默认过期秒数-{}", distributedProperties.getExpiration());
		log.info("     缓存有效秒数-{}", distributedProperties.getCacheExpiration());
		log.info("     缓存刷新间隔秒数-{}", localProperties.getCacheSeconds());
		log.info("     本地缓存初始容量-{}", localProperties.getCapacity());
		log.info("******************************************");
	}

}
