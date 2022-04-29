package com.abtnetworks.totems.issued.annotation;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Administrator
 * @Title:
 * @Description: 对单独的命令在下发中 需要修改的可以使用这个注解来实现
 * @date 2020/9/4
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface UpdateCommandSend {
    /**
     * 具体型号
     *
     * @return
     */
    DeviceModelNumberEnum[] modelValue();
}
