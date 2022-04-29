package com.abtnetworks.totems.command.line.inf.service;

import com.abtnetworks.totems.command.line.dto.ServiceParamDTO;
import com.abtnetworks.totems.command.line.enums.StatusTypeEnum;
import com.abtnetworks.totems.command.line.inf.BasicInterface;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/4/16 14:23'.
 */
public interface ServiceObjectGroupInterface extends BasicInterface {


    /**
     * 服务对象组名称
     * @param groupName 服务组名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateServiceObjectGroupName(String groupName, Map<String, Object> map, String[] args) throws Exception {
        return groupName;
    }

    /**
     * 删除服务对象组 命令行
     * @param delStr 删除标记
     * @param attachStr 附加字符串Str
     * @param groupName 服务对象组名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String deleteServiceObjectGroupCommandLine(String delStr, String attachStr, String groupName, Map<String, Object> map, String[] args) throws Exception {
        return String.format("%s %s", delStr, groupName);
    }

    /**
     * 创建服务对象组 命令行
     * @param statusTypeEnum 状态类型
     * @param name 服务组名称
     * @param id 服务组id
     * @param attachStr 附加Str字符串
     * @param serviceParamDTOList 服务DTO
     * @param description 备注
     * @param serviceObjectNameRefArray 引用服务对象名称
     * @param serviceObjectGroupNameRefArray 引用服务组对象名称
     * @param map 扩展参数 key-value String:Object类型
     * @param args 扩展参数 String[] 类型
     * @return
     * @throws Exception
     */
    default String generateServiceObjectGroupCommandLine(StatusTypeEnum statusTypeEnum,
                                                         String name, String id, String attachStr,
                                                         List<ServiceParamDTO> serviceParamDTOList,
                                                         String description,
                                                         String[] serviceObjectNameRefArray, String[] serviceObjectGroupNameRefArray,
                                                         Map<String, Object> map, String[] args) throws Exception {
        return "";
    }

}
