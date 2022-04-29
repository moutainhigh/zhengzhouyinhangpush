package com.abtnetworks.totems.common.annotation;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.PolicyEnum;

import java.lang.annotation.*;

/**
 * 定制自定义命令行
 * @author Administrator
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CustomCli {
    /**
     * 设备型号枚举
     *
     * @return
     */
    DeviceModelNumberEnum value();

    /**
     * 策略类型枚举
     *
     * @return
     */
    PolicyEnum type();

    /**
     * 被代理的类
     * @return
     */
    Class  classPoxy() default Class.class;
}
