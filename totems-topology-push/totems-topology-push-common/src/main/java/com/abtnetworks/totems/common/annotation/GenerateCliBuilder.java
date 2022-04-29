package com.abtnetworks.totems.common.annotation;

import java.lang.annotation.*;

/**
 * 拦截 自定义 命令行生成器
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface GenerateCliBuilder {

}
