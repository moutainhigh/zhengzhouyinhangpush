package com.abtnetworks.totems.command.line.inf;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 *
 * 命令行 原子化设计 基-接口
 *
 * @Description
 * @Version
 * @Created by hw on '2021/4/6 17:19'.
 */
public interface BasicInterface {

    /**
     * 前置命令
     * @param isVsys 是否为虚设备
     * @param vsysName 虚设备名称
     * @param map
     * @param args
     * @return
     */
    default String generatePreCommandline(Boolean isVsys,String vsysName, Map<String,Object> map , String[] args){
        return StringUtils.EMPTY;
    }

    /**
     * 后置命令
     * @param map
     * @param args
     * @return
     */
    default String generatePostCommandline(Map<String,Object> map , String[] args){
        return StringUtils.EMPTY;
    }
}
