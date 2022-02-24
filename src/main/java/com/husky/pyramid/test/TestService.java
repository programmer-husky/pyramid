package com.husky.pyramid.test;

import com.husky.pyramid.annotation.Del;
import com.husky.pyramid.annotation.Pyramid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用于执行单测的类
 * @author programmer_husky
 */
@Slf4j
@Service
public class TestService {

	@Pyramid(key = "#key", cacheName = "product", redisExpiration = 300)
	public Map<String, String> cacheKey(String key) {
		log.info("内部-{}", key);
		Map<String, String> map = new HashMap<>();
		map.put("fuck", "you");
		return map;
	}

	@Del(key = "#key", cacheName = "product")
	public void clearKey(String key) {
		log.info("清空-{}", key);
	}

	@Pyramid(key = "#list", collection = true, cacheName = "product", redisExpiration = 300)
	public List<Map> cacheKey2(List<String> list) {
		log.info("内部-{}", list);
		return list.stream().map(key -> {
			HashMap<Object, Object> map = new HashMap<>();
			map.put(key, key+"-value");
			return map;
		}).collect(Collectors.toList());
	}

	@Del(key = "#list", collection = true, cacheName = "product")
	public void delKeys(List<String> list) {
		log.info("invoke del keys-{}", String.join(",", list));
	}

	/**
	 * 批量操作只允许返回List
	 * @param ids
	 * @return
	 */
	@Deprecated
	@Pyramid(key = "#ids", collection = true, cacheName = "map", redisExpiration = 300)
	public Map<Integer, String> getByIds(List<Integer> ids) {
		HashMap<Integer, String> integerStringHashMap = new HashMap<>();
		ids.forEach(integer -> integerStringHashMap.put(integer, integer + "-value"));
		return integerStringHashMap;
	}

	@Pyramid(key = "#a.key")
	public A getObject(A a) {
		return a;
	}

	@Pyramid(key = "#list?.![key]", collection = true)
	public List<A> listA(List<A> list) {
		return list;
	}

}

