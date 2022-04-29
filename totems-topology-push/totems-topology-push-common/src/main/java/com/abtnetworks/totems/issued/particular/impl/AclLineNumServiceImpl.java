package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author lifei
 * @desc acl命令行号获取逻辑
 * @date 2021/11/10 16:54
 */
@Service
@Log4j2
public class AclLineNumServiceImpl implements SendParticularPolicyService {
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        log.info("开始acl命令行号获取特殊处理----");
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        String randomKey = pushCmdDTO.getRandomKey();
        String[] commandLines = pushCmdDTO.getCommandline().split(SendCommandStaticAndConstants.LINE_BREAK);

        // 定义show 策略的命令
        String ruleListName = pushCmdDTO.getRecommendTask2IssuedDTO().getRuleListName();
        String matchRuleId = pushCmdDTO.getRecommendTask2IssuedDTO().getMatchRuleId();
        log.info("获取acl命令行号，策略:{}{}匹配到默认策略", pushCmdDTO.getPolicyFlag(), StringUtils.isBlank(matchRuleId) ? "有" : "没有");
        boolean defaultPolicy = StringUtils.isBlank(matchRuleId) ? true : false;

        // 1.如果ruleListName为空，证明是没有找到策略集，需要新建策略集 然后挂载到接口上
        if (StringUtils.isBlank(ruleListName)) {
            Integer initRule = 10;
            for (String commandLine : commandLines) {
                if (commandLine.startsWith(CommonConstants.POLICY_ID)) {
                    commandLine = commandLine.replace(CommonConstants.POLICY_ID, initRule.toString());
                    initRule += 10;
                    expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                } else {
                    expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                }
            }
        } else if (!defaultPolicy) {
            // 2.如果是非默认策略，则证明匹配到了deny策略，按照逻辑这个时候应该是当前这条deny策略-10
            Integer matchRuleIdInt = Integer.valueOf(matchRuleId);
            Integer count = 0;
            for (String commandLine : commandLines) {
                if (commandLine.startsWith(CommonConstants.POLICY_ID)) {
                    count++;
                }
            }

            log.info("当前要下发的策略条数为:{},匹配到的策略id为:{}", count, matchRuleId);
            boolean compareUsableCount = StringUtils.isNotBlank(matchRuleId) ? Integer.valueOf(matchRuleId) / 10 < count : false;

            if (compareUsableCount) {
                throw new IssuedExecutorException(SendErrorEnum.NO_USABLE_RULE_ID);
            } else {
                // 去墙上show 当前策略集下面的所有的策略集策略ids
                List<Integer> ruleIds = getPolicyIdsByShow(globAndRegexElementDTO, expect, randomKey, ruleListName);
                if (CollectionUtils.isEmpty(ruleIds)) {
                    log.info("根据策略集名称:{},show出来的策略id为空，跳过acl命令行的特殊处理", ruleListName);
                    return;
                }
                SendCommandStaticAndConstants.echoCmdMap.remove(randomKey);

                // 根据匹配到的deny策略截取policyId
                List<Integer> existPolicyIds = ruleIds.subList(0, ruleIds.indexOf(Integer.valueOf(matchRuleId)));

                List<Integer> usableIds = new ArrayList<>();
                for (int i = 0; i < existPolicyIds.size(); i++) {
                    Integer item = matchRuleIdInt - 10;
                    if (item <= 0){
                        break;
                    }
                    if(!existPolicyIds.contains(item)){
                        usableIds.add(item);
                    }
                    matchRuleIdInt = item;
                }
                log.info("acl命令行特殊处理，当前要生成的策略条数为:{}匹配到的deny策略id是:{}过滤掉墙上" +
                        "已经存在的id之后的可用id为:{}", count,matchRuleId, usableIds);
                if (usableIds.size() < count) {
                    throw new IssuedExecutorException(SendErrorEnum.NO_USABLE_RULE_ID);
                }

                Integer ruleIndex = 0;
                for (String commandLine : commandLines) {
                    if (commandLine.startsWith(CommonConstants.POLICY_ID)) {
                        commandLine = commandLine.replace(CommonConstants.POLICY_ID, usableIds.get(ruleIndex).toString());
                        expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                        ruleIndex++;
                    } else {
                        expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                    }
                }
            }
        } else {
            // 3.如果是默认策略，按照逻辑这个时候应该是取当前策略集最后面一条策略的id 依次ruleId + 10
            List<Integer> ruleIds = getPolicyIdsByShow(globAndRegexElementDTO, expect, randomKey, ruleListName);
            if (CollectionUtils.isEmpty(ruleIds)) {
                log.info("根据策略集名称:{},show出来的策略id为空，跳过acl命令行的特殊处理", ruleListName);
                return;
            }
            SendCommandStaticAndConstants.echoCmdMap.remove(randomKey);

            Integer maxRuleId = ruleIds.get(ruleIds.size() - 1);

            for (String commandLine : commandLines) {
                if (commandLine.startsWith(CommonConstants.POLICY_ID)) {
                    Integer item = maxRuleId + 10;
                    commandLine = commandLine.replace(CommonConstants.POLICY_ID, item.toString());
                    maxRuleId = item;
                    expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                } else {
                    expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
                }
            }
        }

