package com.husky.pyramid.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * redis锁
 * @author dengweichang
 */
@Slf4j
public class RedisLock {

	private static int lockValue = 1;

	/**
	 * redis锁前缀
	 */
	private static String lock_prefix = "redis_lock:";

	/**
	 * 添加redis分布式锁
	 * @param redisTemplate	redis操作类
	 * @param key	redis key
	 * @param expire	锁过期时间 时间单位 second
	 * @return	true/false
	 */
	public static boolean lock(RedisTemplate<String, Object> redisTemplate, String key, long expire) {
		Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent(lock_prefix + key, lockValue, expire, TimeUnit.SECONDS);
		return setIfAbsent != null && setIfAbsent;
	}

	/**
	 * 解锁redis分布式锁
	 * @param redisTemplate	redis操作类
	 * @param key	redis key
	 */
	public static void unLock(RedisTemplate<String, Object> redisTemplate, String key) {
		redisTemplate.delete(lock_prefix + key);
	}

	/**
	 * 等待释放锁
	 * @param redisTemplate	redis操作类
	 * @param key	redis key
	 * @param sleepMillis	等待过程中查询锁状态间隔时间 单位 millisecond
	 */
	public static void waitUnLock(RedisTemplate<String, Object> redisTemplate, String key, long sleepMillis) {
		key = lock_prefix + key;
		boolean hasLock = true;
		//TODO:缓存key对象，本地同步，降低redis访问频次
		while (hasLock) {
			hasLock = redisTemplate.opsForValue().get(key) != null;
			log.info("尝试获取 -{}，-{}", key, hasLock);
			if (hasLock) {
				try {
					Thread.sleep(sleepMillis);
				} catch (InterruptedException e) {
					log.info("等待锁释放间隔时间休眠失败, -{}", e.getMessage(), e);
				}
			}
		}
	}
}
