package com.abtnetworks.totems.command.line.inf.service;

import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.Map;

/**
 *
 * @Description
 * @Version
 * @Created by hw on '2021/4/15 17:27'.
 */
public interface PortInterface extends BasicInterface {

    /**
     * 生成引用端口Str(文本，对象名等) 命令行
     * @param strRefArray
     * @param map
     * @param args
     * @return
     * @throws Exception
     */
    default String generatePortRefStrCommandLine(String[] strRefArray, Map<String, Object> map, String[] args) throws Exception {
        return "";
    }


}
