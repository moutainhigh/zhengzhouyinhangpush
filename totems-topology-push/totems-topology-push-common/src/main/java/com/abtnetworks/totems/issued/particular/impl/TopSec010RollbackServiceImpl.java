package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/3/12
 */
@Slf4j
@Service
public class TopSec010RollbackServiceImpl implements SendParticularPolicyService {
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        Integer taskType = pushCmdDTO.getTaskType();
        boolean isNatPolicy = PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT == taskType ||
                PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT == taskType ||
                PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT == taskType;
        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);
        String ruleId = "";
        for (String commandLine : commandLines) {
            if (commandLine.contains(CommonConstants.POLICY_SHOW_TOP_SEC)) {
                expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                if (CollectionUtils.isNotEmpty(cmdList)) {
                    String newEchoCmd = cmdList.get(cmdList.size() - 1);
                    if (StringUtils.isBlank(newEchoCmd)) {
                        log.error("TopSec010回滚，未查询到策略ID,命令行{}，回显", commandLine);
                        return;
                    } else {
                        Matcher matcherPolicy = null;
                        if(isNatPolicy){
                            matcherPolicy = SendCommandStaticAndConstants.TOP_SEC_010_POLICY_ID_RGE_NAT.matcher(newEchoCmd);
                        }else{
                            matcherPolicy = SendCommandStaticAndConstants.TOP_SEC_010_POLICY_ID_RGE.matcher(newEchoCmd);
                        }
                        if (matcherPolicy.find()) {
                            ruleId = matcherPolicy.group("obj1");

                        } else {
                            log.error("TopSec010回滚，正则未匹配查询到回显ID，{}", commandLine);
                        }
                    }
                } else {
                    log.error("TopSec010回滚，未查询到回显，{}", commandLine);
                    return;

                }
            } else {
                if (commandLine.contains(CommonConstants.POLICY_ID) && StringUtils.isNotBlank(ruleId)) {
                    commandLine = commandLine.replace(CommonConstants.POLICY_ID, ruleId);
                    expect.send(commandLine + SendCommandStaticAndConstants.LINE_BREAK);
                } else {
                    expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                }

            }
        }
    }
}
