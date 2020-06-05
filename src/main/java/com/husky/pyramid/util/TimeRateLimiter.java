package com.husky.pyramid.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 时间限流工具
 *
 * <p>主要用于 guava RateLimiter permitsPerSecond < 1 的情形
 * 使用时间 + 可重入锁双重限流
 *
 * @author dengweichang
 */
@Slf4j
public class TimeRateLimiter {

	/**
	 * 流量产生的最小间隔时间
	 */
	private final long seconds;

	/**
	 * 最近一次产生流量的毫秒数
	 */
	private volatile long lastTimeMillis;

	private final ReentrantLock lock = new ReentrantLock();


	public TimeRateLimiter(long seconds) {
		this.seconds = seconds;
		this.lastTimeMillis = System.currentTimeMillis() - seconds * 1000;
	}

	public void invoke(Apply apply) {
		long currentMillis = System.currentTimeMillis();
		if (hasPermit(currentMillis) && lock.tryLock()) {
			try {
				apply.apply();
				lastTimeMillis = currentMillis;
			} finally {
				lock.unlock();
			}
		}
		else {
			log.info(Thread.currentThread().getName() + " ：被限流");

		}
	}

	private boolean hasPermit(long currentMillis) {
		return currentMillis - lastTimeMillis > seconds * 1000;
	}

}
