package com.husky.pyramid.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * 开启二级缓存条件
 * @author programmer_husky
 */
@Slf4j
public class StartCondition implements Condition {

	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
		Environment environment = conditionContext.getEnvironment();
		String enable = environment.getProperty("pyramid.enable");
		if (StringUtils.isEmpty(enable) || Boolean.FALSE.toString().equals(enable)) {
			log.info("未开启 Pyramid cache");
			return false;
		}
		String distributedEnableStr = environment.getProperty("pyramid.distributed-enable");
		boolean distributedEnable = false;
		boolean localEnable = false;
		if (!StringUtils.isEmpty(distributedEnableStr)) {
			distributedEnable = Boolean.TRUE.toString().equals(distributedEnableStr);
		}
		String localEnableStr = environment.getProperty("pyramid.local.enable");
		if (!StringUtils.isEmpty(localEnableStr)) {
			localEnable = Boolean.TRUE.toString().equals(localEnableStr);
		}
		if (!(distributedEnable || localEnable)) {
			log.error("两级缓存都未开启");
			return false;
		}
		String node = environment.getProperty("pyramid.node");
		boolean b = StringUtils.hasLength(node);
		if (!b) {
			log.error("请设置分布式缓存节点 Pyramid.node");
		}
		return b;
	}

}
