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
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Expect4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants.H3C_V5_MOVE;

/**
 * @author Administrator
 * @Title:
 * @Description: 华三v5，移动的方式
 * @date 2020/8/13
 */
@Slf4j
@Service
public class H3V5MoveServiceImpl extends SendParticularPolicyBaseService implements SendParticularPolicyService {

    private final Logger LOGGER = LoggerFactory.getLogger(H3V5MoveServiceImpl.class);
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        LOGGER.info("华三v5开始特例移动");
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        MoveParamDTO moveParamDTO = pushCmdDTO.getMoveParamDTO();
        String relatedRule = moveParamDTO.getRelatedRule();
        String moveType = moveParamDTO.getRelatedName();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
        boolean isBeforeMove;
        if (StringUtils.isBlank(moveType)) {
            isBeforeMove = true;
        } else {
            isBeforeMove = AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE.equalsIgnoreCase(moveType);
        }

        if (isBeforeMove) {
            LOGGER.info("说明H3v5设备默认置顶开始");
            String randomKey = pushCmdDTO.getRandomKey();
            String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);
            String ruleId = "";
            String ruleIdForDevice = "";
            commonAddGlobReg(globAndRegexElementDTO, SendCommandStaticAndConstants.H3V7_MORE_REG);
            for (String commandLine : commandLines) {
                MatchIndexDTO matchIndexDTO = expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                if (commandLine.contains("source-ip")) {
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);

                    if (CollectionUtils.isNotEmpty(cmdList)) {
                        String newEchoCmd = cmdList.get(cmdList.size() - 1);
                        log.info("h3v5 获取到当前的策略id整个回显是{}", newEchoCmd);
                        if (StringUtils.isBlank(newEchoCmd)) {
                            log.error("h3v5查询自己的id，未查询到策略ID,命令行{}，回显", commandLine);
                            expect.send(" \n");
                            return;
                        } else {
                            expect.send(" \n");
                            if (newEchoCmd.contains("rule-") && newEchoCmd.contains("]")) {
                                //todo 觉的这样写不靠谱，最好能自己查到自己id，先暂时针对市场处理下
                                String substring = newEchoCmd.substring(newEchoCmd.lastIndexOf("rule-") + 5, newEchoCmd.lastIndexOf("]")).trim();
                                log.info("h3v5 获取到当前的策略id是{}", substring);
                                if (StringUtils.isNumeric(substring)) {
                                    ruleId = substring;
                                } else {
                                    log.error("h3v5查到自己的id不是正常数字");

                                }
                            } else {
                                log.error("TopSec010回滚，正则未匹配查询到回显ID，{}", commandLine);
                            }

                        }
                    } else {
                        expect.send(" \n");
                    }

                } else if (StringUtils.isEmpty(relatedRule) && commandLine.contains("Display interzone-policy")) {
                    matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    int index = matchIndexDTO.getMatchIndex();
                    List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                    if (CollectionUtils.isNotEmpty(cmdList)) {
                        String newEchoCmd = cmdList.get(cmdList.size() - 1);
                        log.info("h3v5 获取到设备同域的策略id整个回显是{}", newEchoCmd);

                        if (StringUtils.isBlank(newEchoCmd)) {
                            log.error("h3v5查询同域下策略id，未查询到策略ID,命令行{}，回显", commandLine);
                            expect.send(" \n");
                            return;
                        } else {
                            String[] cmdArray = newEchoCmd.split(SendCommandStaticAndConstants.LINE_BREAK);
                            Pattern pattern = SendCommandStaticAndConstants.H3C_V5_RGE;
                            for (int i = 1; i < cmdArray.length; i++) {
                                //从第二行开始算,第一行是发送命令，有可能重复
                                String ruleCmd = cmdArray[i];
                                Matcher matcher = pattern.matcher(ruleCmd);
                                log.info("h3v5 获取到设备同域的策略id回显命令是{}", ruleCmd);
                                if (matcher.find()) {
                                    ruleIdForDevice = matcher.group("id");
                                    log.info("h3v5 获取到设备同域的策略id回显是{}", ruleIdForDevice);
                                    break;
                                }
                            }
                            List<JSONObject> jsonObjectList = globAndRegexElementDTO.getLinuxPromptGlobEx();
                            if (index == jsonObjectList.size() - 1) {
                                expect.send("q" + SendCommandStaticAndConstants.LINE_BREAK);
                                log.info("退出时发送q匹配");
                            } else {
                                expect.send(" \n");
                            }
                        }
                    } else {
                        expect.send(" \n");
                    }

                } else {
                    ruleIdForDevice = relatedRule;

                }

                if (StringUtils.isNotBlank(ruleIdForDevice) && StringUtils.isNumeric(ruleIdForDevice)) {
                    String moveCommandline = H3C_V5_MOVE.replace("#1", ruleId).replace("#2", ruleIdForDevice);
                    expectClientInExecuteService.sendAndCheckMatch(moveCommandline, globAndRegexElementDTO);
                    ruleIdForDevice = "";
                }
            }
            //最后一条命令补上
            defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
        } else {
            particularDTO.setIsExecute(false);
        }

    }
}
