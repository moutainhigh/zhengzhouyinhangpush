package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.generate.subservice.impl.GetFirstPolicyIdCmdServiceImpl;
import com.abtnetworks.totems.whale.baseapi.dto.RoutingTableSearchDTO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.RoutingtableRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.abtnetworks.totems.whale.policy.ro.RoutingEntriesRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description 该类用于获取飞塔静态路由最大ID以及当前生成策略所使用的ID。
 *              当前策略所使用的ID为当前路由表上最大的ID和数据库中存储的ID较大的那个+1，以避免多次生成策略时造成的ID重复
 */
@Slf4j
@Service
public class GetFortinetStatingRoutingFirstPolicyIdAndPolicyIdCmdServiceImp extends GetFirstPolicyIdCmdServiceImpl {

    @Autowired
    WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();

        SettingDTO settingDTO = cmdDTO.getSetting();

        int maxId = getCurrentPolicyId(deviceUuid);
        synchronized (GetFortinetStatingRoutingFirstPolicyIdAndPolicyIdCmdServiceImp.class) {
            int currentId = advancedSettingService.getFortinetStaticRouteId(deviceUuid);
            if (maxId < currentId) {
                maxId = currentId;
            }
            int newMaxId = maxId + 1;
            settingDTO.setPolicyId(String.valueOf(newMaxId));

            advancedSettingService.setFortinetStaticRouteId(deviceUuid, newMaxId);

        }
    }

    Integer getCurrentPolicyId(String deviceUuid) {
        ResultRO<List<RoutingtableRO>> routingTable = whaleDevicePolicyClient.getRoutingTable(deviceUuid);
        log.debug(String.format("设备(%s)路由表相关数据为：\n-----------------------------------\n", deviceUuid)
                + JSONObject.toJSONString(routingTable)  + "\n-----------------------------------");
        if(routingTable == null) {
            return 0;
        }
        List<RoutingtableRO> listRoutingtable = routingTable.getData();
        if(CollectionUtils.isNotEmpty(listRoutingtable)){
            List<String> stringList = listRoutingtable.stream().map(p -> p.getUuid()).collect(Collectors.toList());
            RoutingTableSearchDTO searchDTO = new RoutingTableSearchDTO();
            List<Integer> list1 = new ArrayList<>();
            Integer integer = 0;
            for (String routingTableUuid : stringList) {
                searchDTO.setRoutingTableUuid(routingTableUuid);
                searchDTO.setDeviceUuid(deviceUuid);
                ResultRO<List<RoutingEntriesRO>> listResultRO = whaleDevicePolicyClient.getRoutingEnteries(searchDTO);
                log.debug(String.format("设备(%s)路由表(%s)相关数据为：\n-----------------------------------\n", deviceUuid, routingTableUuid)
                        + JSONObject.toJSONString(routingTable)  + "\n-----------------------------------");
                if(listResultRO == null) {
                    return 0;
                }
                List<RoutingEntriesRO> entriesROS = listResultRO.getData();
                if (CollectionUtils.isNotEmpty(entriesROS)){
                    Integer integer1 = entriesROS.stream().filter(p -> StringUtils.isNotEmpty(p.getName()))
                            .map(p -> p.getName()).map(p -> Integer.valueOf(p.trim()))
                            .distinct().max(Integer::compareTo).get();
                    list1.add(integer1);
                }
            }

            if (CollectionUtils.isNotEmpty(list1)){
                integer = list1.stream().distinct().max(Integer::compareTo).get();
            }
            return integer;
        }else {
            return 0;
        }

    }
}
