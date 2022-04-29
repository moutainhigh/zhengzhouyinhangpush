package com.abtnetworks.totems.push.service.impl;

import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.DeviceNetworkTypeEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.push.service.PushBussExtendService;
import com.abtnetworks.totems.push.vo.CheckRelevancyNatOrderVO;
import com.abtnetworks.totems.push.vo.FivePushInfoVo;
import com.abtnetworks.totems.recommend.dao.mysql.CommandTaskEdiableMapper;
import com.abtnetworks.totems.recommend.dao.mysql.NodeMapper;
import com.abtnetworks.totems.recommend.dto.risk.DeviceInterfaceDto;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.dto.DeviceObjectSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.*;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDeviceObjectClient;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/2/24
 */
@Service
@Log4j2
public class PushBussExtendServiceImpl implements PushBussExtendService {

    @Autowired
    CommandTaskEdiableMapper commandTaskEdiableMapper;

    @Autowired
    private WhaleDeviceObjectClient whaleDeviceObjectClient;

    @Autowired
    private WhaleManager whaleManager;

    @Autowired
    private NodeMapper policyRecommendNodeMapper;

    @Override
    public List<CheckRelevancyNatOrderVO> checkRelevancyNatOrder(String ids) {
        List<CheckRelevancyNatOrderVO> checkRelevancyNatOrderVOS = commandTaskEdiableMapper.selectRecommendNatOrderByIds(ids);
        return checkRelevancyNatOrderVOS;
    }

    @Override
    public FivePushInfoVo queryPoolNameForFive(String deviceUuid) {
        DeviceObjectSearchDTO searchDTO = new DeviceObjectSearchDTO();
        searchDTO.setDeviceUuid(deviceUuid);
        searchDTO.setPsize(500);
        searchDTO.setPage(1);
        ResultRO<List<NetWorkGroupObjectRO>> resultRO = whaleDeviceObjectClient.getNetWorkGroupObject(searchDTO);
        if(null == resultRO || CollectionUtils.isEmpty(resultRO.getData())){
            log.info("根据设备uuid:{}查询pool名称为空",deviceUuid);
            return new FivePushInfoVo();
        }
        // 获取sant名称和下面的所有地址
        Map<String, List<String>> snatPoolNameMap = getSnatListMap(deviceUuid, resultRO);

        FivePushInfoVo fivePushInfoVo = new FivePushInfoVo();
        fivePushInfoVo.setSnatPoolName(snatPoolNameMap);
        // 获取 profileName
        FivePushInfoVo fivePushInfoVo1 = queryProfileName(deviceUuid);
        if (null == fivePushInfoVo1) {
            fivePushInfoVo.setHttpProfileNames(new ArrayList<>());
            fivePushInfoVo.setSslProfileNames(new ArrayList<>());
        } else {
            fivePushInfoVo.setHttpProfileNames(fivePushInfoVo1.getHttpProfileNames());
            fivePushInfoVo.setSslProfileNames(fivePushInfoVo1.getSslProfileNames());
        }
        return fivePushInfoVo;
    }

    /**
     * 获取sant名称和下面的所有地址
     * @param deviceUuid
     * @param resultRO
     * @return
     */
    private Map<String, List<String>> getSnatListMap(String deviceUuid, ResultRO<List<NetWorkGroupObjectRO>> resultRO) {
        Map<String,List<String>> objectNamesMap = new HashMap<>();
        for (NetWorkGroupObjectRO ro : resultRO.getData()){
            if(StringUtils.isNotBlank(ro.getName()) && DeviceNetworkTypeEnum.SRC_POOL.equals(ro.getDeviceNetworkType())){
                if(CollectionUtils.isEmpty(ro.getIncludeItems())){
                    continue;
                }
                List<String> itemNames = ro.getIncludeItems().stream().map(IncludeItemsRO:: getNameRef).collect(Collectors.toList());
                objectNamesMap.put(ro.getName(),itemNames);
            }
        }
        Map<String,List<String>> snatPoolNameMap = new HashMap<>();
        for(String snatPoolName :objectNamesMap.keySet()){
            List<String> ips = new ArrayList<>();
            for (String objectName : objectNamesMap.get(snatPoolName)){
                DeviceObjectSearchDTO objectsearchDTO = new DeviceObjectSearchDTO();
                objectsearchDTO.setDeviceUuid(deviceUuid);
                objectsearchDTO.setName(objectName);
                objectsearchDTO.setPsize(500);
                objectsearchDTO.setPage(1);
                ResultRO<List<NetWorkGroupObjectRO>> objectRo = whaleDeviceObjectClient.getNetWorkObject(objectsearchDTO);
                if(null == resultRO || CollectionUtils.isEmpty(objectRo.getData())){
                    log.info("根据设备uuid:{}和对象名称：{}查询pool名称为空",deviceUuid,objectName);
                    continue;
                }
                // 默认取第一个对象
                NetWorkGroupObjectRO ro = objectRo.getData().get(0);
                if(null == ro){
                    continue;
                }
                List<IncludeItemsRO> itemDetail = ro.getIncludeItems();
                if(CollectionUtils.isEmpty(itemDetail)){
                    continue;
                }
                // 取第一个对象
                IncludeItemsRO singleIncludeItem = itemDetail.get(0);
                if(null == singleIncludeItem){
                    continue;
                }
                ips.addAll(singleIncludeItem.getIp4Addresses());
            }
            snatPoolNameMap.put(snatPoolName,ips);

        }
        return snatPoolNameMap;
    }

