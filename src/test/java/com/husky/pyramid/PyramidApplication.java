package com.husky.pyramid;

import com.husky.pyramid.config.GlobalConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 二级缓存组件
 * @author dengweichang
 */
@SpringBootApplication
@ComponentScan(basePackageClasses = GlobalConfig.class)
public class PyramidApplication {

	public static void main(String[] args) {
		SpringApplication.run(PyramidApplication.class, args);
	}

}
