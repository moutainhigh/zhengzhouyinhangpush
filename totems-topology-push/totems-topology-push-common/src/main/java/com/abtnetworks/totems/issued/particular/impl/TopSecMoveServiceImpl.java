package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Administrator
 * @Title:
 * @Description: 天融信针对三种型号，根据groupName来选着移动的方式 （无法show之后ctrl c拿到回显数据，ng用命令行中查询rule id置顶）
 * @date 2020/8/13
 */
@Service
public class TopSecMoveServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    private final Logger LOGGER = LoggerFactory.getLogger(TopSecMoveServiceImpl.class);

    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        boolean isAfterMove = null != pushCmdDTO.getMoveParamDTO() && AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER.equalsIgnoreCase(pushCmdDTO.getMoveParamDTO().getRelatedName());
        if (!isAfterMove) {
            LOGGER.info(String.format("当前天融信移动方式为:%s，不支持特殊移动", null != pushCmdDTO.getMoveParamDTO() ? pushCmdDTO.getMoveParamDTO().getRelatedName() : "空"));
            particularDTO.setIsExecute(false);
            return;
        }
        LOGGER.info("开始执行天融信特殊处理after移动---");
        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);
        String ruleId = "";
        if(StringUtils.isBlank(pushCmdDTO.getCommandlineRevert())){
            return;
        }
        String[] revertCommandLines = pushCmdDTO.getCommandlineRevert().split(SendCommandStaticAndConstants.LINE_BREAK);
        String showPolicyIdCommandLine = "";
        for (String commandline :revertCommandLines){
            if(commandline.contains(CommonConstants.POLICY_SHOW_TOP_SEC)){
                showPolicyIdCommandLine = commandline;
            }
        }
        showPolicyIdCommandLine = showPolicyIdCommandLine.replace(CommonConstants.POLICY_SHOW_TOP_SEC,SendCommandStaticAndConstants.TOP_SEC_SHOW);
        for (String commandLine : commandLines) {
            if (commandLine.contains(CommonConstants.POLICY_TOP_SEC_MOVE_FLAG)) {

                expectClientInExecuteService.sendAndCheckMatch(showPolicyIdCommandLine, globAndRegexElementDTO);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                if (CollectionUtils.isNotEmpty(cmdList)) {
                    String newEchoCmd = cmdList.get(cmdList.size() - 1);
                    if (StringUtils.isBlank(newEchoCmd)) {
                        LOGGER.error("TopSec010回滚，未查询到策略ID,命令行{}，回显", commandLine);
                        return;
                    } else {
                        Matcher matcherPolicy = SendCommandStaticAndConstants.TOP_SEC_010_POLICY_ID_RGE.matcher(newEchoCmd);
                        if (matcherPolicy.find()) {
                            ruleId = matcherPolicy.group("obj1");

                        } else {
                            LOGGER.error("TopSec010回滚，正则未匹配查询到回显ID，{}", commandLine);
                        }
                    }
                } else {
                    LOGGER.error("TopSec010回滚，未查询到回显，{}", commandLine);
                    return;
                }
                commandLine = commandLine.replace(CommonConstants.POLICY_ID, ruleId);
                expect.send(commandLine + SendCommandStaticAndConstants.LINE_BREAK);
            } else {
                expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
            }
        }
        //最后一条命令补上
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);

    }

}
