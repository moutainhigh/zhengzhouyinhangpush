package com.abtnetworks.totems.translation.service.impl;

import com.abtnetworks.totems.command.line.dto.*;
import com.abtnetworks.totems.command.line.enums.MaskTypeEnum;
import com.abtnetworks.totems.command.line.enums.ProtocolTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.config.ProtocolMapConfig;
import com.abtnetworks.totems.common.constant.Constants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.PageDTO;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.DeviceNetworkTypeEnum;
import com.abtnetworks.totems.common.enums.PolicyTypeEnum;
import com.abtnetworks.totems.common.mapper.TotemsJsonMapper;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.disposal.BaseService;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dto.task.DeviceForExistObjDTO;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.translation.commandline.TranslationCommandline;
import com.abtnetworks.totems.translation.dao.PredefinedServiceMapper;
import com.abtnetworks.totems.translation.dao.TranslationTaskMappingMapper;
import com.abtnetworks.totems.translation.entity.PredefinedService;
import com.abtnetworks.totems.translation.entity.TranslationTaskMappingEntity;
import com.abtnetworks.totems.translation.entity.TranslationTaskRecordEntity;
import com.abtnetworks.totems.translation.service.TranslationDeviceCommandLineService;
import com.abtnetworks.totems.common.dto.TranslationTaskProgressDTO;
import com.abtnetworks.totems.translation.vo.DeviceZoneInterfaceVO;
import com.abtnetworks.totems.whale.baseapi.dto.RoutingTableSearchDTO;
import com.abtnetworks.totems.whale.baseapi.dto.SearchServiceDTO;
import com.abtnetworks.totems.whale.baseapi.dto.ServiceConditionDTO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.common.CommonRangeIntegerDTO;
import com.abtnetworks.totems.whale.policy.ro.RoutingEntriesRO;
import com.abtnetworks.totems.whale.policybasic.ro.NextHopRO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.dozermapper.core.Mapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @Description
 * @Version
 * @Created by hw on '2021/1/14 10:03'.
 */
@Service
public class TranslationDeviceCommandLineServiceImpl extends BaseService implements TranslationDeviceCommandLineService {

    @Resource
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Resource
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Resource
    NodeMapper nodeMapper;

    @Resource
    private TranslationTaskMappingMapper pushTranslationTaskMappingDao;

    @Autowired
    private Mapper dozerMapper;

    @Autowired
    PredefinedServiceMapper predefinedServiceMapper;

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    ProtocolMapConfig protocolMapConfig;

    /**
     * 进度条 {“taskUuid":TranslationTaskProgressDTO}
     */
    public static ConcurrentHashMap<String, TranslationTaskProgressDTO> progressMap = new ConcurrentHashMap<>();

    private final static String USE_RAW_NAME = "USE_RAW_NAME";

    // 进度条默认值 0
    private final static Integer progressDefault = 0;

    public String invokeMethod(Class tClass, String methodName, Object... args) throws Exception {
        try {
            Object service = ConstructorUtils.invokeConstructor(tClass);

            Set<String> methodSet = new HashSet<>();
            Method[] methods = tClass.getMethods();
            for (Method method : methods) {
                methodSet.add(method.getName());
            }
            if (!methodSet.contains(methodName)) {
                logger.error(String.format("方法名 %s 在 %s Class中不存在！", methodName, tClass));
                return null;
            }

//            FieldUtils.writeField(service, USE_RAW_NAME,true, true);
            String command_line = (String) MethodUtils.invokeMethod(service, methodName, args);

            return command_line;
        } catch (Exception e) {
            logger.error("", e);
            throw e;
        }
    }

    @Override
    public String startTranslation(TranslationTaskRecordEntity taskRecord, Class securityClass) throws Exception {
        String sourceDeviceUuid = taskRecord.getDeviceUuid();
        int policyTotal = getPolicyTotalNum(sourceDeviceUuid);
        logger.info("策略迁移任务{},迁移总数:{}",taskRecord.getUuid(),policyTotal);
        //进度条
        progressMap.put(taskRecord.getUuid(), new TranslationTaskProgressDTO(taskRecord.getUuid(), policyTotal, progressDefault));
        StringBuffer commandLineBuffer = new StringBuffer();
        TranslationCommandline translationCommandlineBean = (TranslationCommandline) ConstructorUtils.invokeConstructor(securityClass);

        // 域和接口映射(策略生成时，通过映射表找到新设备对应的域和接口，如果找不到对应的则使用默认域和接口，则使用新设备中同名的域和接口，如果新设备没有则生成该策略异常)
        List<TranslationTaskMappingEntity> deviceZoneMappingList = pushTranslationTaskMappingDao.findVOListByTaskUuid(taskRecord.getUuid());
        // 策略命令行生成
        logger.info("{}->{},命令行生成开始",taskRecord.getDeviceModelNumber(),taskRecord.getTargetDeviceModelNumber());
        Map<String, String> generateSecurityResult = this.createFilterListAndRuleList(taskRecord, deviceZoneMappingList, translationCommandlineBean);
        commandLineBuffer.append(generateSecurityResult.get("commandLine"));
        String warning = generateSecurityResult.get("warning");
        taskRecord.setWarning(warning);
        return commandLineBuffer.toString();
    }

    /**
     * 获取策略总数(只迁移安全策略)
     * @param sourceDeviceUuid
     * @return
     */
    @Override
    public int getPolicyTotalNum(String sourceDeviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(sourceDeviceUuid);
        //安全策略
        int safeTotal = 0;
        if (resultRO != null && CollectionUtils.isNotEmpty(resultRO.getData())) {
            for (DeviceFilterlistRO r : resultRO.getData()) {
                if (r.getRuleListType().equalsIgnoreCase(PolicyTypeEnum.SYSTEM__POLICY_1.getRuleListType()) ||
                        r.getRuleListType().equalsIgnoreCase(PolicyTypeEnum.SYSTEM__POLICY_2.getRuleListType()) ||
                        PolicyTypeEnum.SYSTEM__NAT_LIST.name().equals(r.getRuleListType())) {
                    safeTotal += Integer.parseInt(r.getRuleTotal());
                }
            }
        }

        ResultRO<List<RoutingtableRO>> routingTable = whaleDevicePolicyClient.getRoutingTable(sourceDeviceUuid);
        if (routingTable != null && CollectionUtils.isNotEmpty(routingTable.getData())) {
            for (RoutingtableRO r : routingTable.getData()) {
                if (r.getRoutingEntriesTotal() != null) {
                    safeTotal += r.getRoutingEntriesTotal();
                }
            }
        }

        return safeTotal;
    }

    @Override
    public Map<String, String> createFilterListAndRuleList(TranslationTaskRecordEntity taskRecord, List<TranslationTaskMappingEntity> deviceZoneMappingList, TranslationCommandline translationCommandlineBean, Object... args) throws Exception {
        ResultRO<List<DeviceFilterlistRO>> resultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(taskRecord.getDeviceUuid());
        ResultRO<List<RoutingtableRO>> routingTable = whaleDevicePolicyClient.getRoutingTable(taskRecord.getDeviceUuid());
        if ((resultRO == null || CollectionUtils.isEmpty(resultRO.getData())) && (routingTable == null || CollectionUtils.isEmpty(routingTable.getData()))) {
            return null;
        }
        String targetDeviceUuid = taskRecord.getTargetDeviceUuid();
        DeviceObjectSearchDTO query = new DeviceObjectSearchDTO();
        StringBuilder allCommandLineBuilder = new StringBuilder();
        StringBuffer warningBuilder = new StringBuffer();

        Map<String, ServiceGroupObjectRO> targetPredefinedServiceMap = new HashMap<>();
        Set<String> targetServiceObjectNames = new HashSet<>();
        Set<String> targetServiceGroupObjectNames = new HashSet<>();
        Set<String> targetAddressPoolNames = new HashSet<>();
        Set<String> targetAddressObjectNames = new HashSet<>();
        Set<String> targetAddressGroupObjectNames = new HashSet<>();
        Set<String> targetTimeObjectNames = new HashSet<>();
        Set<String> targetZoneName = new HashSet<>();
        Set<String> targetInterfaceName = new HashSet<>();

        Map<String,String> predefinedServiceMapping = new HashMap<>();
        if(StringUtils.isBlank(targetDeviceUuid)){
            logger.info("策略迁移{} 使用未纳管设备: {}-->{}",taskRecord.getTitleName(),taskRecord.getDeviceName(),taskRecord.getTargetDeviceModelNumber());
        } else {
            logger.info("策略迁移{} 迁移为已纳管设备: {}-->{}",taskRecord.getTitleName(),taskRecord.getDeviceName(),taskRecord.getTargetDeviceName());

            ZoneRO targetDeviceZoneRO = whaleDeviceObjectClient.getDeviceZoneRO(targetDeviceUuid);
            targetZoneName = targetDeviceZoneRO.getData().stream().map(t -> t.getName().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());

            DeviceRO deviceRO = whaleManager.getDeviceByUuid(targetDeviceUuid);
            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            List<DeviceInterfaceRO> deviceInterfaces = deviceDataRO.getDeviceInterfaces();
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(deviceInterfaces)){
                for (DeviceInterfaceRO deviceInterface : deviceInterfaces) {
                    if(StringUtils.isNotBlank(deviceInterface.getName())){
                        targetInterfaceName.add(deviceInterface.getName());
                    }
                    if(StringUtils.isNotBlank(deviceInterface.getAlias())){
                        targetInterfaceName.add(deviceInterface.getAlias());
                    }
                }
            }

            //获取服务对象放入set集合
            //设备对象查询
            query.setDeviceUuid(targetDeviceUuid);
            // 预定义服务对象
            ResultRO<List<ServiceGroupObjectRO>> targetPredefinedService = whaleDeviceObjectClient.getPredefinedService(query);
            if (ObjectUtils.isNotEmpty(targetPredefinedService) && CollectionUtils.isNotEmpty(targetPredefinedService.getData())) {
                targetPredefinedServiceMap = targetPredefinedService.getData().stream().collect(Collectors.toMap(t -> t.getName(), t -> t, (t1, t2) -> t1));
            }

            ResultRO<List<ServiceGroupObjectRO>> targetServiceObjectList = whaleDeviceObjectClient.getServiceObject(query);
            if (ObjectUtils.isNotEmpty(targetServiceObjectList) && CollectionUtils.isNotEmpty(targetServiceObjectList.getData())) {
                targetServiceObjectNames = targetServiceObjectList.getData().stream().map(t -> t.getName()).collect(Collectors.toSet());
            }
            //服务组对象
            ResultRO<List<ServiceGroupObjectRO>> targetServiceGroupObjectList = whaleDeviceObjectClient.getServiceGroupObject(query);
            if (ObjectUtils.isNotEmpty(targetServiceGroupObjectList) && CollectionUtils.isNotEmpty(targetServiceGroupObjectList.getData())) {
                targetServiceGroupObjectNames = targetServiceGroupObjectList.getData().stream().map(t -> t.getName()).collect(Collectors.toSet());
            }
            //地址对象
            ResultRO<List<NetWorkGroupObjectRO>> targetNetWorkObjectList = whaleDeviceObjectClient.getNetWorkObject(query);
            if (ObjectUtils.isNotEmpty(targetNetWorkObjectList) && CollectionUtils.isNotEmpty(targetNetWorkObjectList.getData())) {
                for (NetWorkGroupObjectRO datum : targetNetWorkObjectList.getData()) {
                    if(datum.getDeviceNetworkType() == null){
                        targetAddressObjectNames.add(datum.getName());
                    } else {
                        targetAddressPoolNames.add(datum.getName());
                    }
                }
            }
            //地址组对象
            ResultRO<List<NetWorkGroupObjectRO>> targetAddressGroupObjectList = whaleDeviceObjectClient.getNetWorkGroupObject(query);
            if (ObjectUtils.isNotEmpty(targetAddressGroupObjectList) && CollectionUtils.isNotEmpty(targetAddressGroupObjectList.getData())) {
                for (NetWorkGroupObjectRO datum : targetAddressGroupObjectList.getData()) {
                    if(datum.getDeviceNetworkType() == null){
                        targetAddressGroupObjectNames.add(datum.getName());
                    } else {
                        targetAddressPoolNames.add(datum.getName());
                    }
                }
            }
            //时间对象
            ResultRO<List<TimeObjectRO>> targetTimeObjectList = whaleDeviceObjectClient.getTimeObject(query);
            if (ObjectUtils.isNotEmpty(targetTimeObjectList) && CollectionUtils.isNotEmpty(targetTimeObjectList.getData())) {
                targetTimeObjectNames = targetTimeObjectList.getData().stream().map(t -> t.getName()).collect(Collectors.toSet());
            }


        }
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(targetDeviceUuid);
        Boolean isVsys = null;
        String vSysName = null;
        if (!deviceRO.getSuccess()) {
            logger.error("不存在该设备[{}]", targetDeviceUuid);
        } else {
            DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
            isVsys = deviceDataRO.getIsVsys();
            if (isVsys != null && isVsys) {
                vSysName= deviceDataRO.getVsysName();
            }
        }
        //旧设备
        query.setDeviceUuid(taskRecord.getDeviceUuid());
        Map<String, ServiceGroupObjectRO> sourcePredefinedServiceMap = new HashMap<>();
        Map<String, ServiceGroupObjectRO> sourceServiceObjectMap = new HashMap<>();
        Map<String, ServiceGroupObjectRO> sourceServiceGroupObjectMap = new HashMap<>();
        Map<String, NetWorkGroupObjectRO> sourceAddressPoolMap = new HashMap<>();
        Map<String, NetWorkGroupObjectRO> sourceAddressObjectMap = new HashMap<>();
        Map<String, NetWorkGroupObjectRO> sourceAddressGroupObjectMap = new HashMap<>();
        Map<String, TimeObjectRO> sourceTimeObjectMap = new HashMap<>();
        //获取服务对象

