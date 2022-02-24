package com.husky.pyramid.cache;

import com.alibaba.fastjson.JSON;
import com.husky.pyramid.annotation.DelModel;
import com.husky.pyramid.annotation.PyramidKey;
import com.husky.pyramid.annotation.PyramidModel;
import com.husky.pyramid.config.DistributedProperties;
import com.husky.pyramid.config.LocalProperties;
import com.husky.pyramid.enums.ChannelTypeEnum;
import com.husky.pyramid.event.RedisMessage;
import com.husky.pyramid.event.RedisMessagePublisher;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.support.NullValue;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 二级缓存管理器
 *
 * <p>
 * 		{@link CacheSupport} 中只有各类缓存本身的操作封装
 * 		包含雪崩、穿透解决能力在此处实现
 * </p>
 * @author programmer_husky
 */
@SuppressWarnings({"unchecked", "unused"})
@Slf4j
@Component
public class PyramidCacheManager implements CacheManager, InitializingBean {

	/**
	 * key间隔符
	 */
	private final String split = ":";

	/**
	 * key序列化器
	 */
	private StringRedisSerializer keySerializer = new StringRedisSerializer();

	private final DistributedProperties distributedProperties;
	private final LocalProperties localProperties;
	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * 是否开启分布式缓存
	 */
	private boolean distributedStarted;
	/**
	 * 是否开启本地缓存
	 */
	private boolean localStarted;
	/**
	 * 全局默认过期时间
	 */
	@Getter
	private long expiration;

	/**
	 * 缓存前缀<全局>
	 */
	private String prefix;

	/**
	 * 分布式缓存
	 * redis
	 */
	private DistributedCache distributedCache;
	/**
	 * 本地缓存
	 * coffee
	 */
	private LocalCache localCache;

	volatile Set<String> lockSet = Collections.synchronizedSet(new HashSet<>());

