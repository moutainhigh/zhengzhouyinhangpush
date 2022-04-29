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
import com.abtnetworks.totems.issued.send.SendCommandBeforeBuilderService;
import com.abtnetworks.totems.issued.send.SensitiveWordCommonService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Expect4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @Title:
 * @Description: 华三v7，移动的方式
 * @date 2020/8/13
 */
@Service
public class H3V7MoveServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    private final Logger LOGGER = LoggerFactory.getLogger(H3V7MoveServiceImpl.class);
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Resource
    SensitiveWordCommonService sensitiveWordCommonService;

    @Resource
    SendCommandBeforeBuilderService sendCommandBuilderService;


    /**
     * 选着ipv6还是v4的拼接
     *
     * @param ipType
     * @param ruleName
     * @return
     */
    private String selectIpv4OrV6Show(Integer ipType, String ruleName) {
        String selectCommand;
        if (ipType != null && ipType.equals(1)) {
            selectCommand = SendCommandStaticAndConstants.H3V7_SHOW_RULE_ID_IPV6 + ruleName ;
        } else {
            selectCommand = SendCommandStaticAndConstants.H3V7_SHOW_RULE_ID + ruleName ;
        }
        return selectCommand;
    }

    /**
     * 选着ipv6还是v4的拼接
     *
     * @param ipType
     * @return
     */
    private String selectIpv4OrV6Show(Integer ipType) {
        String selectCommand;
        if (ipType != null && ipType.equals(1)) {
            selectCommand = SendCommandStaticAndConstants.H3V7_SHOW_IPV6_RULE;
        } else {
            selectCommand = SendCommandStaticAndConstants.H3V7_SHOW_RULE;
        }
        return selectCommand;
    }

    /***
     * 公共的移动命令
     * @param randomKey
     * @param relatedRule
     * @param ruleName
     * @param expect
     * @throws IOException
     */
    private void moveRuleIdByH3DeviceName(String randomKey, String relatedRule, String ruleName, Expect4j expect,String moveType) throws Exception {

        //回显命令可能有多条，
        List<String> cmdListReturn = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
        String newEchoCmd = cmdListReturn.get(cmdListReturn.size() - 1);
        if (StringUtils.isNotEmpty(newEchoCmd)) {
            String ruleId = "";
            String[] cmdArray = newEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);
            Pattern pattern = SendCommandStaticAndConstants.RULE_NAME_OWNER;

            for (int i = 1; i < cmdArray.length; i++) {
                //从第二行开始算,第一行是发送命令，有可能重复
                String ruleCmd = cmdArray[i];
                Matcher matcher = pattern.matcher(ruleCmd);
                if (matcher.find()) {
                    String findName = matcher.group("name");
                    if (ruleName.equals(findName)) {
                        //如果从命令行中拿到的name和自己工单中name完全一致就说明对了，取到id结束
                        ruleId = matcher.group("id");
                        break;
                    }
                }
            }
            if (StringUtils.isNotEmpty(ruleId)) {
                String moveCmd;
                if(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER.equalsIgnoreCase(moveType)){
                    moveCmd = SendCommandStaticAndConstants.H3V7_MOVE_RULE_ID_AFTER.replace("#1", ruleId);
                } else{
                    moveCmd = SendCommandStaticAndConstants.H3V7_MOVE_RULE_ID_BEFORE.replace("#1", ruleId);
                }
                moveCmd = moveCmd.replace("#2", relatedRule);
                expect.send(moveCmd + SendCommandStaticAndConstants.LINE_BREAK);

                //为了避开发送时敏感命令打印
                if (sensitiveWordCommonService.checkSensitiveWord(moveCmd)) {
                    LOGGER.info("发送命令到远程******");
                } else {
                    LOGGER.info("发送命令到远程{}", moveCmd);
                }

            } else {
                LOGGER.info("查询策略回显中没有可移动的策略名称{}", ruleId);
            }
        } else {
            //说明不包含
            LOGGER.info("回显命令中没找到ruleName{}---查询结果{}", ruleName, newEchoCmd);
        }
    }

    /***
     * 华三的公共移动之后处理return和save
     * @param globAndRegexElementDTO
     * @throws Exception
     */
    private void returnH3v7Builder(GlobAndRegexElementDTO globAndRegexElementDTO) throws Exception {
        String cmdSave = sendCommandBuilderService.h3cCommandBuild(SendCommandStaticAndConstants.H3V7_RETURN + SendCommandStaticAndConstants.LINE_BREAK);
        String[] commandLines = cmdSave.split(SendCommandStaticAndConstants.LINE_BREAK);
        for (String cmdStr : commandLines) {
            expectClientInExecuteService.sendAndCheckMatch(cmdStr, globAndRegexElementDTO);
        }
    }

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        LOGGER.info("华三v7开始特例移动");
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
            LOGGER.info("说明H3设备向前移动开始");
            expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SYSTEM_VIEW, globAndRegexElementDTO);
            Integer ipType = moveParamDTO.getIpType();
            if (ipType != null && ipType.equals(1)) {
                expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SECURITY_IPV6_POLICY, globAndRegexElementDTO);
            } else {
                expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SECURITY_POLICY, globAndRegexElementDTO);
            }


            for (String ruleName : ruleNames) {
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);

                String selectCommand = selectIpv4OrV6Show(ipType, ruleName);
                expect.send(selectCommand + SendCommandStaticAndConstants.LINE_BREAK);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                // 这里因为发送命令正则里面有] 导致反馈管理的终止符可以匹配到,所以这里需要再匹配一次获取工单号的回显
                // +.name.u$ \n rule 150 name u \n[H3C-security-policy-ip]
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                moveRuleIdByH3DeviceName(randomKey, relatedRule, ruleName, expect,moveType);
            }
            returnH3v7Builder(globAndRegexElementDTO);
        } else if (AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP.equalsIgnoreCase(moveType) && StringUtils.isEmpty(relatedRule)) {
            LOGGER.info("说明H3设备不允许移动，正常下发");
        } else {
            LOGGER.info("说明H3设备默认置顶开始");
            //默认置顶
            expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SYSTEM_VIEW, globAndRegexElementDTO);

            Integer ipType = moveParamDTO.getIpType();
            if (ipType != null && ipType.equals(1)) {
                expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SECURITY_IPV6_POLICY, globAndRegexElementDTO);
            } else {
                expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SECURITY_POLICY, globAndRegexElementDTO);
            }
            String showCommand = selectIpv4OrV6Show(ipType);

            expectClientInExecuteService.sendAndCheckMatch(showCommand, globAndRegexElementDTO);

            commonAddGlobReg(globAndRegexElementDTO, SendCommandStaticAndConstants.H3V7_MORE_REG);
            MatchIndexDTO matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
            int index = matchIndexDTO.getMatchIndex();
            List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
            String newEchoCmd = cmdList.get(cmdList.size() - 1);
            if (StringUtils.isNotEmpty(newEchoCmd)) {
                String[] cmdArray = newEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);
                String ruleId = "";
                Pattern pattern = SendCommandStaticAndConstants.RULE_NAME_RGE;
                for (int i = 1; i < cmdArray.length; i++) {
                    //从第二行开始算,第一行是发送命令，有可能重复
                    String ruleCmd = cmdArray[i];
                    Matcher matcher = pattern.matcher(ruleCmd);
                    if (matcher.find()) {
                        ruleId = matcher.group("obj1");
                        break;
                    }
                }
                List<JSONObject> jsonObjectList = globAndRegexElementDTO.getLinuxPromptGlobEx();
                if (index == jsonObjectList.size() - 1) {
                    expect.send("q" + SendCommandStaticAndConstants.LINE_BREAK);
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);

                }
                //用于记录被发q打乱的下发和匹配回显显流程，从第二次开始要进行匹配才能拿到回显
                AtomicInteger count = new AtomicInteger(0);
                for (String ruleName : ruleNames) {
                    String selectCommand = selectIpv4OrV6Show(ipType, ruleName);
                    if (count.get() > 0) {
                        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    }
                    expect.send(selectCommand + SendCommandStaticAndConstants.LINE_BREAK);
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    // 这里因为发送命令正则里面有] 导致反馈管理的终止符可以匹配到,所以这里需要再匹配一次获取工单号的回显
                    // +.name.u$ \n rule 150 name u \n[H3C-security-policy-ip]
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    moveRuleIdByH3DeviceName(randomKey, ruleId, ruleName, expect,AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE);

                    count.addAndGet(1);
                }

                returnH3v7Builder(globAndRegexElementDTO);


            } else {
                LOGGER.error(SendCommandStaticAndConstants.H3V7_MORE_REG + "匹配不到或者dis security-policy ip | include rule没有查到");
            }
        }
    }
}
