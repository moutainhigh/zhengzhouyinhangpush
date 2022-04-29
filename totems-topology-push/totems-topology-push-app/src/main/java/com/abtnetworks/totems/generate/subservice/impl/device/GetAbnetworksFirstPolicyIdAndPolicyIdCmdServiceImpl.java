package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.command.line.enums.RuleIPTypeEnum;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.ro.ResultRO;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.generate.subservice.impl.GetFirstPolicyIdCmdServiceImpl;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import com.abtnetworks.totems.whale.baseapi.ro.DeviceFilterRuleListRO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class GetAbnetworksFirstPolicyIdAndPolicyIdCmdServiceImpl extends GetFirstPolicyIdCmdServiceImpl {
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
        if (policy != null && policy.getIpType() != null) {
            if (policy.getIpType() == 2) {
                String srcIp = policy.getSrcIp();
                if (StringUtils.isNotEmpty(srcIp)) {
                    List<String> ipList = Arrays.asList(srcIp.split(","));
                    for (int i = 0; i < ipList.size(); i++) {
                        String address = ipList.get(i);
                        if (IpUtils.isIPRange(address) || IpUtils.isIPSegment(address) || IpUtils.isIP(address)) {
                            type = RuleIPTypeEnum.IP4;
                            break;
                        } else if (address.contains(":")) {
                            type = RuleIPTypeEnum.IP6;
                        }
                    }
                }
            } else if (policy.getIpType() == 0) {
                type = RuleIPTypeEnum.IP4;
            } else if (policy.getIpType() == 1) {
                type = RuleIPTypeEnum.IP6;
            }
        }
        int maxId = getCurrentPolicyId(deviceUuid, ruleListUuid);
        boolean isAbt = DeviceModelNumberEnum.ABTNETWORKS.getKey().equalsIgnoreCase(deviceDTO.getModelNumber().getKey());
        boolean isSdnware = DeviceModelNumberEnum.SDNWARE.getKey().equalsIgnoreCase(deviceDTO.getModelNumber().getKey());

        synchronized (GetAbnetworksFirstPolicyIdAndPolicyIdCmdServiceImpl.class) {
            int currentId = 0;
            if (type == RuleIPTypeEnum.IP4) {
                if (isAbt) {
                    currentId = advancedSettingService.getAbtnetworksOrSdnwarePolicyId(deviceUuid, AdvancedSettingsConstants.PARAM_NAME_ABTNETWORKS_POLICY_ID);
                }
                if (isSdnware) {
                    currentId = advancedSettingService.getAbtnetworksOrSdnwarePolicyId(deviceUuid, AdvancedSettingsConstants.PARAM_NAME_SDNWARE_POLICY_ID);
                }
            } else {
                if (isAbt) {
                    currentId = advancedSettingService.getAbtnetworksOrSdnwarePolicy6Id(deviceUuid, AdvancedSettingsConstants.PARAM_NAME_ABTNETWORKS6_POLICY_ID);
                }
                if (isSdnware) {
                    currentId = advancedSettingService.getAbtnetworksOrSdnwarePolicy6Id(deviceUuid, AdvancedSettingsConstants.PARAM_NAME_SDNWARE6_POLICY_ID);
                }
            }
            if (maxId < currentId) {
                maxId = currentId;
            }
            String firstPolicyId = getFirstPolicyId(deviceUuid, ruleListUuid, null);
            if (firstPolicyId != null) {
                if (firstPolicyId.startsWith("Default")) {
                    firstPolicyId = null;
                }
            }
            int newMaxId = maxId + 1;
            settingDTO.setPolicyId(String.valueOf(newMaxId));

            if (type == RuleIPTypeEnum.IP4) {
                if (isAbt) {
                    advancedSettingService.setAbtnetworksPolicyId(deviceUuid, newMaxId, AdvancedSettingsConstants.PARAM_NAME_ABTNETWORKS_POLICY_ID);
                }
                if (isSdnware) {
                    advancedSettingService.setAbtnetworksPolicyId(deviceUuid, newMaxId, AdvancedSettingsConstants.PARAM_NAME_SDNWARE_POLICY_ID);
                }
            } else {
                if (isAbt) {
                    advancedSettingService.setAbtnetworksPolicy6Id(deviceUuid, newMaxId, AdvancedSettingsConstants.PARAM_NAME_ABTNETWORKS6_POLICY_ID);
                }
                if (isSdnware) {
                    advancedSettingService.setAbtnetworksPolicy6Id(deviceUuid, newMaxId, AdvancedSettingsConstants.PARAM_NAME_SDNWARE6_POLICY_ID);
                }
            }

            if (StringUtils.isBlank(settingDTO.getSwapNameId())) {
                settingDTO.setSwapNameId(firstPolicyId);
            }
        }
    }

    Integer getCurrentPolicyId(String deviceUuid, String ruleListUuid) {
        ResultRO<List<DeviceFilterRuleListRO>> resultRO = whaleManager.getFilterRuleList(deviceUuid, ruleListUuid);
        log.debug(String.format("设备(%s)策略集(%s)相关策略数据为：\n-----------------------------------\n", deviceUuid, ruleListUuid)
                + JSONObject.toJSONString(resultRO) + "\n-----------------------------------");
        if (resultRO == null) {
            return 0;
        }
        List<DeviceFilterRuleListRO> list = resultRO.getData();
        Integer id = 0;
        String name = null;
        if (list != null && list.size() > 0) {
            for (DeviceFilterRuleListRO ruleListRO : list) {
                log.debug("找到策略:" + ruleListRO.getRuleId());
                name = ruleListRO.getRuleId();
                if (AliStringUtils.isEmpty(name)) {
                    log.debug("策略名为空！");
                    continue;
                }

                if (name.startsWith("Default")) {
                    log.debug("Default策略，跳过获取id");
                    continue;
                }

                try {
                    Integer policyId = Integer.valueOf(name.trim());
                    if (policyId > id) {
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
