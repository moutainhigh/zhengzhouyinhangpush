package com.abtnetworks.totems.command.line.inf.service;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/16 14:23'.
 */
public interface ServiceObjectInterface extends BasicInterface {


    /**
     * 服务对象名称
     * @param name 服务对象名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateServiceObjectName(String name, Map<String, Object> map, String[] args) throws Exception {
        return name;
    }

    /**
     * 删除服务对象
     * @param delStr 删除服务对象标记字符串
     * @param attachStr 附加字符串
     * @param name 服务对象名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String deleteServiceObjectCommandLine(String delStr, String attachStr, String name, Map<String, Object> map, String[] args) throws Exception {
        return String.format("%s %s", delStr, name);
    }


    /**
     * 创建服务对象
     * @param statusTypeEnum  状态类型
     * @param name 服务对象名称
     * @param id 服务对象id
     * @param attachStr 附加字符串
     * @param serviceParamDTOList
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateServiceObjectCommandLine(StatusTypeEnum statusTypeEnum,
                                                    String name, String id, String attachStr,
                                                    List<ServiceParamDTO> serviceParamDTOList,
                                                    String description,
                                                    Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

}
