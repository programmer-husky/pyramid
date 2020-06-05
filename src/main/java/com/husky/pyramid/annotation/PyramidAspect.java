package com.husky.pyramid.annotation;

import com.husky.pyramid.cache.PyramidCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @see Pyramid 切面
 * @author dengweichang
 */
@Slf4j
@Component
@Aspect
public class PyramidAspect {

	/**
	 * SpEL表达式解析器
	 */
	private ExpressionParser parser = new SpelExpressionParser();

	/**
	 * 参数匹配器
	 */
	private LocalVariableTableParameterNameDiscoverer discoverer = new LocalVariableTableParameterNameDiscoverer();

	private final PyramidCacheManager pyramidCacheManager;

	@Autowired
	public PyramidAspect(PyramidCacheManager pyramidCacheManager) {
		this.pyramidCacheManager = pyramidCacheManager;
	}

	@Pointcut("@annotation(com.meeruu.pyramid.annotation.Pyramid)")
	public void cachePointcut() {
	}

	@Pointcut("@annotation(com.meeruu.pyramid.annotation.Del)")
	public void clearPointcut() {
	}


	@Around("cachePointcut()")
	public Object registerInvocation(ProceedingJoinPoint joinPoint) {
		Method method = filterMethod(joinPoint);
		Object[] args = joinPoint.getArgs();
		return pyramidCacheManager.generalCache(parserAnnotation(method, args), objects -> {
			try {
				return joinPoint.proceed(objects);
			} catch (Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}, args);
	}

	@AfterReturning("clearPointcut()")
	public void clearInvocation(JoinPoint joinPoint) {
		Method method = filterMethod(joinPoint);
		Object[] args = joinPoint.getArgs();
		DelModel delModel = parserDel(method, args);
		pyramidCacheManager.generalClear(delModel, true);
	}

	private Method filterMethod(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method =  signature.getMethod();
		//防止注解加在接口或抽象方法上，获取真实的调用对象
		Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
		if (targetClass == null && joinPoint.getTarget() != null) {
			targetClass = joinPoint.getTarget().getClass();
		}
		Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
		//防止桥接方法
		return BridgeMethodResolver.findBridgedMethod(specificMethod);
	}

	/**
	 * 解析SpEL表达式，获取注解上的实际值
	 *
	 * @return	{@link PyramidModel}
	 */
	private PyramidModel parserAnnotation(Method method, Object[] args) {
		Pyramid annotation = method.getAnnotation(Pyramid.class);
		Object key = parserKey(method, args, annotation.key(), annotation.collection());
		long redisExpiration = annotation.redisExpiration();
		long defaultNull = -1023L;
		if (defaultNull == redisExpiration) {
			redisExpiration = pyramidCacheManager.getExpiration();
		}
		return new PyramidModel(annotation.cacheName(), key, annotation.collection(), annotation.collectionArgsIndex(),
				annotation.onlyLocal(), annotation.onlyDistributed(),
				redisExpiration, annotation.nativeExpiration(), annotation.refreshTime());
	}

	/**
	 * 解析SpEL表达式，获取注解上的实际值
	 *
	 * @return	{@link DelModel}
	 */
	private DelModel parserDel(Method method, Object[] args) {
		Del del = method.getAnnotation(Del.class);
		Object key = parserKey(method, args, del.key(), del.collection());
		return new DelModel(del.cacheName(), key, del.collection());
	}

	private Object parserKey(Method method, Object[] args, String spel, boolean collection) {
		String[] parameterNames = discoverer.getParameterNames(method);
		if (!StringUtils.isEmpty(parameterNames)) {
			StandardEvaluationContext context = new StandardEvaluationContext();
			for (int i = 0; i < parameterNames.length; i++) {
				context.setVariable(parameterNames[i], args[i]);
			}
			Expression expression = parser.parseExpression(spel);
			return expression.getValue(context, collection ? String.class : Collection.class);
		}
		return spel;
	}

}
