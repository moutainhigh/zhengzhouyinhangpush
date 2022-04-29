package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2020/9/11
 */
@Service
public class HillStoneForbidIpServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    private final Logger LOGGER = LoggerFactory.getLogger(HillStoneForbidIpServiceImpl.class);


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {

        LOGGER.info("山石开始特例查询id进行封禁ip业务适配");
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        String policyName = pushCmdDTO.getPolicyFlag();
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        String randomKey = pushCmdDTO.getRandomKey();
        String commandline = pushCmdDTO.getCommandline();
        if (commandline.contains(CommonConstants.LABEL_POLICY_ID)) {
            String newReg = "(D|E)\\s+(?<obj1>.*?)\\s+#1\\s.*?".replace("#1", policyName);
            Pattern ruleNameIdReg = Pattern.compile(newReg);
            String showCmd = SendCommandStaticAndConstants.HILLSTONE_SHOW_NAME + policyName;
            commonSendMode(particularDTO, showCmd);
            List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
            String newEchoCmd = cmdList.get(cmdList.size() - 1);
            if (StringUtils.isBlank(newEchoCmd)) {
                LOGGER.error("山石封禁，未查询到策略ID, ruleName:{}", policyName);
                return;
            }
            Matcher matcher = ruleNameIdReg.matcher(newEchoCmd);
            if (matcher.find()) {
                String ruleIdStr = matcher.group("obj1");
                commandline = commandline.replace(CommonConstants.LABEL_POLICY_ID, ruleIdStr);
                pushCmdDTO.setCommandline(commandline);
            }

            expect.send(" " + SendCommandStaticAndConstants.LINE_BREAK);

        }

    }
}