        Map<String,PredefinedService> allSourcePredefinedServiceMap = new HashMap<>();
        List<PredefinedService> allPredefinedServiceList = predefinedServiceMapper.findAll();
        for (PredefinedService predefinedService : allPredefinedServiceList) {
            String venderObjName = predefinedService.getVenderObjName();
            JSONArray venderObjNameList = TotemsJsonMapper.fromJson(venderObjName, JSONArray.class);
            for (int i = 0; i < venderObjNameList.size(); i++) {
                JSONObject jsonObject = venderObjNameList.getJSONObject(i);
                String vender = jsonObject.getString("vender");
                String objName = jsonObject.getString("objName");
                if(taskRecord.getDeviceVendorId().equals(vender)){
                    allSourcePredefinedServiceMap.put(objName,predefinedService);
                }
            }
        }


        // 预定义服务对象
        ResultRO<List<ServiceGroupObjectRO>> sourcePredefinedService = whaleDeviceObjectClient.getPredefinedService(query);
        if (ObjectUtils.isNotEmpty(sourcePredefinedService) && CollectionUtils.isNotEmpty(sourcePredefinedService.getData())) {
            sourcePredefinedServiceMap = sourcePredefinedService.getData().stream().collect(Collectors.toMap(t -> t.getName(), t -> t, (t1, t2) -> t1));
        }
        // 服务对象
        ResultRO<List<ServiceGroupObjectRO>> sourceServiceObjectList = whaleDeviceObjectClient.getServiceObject(query);
        if (ObjectUtils.isNotEmpty(sourceServiceObjectList) && CollectionUtils.isNotEmpty(sourceServiceObjectList.getData())) {
            sourceServiceObjectMap = sourceServiceObjectList.getData().stream().collect(Collectors.toMap(t -> t.getName(), t -> t, (t1, t2) -> t1));
        }
        // 服务组对象
        ResultRO<List<ServiceGroupObjectRO>> sourceServiceGroupObjectList = whaleDeviceObjectClient.getServiceGroupObject(query);
        if (ObjectUtils.isNotEmpty(sourceServiceGroupObjectList) && CollectionUtils.isNotEmpty(sourceServiceGroupObjectList.getData())) {
            sourceServiceGroupObjectMap = sourceServiceGroupObjectList.getData().stream().collect(Collectors.toMap(t -> t.getName(), t -> t, (t1, t2) -> t1));
        }
        //地址对象
        ResultRO<List<NetWorkGroupObjectRO>> sourceNetWorkObjectList = whaleDeviceObjectClient.getNetWorkObject(query);
        if (ObjectUtils.isNotEmpty(sourceNetWorkObjectList) && CollectionUtils.isNotEmpty(sourceNetWorkObjectList.getData())) {
            for (NetWorkGroupObjectRO datum : sourceNetWorkObjectList.getData()) {
                if(datum.getDeviceNetworkType() == null){
                    sourceAddressObjectMap.put(datum.getName(),datum);
                } else {
                    sourceAddressPoolMap.put(datum.getName(),datum);
                }
            }
        }
        //地址组对象
        ResultRO<List<NetWorkGroupObjectRO>> sourceAddressGroupObjectList = whaleDeviceObjectClient.getNetWorkGroupObject(query);
        if (ObjectUtils.isNotEmpty(sourceAddressGroupObjectList) && CollectionUtils.isNotEmpty(sourceAddressGroupObjectList.getData())) {
            for (NetWorkGroupObjectRO datum : sourceAddressGroupObjectList.getData()) {
                if(datum.getDeviceNetworkType() == null){
                    sourceAddressGroupObjectMap.put(datum.getName(),datum);
                } else {
                    sourceAddressPoolMap.put(datum.getName(),datum);
                }
            }
        }
        //时间对象
        ResultRO<List<TimeObjectRO>> sourceTimeObjectList = whaleDeviceObjectClient.getTimeObject(query);
        if (ObjectUtils.isNotEmpty(sourceTimeObjectList) && CollectionUtils.isNotEmpty(sourceTimeObjectList.getData())) {
            sourceTimeObjectMap = sourceTimeObjectList.getData().stream().collect(Collectors.toMap(t -> t.getName(), t -> t, (t1, t2) -> t1));
        }

//        组名与对象信息的映射
        Map<String, Object> addressGroupNameObjectMap = new HashMap<>();
        Map<String, Object> serviceGroupNameObjectMap = new HashMap<>();
        Map<String, Object> srcIpNameObjectMap = new HashMap<>();
        Map<String, Object> dstIpNameObjectMap = new HashMap<>();
        Map<String, Object> serviceNameObjectMap = new HashMap<>();

        Map<String,String> interfaceMap = new HashMap<>();
        Map<String,String> zoneMap = new HashMap<>();

        Map<String, String> mappingMap = deviceZoneMappingList.stream().collect(Collectors.toMap(t -> t.getSourceValue().toLowerCase(Locale.ROOT), t -> t.getTargetValue(), (t1, t2) -> t1));
        for (TranslationTaskMappingEntity translationTaskMappingEntity : deviceZoneMappingList) {
            if("1".equals(translationTaskMappingEntity.getTargetType())){
                zoneMap.put(translationTaskMappingEntity.getSourceValue().toLowerCase(Locale.ROOT),translationTaskMappingEntity.getTargetValue());
            } else if ("2".equals(translationTaskMappingEntity.getTargetType())){
                interfaceMap.put(translationTaskMappingEntity.getSourceValue().toLowerCase(Locale.ROOT),translationTaskMappingEntity.getTargetValue());
            }else {
                interfaceMap.put(translationTaskMappingEntity.getSourceValue().toLowerCase(Locale.ROOT),translationTaskMappingEntity.getTargetValue());
                zoneMap.put(translationTaskMappingEntity.getSourceValue().toLowerCase(Locale.ROOT),translationTaskMappingEntity.getTargetValue());
            }
        }

        Map<String,List<String>> addressNameMap = new HashMap<>();

        allCommandLineBuilder.append(translationCommandlineBean.generatePreCommandLine(isVsys,vSysName));

