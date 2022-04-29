package com.abtnetworks.totems.issued.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 对连接远程时的处理
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ConnectDispose {
    /*默认不需要**/

    boolean isUse() default false;
}
