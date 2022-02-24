package com.husky.pyramid.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.husky.pyramid.annotation.PyramidModel;
import lombok.NonNull;
import org.springframework.cache.support.NullValue;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存
 * <p>基于coffee</p>
 * @author programmer_husky
 */
public class LocalCache implements CacheSupport {

	private Cache<Object, Object> cache;

	LocalCache(long size, long seconds) {
		cache = Caffeine.newBuilder()
				.maximumSize(size)
				.expireAfterWrite(seconds, TimeUnit.SECONDS)
				.build();
	}


	/**
	 * 缓存单条数据
	 *
	 * @param key     缓存标识
	 * @param object  缓存单个缓存
	 * @param pyramid 缓存配置相关
	 */
	@Override
	public void singleCache(@NonNull String key, Object object, PyramidModel pyramid) {
		cache.put(key, object == null ? NullValue.INSTANCE : object);
	}

	/**
	 * 缓存批量数据
	 *
	 * @param strMap  单条数据组合的hash结构
	 * @param pyramid 缓存配置相关
	 */
	@Override
	public void batchCache(@NonNull Map<String, Object> strMap, PyramidModel pyramid) {
		cache.putAll(strMap);
	}

	/**
	 * 清除单条缓存数据
	 *
	 * @param key 缓存标识
	 */
	@Override
	public void clear(@NonNull String key) {
		cache.invalidate(key);
	}

	/**
	 * 批量清除缓存数据
	 *
	 * @param keys 缓存标识的集合
	 */
	@Override
	public void batchClear(@NonNull Collection<String> keys) {
		cache.invalidateAll(keys);
	}

	/**
	 * 获取单个缓存
	 *
	 * @param key 缓存唯一标记
	 * @return 缓存值
	 */
	@Override
	public Object getCache(@NonNull String key) {
		return cache.getIfPresent(key);
	}

	/**
	 * 批量获取缓存
	 *
	 * @param set 缓存标记集合
	 * @return 缓存值的集合
	 */
	@Override
	public Map<String, Object> batchGetCache(Set<String> set) {
		Map<Object, Object> allPresent = cache.getAllPresent(set);
		if (CollectionUtils.isEmpty(allPresent)) {
			return null;
		}
		HashMap<String, Object> result = new HashMap<>(allPresent.size(), 1L);
		allPresent.forEach((k, v) -> result.put(String.valueOf(k), v));
		return result;
	}
}