        List<DeviceFilterlistRO> securityFilterList = new ArrayList<>();
        List<DeviceFilterlistRO> natSecurityFilterList = new ArrayList<>();
        for (DeviceFilterlistRO datum : resultRO.getData()) {
            if (PolicyTypeEnum.SYSTEM__IMPORTED_ROUTING_TABLES.name().equals(datum.getRuleListType()) ||
                    PolicyTypeEnum.SYSTEM__ROUTING_TABLES.name().equals(datum.getRuleListType()) ||
                    PolicyTypeEnum.SYSTEM__POLICY_ROUTING.name().equals(datum.getRuleListType()) ||
                    PolicyTypeEnum.SYSTEM__GENERIC_ACL.name().equals(datum.getRuleListType())) {
                continue;
            }
            if(PolicyTypeEnum.SYSTEM__NAT_LIST.name().equals(datum.getRuleListType())){
                natSecurityFilterList.add(datum);
            } else {
                securityFilterList.add(datum);
            }
        }
        //先生成安全策略命令行然后生成nat策略命令行
        List<DeviceFilterlistRO> allFilterList = new ArrayList<>();
        allFilterList.addAll(securityFilterList);
        allFilterList.addAll(natSecurityFilterList);
        for (DeviceFilterlistRO deviceFilterlistRO : allFilterList) {

            if ("0".equals(deviceFilterlistRO.getRuleTotal())) {
                continue;
            }
             //只生成安全策略
            if (PolicyTypeEnum.SYSTEM__IMPORTED_ROUTING_TABLES.name().equals(deviceFilterlistRO.getRuleListType()) ||
                    PolicyTypeEnum.SYSTEM__ROUTING_TABLES.name().equals(deviceFilterlistRO.getRuleListType()) ||
                    PolicyTypeEnum.SYSTEM__POLICY_ROUTING.name().equals(deviceFilterlistRO.getRuleListType()) ||
                    PolicyTypeEnum.SYSTEM__GENERIC_ACL.name().equals(deviceFilterlistRO.getRuleListType())) {
                continue;
            }
            String msgPrefix = null;
            if(PolicyTypeEnum.SYSTEM__NAT_LIST.name().equals(deviceFilterlistRO.getRuleListType())){
                msgPrefix = "NAT";
            } else {
                msgPrefix = "安全";
            }
            ResultRO<List<DeviceFilterRuleListRO>> listResultRO = whaleDevicePolicyClient.getFilterRuleList(taskRecord.getDeviceUuid(), deviceFilterlistRO.getUuid());
            List<DeviceFilterRuleListRO> needAddFilter = new ArrayList<>();
            DeviceFilterRuleListRO lastStaticNatFilter = null;
            out:for (int h = 0;h < listResultRO.getData().size();h++) {
                DeviceFilterRuleListRO deviceFilterRuleListRO = listResultRO.getData().get(h);
                String ruleName = deviceFilterRuleListRO.getName();
                if(ruleName == null){
                    ruleName = deviceFilterRuleListRO.getRuleId();
                }
                // 避免当前策略下的对象未生成成功，后续对象判断时目的设备却包含这些对象
                Set<String> currentServiceObjectNames = new HashSet<>();
                Set<String> currentServiceGroupObjectNames = new HashSet<>();
                Set<String> currentAddressPoolNames = new HashSet<>();
                Set<String> currentAddressObjectNames = new HashSet<>();
                Set<String> currentAddressGroupObjectNames = new HashSet<>();
                Set<String> currentTimeObjectNames = new HashSet<>();

                Set<String> currentRuleAddressNameSet = new HashSet<>();
                StringBuilder commandLineBuilder = new StringBuilder();
                if(StringUtils.isNotBlank(taskRecord.getUuid()) && TranslationDeviceCommandLineServiceImpl.progressMap.containsKey(taskRecord.getUuid())){
                    TranslationDeviceCommandLineServiceImpl.progressMap.get(taskRecord.getUuid()).increOne();
                }

                // 处理默认策略
                if(!taskRecord.getDeviceVendorId().equals("cisco") && deviceFilterRuleListRO.isImplicit()){
                    String msg = String.format("%s策略 【%s】是默认策略，暂不迁移 \n",msgPrefix,ruleName);
                    logger.info(msg);
                    warningBuilder.append(msg);
                    continue;
                }
                //如果旧设备中的源域替换成新设备中的源域完成映射
                List<String> inInterfaceGroupRefs = deviceFilterRuleListRO.getInInterfaceGroupRefs();
                if(PolicyTypeEnum.SYSTEM__NAT_LIST.name().equals(deviceFilterlistRO.getRuleListType())){
                    if (CollectionUtils.isNotEmpty(inInterfaceGroupRefs)) {
                        String sourceInZone = inInterfaceGroupRefs.get(0).toLowerCase(Locale.ROOT);
                        if (interfaceMap.containsKey(sourceInZone) && StringUtils.isNotBlank(interfaceMap.get(sourceInZone))) {
                            inInterfaceGroupRefs.set(0, interfaceMap.get(sourceInZone));
                        } else {
                            if ("DST".equals(deviceFilterRuleListRO.getNatClause().getString("natField"))) {
                                warningBuilder.append(String.format("%s策略 【%s】 的源域 %s 未映射到新设备，该条策略迁移失败 \n",msgPrefix, ruleName, sourceInZone));
                                continue;
                            }
                        }
                    }
                    List<String> outInterfaceGroupRefs = deviceFilterRuleListRO.getOutInterfaceGroupRefs();
                    if (CollectionUtils.isNotEmpty(outInterfaceGroupRefs)) {
                        String sourceOutZone = outInterfaceGroupRefs.get(0).toLowerCase(Locale.ROOT);
                        if (interfaceMap.containsKey(sourceOutZone) && StringUtils.isNotBlank(interfaceMap.get(sourceOutZone))) {
                            outInterfaceGroupRefs.set(0, interfaceMap.get(sourceOutZone));
                        } else {
                            if ("SRC".equals(deviceFilterRuleListRO.getNatClause().getString("natField"))) {
                                warningBuilder.append(String.format("%s策略 【%s】 的目的域 %s 未映射到新设备，该条策略迁移失败 \n",msgPrefix, ruleName, sourceOutZone));
                                continue;
                            }
                        }
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(inInterfaceGroupRefs)) {
                        String sourceInZone = inInterfaceGroupRefs.get(0).toLowerCase(Locale.ROOT);
                        if (zoneMap.containsKey(sourceInZone) && StringUtils.isNotBlank(zoneMap.get(sourceInZone))) {
                            inInterfaceGroupRefs.set(0, zoneMap.get(sourceInZone));
                        } else if (interfaceMap.containsKey(sourceInZone) && StringUtils.isNotBlank(interfaceMap.get(sourceInZone))) {
                            inInterfaceGroupRefs.set(0, interfaceMap.get(sourceInZone));
                        } else {
                            warningBuilder.append(String.format("%s策略 【%s】 的源域 %s 未映射到新设备，该条策略迁移失败 \n",msgPrefix, ruleName, sourceInZone));
                            continue;
                        }
                    }
                    List<String> outInterfaceGroupRefs = deviceFilterRuleListRO.getOutInterfaceGroupRefs();
                    if (CollectionUtils.isNotEmpty(outInterfaceGroupRefs)) {
                        String sourceOutZone = outInterfaceGroupRefs.get(0).toLowerCase(Locale.ROOT);
                        if (zoneMap.containsKey(sourceOutZone) && StringUtils.isNotBlank(zoneMap.get(sourceOutZone))) {
                            outInterfaceGroupRefs.set(0, zoneMap.get(sourceOutZone));
                        } else  if (interfaceMap.containsKey(sourceOutZone) && StringUtils.isNotBlank(interfaceMap.get(sourceOutZone))) {
                            outInterfaceGroupRefs.set(0, interfaceMap.get(sourceOutZone));
                        } else {
                            warningBuilder.append(String.format("%s策略 【%s】 的目的域 %s 未映射到新设备，该条策略迁移失败 \n",msgPrefix, ruleName, sourceOutZone));
                            continue;
                        }
                    }
                }

                List<ServiceGroupObjectRO> needAddServiceObjectList = new ArrayList<>();
                List<ServiceGroupObjectRO> needAddServiceGroupObjectList = new ArrayList<>();
                List<NetWorkGroupObjectRO> needAddAddressObjectList = new ArrayList<>();
                List<NetWorkGroupObjectRO> needAddAddressGroupObjectList = new ArrayList<>();
                List<TimeObjectRO> needAddTimeObjectList = new ArrayList<>();

                if(StringUtils.isBlank(deviceFilterRuleListRO.getName())){
                    deviceFilterlistRO.setName(deviceFilterRuleListRO.getRuleId());
                }

                JSONObject clause;
                if(PolicyTypeEnum.SYSTEM__NAT_LIST.name().equals(deviceFilterlistRO.getRuleListType())){
                    // NAT 策略迁移
                    clause = deviceFilterRuleListRO.getNatClause();
                    String filterTimeGroupName = deviceFilterRuleListRO.getFilterTimeGroupName();
                    if (StringUtils.isNotBlank(filterTimeGroupName) && filterTimeGroupName.length() > 2) {
                        filterTimeGroupName = filterTimeGroupName.substring(0, filterTimeGroupName.length() - 2);
                        deviceFilterRuleListRO.setFilterTimeGroupName(filterTimeGroupName);
                        if (!targetTimeObjectNames.contains(filterTimeGroupName) && !currentTimeObjectNames.contains(filterTimeGroupName)) {
                            if (sourceTimeObjectMap.containsKey(filterTimeGroupName)) {
                                needAddTimeObjectList.add(sourceTimeObjectMap.get(filterTimeGroupName));
                                currentTimeObjectNames.add(filterTimeGroupName);
                            }
                        }
                    }

                    String natField = clause.getString("natField");
                    if(taskRecord.getTargetDeviceVendorId().equals("westone") &&
                            ("BOTH".equals(natField) || "BI_DIR_BOTH".equals(natField) || "BOTH_SRC".equals(natField) || "BOTH_DST".equals(natField))){
                        warningBuilder.append(String.format("%s策略 【%s】迁移失败,目的设备暂不支持 \n",msgPrefix,ruleName));
                        continue out;
                    }
                    if(clause.containsKey("preServices")){
                        JSONArray preServices = clause.getJSONArray("preServices");
                        takeServiceObject(taskRecord, warningBuilder, targetPredefinedServiceMap, targetServiceObjectNames,currentServiceObjectNames, targetServiceGroupObjectNames, currentServiceGroupObjectNames,sourcePredefinedServiceMap, sourceServiceObjectMap, sourceServiceGroupObjectMap, allSourcePredefinedServiceMap, ruleName, needAddServiceObjectList, needAddServiceGroupObjectList,predefinedServiceMapping, preServices);
                    }
                    if(clause.containsKey("postServices")){
                        JSONArray postServices = clause.getJSONArray("postServices");
                        takeServiceObject(taskRecord, warningBuilder, targetPredefinedServiceMap, targetServiceObjectNames,currentServiceObjectNames,targetServiceGroupObjectNames, currentServiceGroupObjectNames, sourcePredefinedServiceMap, sourceServiceObjectMap, sourceServiceGroupObjectMap, allSourcePredefinedServiceMap, ruleName, needAddServiceObjectList, needAddServiceGroupObjectList,predefinedServiceMapping, postServices);
                    }
                    if(clause.containsKey("protocols")){
                        JSONArray protocols = clause.getJSONArray("protocols");
                        takeServiceObject(taskRecord, warningBuilder, targetPredefinedServiceMap, targetServiceObjectNames,currentServiceObjectNames,targetServiceGroupObjectNames, currentServiceGroupObjectNames, sourcePredefinedServiceMap, sourceServiceObjectMap, sourceServiceGroupObjectMap, allSourcePredefinedServiceMap, ruleName, needAddServiceObjectList, needAddServiceGroupObjectList,predefinedServiceMapping, protocols);
                    }
                    if(clause.containsKey("preSrcIPItems")){
                        boolean setPool = false;
                        DeviceNetworkTypeEnum deviceNetworkTypeEnum = null;
                        if(taskRecord.getTargetDeviceVendorId().equals("westone")){
                            setPool = true;
                        }
                        JSONArray preSrcIPItems = clause.getJSONArray("preSrcIPItems");
                        takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames,sourceAddressPoolMap, sourceAddressObjectMap, sourceAddressGroupObjectMap, currentAddressPoolNames,currentAddressObjectNames,
                                currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, preSrcIPItems,setPool,deviceNetworkTypeEnum);
                    }
                    if(clause.containsKey("postSrcIPItems")){
                        boolean setPool = false;
                        DeviceNetworkTypeEnum deviceNetworkTypeEnum = null;
                        if(taskRecord.getTargetDeviceVendorId().equals("westone")){
                            if(natField.contains("SRC")){
                                setPool = true;
                                deviceNetworkTypeEnum = DeviceNetworkTypeEnum.SRC_POOL;
                            }
                        }
                        JSONArray postSrcIPItems = clause.getJSONArray("postSrcIPItems");
                        takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames,sourceAddressPoolMap, sourceAddressObjectMap, sourceAddressGroupObjectMap, currentAddressPoolNames,currentAddressObjectNames,
                                currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, postSrcIPItems,setPool,deviceNetworkTypeEnum);
                    }
                    if(clause.containsKey("preDstIPItems")){
                        boolean setPool = false;
                        DeviceNetworkTypeEnum deviceNetworkTypeEnum = null;
                        if(taskRecord.getTargetDeviceVendorId().equals("westone")){
                            if(natField.contains("DST")){
                                setPool = true;
                                deviceNetworkTypeEnum = DeviceNetworkTypeEnum.DST_POOL;
                            }
                        }
                        JSONArray preDstIPItems = clause.getJSONArray("preDstIPItems");
                        takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames,sourceAddressPoolMap, sourceAddressObjectMap, sourceAddressGroupObjectMap, currentAddressPoolNames,currentAddressObjectNames,
                                currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, preDstIPItems,setPool,deviceNetworkTypeEnum);
                    }
                    if(clause.containsKey("postDstIPItems")){
                        boolean setPool = false;
                        DeviceNetworkTypeEnum deviceNetworkTypeEnum = null;
                        if(taskRecord.getTargetDeviceVendorId().equals("westone")){
                            if(natField.contains("DST")){
                                setPool = true;
                            }
                        }
                        JSONArray postDstIPItems = clause.getJSONArray("postDstIPItems");
                        takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames,sourceAddressPoolMap, sourceAddressObjectMap, sourceAddressGroupObjectMap, currentAddressPoolNames,currentAddressObjectNames,
                                currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, postDstIPItems,setPool,deviceNetworkTypeEnum);
                    }
                } else {
                    // 安全策略
                    clause = deviceFilterRuleListRO.getMatchClause();
                    String filterTimeGroupName = deviceFilterRuleListRO.getFilterTimeGroupName();
                    if (StringUtils.isNotBlank(filterTimeGroupName) && filterTimeGroupName.length() > 2) {
                        filterTimeGroupName = filterTimeGroupName.substring(0, filterTimeGroupName.length() - 2);
                        deviceFilterRuleListRO.setFilterTimeGroupName(filterTimeGroupName);
                        if (!targetTimeObjectNames.contains(filterTimeGroupName) && !currentTimeObjectNames.contains(filterTimeGroupName)) {
                            if (sourceTimeObjectMap.containsKey(filterTimeGroupName)) {
                                needAddTimeObjectList.add(sourceTimeObjectMap.get(filterTimeGroupName));
                                currentTimeObjectNames.add(filterTimeGroupName);
                            }
                        }
                    }

                    if (ObjectUtils.isNotEmpty(clause)) {
                        if (clause.containsKey("services")) {
                            JSONArray services = clause.getJSONArray("services");
                            takeServiceObject(taskRecord, warningBuilder, targetPredefinedServiceMap, targetServiceObjectNames,currentServiceObjectNames,targetServiceGroupObjectNames, currentServiceGroupObjectNames,sourcePredefinedServiceMap, sourceServiceObjectMap, sourceServiceGroupObjectMap, allSourcePredefinedServiceMap, ruleName, needAddServiceObjectList, needAddServiceGroupObjectList,predefinedServiceMapping, services);
                        }

                        if (clause.containsKey("srcIp")) {
                            JSONArray srcIpArray = clause.getJSONArray("srcIp");
                            takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames,sourceAddressPoolMap, sourceAddressObjectMap, sourceAddressGroupObjectMap, currentAddressPoolNames,currentAddressObjectNames,
                                    currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, srcIpArray,false,null);
                        }

                        if (clause.containsKey("dstIp")) {
                            JSONArray dstIpArray = clause.getJSONArray("dstIp");
                            takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames,sourceAddressPoolMap, sourceAddressObjectMap, sourceAddressGroupObjectMap, currentAddressPoolNames,currentAddressObjectNames,
                                    currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, dstIpArray,false,null);
                        }
                    }
                }

                //对象命令行按需生成
                Map<String,Object> param = new HashMap<>();
                if(CollectionUtils.isNotEmpty(needAddTimeObjectList)) {
                    try {
                        commandLineBuilder.append(translationCommandlineBean.generateTimeCommandLine(needAddTimeObjectList, param));
                    } catch (Exception e) {
                        logger.error("生成时间对象命令行异常：", e);
                        warningBuilder.append(String.format("%s策略 【%s】迁移失败,时间对象命令行创建异常, %s \n",msgPrefix,ruleName, e.getMessage()));
                        continue out;
                    }
                }

                if(CollectionUtils.isNotEmpty(needAddAddressGroupObjectList)){
                    for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddAddressGroupObjectList) {
                        if(CollectionUtils.isNotEmpty(netWorkGroupObjectRO.getIncludeItemNames())){
                            List<String> addressNameList = new ArrayList<>();
                            for (String includeItemName : netWorkGroupObjectRO.getIncludeItemNames()) {

                                if (StringUtils.isNotBlank(includeItemName) && !targetAddressObjectNames.contains(includeItemName) && !targetAddressGroupObjectNames.contains(includeItemName)
                                        && !currentAddressObjectNames.contains(includeItemName) && !currentAddressGroupObjectNames.contains(includeItemName)) {
                                    JSONArray ipArray = new JSONArray();
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("nameRef",includeItemName+"()");
                                    ipArray.add(jsonObject);
                                    takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames, sourceAddressPoolMap,sourceAddressObjectMap, sourceAddressGroupObjectMap,currentAddressPoolNames, currentAddressObjectNames,
                                            currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, ipArray,false,null);
                                    String nameRef = ipArray.getJSONObject(0).getString("nameRef");
                                    nameRef = nameRef.substring(0, nameRef.length() - 2);
                                    addressNameList.add(nameRef);
                                }
                            }
                            netWorkGroupObjectRO.setIncludeItemNames(addressNameList);
                        }

                        if(CollectionUtils.isNotEmpty(netWorkGroupObjectRO.getIncludeGroupNames())){
                            List<String> addressGroupNameList = new ArrayList<>();
                            for (String includeGroupName : netWorkGroupObjectRO.getIncludeGroupNames()) {

                                if (StringUtils.isNotBlank(includeGroupName) && !targetAddressObjectNames.contains(includeGroupName) && !targetAddressGroupObjectNames.contains(includeGroupName)
                                        && !currentAddressObjectNames.contains(includeGroupName) && !currentAddressGroupObjectNames.contains(includeGroupName)) {
                                    JSONArray ipArray = new JSONArray();
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("nameRef",includeGroupName+"()");
                                    ipArray.add(jsonObject);
                                    takeAddressObject(targetAddressPoolNames,targetAddressObjectNames, targetAddressGroupObjectNames, sourceAddressPoolMap,sourceAddressObjectMap, sourceAddressGroupObjectMap,currentAddressPoolNames, currentAddressObjectNames,
                                            currentAddressGroupObjectNames, currentRuleAddressNameSet, needAddAddressObjectList, needAddAddressGroupObjectList, ipArray,false,null);
                                    String nameRef = ipArray.getJSONObject(0).getString("nameRef");
                                    nameRef = nameRef.substring(0, nameRef.length() - 2);
                                    addressGroupNameList.add(nameRef);
                                }
                            }
                            netWorkGroupObjectRO.setIncludeGroupNames(addressGroupNameList);
                        }
                    }
                }

                // 地址对象名字替换
                if (CollectionUtils.isNotEmpty(needAddAddressObjectList)) {
                    //TODO 需整理出不支持ip混合使用的厂家
                    if(taskRecord.getDeviceVendorId().equals("huawei") && !taskRecord.getTargetDeviceVendorId().equals("huawei")){
                        // 找出华为ipv4和ipv6混用的地址对象
                        List<NetWorkGroupObjectRO> newNeedAddressObjectList = new ArrayList<>();
                        for (NetWorkGroupObjectRO netWorkGroupObjectRO : needAddAddressObjectList) {
                            List<IncludeItemsRO> ip4List = new ArrayList<>();
                            List<IncludeItemsRO> ip6List = new ArrayList<>();
                            if(netWorkGroupObjectRO.getIncludeItems() == null){
                                continue;
                            }
                            for (IncludeItemsRO includeItem : netWorkGroupObjectRO.getIncludeItems()) {
                                String type = includeItem.getType();
                                if(type.equals(Constants.ANY4) || type.equals(Constants.ANY)) {
                                }else if (Constants.SUBNET.equals(type)) {
                                    String ip4Prefix = includeItem.getIp4Prefix();
                                    String ip4Length = includeItem.getIp4Length();
                                    String ip6Prefix = includeItem.getIp6Prefix();
                                    String ip6Length = includeItem.getIp6Length();
                                    if(StringUtils.isNotBlank(ip4Prefix) && StringUtils.isNotBlank(ip4Length)){
                                        ip4List.add(includeItem);
                                    } else if(StringUtils.isNotBlank(ip6Prefix) && StringUtils.isNotBlank(ip6Length)){
                                        ip6List.add(includeItem);
                                    }

                                }else if ("INTERFACE".equals(type)) {

                                }else if (Constants.HOST_IP.equals(type)) {
                                    List<String> ip4Addresses = includeItem.getIp4Addresses();
                                    List<String> ip6Addresses = includeItem.getIp6Addresses();
                                    if (ip4Addresses != null && ip4Addresses.size() > 0) {
                                        ip4List.add(includeItem);
                                    } else if (ip6Addresses != null && ip6Addresses.size() > 0) {
                                        ip6List.add(includeItem);
                                    }
                                }else if (Constants.RANGE.equals(type)) {
                                    Ip4RangeRO ip4Range = includeItem.getIp4Range();
                                    Ip4RangeRO ip6Range = includeItem.getIp6Range();
                                    if (ip4Range != null) {
                                        ip4List.add(includeItem);
                                    } else if (ip6Range != null) {
                                        ip6List.add(includeItem);
                                    }
                                }else if (Constants.FQDN.equals(type)) {
                                }else if (Constants.IP4WILDCARD.equals(type)) {
                                    ip4List.add(includeItem);
                                }
                            }
                            // 一个地址对象中，同时存在ipv4和ipv6 拆分成两个地址对象
                            if(CollectionUtils.isNotEmpty(ip4List) && CollectionUtils.isNotEmpty(ip6List)){
                                String oldAddressName = netWorkGroupObjectRO.getName();
                                // 地址对象中ipv4和ipv6混用
                                NetWorkGroupObjectRO newNetWorkGroupObjectRO = new NetWorkGroupObjectRO();
                                dozerMapper.map(netWorkGroupObjectRO,newNetWorkGroupObjectRO);
                                netWorkGroupObjectRO.setIncludeItems(ip4List);
                                newNetWorkGroupObjectRO.setIncludeItems(ip6List);
                                if(taskRecord.getTargetDeviceVendorId().equalsIgnoreCase("topsec")){
                                    netWorkGroupObjectRO.setName(oldAddressName+"-4");
                                    newNetWorkGroupObjectRO.setName(oldAddressName+"-6");
                                } else {
                                    netWorkGroupObjectRO.setName(oldAddressName+"_4");
                                    newNetWorkGroupObjectRO.setName(oldAddressName+"_6");
                                }

                                List<String> newAddressName = new ArrayList<>();
                                newAddressName.add(netWorkGroupObjectRO.getName());
                                newAddressName.add(newNetWorkGroupObjectRO.getName());
                                addressNameMap.put(oldAddressName,newAddressName);
                                newNeedAddressObjectList.add(newNetWorkGroupObjectRO);
                            }
                        }
                        if(CollectionUtils.isNotEmpty(newNeedAddressObjectList)){
                            needAddAddressObjectList.addAll(newNeedAddressObjectList);
                        }
                    }

                    try {
                        commandLineBuilder.append(translationCommandlineBean.generateIpAddressObjectCommandLine(needAddAddressObjectList, addressGroupNameObjectMap));
                    } catch (Exception e) {
                        logger.error("生成地址对象命令行异常：", e);
                        warningBuilder.append(String.format("策略 【%s】迁移失败,地址对象命令行创建异常：%s \n",ruleName,  e.getMessage()));
                        continue out;
                    }
                }
                if (CollectionUtils.isNotEmpty(needAddAddressGroupObjectList)) {
                    try {
                        commandLineBuilder.append(translationCommandlineBean.generateIpAddressObjectGroupCommandLine(needAddAddressGroupObjectList, addressGroupNameObjectMap));
                    } catch (Exception e) {
                        logger.error("生成地址组对象命令行异常：", e);
                        warningBuilder.append(String.format("%s策略 【%s】迁移失败,地址组对象命令行创建异常：%s \n",msgPrefix, ruleName, e.getMessage()));
                        continue out;
                    }
                }
                // 将服务组下面的服务对象判断一边，按需生成服务对象
                if (CollectionUtils.isNotEmpty(needAddServiceGroupObjectList)) {
                    for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceGroupObjectList) {
                        if(CollectionUtils.isEmpty(serviceGroupObjectRO.getIncludeFilterServiceNames()) && CollectionUtils.isEmpty(serviceGroupObjectRO.getIncludeFilterServiceGroupNames())){
                            continue ;
                        }

                        if(CollectionUtils.isNotEmpty(serviceGroupObjectRO.getIncludeFilterServiceNames())){
                            List<String> includeServiceName = new ArrayList<>();
                            for (String includeFilterServiceName : serviceGroupObjectRO.getIncludeFilterServiceNames()) {
                                if(!targetPredefinedServiceMap.containsKey(includeFilterServiceName) && !targetServiceObjectNames.contains(includeFilterServiceName) && !currentServiceObjectNames.contains(includeFilterServiceName)
                                        && !targetServiceGroupObjectNames.contains(includeFilterServiceName) && !currentServiceGroupObjectNames.contains(includeFilterServiceName)){
                                    JSONArray services = new JSONArray();
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("nameRef",includeFilterServiceName+"()");
                                    services.add(jsonObject);
                                    takeServiceObject(taskRecord, warningBuilder, targetPredefinedServiceMap, targetServiceObjectNames,currentServiceObjectNames, targetServiceGroupObjectNames, currentServiceGroupObjectNames,sourcePredefinedServiceMap, sourceServiceObjectMap, sourceServiceGroupObjectMap, allSourcePredefinedServiceMap, ruleName, needAddServiceObjectList, needAddServiceGroupObjectList,predefinedServiceMapping, services);
                                    String nameRef = services.getJSONObject(0).getString("nameRef");
                                    nameRef = nameRef.substring(0, nameRef.length() - 2);
                                    includeServiceName.add(nameRef);
                                }
                            }
                            serviceGroupObjectRO.setIncludeFilterServiceNames(includeServiceName);
                        }

                        if(CollectionUtils.isNotEmpty(serviceGroupObjectRO.getIncludeFilterServiceGroupNames())){
                            List<String> includeServiceGroupName = new ArrayList<>();
                            for (String includeFilterServiceGroupNames : serviceGroupObjectRO.getIncludeFilterServiceGroupNames()) {
                                if(!targetPredefinedServiceMap.containsKey(includeFilterServiceGroupNames) && !targetServiceObjectNames.contains(includeFilterServiceGroupNames) && !currentServiceObjectNames.contains(includeFilterServiceGroupNames)
                                        && !targetServiceGroupObjectNames.contains(includeFilterServiceGroupNames) && !currentServiceGroupObjectNames.contains(includeFilterServiceGroupNames)){
                                    JSONArray services = new JSONArray();
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("nameRef",includeFilterServiceGroupNames+"()");
                                    services.add(jsonObject);
                                    takeServiceObject(taskRecord, warningBuilder, targetPredefinedServiceMap, targetServiceObjectNames,currentServiceObjectNames, targetServiceGroupObjectNames, currentServiceGroupObjectNames,sourcePredefinedServiceMap, sourceServiceObjectMap, sourceServiceGroupObjectMap, allSourcePredefinedServiceMap, ruleName, needAddServiceObjectList, needAddServiceGroupObjectList,predefinedServiceMapping, services);
                                    String nameRef = services.getJSONObject(0).getString("nameRef");
                                    nameRef = nameRef.substring(0, nameRef.length() - 2);
                                    includeServiceGroupName.add(nameRef);
                                }
                            }
                            serviceGroupObjectRO.setIncludeFilterServiceGroupNames(includeServiceGroupName);
                        }
                        }

                }

                if (CollectionUtils.isNotEmpty(needAddServiceObjectList)) {
                    try {
                        commandLineBuilder.append(translationCommandlineBean.generateServiceObjectCommandLine(needAddServiceObjectList, serviceGroupNameObjectMap));
                    } catch (Exception e) {
                        logger.error("生成服务对象命令行异常：", e);
                        warningBuilder.append(String.format("%s策略 【%s】迁移失败,服务对象命令行创建异常：%s \n",msgPrefix,ruleName,  e.getMessage()));
                        continue out;
                    }
                }
                if (CollectionUtils.isNotEmpty(needAddServiceGroupObjectList)) {
                    try {
                        //处理服务组应用对象名字变化的情况
                        for (ServiceGroupObjectRO serviceGroupObjectRO : needAddServiceGroupObjectList) {

                            List<String> includeFilterServiceNames = serviceGroupObjectRO.getIncludeFilterServiceNames();
                            if(CollectionUtils.isNotEmpty(includeFilterServiceNames)){
                                serviceGroupObjectRO.setIncludeFilterServiceNames(includeFilterServiceNames.stream().map(t -> {
                                    if (predefinedServiceMapping.containsKey(t)) {
                                        return predefinedServiceMapping.get(t);
                                    } else {
                                        return t;
                                    }
                                }).collect(Collectors.toList()));
                            }
                            List<String> includeFilterServiceGroupNames = serviceGroupObjectRO.getIncludeFilterServiceGroupNames();
                            if(CollectionUtils.isNotEmpty(includeFilterServiceGroupNames)){
                                serviceGroupObjectRO.setIncludeFilterServiceGroupNames(includeFilterServiceGroupNames.stream().map(t -> {
                                    if(predefinedServiceMapping.containsKey(t)){
                                        return predefinedServiceMapping.get(t);
                                    }else{
                                        return t;
                                    }
                                }).collect(Collectors.toList()));
                            }
                        }

                        commandLineBuilder.append(translationCommandlineBean.generateServiceObjectGroupCommandLine(needAddServiceGroupObjectList, serviceGroupNameObjectMap));
                    } catch (Exception e) {
                        logger.error("生成服务组对象命令行异常：", e);
                        warningBuilder.append(String.format("%s策略 【%s】迁移失败,服务组对象命令行创建异常：%s \n",msgPrefix,ruleName,  e.getMessage()));
                        continue out;
                    }
                }
                List<DeviceFilterRuleListRO> list = new ArrayList<>();
                list.add(deviceFilterRuleListRO);
                if(MapUtils.isNotEmpty(addressNameMap) && clause != null) {
                    JSONArray newSrcJsonArray = new JSONArray();
                    JSONArray newDstJsonArray = new JSONArray();
                    if (clause.containsKey("srcIp")) {
                        JSONArray srcIpArray = clause.getJSONArray("srcIp");
                        for (int i = 0; i < srcIpArray.size(); i++) {
                                JSONObject srcIp = srcIpArray.getJSONObject(i);
                                if (srcIp.containsKey("nameRef")) {
                                String nameRef = srcIp.getString("nameRef");
                                nameRef = nameRef.substring(0, nameRef.length() - 2);
                                if (addressNameMap.containsKey(nameRef)) {
                                    List<String> newAddressName = addressNameMap.get(nameRef);
                                    srcIp.put("nameRef", newAddressName.get(0)+"()");
                                    JSONObject newSrcObj = new JSONObject();
                                    dozerMapper.map(srcIp,newSrcObj);
                                    newSrcObj.put("nameRef", newAddressName.get(1)+"()");
                                    newSrcJsonArray.add(newSrcObj);
                                }
                            }
                        }
                    }

                    if (clause.containsKey("dstIp")) {
                        JSONArray dstIpArray = clause.getJSONArray("dstIp");
                        for (int i = 0; i < dstIpArray.size(); i++) {
                            JSONObject dstIp = dstIpArray.getJSONObject(i);
                            if (dstIp.containsKey("nameRef")) {
                                String nameRef = dstIp.getString("nameRef");
                                nameRef = nameRef.substring(0, nameRef.length() - 2);
                                if (addressNameMap.containsKey(nameRef)) {
                                    List<String> newAddressName = addressNameMap.get(nameRef);
                                    dstIp.put("nameRef", newAddressName.get(0)+"()");
                                    JSONObject newDstObj = new JSONObject();
                                    dozerMapper.map(dstIp,newDstObj);
                                    newDstObj.put("nameRef", newAddressName.get(1)+"()");
                                    newDstJsonArray.add(newDstObj);
                                }
                            }
                        }
                    }

                    if(CollectionUtils.isNotEmpty(newSrcJsonArray) && CollectionUtils.isEmpty(newDstJsonArray)){
                        String msg = String.format("%s策略 【%s】 的源地址对象混用IPv4和IPv6,目的地址未混用 ，导致无法拆分策略，迁移失败\n",msgPrefix,ruleName);
                        logger.error(msg);
                        warningBuilder.append(msg);
                        continue out;
                    } else if(CollectionUtils.isEmpty(newSrcJsonArray) && CollectionUtils.isNotEmpty(newDstJsonArray)){
                        String msg = String.format("%s策略 【%s】 的目的地址对象混用IPv4和IPv6,源地址未混用，导致无法拆分策略，迁移失败\n",msgPrefix,ruleName);
                        logger.error(msg);
                        warningBuilder.append(msg);
                        continue out;
                    } else if(CollectionUtils.isNotEmpty(newSrcJsonArray) && CollectionUtils.isNotEmpty(newDstJsonArray)){
                        DeviceFilterRuleListRO newDeviceFilterRuleListRO = new DeviceFilterRuleListRO();
                        dozerMapper.map(deviceFilterRuleListRO,newDeviceFilterRuleListRO);
                        newDeviceFilterRuleListRO.setIpType("ip6");
                        JSONObject newMatchClause = newDeviceFilterRuleListRO.getMatchClause();
                        newMatchClause.put("srcIp",newSrcJsonArray);
                        newMatchClause.put("dstIp",newDstJsonArray);
                        if(StringUtils.isNotBlank(newDeviceFilterRuleListRO.getName())){
                            if(taskRecord.getTargetDeviceVendorId().equalsIgnoreCase("topsec")){
                                newDeviceFilterRuleListRO.setName(newDeviceFilterRuleListRO.getName() + "-6");
                                deviceFilterRuleListRO.setName(deviceFilterRuleListRO.getName() + "-4");
                            }else {
                                newDeviceFilterRuleListRO.setName(newDeviceFilterRuleListRO.getName() + "_6");
                                deviceFilterRuleListRO.setName(deviceFilterRuleListRO.getName() + "_4");
                            }
                        }
                        list.add(newDeviceFilterRuleListRO);
                        String msg = String.format("%s策略 【%s】 的源地址和目的地址对象混用IPv4和IPv6,拆分成2条策略:%s,%s\n",msgPrefix,ruleName,deviceFilterRuleListRO.getName(),newDeviceFilterRuleListRO.getName());
                        logger.info(msg);
                        warningBuilder.append(msg);
                    }
                }

                //扩展参数对象名字与对象内容的map
                Map<String, Object> parameterMap = new HashMap<>();
                parameterMap.put("serviceNameObjectMap", serviceNameObjectMap);
                parameterMap.put("srcIpNameObjectMap", srcIpNameObjectMap);
                parameterMap.put("dstIpNameObjectMap", dstIpNameObjectMap);
                parameterMap.put("targetPredefinedServiceMap", targetPredefinedServiceMap);
                parameterMap.put("targetServiceObjectNames", targetServiceObjectNames);
                parameterMap.put("currentServiceObjectNames", currentServiceObjectNames);
                parameterMap.put("targetServiceGroupObjectNames", targetServiceGroupObjectNames);
                parameterMap.put("currentServiceGroupObjectNames", currentServiceGroupObjectNames);


                parameterMap.put("targetAddressPoolNames", targetAddressPoolNames);
                parameterMap.put("targetAddressObjectNames", targetAddressObjectNames);
                parameterMap.put("targetAddressGroupObjectNames", targetAddressGroupObjectNames);
                parameterMap.put("currentAddressPoolNames", currentAddressPoolNames);
                parameterMap.put("currentAddressObjectNames", currentAddressObjectNames);
                parameterMap.put("currentAddressGroupObjectNames", currentRuleAddressNameSet);

                String ruleIpType = "ip4";
                for (String currentRuleAddressName : currentRuleAddressNameSet) {
                    if(sourceAddressObjectMap.containsKey(currentRuleAddressName)){
                        NetWorkGroupObjectRO netWorkGroupObjectRO = sourceAddressObjectMap.get(currentRuleAddressName);
                        String addressIpType = getNetWorkObjectIpType(netWorkGroupObjectRO);
                        if(addressIpType.equals("ip6")){
                            ruleIpType = "ip6";
                        }
                    } else if(sourceAddressPoolMap.containsKey(currentRuleAddressName)){
                        NetWorkGroupObjectRO netWorkGroupObjectRO = sourceAddressPoolMap.get(currentRuleAddressName);
                        String addressIpType = getNetWorkObjectIpType(netWorkGroupObjectRO);
                        if(addressIpType.equals("ip6")){
                            ruleIpType = "ip6";
                        }
                    }
                }
                list.get(0).setIpType(ruleIpType);
                Map<String, String> generateResult = new HashMap<>();
                generateResult.put("commandLine",StringUtils.EMPTY);
                generateResult.put("warning",StringUtils.EMPTY);
                if(PolicyTypeEnum.SYSTEM__NAT_LIST.name().equals(deviceFilterlistRO.getRuleListType())){
                    try {
                        if("STATIC".equals(clause.getString("type"))){
                            if(lastStaticNatFilter != null){
                                if(lastStaticNatFilter.getLineNumbers().equals(list.get(0).getLineNumbers())){
                                    list.add(lastStaticNatFilter);
                                    generateResult = translationCommandlineBean.generateNatPolicyCommandLine(taskRecord.getUuid(), deviceFilterlistRO, list, parameterMap);
                                    lastStaticNatFilter = null;
                                }
                            } else {
                                lastStaticNatFilter = deviceFilterRuleListRO;
                            }
                        } else {
                            generateResult = translationCommandlineBean.generateNatPolicyCommandLine(taskRecord.getUuid(), deviceFilterlistRO, list, parameterMap);
                        }
                    } catch (Exception e) {
                        logger.error("generateNATPolicyCommandLine error:",e);
                        warningBuilder.append(String.format("NAT策略 【%s】 生成命令行异常，报错信息为：%s \n",ruleName,e.getMessage()));
                        continue out;
                    } finally {
                        Boolean sameNat = lastStaticNatFilter != null
                                && lastStaticNatFilter.getLineNumbers().equals(deviceFilterRuleListRO.getLineNumbers())
                                && !lastStaticNatFilter.getUuid().equals(deviceFilterRuleListRO.getUuid());
                        if (sameNat) {
                            lastStaticNatFilter = null;
                        }
                    }
                } else {
                    try {
                        generateResult = translationCommandlineBean.generateSecurityPolicyCommandLine(taskRecord.getUuid(), deviceFilterlistRO, list, parameterMap);
                    } catch (Exception e) {
                        logger.error("generateSecurityPolicyCommandLine error:",e);
                        warningBuilder.append(String.format("安全策略 【%s】 生成命令行异常，报错信息为：%s \n",ruleName,e.getMessage()));
                        continue out;
                    }
                }
                //策略命令行和告警信息
                commandLineBuilder.append(generateResult.get("commandLine"));
                warningBuilder.append(generateResult.get("warning"));
                allCommandLineBuilder.append(commandLineBuilder);

                targetServiceObjectNames.addAll(currentServiceObjectNames);
                targetServiceGroupObjectNames.addAll(currentServiceGroupObjectNames);
                targetAddressPoolNames.addAll(currentAddressPoolNames);
                targetAddressObjectNames.addAll(currentAddressObjectNames);
                targetAddressGroupObjectNames.addAll(currentAddressGroupObjectNames);
                targetTimeObjectNames.addAll(currentTimeObjectNames);
            }
            if (CollectionUtils.isEmpty(needAddFilter)) {
                continue;
            }

            logger.info("%s策略集{}迁移完成,条数:{}",msgPrefix,deviceFilterlistRO.getName(),needAddFilter.size());
        }

        if(routingTable != null && CollectionUtils.isNotEmpty(routingTable.getData())){
            List<RoutingtableRO> routingTableData = routingTable.getData();
            for (RoutingtableRO routingTableRO : routingTableData) {
                String routingTableROName = routingTableRO.getName();
                if(taskRecord.getDeviceVendorId().contains("juniper") && !routingTableROName.contains("GLOBAL")){
                    String msg = String.format("路由集【%s】 迁移失败:%s \n",routingTableROName,"暂不支持带VPN实例的静态路由迁移");
                    warningBuilder.append(msg);
                    continue;
                }
                String uuid = routingTableRO.getUuid();
                RoutingTableSearchDTO searchDTO = new RoutingTableSearchDTO();
                searchDTO.setDeviceUuid(taskRecord.getDeviceUuid());
                searchDTO.setRoutingTableUuid(uuid);
                ResultRO<List<RoutingEntriesRO>> listResultRO = whaleDevicePolicyClient.getRoutingEnteries(searchDTO);
                routeOut:for (RoutingEntriesRO routingEntriesRO : listResultRO.getData()) {
                    try {
                        NextHopRO nextHop = routingEntriesRO.getNextHop();
                        if(nextHop != null && StringUtils.isNotBlank(nextHop.getInterfaceName())){
                            String interfaceName = nextHop.getInterfaceName().toLowerCase(Locale.ROOT);
                            if (interfaceMap.containsKey(interfaceName) && StringUtils.isNotBlank(interfaceMap.get(interfaceName))) {
                                nextHop.setInterfaceName(interfaceMap.get(interfaceName));
                            } else if (targetInterfaceName.contains(interfaceName)) {

                            } else {
                                warningBuilder.append(String.format("路由 %s 的下一跳接口 %s 未映射到新设备，该条路由迁移失败 \n", routingEntriesRO.getLineNumbers(), interfaceName));
                                continue routeOut;
                            }
                        }
                        allCommandLineBuilder.append(translationCommandlineBean.generateRoutingCommandLine(routingEntriesRO));
                    } catch (Exception e) {
                        logger.error("生成路由命令行异常：", e);
                        String msg = String.format("路由 %s 迁移失败:%s \n",routingEntriesRO.getLineNumbers(),e.getMessage());
                        warningBuilder.append(msg);
                    }
                }
            }
        }

        allCommandLineBuilder.append(translationCommandlineBean.generatePostCommandLine());
        Map<String, String> generateSecurityResult = new HashMap<>();
        generateSecurityResult.put("commandLine", allCommandLineBuilder.toString());
        generateSecurityResult.put("warning", warningBuilder.toString());
        logger.info("策略迁移任务{}命令行生成结束",taskRecord.getUuid());
        return generateSecurityResult;
    }

    /**
     *
     * @param targetAddressPoolNames
     * @param targetAddressObjectNames
     * @param targetAddressGroupObjectNames
     * @param sourceAddressPoolMap
     * @param sourceAddressObjectMap
     * @param sourceAddressGroupObjectMap
     * @param currentAddressPoolNames
     * @param currentAddressObjectNames
     * @param currentAddressGroupObjectNames
     * @param currentRuleAddressNameSet
     * @param needAddAddressObjectList
     * @param needAddAddressGroupObjectList
     * @param srcIpArray
     * @param setPool   需要指定转为对象或池
     * @param deviceNetworkTypeEnum 为空则转为对象，不为空则转为池
     */
    private void takeAddressObject(Set<String> targetAddressPoolNames,Set<String> targetAddressObjectNames, Set<String> targetAddressGroupObjectNames,
                                   Map<String, NetWorkGroupObjectRO> sourceAddressPoolMap,Map<String, NetWorkGroupObjectRO> sourceAddressObjectMap, Map<String, NetWorkGroupObjectRO> sourceAddressGroupObjectMap,
                                   Set<String> currentAddressPoolNames,Set<String> currentAddressObjectNames, Set<String> currentAddressGroupObjectNames, Set<String> currentRuleAddressNameSet, List<NetWorkGroupObjectRO> needAddAddressObjectList, List<NetWorkGroupObjectRO> needAddAddressGroupObjectList, JSONArray srcIpArray, boolean setPool,DeviceNetworkTypeEnum deviceNetworkTypeEnum) {
        for (int i = 0; i < srcIpArray.size(); i++) {
            JSONObject srcIp = srcIpArray.getJSONObject(i);
            if (srcIp.containsKey("nameRef")) {
                String nameRef = srcIp.getString("nameRef");
                nameRef = nameRef.substring(0, nameRef.length() - 2);
                if (StringUtils.isNotBlank(nameRef) && !targetAddressPoolNames.contains(nameRef) && !targetAddressObjectNames.contains(nameRef) && !targetAddressGroupObjectNames.contains(nameRef)
                        && !currentAddressObjectNames.contains(nameRef) && !currentAddressGroupObjectNames.contains(nameRef) && !currentAddressPoolNames.contains(nameRef)) {


                    NetWorkGroupObjectRO netWorkGroupObjectRO = null;
                    String type = null;
                    if (sourceAddressObjectMap.containsKey(nameRef)) {
                        type = "object";
                        netWorkGroupObjectRO = sourceAddressObjectMap.get(nameRef);
                        needAddAddressObjectList.add(netWorkGroupObjectRO);
                    } else if (sourceAddressGroupObjectMap.containsKey(nameRef)) {
                        type = "group";
                        netWorkGroupObjectRO = sourceAddressGroupObjectMap.get(nameRef);
                        needAddAddressGroupObjectList.add(netWorkGroupObjectRO);
                    } else if(sourceAddressPoolMap.containsKey(nameRef)){
                        type = "pool";
                        netWorkGroupObjectRO = sourceAddressPoolMap.get(nameRef);
                        needAddAddressObjectList.add(netWorkGroupObjectRO);
                    }
                    if(setPool){
                        if(deviceNetworkTypeEnum != null){
                            netWorkGroupObjectRO.setDeviceNetworkType(deviceNetworkTypeEnum);
                            currentAddressPoolNames.add(nameRef);
                            currentRuleAddressNameSet.add(nameRef);
                        } else {
                            netWorkGroupObjectRO.setDeviceNetworkType(null);
                            if("group".equals(type)){
                                currentAddressGroupObjectNames.add(nameRef);
                            } else {
                                currentAddressObjectNames.add(nameRef);
                                currentRuleAddressNameSet.add(nameRef);
                            }
                        }
                    } else {
                        if("pool".equals(type)){
                            currentAddressPoolNames.add(nameRef);
                            currentRuleAddressNameSet.add(nameRef);
                        } else if("object".equals(type)){
                            currentAddressObjectNames.add(nameRef);
                            currentRuleAddressNameSet.add(nameRef);
                        } else if("group".equals(type)){
                            currentAddressGroupObjectNames.add(nameRef);
                        }
                    }
                }
            }

        }
    }

    private void takeServiceObject(TranslationTaskRecordEntity taskRecord,  StringBuffer warningBuilder,
                           Map<String, ServiceGroupObjectRO> targetPredefinedServiceMap, Set<String> targetServiceObjectNames, Set<String> targetServiceGroupObjectNames,
                           Set<String> currentServiceObjectNames, Set<String> currentServiceGroupObjectNames,
                           Map<String, ServiceGroupObjectRO> sourcePredefinedServiceMap, Map<String, ServiceGroupObjectRO> sourceServiceObjectMap, Map<String, ServiceGroupObjectRO> sourceServiceGroupObjectMap, Map<String, PredefinedService> allSourcePredefinedServiceMap,
                           String ruleName, List<ServiceGroupObjectRO> needAddServiceObjectList, List<ServiceGroupObjectRO> needAddServiceGroupObjectList,Map<String,String> predefinedServiceMapping, JSONArray services) {
        for (int i = 0; i < services.size(); i++) {
            JSONObject service = services.getJSONObject(i);
            if (service.containsKey("nameRef") || service.containsKey("serviceValue") || service.containsKey("protocolName")) {
                String nameRef;
                boolean isProtocol = false;
                if(service.containsKey("nameRef")){
                    nameRef = service.getString("nameRef");
                    nameRef = nameRef.substring(0, nameRef.length() - 2);
                } else if(service.containsKey("serviceValue")){
                    JSONObject serviceValue = service.getJSONObject("serviceValue");
                    nameRef = serviceValue.getString("protocolName");
                    isProtocol = true;
                    if(StringUtils.isBlank(nameRef) || serviceValue.containsKey("portValues")){
                        continue ;
                    }
                } else{
                    isProtocol = true;
                    nameRef = service.getString("protocolName");
                }
                if(StringUtils.isBlank(nameRef)){
                    continue;
                }
                if (StringUtils.isNotBlank(nameRef) && !targetServiceObjectNames.contains(nameRef) && !targetServiceGroupObjectNames.contains(nameRef) && !currentServiceObjectNames.contains(nameRef) && !currentServiceGroupObjectNames.contains(nameRef)) {
                    //判断目的设备预定义对象
                    boolean isTartetPredefinedService = false;
                    if(isProtocol){
                        // 使用内容为协议的服务
                        ServiceDTO serviceDTO = new ServiceDTO();
                        String protocol = null;
                        if(StringUtils.isNotBlank(nameRef)){
                            protocol = protocolMapConfig.getStrMap().get(nameRef);
                        }
                        List<ServiceDTO> serviceDTOList = new ArrayList<>();
                        serviceDTO.setProtocol(protocol);
                        serviceDTOList.add(serviceDTO);
                        if(CollectionUtils.isNotEmpty(serviceDTOList)) {
                            DeviceForExistObjDTO deviceForExistObjDTO = new DeviceForExistObjDTO();
                            deviceForExistObjDTO.setModelNumber(DeviceModelNumberEnum.fromString(taskRecord.getTargetDeviceModelNumber()));
                            deviceForExistObjDTO.setDeviceUuid(taskRecord.getTargetDeviceUuid());
                            String targetPredefinedServiceObjectName = whaleManager.getCurrentServiceObjectName(serviceDTOList, deviceForExistObjDTO, null);
                            if (StringUtils.isNotBlank(targetPredefinedServiceObjectName)) {
                                service.put("nameRef", targetPredefinedServiceObjectName + "()");
                                if (!targetPredefinedServiceObjectName.equalsIgnoreCase(nameRef)) {
                                    predefinedServiceMapping.put(nameRef, targetPredefinedServiceObjectName);
                                    currentServiceObjectNames.add(targetPredefinedServiceObjectName);
                                    warningBuilder.append(String.format("策略 【%s】 使用预定义服务对象 【%s】 转为目标设备的预定义服务对象【%s】 \n", ruleName, nameRef, targetPredefinedServiceObjectName));
                                }
                            }
                        }
                    } else if(sourcePredefinedServiceMap.containsKey(nameRef)){
                        // 该对象为预定义对象
                        if(StringUtils.isNotBlank(taskRecord.getTargetDeviceUuid())){
                            // 目的设备不为空，通过协议和端口查询设备解析出的预定义对象
                            ServiceGroupObjectRO serviceGroupObjectRO = sourcePredefinedServiceMap.get(nameRef);
                            List<ServiceDTO> serviceDTOList = new ArrayList<>();
                            StringBuffer portStringBuffer = new StringBuffer();
                            for (IncludeFilterServicesRO includeFilterService : serviceGroupObjectRO.getIncludeFilterServices()) {
                                ServiceDTO serviceDTO = new ServiceDTO();
                                String protocolName = includeFilterService.getProtocolName();
                                String protocolNum = includeFilterService.getProtocolNum();
                                String protocolStr;
                                if(StringUtils.isNotBlank(protocolName)){
                                    protocolStr = protocolName;
                                } else {
                                    protocolStr = protocolNum;
                                }
                                if(StringUtils.isBlank(protocolStr)){
                                    continue;
                                }

                                if(StringUtils.isNumeric(protocolStr)){
                                    serviceDTO.setProtocol(protocolStr);
                                } else {
                                    serviceDTO.setProtocol(protocolMapConfig.getStrMap().get(protocolStr));

                                }
                                List<String> portValues = includeFilterService.getPortValues();
                                String portOp = includeFilterService.getPortOp();

                                if (portValues != null && portValues.size() != 0) {
                                    if (portValues.size() == 2 && portOp.equalsIgnoreCase("RANGE")) {
                                        portStringBuffer.append(String.format("%s-%s",portValues.get(0),portValues.get(1))).append(",");
                                    } else {
                                        int m;
                                        if (portOp.equalsIgnoreCase("MULTI_RANGE")) {
                                            for (m = 0; m < portValues.size(); m += 2) {
                                                String startPort = (String) portValues.get(m);
                                                String endPort = (String) portValues.get(m + 1);
                                                if (startPort.equals(endPort)) {
                                                    portStringBuffer.append(endPort).append(",");
                                                } else {
                                                    portStringBuffer.append(String.format("%s-%s",startPort,endPort)).append(",");
                                                }
                                            }
                                        } else {
                                            for (m = 0; m < portValues.size(); ++m) {
                                                String portStr = portValues.get(m);
                                                portStringBuffer.append(portStr).append(",");
                                            }
                                        }
                                    }
                                    String portString = portStringBuffer.toString();
                                    if(portString.endsWith(",")){
                                        portString = portString.substring(0,portString.length()-1);
                                    }
                                    serviceDTO.setDstPorts(portString);
                                }

                                if (serviceDTO == null || StringUtils.isBlank(serviceDTO.getProtocol())) {
                                    continue;
                                }
                                serviceDTOList.add(serviceDTO);
                            }
                            if(CollectionUtils.isNotEmpty(serviceDTOList)){
                                DeviceForExistObjDTO deviceForExistObjDTO =  new DeviceForExistObjDTO();
                                deviceForExistObjDTO.setModelNumber(DeviceModelNumberEnum.fromString(taskRecord.getTargetDeviceModelNumber()));
                                deviceForExistObjDTO.setDeviceUuid(taskRecord.getTargetDeviceUuid());
                                String targetPredefinedServiceObjectName = whaleManager.getCurrentServiceObjectName(serviceDTOList, deviceForExistObjDTO, null);
                                if(StringUtils.isNotBlank(targetPredefinedServiceObjectName)){
                                    service.put("nameRef",targetPredefinedServiceObjectName+"()");
                                    isTartetPredefinedService = true;
                                    if(!targetPredefinedServiceObjectName.equalsIgnoreCase(nameRef)){
                                        predefinedServiceMapping.put(nameRef,targetPredefinedServiceObjectName);
                                        currentServiceObjectNames.add(targetPredefinedServiceObjectName);
                                        warningBuilder.append(String.format("策略 【%s】 使用预定义服务对象 【%s】 转为目标设备的预定义服务对象【%s】 \n", ruleName,nameRef, targetPredefinedServiceObjectName));
                                    }
                                }
                            } else if(targetPredefinedServiceMap.containsKey(nameRef)){
                                ServiceGroupObjectRO targetServiceGroupObjectRO = targetPredefinedServiceMap.get(nameRef);
                                List<IncludeFilterServicesRO> includeFilterServices = serviceGroupObjectRO.getIncludeFilterServices();
                                List<IncludeFilterServicesRO> targetIncludeFilterServices = targetServiceGroupObjectRO.getIncludeFilterServices();
                                boolean sameServiceObj = true;
                                if(CollectionUtils.isNotEmpty(targetIncludeFilterServices)){
                                    for (int h = 0; h < includeFilterServices.size(); h++) {
                                        IncludeFilterServicesRO includeFilterServicesRO = includeFilterServices.get(h);
                                        IncludeFilterServicesRO targetIncludeFilterServicesRO = targetIncludeFilterServices.get(h);
                                        if(StringUtils.isNotBlank(includeFilterServicesRO.getType()) && !includeFilterServicesRO.getType().equals(targetIncludeFilterServicesRO.getType())){
                                            sameServiceObj = false;
                                            break ;
                                        }
                                        if(StringUtils.isNotBlank(includeFilterServicesRO.getProtocolName()) && !includeFilterServicesRO.getProtocolName().equals(targetIncludeFilterServicesRO.getProtocolName())){
                                            sameServiceObj = false;
                                            break ;
                                        }
                                        if(StringUtils.isNotBlank(includeFilterServicesRO.getPortOp()) && !includeFilterServicesRO.getPortOp().equals(targetIncludeFilterServicesRO.getPortOp())){
                                            sameServiceObj = false;
                                            break ;
                                        }

                                        if(includeFilterServicesRO.getPortValues() != null && targetIncludeFilterServicesRO.getPortValues() != null){
                                            Set<String> portValues = includeFilterServicesRO.getPortValues().stream().collect(Collectors.toSet());
                                            if(CollectionUtils.size(targetIncludeFilterServicesRO.getPortValues()) == CollectionUtils.size(targetIncludeFilterServicesRO.getPortValues())){
                                                for (String portValue : targetIncludeFilterServicesRO.getPortValues()) {
                                                    if(!portValues.contains(portValue)){
                                                        sameServiceObj = false;
                                                        break ;
                                                    }
                                                }
                                            } else {
                                                sameServiceObj = false;
                                                break ;
                                            }
                                        } else if((includeFilterServicesRO.getPortValues() == null && targetIncludeFilterServicesRO.getPortValues() != null)
                                            || (includeFilterServicesRO.getPortValues() != null && targetIncludeFilterServicesRO.getPortValues() == null)){
                                            sameServiceObj = false;
                                            break ;
                                        }
                                    }
                                }
                                if(sameServiceObj){
                                    // 预定义服务对象名称相同
                                    isTartetPredefinedService = true;
                                }
                            }
                        } else if(allSourcePredefinedServiceMap.containsKey(nameRef)){
                            // 目的设备为空查询，查询厂家的预定义设备
                            PredefinedService predefinedService = allSourcePredefinedServiceMap.get(nameRef);
                            String venderObjName = predefinedService.getVenderObjName();
                            JSONArray venderObjNameList = TotemsJsonMapper.fromJson(venderObjName, JSONArray.class);
                            for (int j = 0; j < venderObjNameList.size(); j++) {
                                JSONObject jsonObject = venderObjNameList.getJSONObject(j);
                                String vender = jsonObject.getString("vender");
                                String objName = jsonObject.getString("objName");
                                if(taskRecord.getTargetDeviceVendorId().equals(vender)){
                                    service.put("nameRef",objName+"()");
                                    isTartetPredefinedService = true;
                                    if(!objName.equalsIgnoreCase(nameRef)){
                                        predefinedServiceMapping.put(nameRef,objName);
                                        currentServiceObjectNames.add(objName);
                                        warningBuilder.append(String.format("策略 【%s】 使用预定义服务对象 【%s】 转为目标设备的预定义服务对象【%s】 \n", ruleName,nameRef, objName));
                                    }
                                    break ;
                                }
                            }
                        }

                        // 该服务对象既不存在于目的设备的自定义服务对象，也不存在于预定义服务对象，则新建服务对象
                        if(isTartetPredefinedService){
                           continue ;
                        } else if(sourcePredefinedServiceMap.containsKey(nameRef)){
                            needAddServiceObjectList.add(sourcePredefinedServiceMap.get(nameRef));
                            currentServiceObjectNames.add(nameRef);
                        }
                    } else if (sourceServiceObjectMap.containsKey(nameRef)) {
                        // 为对象
                        needAddServiceObjectList.add(sourceServiceObjectMap.get(nameRef));
                        currentServiceObjectNames.add(nameRef);

                    } else if (sourceServiceGroupObjectMap.containsKey(nameRef)) {
                        // 为对象组
                        needAddServiceGroupObjectList.add(sourceServiceGroupObjectMap.get(nameRef));
                        currentServiceGroupObjectNames.add(nameRef);
                    }
                }else {
                    // 目的设备包含此服务
                    service.put("nameRef",nameRef+"()");
                }
            }
        }
    }

    private String getNetWorkObjectIpType(NetWorkGroupObjectRO netWorkGroupObjectRO) {
        List<IncludeItemsRO> includeItems = netWorkGroupObjectRO.getIncludeItems();
        String ipType = "ip4";
        if(CollectionUtils.isEmpty(includeItems)){
            return ipType;
        }
        for (IncludeItemsRO ro : includeItems) {
            String type = ro.getType();
            if (type.equals(Constants.ANY4) || type.equals(Constants.ANY)) {

            } else if (Constants.SUBNET.equals(type)) {

                String ip6Prefix = ro.getIp6Prefix();
                String ip6Length = ro.getIp6Length();
                if (StringUtils.isNotBlank(ip6Prefix) && StringUtils.isNotBlank(ip6Length)) {
                    ipType = "ip6";
                }

            } else if ("INTERFACE".equals(type)) {

            } else if (Constants.HOST_IP.equals(type)) {

                List<String> ip6Addresses = ro.getIp6Addresses();
                if (ip6Addresses != null && ip6Addresses.size() > 0) {
                    ipType = "ip6";
                }
            } else if (Constants.RANGE.equals(type)) {
                Ip4RangeRO ip4Range = ro.getIp4Range();
                Ip4RangeRO ip6Range = ro.getIp6Range();
                if (ip6Range != null) {
                    ipType = "ip6";
                }
            }
        }
        return ipType;
    }

    private static IpAddressRangeDTO getStartEndByJsonObject(Ip4RangeRO rangeRO) {
        IpAddressRangeDTO ipAddressRangeDTO = new IpAddressRangeDTO();
        String start = rangeRO.getStart();
        String end = rangeRO.getEnd();
        if (StringUtils.isNotBlank(start)) {
            ipAddressRangeDTO.setStart(start);
        }
        if (StringUtils.isNotBlank(end)) {
            ipAddressRangeDTO.setEnd(end);
        }
        return ipAddressRangeDTO;
    }
}


