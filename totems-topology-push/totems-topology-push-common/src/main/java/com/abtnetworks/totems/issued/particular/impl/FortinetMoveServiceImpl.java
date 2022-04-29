package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MatchIndexDTO;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.issued.send.SensitiveWordCommonService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Expect4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @Title:
 * @Description: 飞塔特殊移动，移动的方式
 * @date 2021/9/2
 */
@Service
@Slf4j
public class FortinetMoveServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Resource
    SensitiveWordCommonService sensitiveWordCommonService;


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        log.info("飞塔开始特例移动");
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        MoveParamDTO moveParamDTO = pushCmdDTO.getMoveParamDTO();
        String relatedRule = moveParamDTO.getRelatedRule();
        String moveType = moveParamDTO.getRelatedName();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        List<String> ruleNames = particularDTO.getRuleNames();
        String randomKey = pushCmdDTO.getRandomKey();

        if (StringUtils.isNotEmpty(relatedRule) && (AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE.equalsIgnoreCase(moveType)
                || AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER.equalsIgnoreCase(moveType))) {
            log.info("说明飞塔设备向前移动开始");
            expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.FORTINE_SECURITY_POLICY, globAndRegexElementDTO);

            for (String ruleName : ruleNames) {
                moveRuleIdByDeviceName(relatedRule, ruleName, expect,moveType);
            }
            //最后一条命令补上
            defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
        } else if (AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP.equalsIgnoreCase(moveType) && StringUtils.isEmpty(relatedRule)) {
            log.info("说明飞塔设备不允许移动，正常下发");
        } else {
            log.info("说明飞塔设备默认置顶开始");
            //发送show命令
            expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.SHOW_FORTINE_SECURITY_POLICY, globAndRegexElementDTO);

            commonAddGlobReg(globAndRegexElementDTO, SendCommandStaticAndConstants.HILLESTONE_MORE_REG);
            MatchIndexDTO matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
            int index = matchIndexDTO.getMatchIndex();
            List<JSONObject> jsonObjectList = globAndRegexElementDTO.getLinuxPromptGlobEx();

            if (index == jsonObjectList.size() - 1) {
                expect.send("q" + SendCommandStaticAndConstants.LINE_BREAK);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
            }

            //从show 回显命令中抓取策略Id
            List<String> cmdListReturn = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
            String newEchoCmd = cmdListReturn.get(cmdListReturn.size() - 1);
            List<Integer> policyIdList = new ArrayList<>();

            if (StringUtils.isNotEmpty(newEchoCmd)) {
                String[] cmdArray = newEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);

                for (int i = 1; i < cmdArray.length; i++) {
                    String comStr = cmdArray[i];
                    Pattern idPattern = Pattern.compile(SendCommandStaticAndConstants.FORTINET_ID_FLAG);
                    Matcher idMatcher = idPattern.matcher(comStr);
                    if (idMatcher.find()) {
                        String policyId = StringUtils.substring(comStr,idMatcher.end()).trim();
                        if (com.abtnetworks.totems.common.utils.StringUtils.isNumeric(policyId) && !"0".equals(policyId)) {
                            policyIdList.add(Integer.parseInt(policyId));
                        }
                    }
                }
            } else {
                //说明不包含
                log.info("回显命令中没找到ruleId查询结果{}", newEchoCmd);
            }

            /*//对策略id进行排序，取最小Id
            Collections.sort(policyIdList);*/

            if (ObjectUtils.isNotEmpty(policyIdList)) {
                //默认置顶
                expect.send(SendCommandStaticAndConstants.LINE_BREAK);
                expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.FORTINE_SECURITY_POLICY, globAndRegexElementDTO);
                for (String ruleName : ruleNames) {
                    moveRuleIdByDeviceName(String.valueOf(policyIdList.get(0)), ruleName, expect, AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE);
                }
                //最后一条命令补上
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
            }


        }
    }

    /***
     * 公共的移动命令
     * @param relatedRule
     * @param ruleId
     * @param expect
     * @throws IOException
     */
    private void moveRuleIdByDeviceName(String relatedRule, String ruleId, Expect4j expect,String moveType) throws Exception {

        if (StringUtils.isNotEmpty(ruleId)) {
            String moveCmd;
            if(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER.equalsIgnoreCase(moveType)) {
                moveCmd = SendCommandStaticAndConstants.FORTINET_MOVE_RULE_ID_AFTER.replace("#1", ruleId);
            } else {
                moveCmd = SendCommandStaticAndConstants.FORTINET_MOVE_RULE_ID_BEFORE.replace("#1", ruleId);
            }
            moveCmd = moveCmd.replace("#2", relatedRule);
            expect.send(moveCmd + SendCommandStaticAndConstants.LINE_BREAK);

            //为了避开发送时敏感命令打印
            if (sensitiveWordCommonService.checkSensitiveWord(moveCmd)) {
                log.info("发送命令到远程******");
            } else {
                log.info("发送命令到远程{}", moveCmd);
            }

        } else {
            log.info("查询策略回显中没有可移动的策略名称{}", ruleId);
        }
    }

}
