package com.husky.pyramid.annotation;

import java.lang.annotation.*;

/**
 * 对象中的缓存标记
 * @author dengweichang
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PyramidKey {
}
