package com.abtnetworks.totems.issued.annotation;

import java.lang.annotation.*;

/**
 * @author huangsheng
 * @Description: 下发前 检查 当前时间 是否在下发时间锁（高级设置中）范围内 在 则不允许发
 * @date 2021/9/24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PushTimeLockCheck {
    boolean value() default true;
}
