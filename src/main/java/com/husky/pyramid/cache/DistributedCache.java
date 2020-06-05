package com.husky.pyramid.cache;

import com.husky.pyramid.annotation.PyramidModel;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 分布式缓存
 * <p>基于redis
 * @author dengweichang
 */
@Data
@Slf4j
public class DistributedCache implements CacheSupport {

	/**
	 * redis操作类
	 */
	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisSerializer<String> keySerializer;
	private final RedisSerializer<Object> valueSerializer;

	@SuppressWarnings("unchecked")
	DistributedCache(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
		this.valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
	}

	/**
	 * 缓存单条数据
	 *  @param key    	缓存标识
	 * @param object 	缓存单个缓存
	 * @param pyramid	缓存配置相关
	 */
	@Override
	public void singleCache(@NonNull String key, Object object, PyramidModel pyramid) {
		if (pyramid.getRedisExpiration() < 0) {
			redisTemplate.opsForValue().set(key, object);
		} else {
			redisTemplate.opsForValue().set(key, object, pyramid.getRedisExpiration(), TimeUnit.SECONDS);
		}
	}

	/**
	 * 缓存批量数据
	 * 若key已存在则覆盖
	 * @param strMap 	单条数据组合的hash结构
	 * @param pyramid	缓存配置相关
	 */
	@Override
	public void batchCache(@NonNull Map<String, Object> strMap, PyramidModel pyramid) {
		long redisExpiration = pyramid.getRedisExpiration();
		if (redisExpiration > 0) {
			redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
				Expiration expiration = Expiration.from(redisExpiration, TimeUnit.SECONDS);
				strMap.forEach((k, v) -> connection.set(keySerializer.serialize(k), valueSerializer.serialize(v), expiration, RedisStringCommands.SetOption.UPSERT));
				return null;
			});
		} else {
			redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
				strMap.forEach((k, v) -> connection.set(keySerializer.serialize(k), valueSerializer.serialize(v)));
				return null;
			});
		}

	}

	/**
	 * 清除单条缓存数据
	 *
	 * @param key 缓存标识
	 */
	@Override
	public void clear(@NonNull String key) {
		redisTemplate.delete(key);
	}

	/**
	 * 批量清除缓存数据
	 *
	 * @param keys 缓存标识的集合
	 */
	@Override
	public void batchClear(@NonNull Collection<String> keys) {
		redisTemplate.delete(keys);
	}

	/**
	 * 获取单个缓存
	 *
	 * @param key 缓存唯一标记
	 * @return 缓存值
	 */
	@Override
	public Object getCache(@NonNull String key) {
		return redisTemplate.opsForValue().get(key);
	}

	/**
	 * 批量获取缓存
	 *
	 * @param set 缓存标记集合
	 * @return 缓存值的集合
	 */
	@Override
	public Map<String, Object> batchGetCache(Set<String> set) {
		List<Object> cache = redisTemplate.opsForValue().multiGet(set);
		if (CollectionUtils.isEmpty(cache)) {
			return null;
		}
		Map<String, Object> map = new HashMap<>(set.size(), 1L);

		ArrayList<String> keys = new ArrayList<>(set);
		for (int i = 0, size = keys.size(); i < size; i++) {
			if (Objects.nonNull(cache.get(i))) {
				map.put(keys.get(i), cache.get(i));
			}
		}
		return map;
	}

}
