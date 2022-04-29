package com.abtnetworks.totems.issued.annotation;

import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author liuchanghao
 * @desc 命令下发前检查主备双活状态
 * @date 2020-11-02 14:52
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AroundCheckActiveStandBy {

    /**
     * 具体型号
     *
     * @return
     */
    DeviceModelNumberEnum[] modelValue();
}
