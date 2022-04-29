package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description 该类用于获取当前策略集上第一条策略的名称，用于生成移动策略到第一条的语句
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetFirstPolicyNameCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();
        String ruleListUuid = deviceDTO.getRuleListUuid();

        String firstPolicyName = getFirstPolicyName(deviceUuid, ruleListUuid);

        SettingDTO settingDTO = cmdDTO.getSetting();
        settingDTO.setSwapNameId(firstPolicyName);
    }

    /**
     * 查询策略集下，符合条件的第一个策略集名称
     * @param deviceUuid  设备uuid
     * @param ruleListUuid 策略集uuid
     * @return
     */
    protected String getFirstPolicyName(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);

        if(resultRO == null) {
            return null;
        }

        List<DeviceFilterRuleListRO> list = resultRO.getData();
        String name = null;
        if(list != null && list.size() > 0) {
            int index = 0;
            for(DeviceFilterRuleListRO ruleListRO:list) {
                index++;
                if (ruleListRO.isImplicit() == true) {
                    log.info(String.format("第(%d)条策略(%s)为默认策略，不返回策略名称...", index, ruleListRO.getName()));
                } else {
                    log.info("找到策略:" + ruleListRO.getName());
                    name = ruleListRO.getName();
                    return name;
                }
            }
        }
        return name;
    }
}
