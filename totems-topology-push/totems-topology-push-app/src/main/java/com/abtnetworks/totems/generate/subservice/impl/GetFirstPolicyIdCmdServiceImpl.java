package com.abtnetworks.totems.generate.subservice.impl;

import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @Description 该类用于获取当前策略集上第一条策略的ID，用于生成移动策略到第一条的语句
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetFirstPolicyIdCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager whaleManager;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {
        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();
        String ruleListUuid = deviceDTO.getRuleListUuid();

        String firstId = getFirstPolicyId(deviceUuid, ruleListUuid,null);

        SettingDTO settingDTO = cmdDTO.getSetting();
        settingDTO.setSwapNameId(firstId);
    }

    protected String getFirstPolicyId(String deviceUuid, String ruleListUuid, PolicyEnum policyType) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        if(resultRO == null) {
            return null;
        }
        log.debug(String.format("迪普设备(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + resultRO.toString()  + "\n-----------------------------------\n");
        String natField = getNatFieldType(policyType);
        List<DeviceFilterRuleListRO> list = resultRO.getData();
        String name = null;
        if(list != null && list.size() > 0) {
            for(DeviceFilterRuleListRO ruleListRO:list) {
                if(natField==null) {
                    log.info("找到策略:" + ruleListRO.getRuleId());
                    name = ruleListRO.getRuleId();
                    return name;
                }else {
                    //nat策略
                    if (ruleListRO.getNatClause() != null && ruleListRO.getNatClause().get("natField") != null && Objects.equals(natField,ruleListRO.getNatClause().get("natField"))) {
                        log.info("找到策略:" + ruleListRO.getRuleId());
                        name = ruleListRO.getRuleId();
                        return name;
                    }
                }
            }
        }
        return name;
    }

    public  String getNatFieldType(PolicyEnum policyType) {
        if(null == policyType){
            return null;
        }
        switch (policyType) {
            case SNAT:
                return NatTypeEnum.SRC.getNatField();
            case DNAT:
                return NatTypeEnum.DST.getNatField();
            case STATIC:
                return NatTypeEnum.BI_DIR.getNatField();
            default:
                return null;
        }
    }
}
