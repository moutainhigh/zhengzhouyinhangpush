package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.command.line.enums.NatTypeEnum;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.enums.PolicyEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.impl.GetFirstPolicyIdCmdServiceImpl;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class GetWestoneFirstPolicyIdAndPolicyIdCmdServiceImp extends GetFirstPolicyIdCmdServiceImpl {
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
        RuleIPTypeEnum type = RuleIPTypeEnum.IP4;
        //判断是ipv4还是ipv6
        PolicyDTO policy = cmdDTO.getPolicy();
        PolicyEnum policyType = ObjectUtils.isNotEmpty(policy.getType()) ? policy.getType() : null;
        boolean isNat = false;
        if (PolicyEnum.SNAT.equals(policyType) || PolicyEnum.DNAT.equals(policyType) || PolicyEnum.STATIC.equals(policyType)) {
            isNat = true;
        }
        if (policy.getIpType() != null) {
            if (policy.getIpType() == 2) {
                String srcIp = policy.getSrcIp();
                if (StringUtils.isNotEmpty(srcIp)) {
                    String[] ipList = srcIp.split(",");
                    for (String address : ipList) {
                        if (IpUtils.isIPRange(address) || IpUtils.isIPSegment(address) || IpUtils.isIP(address)) {
                            type = RuleIPTypeEnum.IP4;
                            break;
                        } else if (address.contains(":")) {
                            type = RuleIPTypeEnum.IP6;
                        }
                    }
                }
            } else if (policy.getIpType() == 0) {
            } else if (policy.getIpType() == 1) {
                type = RuleIPTypeEnum.IP6;
            }
        }
        int maxId = getCurrentPolicyId(deviceUuid, ruleListUuid, policyType);
        synchronized (GetWestoneFirstPolicyIdAndPolicyIdCmdServiceImp.class) {
            int currentId = 0;
            if (type == RuleIPTypeEnum.IP4) {
                if (isNat) {
                    currentId = advancedSettingService.getWestoneNatPolicyId(deviceUuid, policyType);
                } else {
                    currentId = advancedSettingService.getWestonePolicyId(deviceUuid);
                }
            } else {
                currentId = advancedSettingService.getWestonePolicy6Id(deviceUuid);
            }
            if (maxId < currentId) {
                maxId = currentId;
            }
            String firstPolicyId = getFirstPolicyId(deviceUuid, ruleListUuid, policyType);
            if (firstPolicyId != null) {
                if (firstPolicyId.startsWith("Default")) {
                    firstPolicyId = null;
                }
            }
            int newMaxId = maxId + 1;
            settingDTO.setPolicyId(String.valueOf(newMaxId));

            if (type == RuleIPTypeEnum.IP4) {
                if (isNat) {
                    advancedSettingService.setWestoneNatPolicyId(deviceUuid, newMaxId, policyType);
                } else {
                    advancedSettingService.setWestonePolicyId(deviceUuid, newMaxId);
                }
            } else {
                advancedSettingService.setWestonePolicy6Id(deviceUuid, newMaxId);
            }

            settingDTO.setSwapNameId(firstPolicyId);
        }
    }

    Integer getCurrentPolicyId(String deviceUuid, String ruleListUuid, PolicyEnum policyType) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        log.debug(String.format("设备(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + JSONObject.toJSONString(resultRO) + "\n-----------------------------------");
        if (resultRO == null) {
            return 0;
        }
        List<DeviceFilterRuleListRO> list = resultRO.getData();
        Integer id = 0;
        String name = null;
        String natField = getNatFieldType(policyType);
        if (list != null && list.size() > 0) {
            for (DeviceFilterRuleListRO ruleListRO : list) {
                if(natField==null){
                    id = filterRuleLIstRo(id, ruleListRO);
                }else {
                    //nat策略
                    if (ruleListRO.getNatClause() != null && ruleListRO.getNatClause().get("natField") != null && Objects.equals(natField,ruleListRO.getNatClause().get("natField"))) {
                        id = filterRuleLIstRo(id, ruleListRO);
                    }
                }
            }
        }

        return id;
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

    private Integer filterRuleLIstRo(Integer id, DeviceFilterRuleListRO ruleListRO) {
        String name;
        log.debug("找到策略:" + ruleListRO.getRuleId());
        name = ruleListRO.getRuleId();
        if (AliStringUtils.isEmpty(name)) {
            log.debug("策略名为空！");
            return id;
        }

        if (name.startsWith("Default")) {
            log.debug("Default策略，跳过获取id");
            return id;
        }

        try {
            Integer policyId = Integer.valueOf(name.trim());
            if (policyId > id) {
                id = policyId;
            }
        } catch (Exception e) {
            log.debug("解析策略名称出错。。。", e);
        }
        return id;
    }
}
