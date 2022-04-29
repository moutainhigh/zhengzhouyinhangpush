package com.abtnetworks.totems.issued.aspect;

import com.abtnetworks.totems.common.constants.CommonConstants;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.DeviceSpecialDealEnum;
import com.abtnetworks.totems.common.utils.NameUtils;
import com.abtnetworks.totems.issued.annotation.InExecuteBuilder;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.MatchIndexDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.particular.impl.*;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.issued.send.SensitiveWordCommonService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Expect4j;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zakyoung
 * @Title:
 * @Description: 这里处理下发执行中的一些切面
 * @date 2020-03-20
 */
@Aspect
@Component
@Order(2)
@Log4j2
public class IssuedExecuteDisposeAspect {
    /***日志打印*/
    private final Logger LOGGER = LoggerFactory.getLogger(IssuedExecuteDisposeAspect.class);
    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Resource
    SensitiveWordCommonService sensitiveWordCommonService;

    @Resource
    Map<String, SendParticularPolicyService> sendMovePolicyServiceMap;

    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.UpdateCommandSend)")
    public void operationInCommandPoint() {
        LOGGER.debug("执行中对命令行的切点标记方法");
    }

    @Around(value = "operationInCommandPoint()")
    public Object operationCommandInExecute(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("开始命令行特殊处理-----");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Object[] objects = joinPoint.getArgs();
        ParticularDTO particularDTO = paramGenerate(objects);
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        DeviceModelNumberEnum modelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        boolean isRevert = pushCmdDTO.getRevert() == null ? false : pushCmdDTO.getRevert();
        switch (modelNumberEnum) {
            case H3CV7_OP:
                //需要查到
                String serviceName1 = NameUtils.getServiceDefaultName(PolicySetByZoneForH3cOp.class);
                SendParticularPolicyService sendMovePolicyService1 = sendMovePolicyServiceMap.get(serviceName1);
                sendMovePolicyService1.deviceParticularByRule(particularDTO);
                return null;
            case TOPSEC_TOS_005:
            case TOPSEC_TOS_010_020:
            case TOPSEC_NG:
            case TOPSEC_NG2:
            case TOPSEC_NG3:
            case TOPSEC_NG4:
                //需要查到
                if (isRevert) {
                    String serviceName2 = NameUtils.getServiceDefaultName(TopSec010RollbackServiceImpl.class);
                    SendParticularPolicyService sendMovePolicyService2 = sendMovePolicyServiceMap.get(serviceName2);
                    sendMovePolicyService2.deviceParticularByRule(particularDTO);
                    return null;
                }
                break;
            case FORTINET:
            case FORTINET_V5_2:
                if(isRevert){
                    String fortRollbackServiceName = NameUtils.getServiceDefaultName(FortRollbackServiceImpl.class);
                    SendParticularPolicyService fortRollbackService = sendMovePolicyServiceMap.get(fortRollbackServiceName);
                    fortRollbackService.deviceParticularByRule(particularDTO);
                    return null;
                }
                break;
            case H3CV7_ZONE_PAIR_ACL:
                //需要查到
                String serviceName4 = NameUtils.getServiceDefaultName(H3V7ZonePairAclServiceImpl.class);
                SendParticularPolicyService sendMovePolicyService4 = sendMovePolicyServiceMap.get(serviceName4);
                sendMovePolicyService4.deviceParticularByRule(particularDTO);
                return null;
            case CISCO_NX_OS:
            case CISCO_IOS:
                //需要查到
                if(!isRevert){
                    String queryAclLineServiceStr = NameUtils.getServiceDefaultName(AclLineNumServiceImpl.class);
                    SendParticularPolicyService queryAclLineService = sendMovePolicyServiceMap.get(queryAclLineServiceStr);
                    queryAclLineService.deviceParticularByRule(particularDTO);
                    return null;
                }
                break;
            default:
                break;

        }
        return joinPoint.proceed();
    }

    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.InExecuteBuilder)")
    public void operationInExecutePoint() {
        LOGGER.debug("执行中的切点标记方法");
    }

    @Before(value = "operationInExecutePoint()")
    public void operationInExecute(JoinPoint joinPoint) throws Exception {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        InExecuteBuilder inExecuteBuilder = signature.getMethod().getDeclaredAnnotation(InExecuteBuilder.class);
        Object[] objects = joinPoint.getArgs();
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[1];
        GlobAndRegexElementDTO globAndRegexElementDTO = (GlobAndRegexElementDTO) objects[0];
        ParticularDTO particularDTO = paramGenerate(objects);
        DeviceModelNumberEnum[] deviceModelNumberEnum = inExecuteBuilder.modelValue();
        DeviceModelNumberEnum modelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        Pattern pattern = Pattern.compile(SendCommandStaticAndConstants.CISCO_REG, Pattern.CASE_INSENSITIVE);
        boolean boolPattern = pattern.matcher(modelNumberEnum.getKey()).find();
        if (DeviceModelNumberEnum.CISCO.equals(deviceModelNumberEnum[0]) && boolPattern) {
            //对思科telnet的特殊处理
            telnetCISCOBuilder(globAndRegexElementDTO, pushCmdDTO);
        }
        switch (modelNumberEnum) {
            case CHECK_POINT:
                //checkpoint中处理登录
                String serviceName = NameUtils.getServiceDefaultName(CheckPointPolicyServiceImpl.class);
                SendParticularPolicyService sendMovePolicyService = sendMovePolicyServiceMap.get(serviceName);
                sendMovePolicyService.deviceParticularByRule(particularDTO);
                break;
            case HILLSTONE:
                //山石封禁ip的特例
                String serviceNameHill = NameUtils.getServiceDefaultName(HillStoneForbidIpServiceImpl.class);
                SendParticularPolicyService sendMovePolicyHillService = sendMovePolicyServiceMap.get(serviceNameHill);
                sendMovePolicyHillService.deviceParticularByRule(particularDTO);
            default:
                break;
        }

    }


    /**
     * 对思科telnet的特殊处理
     *
     * @param globAndRegexElementDTO
     * @param pushCmdDTO
     * @throws Exception
     */
    private void telnetCISCOBuilder(GlobAndRegexElementDTO globAndRegexElementDTO, PushCmdDTO pushCmdDTO) throws Exception {


        if (SendCommandStaticAndConstants.TELNET_TYPE.equals(pushCmdDTO.getExecutorType())) {
            Expect4j expect = globAndRegexElementDTO.getExpect4j();
            MatchIndexDTO matchIndexDTO = defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
            int matchIndex = matchIndexDTO.getMatchIndex();
            List<JSONObject> jsonObjectGlobList = globAndRegexElementDTO.getLinuxPromptGlobEx();
            List<JSONObject> jsonObjectRegList = globAndRegexElementDTO.getLinuxPromptRegEx();
            String keyMatch;
            if (CollectionUtils.isNotEmpty(jsonObjectGlobList) && 0 <= matchIndex && matchIndex < jsonObjectGlobList.size()) {
                JSONObject jsonObject = jsonObjectGlobList.get(matchIndex);
                keyMatch = jsonObject.getString(SendCommandStaticAndConstants.KEY);
            } else {
                JSONObject jsonObject = jsonObjectRegList.get(matchIndex);
                keyMatch = jsonObject.getString(SendCommandStaticAndConstants.KEY);
            }
            if (sensitiveWordCommonService.checkSensitiveWord(keyMatch)) {
                expect.send(pushCmdDTO.getPassword() + SendCommandStaticAndConstants.LINE_BREAK);

                LOGGER.info("思科telnet只在唯一默认用户时登录");
            } else {
                expect.send(pushCmdDTO.getUsername() + SendCommandStaticAndConstants.LINE_BREAK);
                expectClientInExecuteService.sendAndCheckMatch(pushCmdDTO.getPassword(), globAndRegexElementDTO);
                LOGGER.info("思科telnet存在其他用户时登录,状态");

            }

        }
    }

    @Pointcut("@annotation(com.abtnetworks.totems.issued.annotation.InExecuteMove)")
    public void operationAfterSendPoint() {
        LOGGER.debug("执行移动赋能中的切点标记方法");
    }


    @Around("operationAfterSendPoint()")
    public Object handleMove(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] objects = joinPoint.getArgs();
        ParticularDTO particularDTO = paramGenerate(objects);
        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();
        DeviceModelNumberEnum deviceModelNumberEnum1Push = pushCmdDTO.getDeviceModelNumberEnum();

        boolean isRevert = pushCmdDTO.getRevert() == null ? false : pushCmdDTO.getRevert();
        boolean isEditPolicy = pushCmdDTO.getRecommendTask2IssuedDTO().isMergePolicy();
        // 如果是编辑策略则直接跳出，不走移动交互的逻辑
        if (isEditPolicy) {
            return joinPoint.proceed();
        }
        //飞塔
        boolean isFort = DeviceModelNumberEnum.FORTINET.equals(deviceModelNumberEnum1Push) ||
                DeviceModelNumberEnum.FORTINET_V5.equals(deviceModelNumberEnum1Push) ||
                DeviceModelNumberEnum.FORTINET_V5_2.equals(deviceModelNumberEnum1Push);

        // 1.判断当前设备支不支持下发时移动交互
        boolean hasSpecialMove = DeviceSpecialDealEnum.hasSpecialMove(deviceModelNumberEnum1Push.getKey());
        if (!hasSpecialMove) {
            return joinPoint.proceed();
        }

        Integer taskType = pushCmdDTO.getTaskType();
        // 2. 判断当前任务类型是否不支持移动
        if (judgeTaskTypeCannotMove(isFort, taskType)) {
            return joinPoint.proceed();
        }
        // 3. 判断是否是回滚，回滚是没有特殊移动的流程的
        if (isRevert) {
            return joinPoint.proceed();
        }

        // 4.获取特殊处理枚举类中特殊移动的实现类
        DeviceSpecialDealEnum deviceSpecialDealEnum = DeviceSpecialDealEnum.getDevice(deviceModelNumberEnum1Push.getKey());
        if (null == deviceSpecialDealEnum.getSpecialMoveClass()) {
            return joinPoint.proceed();
        }
        SendParticularPolicyService sendMovePolicyService = sendMovePolicyServiceMap.get(NameUtils.getServiceDefaultName(deviceSpecialDealEnum.getSpecialMoveClass()));
        sendMovePolicyService.deviceParticularByRule(particularDTO);
        // 5.对于各个具体设备移动实现，不满足执行特殊移动退出的情况，直接走后面的流程
        if (!particularDTO.getIsExecute()) {
            return joinPoint.proceed();
        }
        return null;
    }

    /**
     * 判断任务类型是否能够移动
     * @param isFortinet
     * @param taskType
     * @return
     */
    private boolean judgeTaskTypeCannotMove(boolean isFortinet, Integer taskType) {
        if (taskType != null) {
            // KSH-5180  非安全策略的，不做移动操作
            boolean unableMove = taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_NAT ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_SRC_NAT ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_CREATE_DST_NAT ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_BOTH_NAT ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DENY ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_PERMIT ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_DELETE ||
                    taskType == PolicyConstants.POLICY_OPTIMIZE ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_POLICY_CHECK ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_DNAT ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_F5_BOTH_NAT ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_TASK_TYPE_MANUAL_STATIC_ROUTING ||
                    taskType == PolicyConstants.ADDRESS_MANAGE ||
                    taskType == PolicyConstants.POLICY_INT_PUSH_CONVERGENCE;
            // 这里除开飞塔设备(源/目的nat也需要移动)，其他设备以上任务状态都不支持和墙上交互移动
            if (unableMove && !isFortinet) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 公共组装参数
     *
     * @param objects
     * @return
     */
    private ParticularDTO paramGenerate(Object[] objects) {
        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[1];
        GlobAndRegexElementDTO globAndRegexElementDTO = (GlobAndRegexElementDTO) objects[0];
        //要让closure中收到信息，需要做一次匹配
        ParticularDTO particularDTO = new ParticularDTO();
        particularDTO.setPushCmdDTO(pushCmdDTO);
        particularDTO.setGlobAndRegexElementDTO(globAndRegexElementDTO);
        return particularDTO;
    }

    /**
     * 提供给<link sendAndCheckMatch()/>
     * @param objects
     * @return
     */
//    private ParticularDTO paramGenerateForUpdateCmd(Object[] objects){
//        Expect4j expect = (Expect4j) objects[0];
//        String  strCmd = (String) objects[1];
//        List<Match> lstPattern = (List<Match>) objects[2];
//        AtomicInteger recordSizeForError = (AtomicInteger) objects[3];
//        List<String> linuxPromptRegEx = (List<String>) objects[4];
//        PushCmdDTO pushCmdDTO = (PushCmdDTO) objects[5];
//        //要让closure中收到信息，需要做一次匹配
//
//        ParticularDTO particularDTO = new ParticularDTO();
//
//        particularDTO.setExpect(expect);
//        particularDTO.setLinuxPromptRegEx(linuxPromptRegEx);
//        particularDTO.setLstPattern(lstPattern);
//
//        particularDTO.setPushCmdDTO(pushCmdDTO);
//        particularDTO.setRecordSizeForError(recordSizeForError);
//        return particularDTO;
//    }

    /**
     * 移动前正常下发，同时在命令中找到要移动的策略标记
     *
     * @param pushCmdDTO
     * @param moveFlag
     * @return
     * @throws Exception
     */
    private List<String> commonIssueFindRuleName(PushCmdDTO pushCmdDTO, GlobAndRegexElementDTO globAndRegexElementDTO, String moveFlag) throws Exception {
        List<String> ruleNames = new ArrayList<>();
        String command = pushCmdDTO.getCommandline();
        String[] commandLines = command.split(SendCommandStaticAndConstants.LINE_BREAK);

        Pattern pattern = Pattern.compile(moveFlag);
        for (String strCmd : commandLines) {
            //是华三V7/华为的找到命令行中
            Matcher matcher = pattern.matcher(strCmd);
            if (matcher.find()) {
                String ruleName = StringUtils.substring(strCmd, matcher.end() + 1).trim();
                ruleNames.add(ruleName);
            }
            expectClientInExecuteService.sendAndCheckMatch(strCmd, globAndRegexElementDTO);
        }
        return ruleNames;
    }

    /**
     * 移动前正常下发，同时在命令中找到要移动的策略标记
     *
     * @param pushCmdDTO
     * @param moveFlag
     * @return
     * @throws Exception
     */
    public List<String> commonIssueFindFortId(PushCmdDTO pushCmdDTO, GlobAndRegexElementDTO globAndRegexElementDTO, String moveFlag) throws Exception {
        List<String> ruleNames = new ArrayList<>();
        String command = pushCmdDTO.getCommandline();
        String[] commandLines = command.split(SendCommandStaticAndConstants.LINE_BREAK);
        String randomKey = pushCmdDTO.getRandomKey();

        //查找关键字命令行
        Pattern pattern = Pattern.compile(moveFlag);
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

        return ruleNames;
    }

    /**
     * 天融信下发show 当前策略id做移动处理
     *
     * @param pushCmdDTO
     * @return
     * @throws Exception
     */
    public void commonIssueFindTopsecId(PushCmdDTO pushCmdDTO, GlobAndRegexElementDTO globAndRegexElementDTO) throws Exception {
        log.info("开始执行天融信特殊处理after移动---");
        Expect4j expect = globAndRegexElementDTO.getExpect4j();
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
                        log.error("TopSec010回滚，未查询到策略ID,命令行{}，回显", commandLine);
                        return;
                    } else {
                        Matcher matcherPolicy = SendCommandStaticAndConstants.TOP_SEC_010_POLICY_ID_RGE.matcher(newEchoCmd);
                        if (matcherPolicy.find()) {
                            ruleId = matcherPolicy.group("obj1");

                        } else {
                            log.error("TopSec010回滚，正则未匹配查询到回显ID，{}", commandLine);
                        }
                    }
                } else {
                    log.error("TopSec010回滚，未查询到回显，{}", commandLine);
                    return;
                }
                commandLine = commandLine.replace(CommonConstants.POLICY_ID, ruleId);
                expect.send(commandLine + SendCommandStaticAndConstants.LINE_BREAK);
            } else {
               expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
            }
        }
    }

}
