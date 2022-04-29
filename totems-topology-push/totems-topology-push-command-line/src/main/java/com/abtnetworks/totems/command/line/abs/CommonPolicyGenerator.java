package com.abtnetworks.totems.command.line.abs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/8 10:40'.
 */
public abstract class CommonPolicyGenerator implements Serializable {

    /**
     * 日志对象 继承者使用
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 日志对象
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(CommonPolicyGenerator.class);

}
