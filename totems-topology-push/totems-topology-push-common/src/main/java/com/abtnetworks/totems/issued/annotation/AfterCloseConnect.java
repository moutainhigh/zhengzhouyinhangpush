package com.abtnetworks.totems.issued.annotation;


import java.lang.annotation.*;

/**
 * @author lifei
 * @desc 通过命令行关闭服务端连接
 * @date 2021-10-26 14:52
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AfterCloseConnect {
}
