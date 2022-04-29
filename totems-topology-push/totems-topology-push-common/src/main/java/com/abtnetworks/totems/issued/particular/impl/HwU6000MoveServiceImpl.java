package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
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
import java.util.regex.Pattern;

/**
 * @author Administrator
 * @Title:
 * @Description: usg6000移动的方式
 * @date 2020/8/13
 */
@Service
public class HwU6000MoveServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    private final Logger LOGGER = LoggerFactory.getLogger(HwU6000MoveServiceImpl.class);

    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Resource
    SensitiveWordCommonService sensitiveWordCommonService;

    @Resource
    SendCommandBeforeBuilderService sendCommandBuilderService;


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        LOGGER.info("usg6000开始特例移动");

        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();

        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();

        List<String> ruleNames = commonIssueFindRuleName(pushCmdDTO, globAndRegexElementDTO, SendCommandStaticAndConstants.H3V7_NAME_FLAG);


        String randomKey = pushCmdDTO.getRandomKey();
        MoveParamDTO moveParamDTO = pushCmdDTO.getMoveParamDTO();
        String relatedRule = moveParamDTO.getRelatedRule();
        String moveType = moveParamDTO.getRelatedName();
        if (StringUtils.isNotEmpty(relatedRule) && AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE.equalsIgnoreCase(moveType)) {
//            LOGGER.info("说明Hwu6000设备向前移动开始");
//            expectClientInExecuteService.sendAndCheckMatch(expect, SendCommandStaticAndConstants.H3V7_SYSTEM_VIEW, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
//            isHwVSyS(expect, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
//            expectClientInExecuteService.sendAndCheckMatch(expect, SendCommandStaticAndConstants.HW_U6000_SECURITY_POLICY, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
//
//            for (String ruleName : ruleNames) {
//                defineSendResultTypeService.expectMatchAndCheckResult(expect, lstPattern, recordSizeForError, linuxPromptRegEx);
//                String selectCommand = SendCommandStaticAndConstants.HW_U6000_SHOW + ruleName;
//                expect.send(selectCommand + SendCommandStaticAndConstants.LINE_BREAK);
//                defineSendResultTypeService.expectMatchAndCheckResult(expect, lstPattern, recordSizeForError, linuxPromptRegEx);
//                moveRuleNameByHwU6000DeviceName(randomKey, relatedRule, ruleName, expect, SendCommandStaticAndConstants.HW_U6000_MOVE_RULE_BEFORE);
//            }
//            returnHwU6000Builder(expect, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
        } else if (AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_TOP.equalsIgnoreCase(moveType) && StringUtils.isEmpty(relatedRule)) {
            LOGGER.info("说明华为u6000设备不允许移动，正常下发");
        } else if (AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_AFTER.equalsIgnoreCase(moveType) && StringUtils.isNotEmpty(relatedRule)) {
//            LOGGER.info("说明Hwu6000设备向后移动开始");
//            expectClientInExecuteService.sendAndCheckMatch(expect, SendCommandStaticAndConstants.H3V7_SYSTEM_VIEW, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
//            isHwVSyS(expect, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
//            expectClientInExecuteService.sendAndCheckMatch(expect, SendCommandStaticAndConstants.HW_U6000_SECURITY_POLICY, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
//
//            for (String ruleName : ruleNames) {
//                defineSendResultTypeService.expectMatchAndCheckResult(expect, lstPattern, recordSizeForError, linuxPromptRegEx);
//                String selectCommand = SendCommandStaticAndConstants.HW_U6000_SHOW + ruleName;
//                expect.send(selectCommand + SendCommandStaticAndConstants.LINE_BREAK);
//                defineSendResultTypeService.expectMatchAndCheckResult(expect, lstPattern, recordSizeForError, linuxPromptRegEx);
//                moveRuleNameByHwU6000DeviceName(randomKey, relatedRule, ruleName, expect, SendCommandStaticAndConstants.HW_U6000_MOVE_RULE_AFTER);
//            }
//            returnHwU6000Builder(expect, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
        } else {
            LOGGER.info("说明华为u6000设备默认置顶开始");
            //默认置顶
            expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SYSTEM_VIEW, globAndRegexElementDTO);
            isHwVSyS(globAndRegexElementDTO, pushCmdDTO);
            expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.HW_U6000_SECURITY_POLICY, globAndRegexElementDTO);
            DeviceModelNumberEnum deviceModelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
            String ruleId = "";
            if (deviceModelNumberEnum.equals(DeviceModelNumberEnum.USG6000_NO_TOP)) {
                expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.HW_U6000_SHOW, globAndRegexElementDTO);
                commonAddGlobReg(globAndRegexElementDTO, SendCommandStaticAndConstants.H3V7_MORE_REG);
                MatchIndexDTO matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                int index = matchIndexDTO.getMatchIndex();
                List<JSONObject> jsonObjectList = globAndRegexElementDTO.getLinuxPromptGlobEx();
                if (index == jsonObjectList.size() - 1) {
                    expect.send("q" + SendCommandStaticAndConstants.LINE_BREAK);
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    ruleId = locationRuleIdByEcho(randomKey,2);
                } else {
                    //说明没有more 不需要发q
                    ruleId = locationRuleIdByEcho(randomKey,1);
                }
            }
            for (String ruleName : ruleNames) {
                if (deviceModelNumberEnum.equals(DeviceModelNumberEnum.USG6000_NO_TOP)) {
                    moveBefore(SendCommandStaticAndConstants.HW_U6000_MOVE_RULE_BEFORE, ruleName, ruleId, expect);
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                }
//                else {
//                    String selectCommand = SendCommandStaticAndConstants.HW_U6000_MOVE_TOP.replace("#1", ruleName);
//                    expectClientInExecuteService.sendAndCheckMatch(expect, selectCommand, lstPattern, recordSizeForError, linuxPromptRegEx, pushCmdDTO);
//                }
            }
            returnHwU6000Builder(globAndRegexElementDTO, pushCmdDTO);
        }
    }


    /**
     * 对华为设备虚墙return之后重新进入
     *
     * @param globAndRegexElementDTO
     * @param pushCmdDTO
     * @throws Exception
     */
    private void isHwVSyS(GlobAndRegexElementDTO globAndRegexElementDTO, PushCmdDTO pushCmdDTO) throws Exception {
        boolean isVSys = pushCmdDTO.getIsVSys() == null ? false : pushCmdDTO.getIsVSys();
        //华为虚墙的情况，拼接命令行要注意重新进入
        if (isVSys) {
            LOGGER.info("华为虚墙重新进入虚墙中查策略");
            String vSysName = pushCmdDTO.getVSysName();
            String entryVSysCmd = SendCommandStaticAndConstants.HW_U6000_ENTRY_VSY_S.replace("#1", vSysName);
            expectClientInExecuteService.sendAndCheckMatch(entryVSysCmd, globAndRegexElementDTO);
            expectClientInExecuteService.sendAndCheckMatch(SendCommandStaticAndConstants.H3V7_SYSTEM_VIEW, globAndRegexElementDTO);

        }
    }

    /**
     * 定位准要移动到那个rule前的辨识
     *
     * @param randomKey
     * @return
     */
    private String locationRuleIdByEcho(String randomKey,int indexCmdList) {
        //回显命令可能有多条，
        List<String> cmdListReturn = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
        String newEchoCmd = cmdListReturn.get(cmdListReturn.size() - indexCmdList);
        if (StringUtils.isNotEmpty(newEchoCmd)) {
            String[] cmdArray = newEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);
            Pattern namePattern = Pattern.compile(SendCommandStaticAndConstants.H3V7_NAME_FLAG);
            for (int i = 2; i < cmdArray.length; i++) {
                //从第二行开始算,第一行是发送命令，有可能重复
                String ruleCmd = cmdArray[i];
                if (namePattern.matcher(ruleCmd).find()) {
                    String ruleId = ruleCmd.substring(ruleCmd.indexOf("name") + 4).trim();
                    return ruleId;
                }
            }
        }
        return "";
    }

    /***
     * 指定移动华为的代码业务
     * @param randomKey
     * @param relatedRule
     * @param ruleName
     * @param expect
     * @throws IOException
     */
    private void moveRuleNameByHwU6000DeviceName(String randomKey, String relatedRule, String ruleName, Expect4j expect, String moveAction) throws IOException {
        //回显命令可能有多条，
        List<String> cmdListReturn = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
        String newEchoCmd = cmdListReturn.get(cmdListReturn.size() - 1);
        if (StringUtils.isNotEmpty(newEchoCmd)) {
            String[] cmdArray = newEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);
            String ruleId = "";
            Pattern namePattern = Pattern.compile(ruleName);
            for (int i = 2; i < cmdArray.length; i++) {
                //从第二行开始算,第一行是发送命令，有可能重复
                String ruleCmd = cmdArray[i];
                if (namePattern.matcher(ruleCmd).find()) {
                    ruleId = ruleCmd.substring(ruleCmd.indexOf("name") + 4).trim();
                    break;
                }
            }
            moveBefore(moveAction, ruleId, relatedRule, expect);
        } else {
            //说明不包含
            LOGGER.info("回显命令中没找到ruleName{}---查询结果{}", ruleName, newEchoCmd);
        }
    }

    /**
     * yid
     *
     * @param moveAction
     * @param ruleId
     * @param relatedRule
     * @param expect
     * @throws IOException
     */
    private void moveBefore(String moveAction, String ruleId, String relatedRule, Expect4j expect) throws IOException {
        if (StringUtils.isNotEmpty(ruleId)) {
            String moveCmd = moveAction.replace("#1", ruleId);
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
    }


    /***
     * 华为的公共移动之后处理return和save
     * @param globAndRegexElementDTO
     * @param pushCmdDTO
     * @throws Exception
     */
    private void returnHwU6000Builder(GlobAndRegexElementDTO globAndRegexElementDTO, PushCmdDTO pushCmdDTO) throws Exception {
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        String cmdSave = sendCommandBuilderService.u6000CommandBuild(SendCommandStaticAndConstants.H3V7_RETURN + SendCommandStaticAndConstants.LINE_BREAK, pushCmdDTO.getPushForbidDTO());
        String[] commandLines = cmdSave.split(SendCommandStaticAndConstants.LINE_BREAK);
        for (String cmdStr : commandLines) {
            //这里是对注释之后，匹配和发送的流程顺序改变了
            expect.send(cmdStr + SendCommandStaticAndConstants.LINE_BREAK);
            //最后一条命令补上回显
            defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
        }
    }

}