        //最后一条命令补上
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);


    }

    /**
     * 根据策略集名称show 策略集下面所有的策略集ids
     * @param globAndRegexElementDTO
     * @param expect
     * @param randomKey
     * @param ruleListName
     * @return
     * @throws Exception
     */
    private List<Integer> getPolicyIdsByShow(GlobAndRegexElementDTO globAndRegexElementDTO, Expect4j expect, String randomKey, String ruleListName) throws Exception {
        String showCommandLine = CommonConstants.SHOW_POLICY_ACCESSNAME + " " + ruleListName;
        expectClientInExecuteService.sendAndCheckMatch(showCommandLine, globAndRegexElementDTO);
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);

        List<Integer> ruleIds = new ArrayList<>();

        List<String> cmdList = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
        if (CollectionUtils.isNotEmpty(cmdList)) {
            expect.send(SendCommandStaticAndConstants.LINE_BREAK);
            String newEchoCmd = cmdList.get(cmdList.size() - 1);
            log.info("acl命令行号获取，根据命令行:{} 查询到策略ID回显为:{}", showCommandLine, newEchoCmd);
            if (StringUtils.isBlank(newEchoCmd)) {
                return null;
            } else {
                Matcher matcherPolicy = SendCommandStaticAndConstants.ACL_COMMANDLINE_MATCH.matcher(newEchoCmd);
                // 对于bothNat，有两个id需要去匹配
                while (matcherPolicy.find()) {
                    String ruleId = matcherPolicy.group("id");
                    if (StringUtils.isNotBlank(ruleId)) {
                        ruleIds.add(Integer.valueOf(ruleId));
                    }
                }
                Collections.sort(ruleIds);
                if (ruleIds.size() == 0) {
                    log.error("acl命令行号获取，根据正则:{},未查询到回显，", showCommandLine);
                    return null;
                }
            }
        } else {
            log.error("acl命令行号获取，根据命令行:{} ,未查询到回显，", showCommandLine);
            return null;
        }
        return ruleIds;
    }

    public static void main(String[] args) {
//        String aa = "Extended IP access list FOTIC-MaYiJinFu\n" +
//                "     10   permit ip host 10.7.68.124 host 10.7.8.169\n" +
//                "      20  permit ip host 10.7.68.125 host 10.7.8.169\n" +
//                "     30   deny ip host 11.11.11.1 host 2.2.2.2";
//        Matcher matcher = Pattern.compile("(?<id>\\d*)\\s+(permit|deny)").matcher(aa);
//        // 对于bothNat，有两个id需要去匹配
//        List<String> ruleIds = new ArrayList<>();
//        while (matcher.find()) {
//            String ruleId = matcher.group("id");
//            ruleIds.add(ruleId);
//        }
//        System.out.println(ruleIds);

//        Integer matchRuleIdInt = Integer.valueOf(matchRuleId);
//        Integer count = 0;
//        for (String commandLine : commandLines) {
//            if (commandLine.startsWith(CommonConstants.POLICY_ID)) {
//                count++;
//            }
//        }
        List<Integer> ruleIds = new ArrayList<>();
        ruleIds.add(30);
        ruleIds.add(40);
        ruleIds.add(50);
        ruleIds.add(60);
        ruleIds.add(70);
        Integer matchRuleId = 60;
        // 根据匹配到的deny策略截取policyId
        List<Integer>  existPolicyIds = ruleIds.subList(0,ruleIds.indexOf(matchRuleId));
        System.out.println(existPolicyIds);
    }
}
