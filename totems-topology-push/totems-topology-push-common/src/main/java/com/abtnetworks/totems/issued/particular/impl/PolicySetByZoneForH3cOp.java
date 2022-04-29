package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.dto.RecommendTask2IssuedDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.issued.send.SendCommandBeforeBuilderService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import expect4j.Expect4j;
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
 * @Description: H3C SecPath V7 OP 查询策略集名，根据域
 * @date 2020/9/4
 */
@Service
public class PolicySetByZoneForH3cOp implements SendParticularPolicyService {
    private final Logger LOGGER = LoggerFactory.getLogger(PolicySetByZoneForH3cOp.class);
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Resource
    SendCommandBeforeBuilderService sendCommandBeforeBuilderService;

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {

        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        MoveParamDTO moveParamDTO = pushCmdDTO.getMoveParamDTO();


        Integer ipType = moveParamDTO.getIpType();
        boolean isRevert = pushCmdDTO.getRevert() == null ? false : pushCmdDTO.getRevert();

        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);
        RecommendTask2IssuedDTO recommendTask2IssuedDTO = pushCmdDTO.getRecommendTask2IssuedDTO();
        String srcZone = StringUtils.isEmpty(recommendTask2IssuedDTO.getSrcZone()) ? PolicyConstants.POLICY_STR_VALUE_ANY : recommendTask2IssuedDTO.getSrcZone();
        String dstZone = StringUtils.isEmpty(recommendTask2IssuedDTO.getDstZone()) ? PolicyConstants.POLICY_STR_VALUE_ANY : recommendTask2IssuedDTO.getDstZone();

