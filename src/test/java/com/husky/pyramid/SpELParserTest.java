package com.husky.pyramid;

import com.alibaba.fastjson.JSON;
import com.husky.pyramid.test.A;
import com.husky.pyramid.test.TestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest()
@Slf4j
public class SpELParserTest {

	@Autowired
	private TestService testService;

	@Test
	public void testCacheKey() throws Exception {
		CountDownLatch countDownLatch = new CountDownLatch(10);
		for (int i = 0; i < 10; i++) {
			int finalI = i;
			new Thread(() -> {
				try {
					countDownLatch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				log.info("序号;" + finalI);
				testService.cacheKey("key1");
			}).start();
			countDownLatch.countDown();
		}
		Thread.sleep(5000);
	}

	@Test
	public void testDel() throws Exception {
		testService.cacheKey("key1");
		Thread.sleep(2000L);
		testService.cacheKey("key1");
	}

	@Test
	public void testList() {
		List<Map> maps = testService.cacheKey2(Arrays.asList("str1", "str2"));
		List<Map> maps1 = testService.cacheKey2(Arrays.asList("str2", "str1"));
		log.info("黑盒返回");
		log.info(JSON.toJSONString(maps));
		log.info(JSON.toJSONString(maps1));
	}

	@Test
	public void testClear() {
		testService.clearKey("haha*");
	}


	@Test
	public void testListDel() {
		List<String> keys = Arrays.asList("str1", "str2");
		List<Map> maps = testService.cacheKey2(keys);
		testService.delKeys(keys);
	}


	@Test
	public void testBatchMap() {
		Map<Integer, String> byIds = testService.getByIds(Arrays.asList(1, 2, 3, 4));
		log.info(JSON.toJSONString(byIds));
	}

	@Test
	public void testObj() throws Exception {
		A a = new A();
		A a1 = testService.getObject(a);
		Thread.sleep(2000L);
		A a2 = testService.getObject(a);
	}

	@Test
	public void testListObj() {
//		A a = new A("fuck", "u");
//		A b = new A("fuck2", "me");
		testService.listA(null);
	}
}
