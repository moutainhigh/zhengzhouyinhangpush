package com.abtnetworks.totems.issued.annotation;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: zy
 * @Date: 2019/11/6
 * @desc: 对下发命令时执行移动赋能
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface InExecuteMove {
    /**
     * 具体型号
     *
     * @return
     */
    DeviceModelNumberEnum[] modelValue();


}
