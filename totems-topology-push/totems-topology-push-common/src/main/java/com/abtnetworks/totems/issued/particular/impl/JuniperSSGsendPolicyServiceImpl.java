package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Expect4j;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author zwr
 * @Title:
 * @Description: SSG下发中取id
 * @date 2021/11/03
 */
@Service
@Log4j2
public class JuniperSSGsendPolicyServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        log.info(CommonConstants.POLICY_ID + " SSG下发特例方法 " + JSONObject.toJSONString(particularDTO));
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);

        for (String commandLine : commandLines) {
            if (commandLine.contains(CommonConstants.LABEL_POLICY_ID)) {
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                if (CollectionUtils.isEmpty(cmdList)) {
                    log.error("SSG下发，未匹配查询到回显ID，{}", commandLine);
                    return;
                }

                String newEchoCmd = cmdList.get(cmdList.size() - 1);
                log.info("下发获取到的回显：" + newEchoCmd);
                if (StringUtils.isBlank(newEchoCmd)) {
                    log.error("SSG下发，未查询到到策略ID,命令行{}，回显{}", commandLine, newEchoCmd);
                    return;
                }

                Matcher matcherPolicy = SendCommandStaticAndConstants.SSG_POLICY_ID_RGE.matcher(newEchoCmd);
                if (!matcherPolicy.find()) {
                    log.error("SSG下发，正则未匹配查询到回显ID，{}", commandLine);
                    return;
                }

                log.info("ruleId " + matcherPolicy.group("id"));
                String ruleId = matcherPolicy.group("id");

                commandLine = commandLine.replace(CommonConstants.LABEL_POLICY_ID, ruleId);
                expect.send(commandLine + SendCommandStaticAndConstants.LINE_BREAK);
            } else {
                expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
            }
        }
        //最后一条命令补上
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
    }

}