	@Autowired
	public PyramidCacheManager(DistributedProperties distributedProperties, LocalProperties localProperties, RedisTemplate<String, Object> redisTemplate) {
		this.distributedProperties = distributedProperties;
		this.localProperties = localProperties;
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void afterPropertiesSet() {
		initDistributed(distributedProperties.isDistributedEnable());
		initLocal(localProperties.isEnable());
		initExpiration(distributedProperties.getExpiration());
		initPrefix(distributedProperties.getPrefix());
		initCache();
	}




	/**
	 * 通用的获取缓存的方法
	 *
	 * @param pyramid  注解附带的值
	 * @param function 获取不到缓存时的操作
	 * @param args     {@link org.aspectj.lang.ProceedingJoinPoint#getArgs()}
	 * @return Object
	 */
	@Override
	public Object generalCache(PyramidModel pyramid, Function<Object[], Object> function, Object[] args) {
		if (pyramid.getRedisExpiration() == 0) {
			pyramid.setRedisExpiration(expiration);
		}
		if (pyramid.getNativeExpiration() == 0) {
			pyramid.setNativeExpiration(expiration);
		}
		avalancheSolution(pyramid);
		if (!pyramid.isCollection()) {
			return getCache(pyramidKey(pyramid.getCacheName(), String.valueOf(pyramid.getKey())), function, args, pyramid, false);
		} else {
			Collection<Object> keyConnection = (Collection<Object>) pyramid.getKey();
			Set<String> set = pyramidKey(pyramid.getCacheName(), keyConnection);
			Map<String, Object> map = batchGetCache(new HashSet<>(set), pyramid, function, args, false);
			List<Object> list = new ArrayList(set.size());
			set.forEach(key -> {
				if (Objects.nonNull(map.get(key))) {
					list.add(map.get(key));
				}
			});
			return list;
		}
	}

	/**
	 * 删除缓存
	 *  @param delModel 注解附带的值
	 * @param publish 是否发送通知
	 */
	@Override
	public void generalClear(DelModel delModel, boolean publish) {
		log.info("invoke general clear -{}", JSON.toJSONString(delModel));
		List<String> keys = new ArrayList<>();
		if (!delModel.isCollection()) {
			String key = String.valueOf(delModel.getKey());
			clear(pyramidKey(delModel.getCacheName(), String.valueOf(delModel.getKey())), publish);
			keys.add(key);
		} else {
			Collection<String> key = (Collection<String>) delModel.getKey();
			batchClear(key.stream().map(item -> pyramidKey(delModel.getCacheName(), item)).collect(Collectors.toList()), publish);
			keys.addAll(key);
		}
		if (publish) {
			keys.forEach(key -> RedisMessagePublisher.publish(redisTemplate,
					new RedisMessage(ChannelTypeEnum.DELETE, delModel.getCacheName(), key)));
		}
	}



	/**
	 * 获取单个缓存
	 *
	 * @param key  缓存唯一标记
	 * @param function 若缓存中有不存在的，则执行
	 * @param pyramid  缓存配置相关
	 * @param locked	是否已被锁定（防止并发）
	 * @param args 		invoke方法入参
	 * @return 缓存值
	 * @since 0.0.4 分布式锁切换为本地锁，放弃集群唯一性，提高单机缓存击穿性能消耗
	 */
	private <R> R getCache(@NonNull String key, Function<Object[], R> function, Object[] args, PyramidModel pyramid, boolean locked) {
		Object cache;
		if (localStarted && !pyramid.isOnlyDistributed()) {
			Object localCacheValue = localCache.getCache(key);
			if (!Objects.isNull(localCacheValue)) {
				if (localCacheValue instanceof NullValue) {
					return null;
				}
				return (R) localCacheValue;
			}
		}
		if (distributedStarted && !pyramid.isOnlyLocal()) {
			cache = distributedCache.getCache(key);
			if (Objects.nonNull(cache)) {
				if (localStarted) {
					localCache.singleCache(key, cache, pyramid);
				}
				return (R) cache;
			} else {
				Boolean execute = redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.exists(Objects.requireNonNull(keySerializer.serialize(key))));
				if (execute != null && execute) {
					return null;
				}
			}
		}

		if (locked) {
			//若两级缓存都为null
			log.info("{}:执行DB查询===============================", key);
			R call;
			try {
				call = function.apply(args);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			singleCache(key, call, pyramid);
			return call;
		} else {
			if (log.isDebugEnabled()) {
				log.debug("缓存击穿阻拦 :{}", key);
			}
		}

		try {
			/*
			 * 不考虑虚假唤醒
			 * 等待200ms防止死锁，也可以阻拦绝大多数的击穿流量
			 */
			if (!lockSet.add(key)) {
				synchronized (key) {
					try {
						key.wait(200);
					} catch (InterruptedException e) {
						log.error("pyramid: {} 线程中断", key);
					}
				}
			}
//
//			boolean lock = RedisLock.lock(redisTemplate, key, 1000);
//			if (lock) {
//				return getCache(key, function, args, pyramid, true);
//			}
//			RedisLock.waitUnLock(redisTemplate, key, 50);

			return getCache(key, function, args, pyramid, true);
		} finally {
			synchronized (key) {
				if (log.isDebugEnabled()) {
					log.debug("解锁-{}", key);
				}
				lockSet.remove(key);
			}
//			RedisLock.unLock(redisTemplate, key);
		}
	}

	/**
	 * 批量获取缓存
	 *
	 * @param finalSet  缓存标记集合
	 * @param pyramid 缓存配置相关
	 * @param function 若缓存未全命中，则执行
	 * @param locked 是否被锁定（防止并发）
	 * @return 缓存值的集合
	 */
	private Map<String, Object> batchGetCache(Set<String> finalSet, PyramidModel pyramid, Function<Object[], Object> function, Object[] args, boolean locked) {
		Map<String, Object> cache;
		final Map<String, Object> resultMap = new HashMap<>(finalSet.size() << 1);
		if (localStarted) {
			cache = localCache.batchGetCache(finalSet);
			if (Objects.nonNull(cache)) {
				for (Map.Entry<String, Object> entry : cache.entrySet()) {
					String k = entry.getKey();
					Object v = entry.getValue();
					resultMap.put(k, v);
					finalSet.remove(k);
				}
				if (finalSet.size() == 0) {
					return resultMap;
				}
			}
		}
		HashSet<String> set = new HashSet<>(finalSet);
		if (distributedStarted) {
			cache = distributedCache.batchGetCache(set);
			if (Objects.nonNull(cache)) {
				Map<String, Object> localCacheMap = new HashMap<>(set.size() << 1);
				cache.forEach((k, v) -> {
					resultMap.put(k, v);
					localCacheMap.put(k, v);
					set.remove(k);
				});
				if (localStarted && localCacheMap.size() > 0) {
					localCache.batchCache(localCacheMap, pyramid);
				}
			}
		}
		if (set.size() > 0) {
			Collection arg = (Collection) args[pyramid.getCollectionArgsIndex()];
			arg = new ArrayList<>(arg);
			arg.removeIf(item -> {
				String s = pyramidKey(pyramid.getCacheName(), String.valueOf(item));
				return !set.contains(s);
			});
			Object apply;
			Map<String, Object> map = new HashMap<>(set.size(), 1L);
			try {
				apply = function.apply(args);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			((Collection) apply).forEach(item -> {
				try {
					String s = annotationKey(item);
					Object o = map.computeIfAbsent(pyramidKey(pyramid.getCacheName(), s), k -> item);
				} catch (Exception e) {
					log.error("执行@PyramidKey的方法失败, 缓存跳过", e);
				}
			});
			batchCache(map, pyramid);
			resultMap.putAll(map);
		}
		log.info("批量缓存返回值-{}", JSON.toJSONString(resultMap));
		return resultMap;
	}

	private String pyramidKey(String cacheName, String key) {
		if (StringUtils.isEmpty(key)) {
			return null;
		}
		return prefix + cacheName + split + key;
	}

	private Set<String> pyramidKey(String cacheName, Collection<Object> collection) {
		if (CollectionUtils.isEmpty(collection)) {
			return null;
		}
		return collection.stream()
				.filter(Objects::nonNull)
				.map(key -> pyramidKey(cacheName, String.valueOf(key)))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	/**
	 * 获取{@link PyramidKey}标注的方法返回的值
	 * @param object	拥有标注PyramidKey方法的类
	 * @return	String
	 * @throws Exception	any exception
	 */
	private String annotationKey(Object object) throws Exception {
		if (object instanceof Map) {
			Object o = ((Map) object).keySet().stream().findFirst().orElse(null);
			return String.valueOf(o);
		}
		Method[] methods = object.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(PyramidKey.class)) {
				method.setAccessible(true);
				return (String) method.invoke(object);
			}
		}
		throw new RuntimeException("没有@PyramidKey标注的方法");
	}



	/* init start */
	/**
	 * 设置分布式缓存开关
	 *
	 * @param start 是否启用
	 */
	private void initDistributed(boolean start) {
		distributedStarted = start;
	}

	/**
	 * 设置本地缓存开关
	 *
	 * @param start 是否启用
	 */
	private void initLocal(boolean start) {
		localStarted = start;
	}

	/**
	 * 设置全局默认过期时间
	 *	若小于0，则默认60s
	 * @param expiration 过期时间
	 *                   time unit seconds
	 */
	private void initExpiration(long expiration) {
		if (expiration <= 0L) {
			expiration = 60L;
		}
		this.expiration = expiration;
	}

	/**
	 * 设置全局前缀
	 * @param prefix	前缀字符串
	 */
	private void initPrefix(String prefix) {
		if (StringUtils.isEmpty(prefix)) {
			prefix = "pyramid";
		}
		this.prefix = prefix + split;
	}

	/**
	 * 初始化缓存
	 */
	private void initCache() {
		if (distributedStarted) {
			distributedCache = new DistributedCache(redisTemplate);
		}
		if (localStarted) {
			localCache = new LocalCache(localProperties.getCapacity(), localProperties.getCacheSeconds());
		}
	}
	/* init end */






	/* cache operation start */
	/**
	 * 缓存单条数据
	 *
	 * @param key    缓存标识
	 * @param object 缓存单个缓存
	 * @param pyramid 缓存配置相关
	 */
	private void singleCache(@NonNull String key, Object object, PyramidModel pyramid) {
		log.info("刷新缓存 -{}, -{}", key, JSON.toJSONString(object));
		if (distributedStarted) {
			distributedCache.singleCache(key, object, pyramid);
		}
		if (localStarted) {
			localCache.singleCache(key, object, pyramid);
		}
	}

	/**
	 * 缓存批量数据
	 *
	 * @param strMap 单条数据组合的hash结构
	 * @param pyramid 缓存配置相关
	 */
	private void batchCache(@NonNull Map<String, Object> strMap, PyramidModel pyramid) {
		for (Map.Entry<String, Object> entry : strMap.entrySet()) {
			entry.setValue(entry.getValue() == null ? NullValue.INSTANCE : entry.getValue());
		}
		if (distributedStarted) {
			distributedCache.batchCache(strMap, pyramid);
		}
		if (localStarted) {
			localCache.batchCache(strMap, pyramid);
		}
	}

	/**
	 * 清除单条缓存数据
	 *
	 * @param key 缓存标识
	 * @param publish	是否需要发布，若不需要发布则不处理分布式缓存
	 */
	private void clear(@NonNull String key, boolean publish) {
		if (distributedStarted) {
			distributedCache.clear(key);
		}
		if (localStarted) {
			localCache.clear(key);
		}
	}

	/**
	 * 批量清除缓存数据
	 *
	 * @param keys 缓存标识的集合
	 * @param publish	是否需要发布，若不需要发布则不处理分布式缓存
	 */
	private void batchClear(@NonNull Collection<String> keys, boolean publish) {
		if (distributedStarted) {
			distributedCache.batchClear(keys);
		}
		if (localStarted) {
			localCache.batchClear(keys);
		}
	}

	/* cache operation end */

}
