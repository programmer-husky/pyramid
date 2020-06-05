package com.husky.pyramid.cache;

import com.husky.pyramid.annotation.PyramidModel;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 缓存行为接口
 * @author dengweichang
 */
@SuppressWarnings("unused")
public interface CacheSupport {

	/**
	 * 缓存单条数据
	 * @param key 缓存标识
	 * @param object    缓存单个缓存
	 * @param pyramid	缓存配置相关
	 */
	void singleCache(@NonNull String key, Object object, PyramidModel pyramid);

	/**
	 * 缓存批量数据
	 * @param strMap    单条数据组合的hash结构
	 * @param pyramid	缓存配置相关
	 */
	void batchCache(@NonNull Map<String, Object> strMap, PyramidModel pyramid);

	/**
	 * 清除单条缓存数据
	 * @param key	缓存标识
	 */
	void clear(@NonNull String key);

	/**
	 * 批量清除缓存数据
	 * @param keys	缓存标识的集合
	 */
	void batchClear(@NonNull Collection<String> keys);

	/**
	 * 获取单个缓存
	 * @param key    缓存唯一标记
	 * @return	缓存值
	 */
	Object getCache(@NonNull String key);

	/**
	 * 批量获取缓存
	 * @param set    缓存标记集合
	 * @return	缓存值的集合
	 */
	Map<String, Object> batchGetCache(Set<String> set);

}