    @Override
    public FivePushInfoVo queryProfileName(String deviceUuid) {
        DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
        if (null == deviceRO || CollectionUtils.isEmpty(deviceRO.getData())) {
            log.info("根据设备uuid:{}查询设备详情为空", deviceUuid);
            return null;
        }
        DeviceDataRO deviceDataRO = deviceRO.getData().get(0);
        if (null == deviceDataRO) {
            log.info("根据设备uuid:{}查询设备详情为空", deviceUuid);
            return null;
        }
        Map<String, Object> detailMap = deviceDataRO.getArgsMap();
        if (null == detailMap || detailMap.isEmpty()) {
            log.info("根据设备uuid:{}查询设备详情解析出来的profile参数为空", deviceUuid);
            return null;
        }
        FivePushInfoVo fivePushInfoVo = new FivePushInfoVo();
        fivePushInfoVo.setHttpProfileNames((List) detailMap.get("HTTP_PROFILES"));
        fivePushInfoVo.setSslProfileNames((List) detailMap.get("SSL_PROFILES"));
        return fivePushInfoVo;
    }

    @Override
    public List<String> queryRouteNames(String deviceUuid) {
        // 根据设备uuid查询设备路由名称集合
        List<RoutingtableRO> routeNames = whaleManager.getRoutTable(deviceUuid);
        if(CollectionUtils.isEmpty(routeNames)){
            return new ArrayList<>();
        }
        List<String> resultRouteNames = new ArrayList<>();
        for (RoutingtableRO routingtableRO : routeNames){
            resultRouteNames.add(routingtableRO.getName());
        }
        return resultRouteNames;
    }

    @Override
    public List<String> queryInterfaceNames(String deviceUuid) {
        List<String> resultInterfaceNames = new ArrayList<>();
        // 首先查询设备，看是不是思科的设备.
        NodeEntity nodeEntity = policyRecommendNodeMapper.getTheNodeByUuid(deviceUuid);
        DeviceModelNumberEnum deviceModelNumberEnum = DeviceModelNumberEnum.fromString(nodeEntity.getModelNumber());
        boolean isCisco = DeviceModelNumberEnum.isRangeCiscoCode(deviceModelNumberEnum.getCode());
        // 只有是思科设备，且类型是防火墙的时候才会去查询接口别名
        if (isCisco && "0".equals(nodeEntity.getType())) {
            DeviceRO deviceRO = whaleDeviceObjectClient.getDeviceROByUuid(deviceUuid);
            if (null == deviceRO || CollectionUtils.isEmpty(deviceRO.getData())) {
                return new ArrayList<>();
            }
            for (DeviceDataRO deviceDataRO : deviceRO.getData()) {
                List<DeviceInterfaceRO> deviceInterfaceROS = deviceDataRO.getDeviceInterfaces();
                if (CollectionUtils.isEmpty(deviceInterfaceROS)) {
                    continue;
                }
                for (DeviceInterfaceRO deviceInterfaceRO : deviceInterfaceROS) {
                    if(StringUtils.isBlank(deviceInterfaceRO.getAlias())){
                        continue;
                    }
                    resultInterfaceNames.add(deviceInterfaceRO.getAlias());
                }
            }
            // 思科防火墙做特殊处理，接口别名返回Null0
            if(!resultInterfaceNames.contains("Null0")){
                resultInterfaceNames.add("Null0");
            }
        } else {
            // 根据设备uuid查询设备路由名称集合
            List<DeviceInterfaceDto> interfaceNames = whaleManager.getDeviceInterfaces(deviceUuid);
            if (CollectionUtils.isEmpty(interfaceNames)) {
                return new ArrayList<>();
            }

            for (DeviceInterfaceDto deviceInterfaceDto : interfaceNames) {
                resultInterfaceNames.add(deviceInterfaceDto.getInterfaceName());
            }
        }

        return resultInterfaceNames;
    }

}
