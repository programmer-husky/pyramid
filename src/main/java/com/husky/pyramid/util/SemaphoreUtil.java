package com.husky.pyramid.util;

import java.util.concurrent.Semaphore;

/**
 * 信号量工具
 *
 * @author programmer_husky
 */
public class SemaphoreUtil {

	/**
	 * 信号量队列中最大等待线程数量
	 */
	private static final int MAX_QUEUE = 50;

	public static void invoke(Semaphore semaphore, Apply apply) {
		invoke(semaphore, apply, null);
	}

	public static void invoke(Semaphore semaphore, Apply apply, Apply orElse) {
		if (semaphore.getQueueLength() > MAX_QUEUE) {
			//这里可以不写，tryAcquire不会进入AQS队列
			return;
		}
		boolean hasPermit = semaphore.tryAcquire();
		if (hasPermit) {
			try {
				apply.apply();
			} finally {
				semaphore.release(1);
			}
		}
		else if (orElse != null) {
			orElse.apply();
		}
	}

}
