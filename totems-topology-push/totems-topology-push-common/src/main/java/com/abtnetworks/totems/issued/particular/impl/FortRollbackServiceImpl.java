package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author lifei
 * @desc 飞塔回滚特例，根据策略名称show出这条策略的id，然后动态再替换占位符 再下发
 * @date 2021/9/30 14:43
 */
@Service
@Log4j2
public class FortRollbackServiceImpl implements SendParticularPolicyService {
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        log.info("开始飞塔回滚查询策略id特殊处理----");
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);
        String ruleId = "";
        for (String commandLine : commandLines) {
            if (commandLine.contains(CommonConstants.POLICY_SHOW_FORTINET)) {
                expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);

                List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                if (CollectionUtils.isNotEmpty(cmdList)) {
                    expect.send(SendCommandStaticAndConstants.LINE_BREAK);
                    String newEchoCmd = cmdList.get(cmdList.size() - 1);
                    log.info("Fortinet回滚，根据命令行:{} 查询到策略ID回显为:{}", commandLine,newEchoCmd);
                    if (StringUtils.isBlank(newEchoCmd)) {
                        return;
                    } else {
                        Matcher matcherPolicy = SendCommandStaticAndConstants.FORTINET_RULE_NAME_OWNER.matcher(newEchoCmd);
                        if (matcherPolicy.find()) {
                            ruleId = matcherPolicy.group("id");
                        } else {
                            log.error("Fortinet回滚,根据命令行show{},未查询到回显，", commandLine);
                        }
                    }
                } else {
                    log.error("Fortinet回滚,根据命令行show{},未查询到回显，", commandLine);
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
        //最后一条命令补上
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
    }

}
