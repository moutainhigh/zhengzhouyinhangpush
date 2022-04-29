package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.generate.subservice.impl.GetFirstPolicyNameInZoneCmdServiceImpl;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterlistRO;
import com.abtnetworks.totems.whale.baseapi.service.WhaleDevicePolicyClient;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Description
 *              其获取当前策略id的方式，是插空选取id
 */
@Slf4j
@Service
public class GetCiscoASAPolicyIdCmdServiceImpl extends GetFirstPolicyNameInZoneCmdServiceImpl {

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Autowired
    private WhaleDevicePolicyClient whaleDevicePolicyClient;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        log.debug("CiscoASA的策略id是插空选取id");

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();

        SettingDTO settingDTO = cmdDTO.getSetting();
        if (StringUtils.isNotEmpty(settingDTO.getPolicyId())){
            return;
        }
        ResultRO<List<JSONObject>> resultRO = getPolicyRuleList(deviceUuid);
        //KSH-5469
        int currentId = advancedSettingService.getCiscoASAPolicyId(deviceUuid);
        Integer freeId ;
        if(ObjectUtils.isNotEmpty(resultRO) && CollectionUtils.isNotEmpty(resultRO.getData())) {
            synchronized (GetCiscoASAPolicyIdCmdServiceImpl.class) {

                freeId = getFreePolicyId(resultRO, currentId + 1);
                settingDTO.setPolicyId(String.valueOf(freeId));
            }
        }else{
            freeId = currentId + 1;
            settingDTO.setPolicyId(String.valueOf(freeId));
            log.info("思科ASA防火墙没有策略集");
        }
        advancedSettingService.setCiscoASAPolicyId(deviceUuid, freeId);
    }

    protected ResultRO<List<JSONObject>> getPolicyRuleList(String deviceUuid) {
        ResultRO<List<DeviceFilterlistRO>> dataResultRO = whaleDevicePolicyClient.getDevicePolicyFilterlist(deviceUuid);
        if (dataResultRO == null || !dataResultRO.getSuccess() || dataResultRO.getData() == null || dataResultRO.getData().size() == 0) {
            log.info("没有查询到策略集");
            return null;
        }
        List<DeviceFilterlistRO> resultList = new ArrayList<>();
        String typeStr = "SYSTEM__NAT_LIST";
        for (DeviceFilterlistRO ro : dataResultRO.getData()) {
            String ruleListType = ro.getRuleListType();
            if (StringUtils.isBlank(ruleListType)) {
                continue;
            }
            //其他策略集
            boolean booleanType = ruleListType.contains(typeStr);
            if (booleanType) {
                resultList.add(ro);
            }

        }
        ResultRO<List<JSONObject>> resultRO = null;
        if(CollectionUtils.isNotEmpty(resultList)){
            List<String> uuids = resultList.stream().map(p -> p.getUuid()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(uuids)){
                resultRO = whaleDevicePolicyClient.getRuleIndex(deviceUuid, uuids.get(0));
                log.debug(String.format("思科ASA(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, uuids.get(0))
                        + JSONObject.toJSONString(resultRO, SerializerFeature.PrettyFormat) + "\n-----------------------------------\n");
            }
        }

        return resultRO;
    }


    /**
     * 获取没有使用的最小策略id
     *
     * @param resultRO 策略集数据对象
     * @param id 可使用的最小id
     * @return 比当前已使用id大的最小的策略集中未使用的id
     */
    protected Integer getFreePolicyId(ResultRO<List<JSONObject>> resultRO, Integer id) {
        List<JSONObject> list = resultRO.getData();

        Set<Integer> idSet = new HashSet<>();
        if(list != null && list.size() > 0) {
            for(JSONObject rule: resultRO.getData()) {
                Integer ruleId = 0;
                try{
                    if (!rule.containsKey("ruleId")){
                        continue;
                    }
                    String ruleId1 = (String)rule.get("ruleId");
                    if(StringUtils.isBlank(ruleId1) && !StringUtils.isNumeric(ruleId1)){
                        continue;
                    }
                    ruleId = Integer.valueOf(ruleId1);
                } catch(Exception e) {
                    log.error(String.format("思科ASA策略 %s ID异常", JSONObject.toJSONString(rule, SerializerFeature.PrettyFormat)));
                }
                idSet.add(ruleId);
            }
        }

        while(idSet.contains(id)) {
            id++;
        }

        return id;
    }
}
