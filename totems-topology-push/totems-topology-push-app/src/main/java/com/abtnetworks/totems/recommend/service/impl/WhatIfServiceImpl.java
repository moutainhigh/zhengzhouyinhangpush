package com.abtnetworks.totems.recommend.service.impl;

import com.abtnetworks.totems.common.enums.IPTypeEnum;
import com.abtnetworks.totems.common.enums.IpTypeEnum;
import com.abtnetworks.totems.common.utils.*;
import com.abtnetworks.totems.common.dto.commandline.ServiceDTO;
import com.abtnetworks.totems.recommend.dto.task.WhatIfNatDTO;
import com.abtnetworks.totems.recommend.dto.task.WhatIfRouteDTO;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.recommend.service.WhatIfService;
import com.abtnetworks.totems.whale.baseapi.ro.SimpleNatRuleRO;
import com.abtnetworks.totems.whale.baseapi.ro.SimpleRouteRuleRO;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfDataRO;
import com.abtnetworks.totems.whale.baseapi.ro.WhatIfRO;
import com.abtnetworks.totems.whale.policy.dto.RuleCheckServiceDTO;
import com.abtnetworks.totems.whale.policybasic.ro.NextHopRO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class WhatIfServiceImpl implements WhatIfService {
    private static Logger logger = LoggerFactory.getLogger(WhatIfServiceImpl.class);

    @Autowired
    WhaleManager whaleManager;
    @Value("${push.whale:false}")
    private Boolean isNginZ;

    @Override
    public WhatIfRO createWhatIfCase(List<WhatIfNatDTO> natList, List<WhatIfRouteDTO> routeList, String name, String description) {
        logger.info("开始模拟变更...");
        logger.info("模拟变更NAT列表:{}",JSONObject.toJSONString(natList));
        logger.info("模拟变更ROUTE列表:{}",JSONObject.toJSONString(routeList));

        if(CollectionUtils.isEmpty(natList) && CollectionUtils.isEmpty(routeList)) {
            return new WhatIfRO();
        }

        WhatIfRO whatIfRO = new WhatIfRO();
        whatIfRO.setName(name);
        whatIfRO.setDescription(description);
        JSONObject deviceWhatifs = new JSONObject();

        int index = 1;
        Map<String, WhatIfDataRO> deviceWhatIfMap = new HashedMap();
        // 处理NAT数据
        for(WhatIfNatDTO natDTO: natList) {
            String deviceUuid = natDTO.getDeviceUuid();
            SimpleNatRuleRO simpleNatRuleRO = new SimpleNatRuleRO();

            simpleNatRuleRO.setName(natDTO.getName());
            simpleNatRuleRO.setDescription(description);
            simpleNatRuleRO.setNatField(natDTO.getNatField());
            if(natDTO.getNatType().equalsIgnoreCase("STATIC")) {
                simpleNatRuleRO.setType("STATIC");
            } else {
                simpleNatRuleRO.setType("DYNAMIC");
            }

            if(!AliStringUtils.isEmpty(natDTO.getSrcZone())) {
                simpleNatRuleRO.setFromZone(natDTO.getSrcZone());
            }

            if(!AliStringUtils.isEmpty(natDTO.getDstZone())) {
                simpleNatRuleRO.setToZone(natDTO.getDstZone());
            }

            if(!AliStringUtils.isEmpty(natDTO.getInDevItf())) {
                simpleNatRuleRO.setFromInterfaceName(natDTO.getInDevItf());
            }

            if(!AliStringUtils.isEmpty(natDTO.getOutDevItf())) {
                simpleNatRuleRO.setToInterfaceName(natDTO.getOutDevItf());
            }

            String ipType = natDTO.getIpType();
            simpleNatRuleRO.setIpType(ipType);
            if(IPTypeEnum.IP6.toString().equalsIgnoreCase(ipType)){
                simpleNatRuleRO.setPreIp6SrcIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPreIp6SrcAddress(),ipType));
                simpleNatRuleRO.setPostIp6SrcIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPostIp6SrcAddress(),ipType));
                simpleNatRuleRO.setPreIp6DstIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPreIp6DstAddress(),ipType));
                simpleNatRuleRO.setPostIp6DstIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPostIp6DstAddress(),ipType));
            }else{
                simpleNatRuleRO.setPreSrcIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPreSrcAddress(),ipType));
                simpleNatRuleRO.setPostSrcIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPostSrcAddress(),ipType));
                simpleNatRuleRO.setPreDstIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPreDstAddress(),ipType));
                simpleNatRuleRO.setPostDstIps(WhaleDoUtils.getSrcDstStringDTOList(natDTO.getPostDstAddress(),ipType));
            }

            List<ServiceDTO> preServiceDTOList = natDTO.getPreServiceList();
            List<RuleCheckServiceDTO> preServices = new ArrayList<>();
            if(preServiceDTOList != null && preServiceDTOList.size() > 0) {
                for(ServiceDTO serviceDTO: preServiceDTOList) {
                    RuleCheckServiceDTO ruleCheckServiceDTO = new RuleCheckServiceDTO();
                    ruleCheckServiceDTO.setProtocolName(AliStringUtils.isEmpty(serviceDTO.getProtocol())?"":ProtocolUtils.getProtocolByString(serviceDTO.getProtocol()));
                    ruleCheckServiceDTO.setDstPorts(WhaleDoUtils.getSrcDstIntegerDTOList(serviceDTO.getDstPorts()));
                    preServices.add(ruleCheckServiceDTO);
                }
                simpleNatRuleRO.setPreServices(preServices);
            }

            List<ServiceDTO> postServiceDTOList = natDTO.getPostServiceList();
            List<RuleCheckServiceDTO> postServices = new ArrayList<>();
            if(postServiceDTOList != null && postServiceDTOList.size() > 0) {
                for(ServiceDTO serviceDTO : postServiceDTOList) {
                    RuleCheckServiceDTO ruleCheckServiceDTO = new RuleCheckServiceDTO();
                    ruleCheckServiceDTO.setProtocolName(ProtocolUtils.getProtocolByString(serviceDTO.getProtocol()));
                    ruleCheckServiceDTO.setDstPorts(WhaleDoUtils.getSrcDstIntegerDTOList(serviceDTO.getDstPorts()));
                    postServices.add(ruleCheckServiceDTO);
                }
                simpleNatRuleRO.setPostServices(postServices);
            }

            if(deviceWhatIfMap.containsKey(deviceUuid)) {
                WhatIfDataRO whatIfDataRO = deviceWhatIfMap.get(deviceUuid);
                List<SimpleNatRuleRO> simpleNatRules = whatIfDataRO.getSimpleNatRules();
                simpleNatRules.add(simpleNatRuleRO);
            } else {
                WhatIfDataRO whatIfDataRO = new WhatIfDataRO();
                whatIfDataRO.setDeviceUuid(deviceUuid);
                List<SimpleNatRuleRO> simpleNatRules = new ArrayList<>();
                simpleNatRules.add(simpleNatRuleRO);
                whatIfDataRO.setSimpleNatRules(simpleNatRules);
                deviceWhatIfMap.put(deviceUuid, whatIfDataRO);
            }
        }

        // 处理route数据
        for (WhatIfRouteDTO routeDTO : routeList) {
            SimpleRouteRuleRO routeRuleDTO = new SimpleRouteRuleRO();
            routeRuleDTO.setType(routeDTO.getRouteType());
            routeRuleDTO.setMaskLength(routeDTO.getMaskLength());
            routeRuleDTO.setRoutingTableUuid(routeDTO.getRoutingTableUuid());
            routeRuleDTO.setDstRoutingTableUuid(routeDTO.getDstRoutingTableUuid());
            routeRuleDTO.setDistance(routeDTO.getDistance());
            NextHopRO nextHopRO = new NextHopRO();
            Integer ipType = routeDTO.getIpType();
            if (IpTypeEnum.IPV4.getCode().equals(ipType)) {
                routeRuleDTO.setIp4Prefix(routeDTO.getIpv4DstIp());
                if (StringUtils.isNotBlank(routeDTO.getIp4Gateway())) {
                    nextHopRO.setIp4Gateway(routeDTO.getIp4Gateway());
                }

            } else if (IpTypeEnum.IPV6.getCode().equals(ipType)) {
                routeRuleDTO.setIp6Prefix(routeDTO.getIpv6DstIp());
                if (StringUtils.isNotBlank(routeDTO.getIp6Gateway())) {
                    nextHopRO.setIp6Gateway(routeDTO.getIp6Gateway());
                }
            }
            nextHopRO.setInterfaceName(routeDTO.getInterfaceName());
            routeRuleDTO.setNextHop(nextHopRO);

            String deviceUuid = routeDTO.getDeviceUuid();
            if (deviceWhatIfMap.containsKey(deviceUuid)) {
                WhatIfDataRO whatIfDataRO = deviceWhatIfMap.get(deviceUuid);
                List<SimpleRouteRuleRO> simpleRouteRules = whatIfDataRO.getSimpleRouteRules();
                if (CollectionUtils.isEmpty(simpleRouteRules)) {
                    List<SimpleRouteRuleRO> simpleRouteRule = new ArrayList<>();
                    simpleRouteRule.add(routeRuleDTO);
                    whatIfDataRO.setSimpleRouteRules(simpleRouteRule);
                } else {
                    simpleRouteRules.add(routeRuleDTO);
                }
            } else {
                WhatIfDataRO whatIfDataRO = new WhatIfDataRO();
                whatIfDataRO.setDeviceUuid(deviceUuid);
                List<SimpleRouteRuleRO> simpleRouteRule = new ArrayList<>();
                simpleRouteRule.add(routeRuleDTO);
                whatIfDataRO.setSimpleRouteRules(simpleRouteRule);
                deviceWhatIfMap.put(deviceUuid, whatIfDataRO);
            }
        }

        Set<String> deviceList = deviceWhatIfMap.keySet();
        for(String device:deviceList) {
            deviceWhatifs.put(device, deviceWhatIfMap.get(device));
        }


        whatIfRO.setDeviceWhatifs(deviceWhatifs);

        logger.info("模拟变更传入参数为：" + JSONObject.toJSONString(whatIfRO));
        WhatIfRO result = null;
        if (isNginZ == null || !isNginZ) {
            result = whaleManager.addWhatIfCase(whatIfRO);
        }else{
            result = whatIfRO;
            result.setUuid(UUID.randomUUID().toString());
        }

        logger.info("模拟变更创建结果为：" + JSONObject.toJSONString(result));

        if(result != null) {
            return result;
        }
        return new WhatIfRO();
    }
}
