package com.abtnetworks.totems.generate.subservice.impl.device;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.dto.CmdDTO;
import com.abtnetworks.totems.common.dto.DeviceDTO;
import com.abtnetworks.totems.common.dto.PolicyDTO;
import com.abtnetworks.totems.common.dto.SettingDTO;
import com.abtnetworks.totems.generate.subservice.CmdService;
import com.abtnetworks.totems.recommend.manager.WhaleManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description 该类用于获取Cisco设备接口上现有策略集名称。若该接口上没有策略集，则新建一个策略集。新建策略集的名称格式为
 *              [接口别名]_[出/入方向]
 * @Author Wen Jiachang
 */
@Slf4j
@Service
public class GetCiscoInterfaceRuleListCmdServiceImpl implements CmdService {

    @Autowired
    WhaleManager  whaleManager;

    @Autowired
    AdvancedSettingService advancedSettingService;

    @Override
    public void modify(CmdDTO cmdDTO) throws Exception {

        DeviceDTO deviceDTO = cmdDTO.getDevice();
        String deviceUuid = deviceDTO.getDeviceUuid();

        PolicyDTO policyDTO = cmdDTO.getPolicy();

        SettingDTO settingDTO = cmdDTO.getSetting();

        String interfaceAlias = policyDTO.getSrcItfAlias();
        //高级配置：思科出接口是否挂在out方向，默认是in
        boolean outBound = advancedSettingService.isDeviceInTheList(AdvancedSettingsConstants.PARAM_NAME_CISCO_ACL_OUT_INTERFACE, deviceUuid);
        if (outBound) {
            interfaceAlias = policyDTO.getDstItfAlias();
        } else if (!outBound && AliStringUtils.isEmpty(interfaceAlias)) {
            //入接口为空，则用出接口
            interfaceAlias = policyDTO.getDstItfAlias();
            outBound = true;
        }

        settingDTO.setOutBound(outBound);

        if (StringUtils.isNotBlank(interfaceAlias)) {
            log.debug(String.format("思科设备(%s)接口别名为(%s)，查询现有策略集名称...", deviceUuid, interfaceAlias));
            //转换为接口入参需要的isInbound值
            boolean inbound = outBound ? false : true;
            String policyName = whaleManager.getInterfacePolicyName(deviceUuid, interfaceAlias, inbound);
            if (AliStringUtils.isEmpty(policyName)) {
                policyName = String.format("%s_%s", interfaceAlias, inbound ? "in" : "out");
//                settingDTO.setCreateCiscoItfRuleList(true); KSH-5378 20210525
                log.error("思科通过设备deviceUuid={}，挂载方向{}，没有查到接口别名{}的安全策略或者策略描述为空，按照别名和挂载方向拼{}",deviceUuid,inbound ? "in" : "out",interfaceAlias,policyName);
            }
            settingDTO.setCiscoItfRuleListName(policyName);
        } else {
            log.info("思科接口别名为空...");
        }
    }
}
