package com.abtnetworks.totems.issued.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuchanghao
 * @desc 命令下发之后检查主备双活状态命令回显
 * @date 2020-11-02 14:52
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AfterActiceStandby {
}