        String ruleId = String.format("%s_to_%s", srcZone, dstZone);
        boolean isShowPolicy = false;
        String resultRuleId = "";
        if (!isRevert) {
            // 下发特例
            pushSpecial(globAndRegexElementDTO, expect, ipType, randomKey, commandLines, srcZone, dstZone, ruleId, isShowPolicy);
        } else {
            // 回滚特例
            rollbackSpecial(globAndRegexElementDTO, expect, ipType, randomKey, commandLines, srcZone, dstZone, ruleId, resultRuleId);
        }
        //最后一条命令补上回显
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
    }

    /**
     * 华三V7-op 下发特例
     *
     * @param globAndRegexElementDTO
     * @param expect
     * @param ipType
     * @param randomKey
     * @param commandLines
     * @param srcZone
     * @param dstZone
     * @param ruleId
     * @param isShowPolicy
     * @throws Exception
     */
    private void pushSpecial(GlobAndRegexElementDTO globAndRegexElementDTO, Expect4j expect, Integer ipType, String randomKey, String[] commandLines, String srcZone, String dstZone, String ruleId, boolean isShowPolicy) throws Exception {
        for (int i = 0; i < commandLines.length; i++) {
            //是华三V7/华为的找到命令行中
            String strCmd = commandLines[i];
            if (strCmd.contains(CommonConstants.OBJECT_POLICY)) {
                LOGGER.info("H3C SecPath V7 OP 执行特例下发");
                String showPolicySet = SendCommandStaticAndConstants.H3C_POLICY_SHOW.replace("#1", srcZone).replace("#2", dstZone);
                expectClientInExecuteService.sendAndCheckMatch(showPolicySet, globAndRegexElementDTO);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                String newEchoCmd = cmdList.get(cmdList.size() - 1);

                Matcher matcherPolicy = SendCommandStaticAndConstants.H3C_OBJECT_POLICY.matcher(newEchoCmd);
                if (matcherPolicy.find()) {
                    ruleId = matcherPolicy.group("obj1");
                    isShowPolicy = true;
                } else {
                    isShowPolicy = false;
                }
                String selectCommand = selectIpv4OrV6Show(ipType, ruleId, strCmd);
                expect.send(selectCommand + SendCommandStaticAndConstants.LINE_BREAK);
                LOGGER.info("H3C SecPath V7 OP 拼接下发命令{}", selectCommand);
                continue;
            }

            if (i == commandLines.length - 1) {
                if (!isShowPolicy) {
                    //到最后一个命令时就开始拼接倒挂 ，
                    StringBuffer notShowPolicyName = new StringBuffer(strCmd + SendCommandStaticAndConstants.LINE_BREAK);
                    String zonePair = String.format("zone-pair security source %s destination %s\n", srcZone, dstZone);
                    notShowPolicyName.append(zonePair);
                    String policySetNameCmd = String.format("object-policy apply ip %s\nquit\nreturn\n", ruleId);
                    notShowPolicyName.append(policySetNameCmd);
                    String notPolicyName = sendCommandBeforeBuilderService.h3cCommandBuild(notShowPolicyName.toString());
                    returnAndSave(notPolicyName, globAndRegexElementDTO);
                } else {
                    expectClientInExecuteService.sendAndCheckMatch(strCmd, globAndRegexElementDTO);
                    String notPolicyName = sendCommandBeforeBuilderService.h3cCommandBuild(SendCommandStaticAndConstants.H3V7_RETURN);
                    returnAndSave(notPolicyName, globAndRegexElementDTO);
                }
            } else {
                expectClientInExecuteService.sendAndCheckMatch(strCmd, globAndRegexElementDTO);

            }

        }
    }

    /**
     * 回滚特例
     *
     * @param globAndRegexElementDTO
     * @param expect
     * @param ipType
     * @param randomKey
     * @param commandLines
     * @param srcZone
     * @param dstZone
     * @param ruleId
     * @param resultRuleId
     * @return
     * @throws Exception
     */
    private void rollbackSpecial(GlobAndRegexElementDTO globAndRegexElementDTO, Expect4j expect, Integer ipType, String randomKey, String[] commandLines, String srcZone, String dstZone, String ruleId, String resultRuleId) throws Exception {
        LOGGER.info("开始华三V7-op 回滚特例...");
        // 华三V7-op 回滚特例
        String targetCompareCmd = "";
        String objectPolicyName = "";
        for (String strCmd : commandLines) {
            //是华三V7/华为的找到命令行中
            if(StringUtils.isBlank(strCmd)){
                continue;
            }
            if (strCmd.contains(CommonConstants.POLICY_SHOW_TOP_SEC)) {
                targetCompareCmd = strCmd.replace(CommonConstants.POLICY_SHOW_TOP_SEC, "").trim();
                continue;
            }

            if (strCmd.contains(CommonConstants.pre_commandLine) && strCmd.contains(CommonConstants.OBJECT_POLICY)) {
                LOGGER.info("H3C SecPath V7 OP 执行特例下发");
                String showPolicySet = SendCommandStaticAndConstants.H3C_POLICY_SHOW.replace("#1", srcZone).replace("#2", dstZone);
                expectClientInExecuteService.sendAndCheckMatch(showPolicySet, globAndRegexElementDTO);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                String newEchoCmd = cmdList.get(cmdList.size() - 1);

                Matcher matcherPolicy = SendCommandStaticAndConstants.H3C_OBJECT_POLICY.matcher(newEchoCmd);
                if (matcherPolicy.find()) {
                    ruleId = matcherPolicy.group("obj1");
                    objectPolicyName = ruleId;
                }
                String selectCommand = selectIpv4OrV6Show(ipType, ruleId, strCmd);
                expect.send(selectCommand + SendCommandStaticAndConstants.LINE_BREAK);
                LOGGER.info("H3C SecPath V7 OP 拼接下发命令{}", selectCommand);

                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                String newSendEchoCmd = cmdList.get(cmdList.size() - 1);
                LOGGER.info("H3C SecPath V7 OP 发送show源域目的域下面策略集合的回显为{}", newSendEchoCmd);
                if (StringUtils.isBlank(newSendEchoCmd)) {
                    LOGGER.error("H3C SecPath V7 OP 发送show源域目的域下面策略集合的回显为空");
                    return;
                }

                String[] echoCmdList = newSendEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);
                String policyInfo = null;
                for (String echoCmdStr : echoCmdList) {
                    if(StringUtils.isBlank(echoCmdStr)){
                        continue;
                    }
                    Matcher matcherPolicyInfo = SendCommandStaticAndConstants.H3C_OBJECT_POLICYI_FIND_ID.matcher(echoCmdStr);
                    if (matcherPolicyInfo.find()) {
                        ruleId = matcherPolicyInfo.group("id");
                        policyInfo = matcherPolicyInfo.group("policyInfo");
                    }
                    if (StringUtils.isNotBlank(policyInfo) && StringUtils.isNotBlank(targetCompareCmd) && policyInfo.trim().contains(targetCompareCmd)) {
                        resultRuleId = ruleId;
                        break;
                    }
                }
                expect.send(" " + SendCommandStaticAndConstants.LINE_BREAK);
                LOGGER.info("根据对比字段:{},匹配上的策略id为:{}",targetCompareCmd,resultRuleId);
            } else if(strCmd.contains(CommonConstants.OBJECT_POLICY)){
                String selectCommand = selectIpv4OrV6Show(ipType, objectPolicyName, strCmd);
                expectClientInExecuteService.sendAndCheckMatch(selectCommand, globAndRegexElementDTO);
            } else if (strCmd.contains(CommonConstants.POLICY_ID)) {
                strCmd = strCmd.replace(CommonConstants.POLICY_ID, resultRuleId);
                expectClientInExecuteService.sendAndCheckMatch(strCmd, globAndRegexElementDTO);
            } else {
                expectClientInExecuteService.sendAndCheckMatch(strCmd, globAndRegexElementDTO);
            }
        }
    }


    private void returnAndSave(String notPolicyName, GlobAndRegexElementDTO globAndRegexElementDTO) throws Exception {
        String[] commandLines2 = notPolicyName.split(SendCommandStaticAndConstants.LINE_BREAK);
        for (String cmdStr : commandLines2) {
            expectClientInExecuteService.sendAndCheckMatch(cmdStr, globAndRegexElementDTO);
        }
    }

    /**
     * 区别下ip和ipv6
     *
     * @param ipType
     * @param ruleName
     * @return
     */
    private String selectIpv4OrV6Show(Integer ipType, String ruleName, String strCmd) {
        String selectCommand = strCmd.replace(CommonConstants.OBJECT_POLICY, ruleName);
        if (ipType != null && ipType.equals(1)) {
            selectCommand = selectCommand.replace("ip", "ipv6");
        }
        return selectCommand;
    }
}
