package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lifei
 * @desc 飞塔特殊移动实现类
 * @date 2022/1/5 18:02
 */
@Service
@Log4j2
public class FortMoveServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {

        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();

        if (pushCmdDTO.getVerifyFlag()) {
            particularDTO.setIsExecute(false);
            return;
        }

        Integer taskType = pushCmdDTO.getTaskType();
        boolean unableMove = taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT ||
                                taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DENY ||
                                taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_PERMIT ||
                                taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DELETE ||
                                taskType == PolicyConstants.CUSTOMIZE_CMD_PUSH ||
                                taskType == PolicyConstants.POLICY_OPTIMIZE ||
                                taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK ||
                                taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT ||
                                taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT ||
                                taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING;
        // 飞塔只有策略建议、安全策略、snat、dnat、bothNat 有移动的逻辑，除开这些之外都跳过特殊移动逻辑
        if (unableMove) {
            particularDTO.setIsExecute(false);
            return;
        }

        List<String> ruleNames = new ArrayList<>();
        String command = pushCmdDTO.getCommandline();
        String[] commandLines = command.split(SendCommandStaticAndConstants.LINE_BREAK);
        String randomKey = pushCmdDTO.getRandomKey();

        //查找关键字命令行
        Pattern pattern = Pattern.compile(SendCommandStaticAndConstants.FORTINET_NAME_FLAG);
        String policyId = "";
        for (String strCmd : commandLines) {
            //如果是move命令行
            if (strCmd.startsWith(SendCommandStaticAndConstants.FORTINET_MOVE_FLAG)) {
                strCmd = strCmd.replace("#1", policyId);
            }

            //发送命令
            expectClientInExecuteService.sendAndCheckMatch(strCmd, globAndRegexElementDTO);
            Matcher matcher = pattern.matcher(strCmd);
            if (matcher.find()) {
                Expect4j expect = globAndRegexElementDTO.getExpect4j();
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                expect.send(SendCommandStaticAndConstants.LINE_BREAK);
                //从show 回显命令中抓取策略Id
                List<String> cmdListReturn = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                String newEchoCmd = cmdListReturn.get(cmdListReturn.size() - 1);

                if (StringUtils.isNotEmpty(newEchoCmd)) {
                    String[] cmdArray = newEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);

                    for (int i = 1; i < cmdArray.length; i++) {
                        String comStr = cmdArray[i];
                        Pattern idPattern = Pattern.compile(SendCommandStaticAndConstants.FORTINET_ID_FLAG);
                        Matcher idMatcher = idPattern.matcher(comStr);
                        if (idMatcher.find()) {
                            String ruleName = StringUtils.substring(comStr,idMatcher.end()).trim();
                            if (com.abtnetworks.totems.common.utils.StringUtils.isNumeric(ruleName) && !"0".equals(ruleName)) {
                                ruleNames.add(ruleName);
                                policyId = ruleName;
                            }
                        }
                    }
                } else {
                    //说明不包含
                    log.info("回显命令中没找到ruleId查询结果{}", newEchoCmd);
                }
            }
        }
        //最后一条命令补上
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
    }
}
