package com.abtnetworks.totems.command.line;

import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.whale.baseapi.ro.*;

import java.util.List;
import java.util.Map;

public interface TranslationPolicyGenerator {

    /**
     * 策略迁移 生成地址对象命令行
     * @param netWorkObjectList 引擎返回的地址对象
     * @param map 扩展参数
     * @return
     * @throws Exception
     */
    public String generateAddressObjectByTranslation(ResultRO<List<NetWorkGroupObjectRO>> netWorkObjectList, Map<String, Object> map) throws Exception;

    /**
     * 策略迁移 生成地址组对象命令行
     * @param netWorkGroupObjectList 引擎返回的地址组对象
     * @param map 扩展参数
     * @return
     * @throws Exception
     */
    public String generateAddressGroupObjectByTranslation(ResultRO<List<NetWorkGroupObjectRO>> netWorkGroupObjectList, Map<String, Object> map) throws Exception;

    /**
     * 策略迁移 创建时间对象命令行
     * @param timeObjectList 引擎返回的时间对象
     * @param map 扩展参数
     * @return
     * @throws Exception
     */
    public String generateTimeObjectByTranslation(ResultRO<List<TimeObjectRO>> timeObjectList, Map<String, Object> map) throws Exception;

    /**
     * 策略迁移 创建服务对象命令行
     * @param serviceObjectList 引擎返回的服务对象
     * @param map 扩展参数
     * @return
     * @throws Exception
     */
    public String generateServiceObjectByTranslation(ResultRO<List<ServiceGroupObjectRO>> serviceObjectList, Map<String, Object> map) throws Exception;

    /**
     * 策略迁移 创建服务组对象命令行
     * @param serviceGroupObjectList 引擎返回的服务组对象
     * @param map 扩展参数
     * @return
     * @throws Exception
     */
    public String generateServiceGroupObjectByTranslation(ResultRO<List<ServiceGroupObjectRO>> serviceGroupObjectList, Map<String, Object> map) throws Exception;


    /**
     * 策略迁移 生成策略集（根据实际防火墙设备，可选） 和 生成策略
     * @param deviceUuid 设备uuid
     * @param deviceFilterlistRO 策略集
     * @param listResultRO 策略列表
     * @param map
     * @return
     * @throws Exception
     */
    public String generateFilterListAndRuleListByTranslation(String deviceUuid, DeviceFilterlistRO deviceFilterlistRO, ResultRO<List<DeviceFilterRuleListRO>> listResultRO, Map<String, Object> map) throws Exception;


}
