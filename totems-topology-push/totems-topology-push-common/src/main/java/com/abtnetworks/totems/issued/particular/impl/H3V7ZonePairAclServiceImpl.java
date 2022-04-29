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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @Author: WangCan
 * @Description h3v7 zonePair 命令行下发
 * @Date: 2021/6/22
 */
@Slf4j
@Service
public class H3V7ZonePairAclServiceImpl implements SendParticularPolicyService {
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);
        String zonePair = "";
        String aclType = null;
        String srcZone = "any";
        String dstZone = "any";
        boolean moveRuleToZone = false;
        for (String commandLine : commandLines) {
            if (commandLine.contains("return")) {
                if (moveRuleToZone) {
                    String zonePairCl = String.format("zone-pair security source %s destination %s \n", srcZone, dstZone);
                    expectClientInExecuteService.sendAndCheckMatch(zonePairCl, globAndRegexElementDTO);

                    StringBuffer moveRuleToZoneCl = new StringBuffer();
                    moveRuleToZoneCl.append("packet-filter ");
                    if (aclType.contains("name")) {
                        moveRuleToZoneCl.append("name ");
                    }
                    moveRuleToZoneCl.append(zonePair).append(StringUtils.LF);
                    expectClientInExecuteService.sendAndCheckMatch(moveRuleToZoneCl.toString(), globAndRegexElementDTO);
                    expectClientInExecuteService.sendAndCheckMatch("quit", globAndRegexElementDTO);
                }
            }
            if (commandLine.contains(CommonConstants.POLICY_SHOW_ZONE_PAIR)) {
                Matcher zonePolicy = SendCommandStaticAndConstants.H3CV7_COMMAND_LINE_ZONE_PAIR.matcher(commandLine);
                if (zonePolicy.find()) {
                    if (StringUtils.isNotBlank(zonePolicy.group("srcZone"))) {
                        srcZone = zonePolicy.group("srcZone");
                    }
                    if (StringUtils.isNotBlank(zonePolicy.group("dstZone"))) {
                        dstZone = zonePolicy.group("dstZone");
                    }
                }
                expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                expectClientInExecuteService.sendAndCheckMatch("", globAndRegexElementDTO);
                List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                if (CollectionUtils.isNotEmpty(cmdList)) {
                    String newEchoCmd = cmdList.get(cmdList.size() - 1);
                    if (StringUtils.isBlank(newEchoCmd)) {
                        log.error(" H3V7 查询域间策略集，未查询到,命令行{}，回显", commandLine);
                        return;
                    } else {
                        Matcher matcherPolicy = SendCommandStaticAndConstants.H3CV7_ZONE_PAIR.matcher(newEchoCmd);
                        while (matcherPolicy.find()) {
                            String ipType = matcherPolicy.group("ipType");
                            String tempZonePair = matcherPolicy.group("zonePair");
                            if (StringUtils.isNotBlank(ipType) && ipType.equalsIgnoreCase("ipv4")) {
                                if (StringUtils.isNotBlank(tempZonePair) && (!StringUtils.isNumeric(tempZonePair) || Integer.parseInt(tempZonePair) >= 3000)) {
                                    zonePair = tempZonePair;
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    log.error("H3V7 查询域间策略集，未查询到回显，{}", commandLine);
                    continue;

                }
            } else if (commandLine.contains(CommonConstants.POLICY_ACL_ADVANCED_IPV4_NAME) || commandLine.contains(CommonConstants.POLICY_ACL_ADVANCED_IPV6_NAME)) {

                if (StringUtils.isNotBlank(zonePair)) {
                    if (StringUtils.isNumeric(zonePair)) {
                        int zonePairNum = Integer.parseInt(zonePair);
                        if (zonePairNum >= 2000 && zonePairNum < 3000) {
                            aclType = " basic ";
                        } else if (zonePairNum >= 3000 && zonePairNum < 4000) {
                            aclType = " advanced ";
                        } else {
                            aclType = " advanced name ";
                        }
                    } else {
                        aclType = " advanced name ";
                    }
                } else {
                    aclType = " advanced name ";
                    zonePair = String.format("%sto%s", srcZone, dstZone);
                    moveRuleToZone = true;
                }
                if (commandLine.contains(CommonConstants.POLICY_ACL_ADVANCED_IPV4_NAME)) {
                    commandLine = String.format(commandLine, aclType, zonePair);
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    expect.send(commandLine + SendCommandStaticAndConstants.LINE_BREAK);
                } else if (commandLine.contains(CommonConstants.POLICY_ACL_ADVANCED_IPV6_NAME)) {
                    commandLine = String.format(commandLine, aclType, zonePair);
                    defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                    expect.send(commandLine + SendCommandStaticAndConstants.LINE_BREAK);
                } else {
                    expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                }
            } else {
                expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                if (commandLine.contains("save") || "y".equalsIgnoreCase(commandLine)) {
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                        log.error("线程等待异常",e);
                    }
                    expectClientInExecuteService.sendAndCheckMatch("", globAndRegexElementDTO);
                }
            }
        }
        if (moveRuleToZone) {
            expectClientInExecuteService.sendAndCheckMatch("y", globAndRegexElementDTO);
        }
        expectClientInExecuteService.sendAndCheckMatch("", globAndRegexElementDTO);
    }
}

