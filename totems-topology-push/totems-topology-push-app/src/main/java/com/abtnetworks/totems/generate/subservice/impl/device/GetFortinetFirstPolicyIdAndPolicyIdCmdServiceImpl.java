package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.generate.subservice.impl.GetFirstPolicyIdCmdServiceImpl;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 该类用于获取飞塔设备接口上现有策略集中第一条策略的ID以及当前生成策略所使用的ID。
 *              当前策略所使用的ID为当前策略集上最大的ID和数据库中存储的ID较大的那个+1，以避免多次生成策略时造成的ID重复
 *              飞塔设备第一条策略的ID用来增加置顶语句。
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetFortinetFirstPolicyIdAndPolicyIdCmdServiceImpl extends GetFirstPolicyIdCmdServiceImpl {

    @Autowired
    WhaleManager whaleManager;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();
        String ruleListUuid = deviceDTO.getRuleListUuid();

        SettingDTO settingDTO = cmdDTO.getSetting();

        int maxId = getCurrentPolicyId(deviceUuid, ruleListUuid);
        synchronized (GetFortinetFirstPolicyIdAndPolicyIdCmdServiceImpl.class) {
            int currentId = advancedSettingService.getFortinetPolicyId(deviceUuid);
            if (maxId < currentId) {
                maxId = currentId;
            }
            String firstPolicyId = getFirstPolicyId(deviceUuid, ruleListUuid,null);
            if (firstPolicyId != null) {
                if (firstPolicyId.startsWith("Default")) {
                    firstPolicyId = null;
                }
            }
            int newMaxId = maxId + 1;
            settingDTO.setPolicyId(String.valueOf(newMaxId));

            advancedSettingService.setFortinetPolicyId(deviceUuid, newMaxId);

            settingDTO.setSwapNameId(firstPolicyId);
        }
    }

    Integer getCurrentPolicyId(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        log.debug(String.format("设备(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + JSONObject.toJSONString(resultRO)  + "\n-----------------------------------");
        if(resultRO == null) {
            return 0;
        }
        List<DeviceFilterRuleListRO> list = resultRO.getData();
        Integer id = 0;
        String name = null;
        if(list != null && list.size() > 0) {
            for(DeviceFilterRuleListRO ruleListRO:list) {
                log.debug("找到策略:" + ruleListRO.getRuleId());
                name = ruleListRO.getRuleId();
                if(AliStringUtils.isEmpty(name)) {
                    log.debug("策略名为空！");
                    continue;
                }

                if(name.startsWith("Default")) {
                    log.debug("Default策略，跳过获取id");
                    continue;
                }

                try {
                    Integer policyId = Integer.valueOf(name.trim());
                    if(policyId>id) {
                        id = policyId;
                    }
                } catch (Exception e) {
                    log.debug("解析策略名称出错。。。", e);
                }
            }
        }

        return id;
    }
}
