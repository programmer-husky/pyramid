package com.husky.pyramid.controller;

import com.husky.pyramid.annotation.Pyramid;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("tag")
public class TagController {

	@GetMapping("/{id}/{name}")
	@Pyramid(cacheName = "yes", key = "#id+#name", redisExpiration = 5)
	public Object list(@PathVariable String id, @PathVariable String name) {
		System.out.println(id);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			return "fuck";
		}
		return id;
	}
}
