package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.abtnetworks.totems.common.enums.DeviceModelNumberEnum.H3CV7;

/**
 * @author lifei
 * @desc 华三V7特殊移动处理实现 服务类 总类
 * @date 2022/1/6 10:25
 */
@Service
@Log4j2
public class H3V7SpecialMoveServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    @Resource
    Map<String, SendParticularPolicyService> sendMovePolicyServiceMap;

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        log.info("华三v7开始特例移动");
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();

        if (pushCmdDTO.getVerifyFlag()) {
            log.info("H3CV7--More处理-a1");
            String serviceName6 = NameUtils.getServiceDefaultName(SendCustomizeCmdServiceImpl.class);
            SendParticularPolicyService sendMovePolicyService6 = sendMovePolicyServiceMap.get(serviceName6);
            particularDTO.setDeviceModelNumberEnum(H3CV7);
            log.info("H3CV7--More处理-a2");
            sendMovePolicyService6.deviceParticularByRule(particularDTO);
            log.info("H3CV7--More处理-结束");
        } else {
            log.info("H3CV7-开始正常移动特例");
            List<String> ruleNames = commonIssueFindRuleName(pushCmdDTO, globAndRegexElementDTO, SendCommandStaticAndConstants.H3V7_NAME_FLAG);
            particularDTO.setRuleNames(ruleNames);
            String serviceName = NameUtils.getServiceDefaultName(H3V7MoveServiceImpl.class);
            SendParticularPolicyService sendMovePolicyService = sendMovePolicyServiceMap.get(serviceName);

            sendMovePolicyService.deviceParticularByRule(particularDTO);
        }
        //最后一条命令补上
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
    }
}
