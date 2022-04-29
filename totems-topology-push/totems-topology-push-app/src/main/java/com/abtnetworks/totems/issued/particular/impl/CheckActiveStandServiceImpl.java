package com.abtnetworks.totems.issued.particular.impl;

import com.abtnetworks.totems.advanced.service.AdvancedSettingService;
import com.abtnetworks.totems.common.config.ShowStandbyCommandConfig;
import com.abtnetworks.totems.common.constants.AdvancedSettingsConstants;
import com.abtnetworks.totems.common.entity.NodeEntity;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.credential.entity.CredentialEntity;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.ParticularDTO;
import com.abtnetworks.totems.issued.particular.SendParticularPolicyService;
import com.abtnetworks.totems.issued.send.ClientInExecuteService;
import com.abtnetworks.totems.issued.send.DefineSendResultTypeService;
import com.abtnetworks.totems.issued.send.SendCommandBeforeBuilderService;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushForbidDTO;
import com.abtnetworks.totems.push.enums.EchoConstantEnum;
import com.abtnetworks.totems.push.manager.impl.PushTaskManagerImpl;
import com.abtnetworks.totems.recommend.manager.RecommendTaskManager;
import com.alibaba.fastjson.JSON;
import expect4j.Expect4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lifei
 * @desc 检查主备结果接口
 * @date 2021/3/19 16:52
 */
@Slf4j
@Service
public class CheckActiveStandServiceImpl implements SendParticularPolicyService {


    @Resource
    DefineSendResultTypeService defineSendResultTypeService;

    @Resource
    ClientInExecuteService expectClientInExecuteService;

    @Autowired
    private AdvancedSettingService advancedSettingService;

    @Autowired
    private RecommendTaskManager recommendTaskService;

    @Autowired
    private PushTaskManagerImpl pushTaskService;

    @Autowired
    private SendCommandService sendCommandService;

    @Autowired
    private SendCommandBeforeBuilderService sendCommandBuilderService;

    private static final String CISCO_FIREWALL_ACTIVE_COMMANDLINE = "primary";

    private static final String CISCO_FIREWALL_STANDBY_COMMANDLINE = "secondary";

    @Autowired
    ShowStandbyCommandConfig showStandbyCommandConfig;


    @Override
    public void deviceParticularByRule(ParticularDTO particularDTO) throws Exception {
        log.info("开始插件处理主备双活规则");
        GlobAndRegexElementDTO globAndRegexElementDTO = particularDTO.getGlobAndRegexElementDTO();
        Expect4j expect = globAndRegexElementDTO.getExpect4j();

        PushCmdDTO pushCmdDTO = particularDTO.getPushCmdDTO();

        DeviceModelNumberEnum modelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();

        String randomKey = pushCmdDTO.getRandomKey();
        String commandLine = pushCmdDTO.getQueryBeforeCommandLine();
        log.info("厂商:{} 开始发送查询主备命令，前置命令行:{}", modelNumberEnum.getKey(), commandLine);
        // 部分厂商show主备之后的前置处理
        spercialDealBeforeShow(globAndRegexElementDTO, pushCmdDTO, modelNumberEnum);

        expectClientInExecuteService.sendAndCheckMatch(commandLine, globAndRegexElementDTO);
        defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
        List<String> cmdList  = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);

        if (CollectionUtils.isNotEmpty(cmdList)) {
            log.info("cmdList:{}", JSON.toJSONString(cmdList));
            String newEchoCmd =  cmdList.get(cmdList.size()-1);
            if(!newEchoCmd.contains(commandLine)){
                // 当前还是拿不到主备回显,重新发送空格去匹配
                expect.send(" " + SendCommandStaticAndConstants.LINE_BREAK);
                // 多写这一行是因为在交互是有停顿拿不到回显
                Thread.sleep(2000);
                defineSendResultTypeService.expectMatchAndCheckResult(globAndRegexElementDTO);
                newEchoCmd =  cmdList.get(cmdList.size()-1);
            }
            log.info("查询当前设备:{}的主备双活命令:{} 回显数据为:{}", modelNumberEnum.getKey(), commandLine, newEchoCmd);
            if (StringUtils.isBlank(newEchoCmd)) {
                log.error("查询当前设备:{}的主备双活命令:{} 没有回显", modelNumberEnum.getKey(), commandLine);
                return;
            }
            switch (modelNumberEnum) {
                case USG6000:
                case USG6000_NO_TOP:
                    rePushHW(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case CISCO:
                case CISCO_ASA_86:
                case CISCO_ASA_99:
                    rePushCISCO(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case HILLSTONE:
                case HILLSTONE_V5:
                    rePushHillstone(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case TOPSEC_TOS_005:
                case TOPSEC_TOS_010_020:
                case TOPSEC_NG:
                    rePushTopsec(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case TOPSEC_NG2:
                    rePushTopsecNG2(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case DPTECHR003:
                case DPTECHR004:
                    rePushDptechr(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case SRX:
                case SRX_NoCli:
                    rePushSrx(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case SSG:
                    rePushSsg(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                case FORTINET:
                case FORTINET_V5_2:
                    rePushFortinet(pushCmdDTO, newEchoCmd.toLowerCase(), expect);
                    break;
                default:
            }
        } else {
            log.error("查询当前设备:{}的主备双活命令:{} 没有回显", modelNumberEnum.getKey(), commandLine);
        }
    }

    /**
     * 部分厂商show主备之前的前置处理
     * @param globAndRegexElementDTO
     * @param pushCmdDTO
     * @param modelNumberEnum
     * @throws Exception
     */
    private void spercialDealBeforeShow(GlobAndRegexElementDTO globAndRegexElementDTO, PushCmdDTO pushCmdDTO, DeviceModelNumberEnum modelNumberEnum) throws Exception {
        // 如果厂商是juniperSrx 则要在执行命令之前需要执行cli命令
        if (DeviceModelNumberEnum.SRX.getKey().equals(modelNumberEnum.getKey())) {
            expectClientInExecuteService.sendAndCheckMatch("cli", globAndRegexElementDTO);
        }
        if (DeviceModelNumberEnum.FORTINET.getKey().equals(modelNumberEnum.getKey()) ||
                DeviceModelNumberEnum.FORTINET_V5_2.getKey().equals(modelNumberEnum.getKey())) {
            if (null != pushCmdDTO.getIsVSys() && pushCmdDTO.getIsVSys()) {
                expectClientInExecuteService.sendAndCheckMatch("config vdom", globAndRegexElementDTO);
                expectClientInExecuteService.sendAndCheckMatch("edit " + pushCmdDTO.getVSysName(), globAndRegexElementDTO);
            } else {
                expectClientInExecuteService.sendAndCheckMatch("config vdom", globAndRegexElementDTO);
                expectClientInExecuteService.sendAndCheckMatch("edit root", globAndRegexElementDTO);
            }
        }
        if (DeviceModelNumberEnum.isRangeCiscoCode(modelNumberEnum.getCode())) {
            if (StringUtils.isNotBlank(pushCmdDTO.getEnableUsername()) && StringUtils.isNotBlank(pushCmdDTO.getEnablePassword())) {
                expectClientInExecuteService.sendAndCheckMatch(pushCmdDTO.getEnableUsername(), globAndRegexElementDTO);
                expectClientInExecuteService.sendAndCheckMatch(pushCmdDTO.getEnablePassword(), globAndRegexElementDTO);
            }
        }
    }

    /**
     * 重新下发华为命令行
     *
     * @param pushCmdDTO
     * @param cmdEcho
     */
    private void rePushHW(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.HUAWEI.getEchoSign())) {
            log.info("检测到当前华为防火墙不支持show主备命令，继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, pushCmdDTO.getPushForbidDTO());
            return;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.HUAWEI.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.HUAWEI.getPostField()));
        if (StringUtils.contains(subCmdStr, EchoConstantEnum.HUAWEI.getBackupSign())) {
            log.info("检测到当前华为防火墙为standby，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else {
            log.info("检测到当前华为防火墙为active，继续下发命令");
            continuePushCmd(pushCmdDTO, expect, pushCmdDTO.getPushForbidDTO());
        }
    }


    /**
     * 处理山石命令行
     *
     * @param pushCmdDTO
     * @param cmdEcho
     * @param expect
     */
    private void rePushHillstone(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.HILLSTONE.getEchoSign())) {
            log.info("检测到当前山石防火墙不支持show主备命令，继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, null);
            return;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.HILLSTONE.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.HILLSTONE.getPostField()));
        if (StringUtils.contains(subCmdStr, EchoConstantEnum.HILLSTONE.getBackupSign())) {
            log.info("检测到当前山石防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else {
            log.info("检测到当前山石防火墙为Master，继续下发命令");
            pushCmdDTO.setHaveQueryActive(true);
            // 清除上一次登陆信息
            continuePushCmd(pushCmdDTO, expect, null);
        }
    }

    /**
     * 处理天融信是否切换主备重新下发
     *
     * @param pushCmdDTO
     * @param cmdEcho
     * @param expect
     */
    private void rePushTopsec(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.TOPSEC.getEchoSign())) {
            log.info("检测到当前天融信防火墙不支持show主备命令，继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, null);
            return;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.TOPSEC.getPreField()));
        String[] subCmdStrs = subCmdStr.split("\n");
        // 取列表数据的第一行里面的主备标识
        String backupSign = null == subCmdStrs ? "" : subCmdStrs[1];
        if (StringUtils.contains(backupSign, EchoConstantEnum.TOPSEC.getBackupSign())) {
            log.info("检测到当前天融信防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else {
            log.info("检测到当前天融信防火墙为Master，继续下发命令");
            pushCmdDTO.setHaveQueryActive(true);
            // 清除上一次登陆信息
            continuePushCmd(pushCmdDTO, expect, null);
        }
    }

    /**
     * 处理天融信NG2是否切换主备重新下发
     *
     * @param pushCmdDTO
     * @param cmdEcho
     * @param expect
     */
    private void rePushTopsecNG2(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.TOPSECNG2.getEchoSign())) {
            log.info("检测到当前天融信防火墙不支持show主备命令，继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, null);
            return;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.TOPSECNG2.getPreField()));
        String[] subCmdStrs = subCmdStr.split("\n");
        // 取列表数据的第一行里面的主备标识
        String backupSign = null == subCmdStrs ? "" : subCmdStrs[1];
        if (StringUtils.contains(backupSign, EchoConstantEnum.TOPSECNG2.getBackupSign())) {
            log.info("检测到当前天融信NG2防火墙为STANDBY，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else {
            log.info("检测到当前天融信NG2防火墙为ACTIVE，继续下发命令");
            pushCmdDTO.setHaveQueryActive(true);
            // 清除上一次登陆信息
            continuePushCmd(pushCmdDTO, expect, null);
        }
    }

    /**
     * 处理迪普是否切换主备重新下发
     *
     * @param pushCmdDTO
     * @param cmdEcho
     * @param expect
     */
    private void rePushDptechr(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.DPTECHR.getEchoSign())) {
            log.info("检测到当前迪普防火墙不支持show主备命令，继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, null);
            return;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.DPTECHR.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.DPTECHR.getPostField()));
        if (StringUtils.contains(subCmdStr, EchoConstantEnum.DPTECHR.getBackupSign())) {
            log.info("检测到当前迪普防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else {
            log.info("检测到当前迪普防火墙为Master，继续下发命令");
            pushCmdDTO.setHaveQueryActive(true);
            // 清除上一次登陆信息
            continuePushCmd(pushCmdDTO, expect, null);
        }
    }

    /**
     * 处理Juniper-SRX是否切换主备重新下发
     *
     * @param pushCmdDTO
     * @param cmdEcho
     * @param expect
     */
    private void rePushSrx(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 在取值之前先去做有没有正确回显的判断。用回显的关键字符去判断
        if (!cmdEcho.contains(EchoConstantEnum.JUNIPER_SRX.getEchoSign())) {
            log.info("检测到当前Juniper-SRX防火墙为不支持主备切换命令,继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, null);
            return;
        }
        String backUpSign = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.JUNIPER_SRX.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.JUNIPER_SRX.getPostField()));
        if (StringUtils.contains(backUpSign, EchoConstantEnum.JUNIPER_SRX.getBackupSign())) {
            log.info("检测到当前Juniper-SRX防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else {
            log.info("检测到当前Juniper-SRX防火墙为Master，继续下发命令");
            pushCmdDTO.setHaveQueryActive(true);
            continuePushCmd(pushCmdDTO, expect, null);
        }
    }

    /**
     * 处理Juniper-SSG是否切换主备重新下发
     *
     * @param pushCmdDTO
     * @param cmdEcho
     * @param expect
     */
    private void rePushSsg(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 在取值之前先去做有没有正确回显的判断。用回显的关键字符去判断
        if (!cmdEcho.contains(EchoConstantEnum.JUNIPER_SSG.getEchoSign())) {
            log.info("检测到当前Juniper-SSG防火墙为不支持主备切换命令,继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, null);
            return;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.JUNIPER_SSG.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.JUNIPER_SSG.getPostField()));
        String[] subCmdStrs = subCmdStr.split("\n");
        // 取数据列表的第一行
        String backupSign = null == subCmdStrs ? "" : subCmdStrs[1];
        backupSign = backupSign.trim();
        backupSign = backupSign.replaceAll(" +", ",");
        String[] newMasters = backupSign.split(",");
        // 之所以取步长5是因为 要取第五列的master的值，如果是myself 则证明当前设备就是主设备，不用去切换。如果不是 则需要切换下发
        if (!StringUtils.contains(null == newMasters ? "" : newMasters[5], EchoConstantEnum.JUNIPER_SSG.getBackupSign())) {
            log.info("检测到当前Juniper-SSG防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else {
            log.info("检测到当前Juniper-SSG防火墙为Master，继续下发命令");
            pushCmdDTO.setHaveQueryActive(true);
            // 清除上一次登陆信息
            continuePushCmd(pushCmdDTO, expect, null);
        }
    }

    /**
     * 处理Fortinet是否切换主备重新下发
     *
     * @param pushCmdDTO
     * @param cmdEcho
     * @param expect
     */
    private void rePushFortinet(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 在取值之前先去做有没有正确回显的判断。用回显的关键字符去判断
        if (!cmdEcho.contains(EchoConstantEnum.FORTINET.getEchoSign())) {
            log.info("检测到当前飞塔防火墙为不支持主备切换命令,继续原流程下发命令");
            continuePushCmd(pushCmdDTO, expect, null);
            return;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.FORTINET.getPreField()));
        String[] subCmdStrs = subCmdStr.split("\n");
        // 取列表数据的第一行里面的主备标识
        String backupSign = null == subCmdStrs ? "" : subCmdStrs[0];

        if (StringUtils.contains(backupSign, EchoConstantEnum.FORTINET.getBackupSign())) {
            log.info("检测到当前飞塔防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        }else {
            log.info("检测到当前飞塔防火墙为Master，继续下发命令");
            pushCmdDTO.setHaveQueryActive(true);
            // 清除上一次登陆信息
            continuePushCmd(pushCmdDTO, expect, null);
        }
    }


    /**
     * 重新下发思科命令行
     *
     * @param pushCmdDTO
     * @param cmdEcho
     */
    private void rePushCISCO(PushCmdDTO pushCmdDTO, String cmdEcho, Expect4j expect) throws Exception{
        // 对思科的查询主备命令show failover | in host回显做特殊处理
        Pattern pattern = Pattern.compile(showStandbyCommandConfig.getCiscoMatchStandby());
        Matcher matcher = pattern.matcher(cmdEcho);

        String standbyFlag = null;
        if (matcher.find()) {
            standbyFlag = matcher.group("obj1");
        }
        if (StringUtils.isBlank(standbyFlag)) {
            log.info("检测到当前思科防火墙为active，继续下发命令：{}");
            pushCmdDTO.setHaveQueryActive(true);
            continuePushCmd(pushCmdDTO, expect, null);
        }
        // 判断命令返回为备用
        if (StringUtils.contains(standbyFlag, CISCO_FIREWALL_STANDBY_COMMANDLINE)) {
            log.info("检测到当前思科防火墙为STANDBY，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            pushCmdDTO.setHaveQueryActive(true);
            reCommandPush(pushCmdDTO, expect);
        } else if (StringUtils.contains(standbyFlag, CISCO_FIREWALL_ACTIVE_COMMANDLINE)) {
            log.info("检测到当前思科防火墙为active，继续下发命令：{}");
            pushCmdDTO.setHaveQueryActive(true);
            continuePushCmd(pushCmdDTO, expect, null);
        }

    }


    /**
     * 继续下发之前的流程
     *
     * @param pushCmdDTO
     * @param expect
     * @param o
     */
    private void continuePushCmd(PushCmdDTO pushCmdDTO, Expect4j expect, PushForbidDTO o) throws Exception {
        pushCmdDTO.setHaveQueryActive(true);
        pushCmdDTO.setIsMaster(true);
        expect.send(SendCommandStaticAndConstants.LINE_BREAK);
        // 对于一些show主备命令前面还有其他前置命令的情况，这个时候继续下发 需要在命令行里面剔除掉这些前置命令
        String commandLine = pushCmdDTO.getCommandline();
        if (DeviceModelNumberEnum.SRX.getKey().equals(pushCmdDTO.getDeviceModelNumberEnum().getKey())) {
            commandLine = commandLine.replaceFirst("cli\n", "");
        } else if (DeviceModelNumberEnum.FORTINET.getKey().equals(pushCmdDTO.getDeviceModelNumberEnum().getKey()) ||
                DeviceModelNumberEnum.FORTINET_V5_2.getKey().equals(pushCmdDTO.getDeviceModelNumberEnum().getKey())) {
            if (null != pushCmdDTO.getIsVSys() && pushCmdDTO.getIsVSys()) {
                commandLine = commandLine.replaceFirst("config vdom\n", "");
                commandLine = commandLine.replaceFirst("edit " + pushCmdDTO.getVSysName() + "\n", "");
            } else {
                commandLine = commandLine.replaceFirst("config vdom\n", "");
                commandLine = commandLine.replaceFirst("edit root\n", "");
            }
        } else if (DeviceModelNumberEnum.isRangeCiscoCode(pushCmdDTO.getDeviceModelNumberEnum().getCode())){
//            // 如果是思科的设备，但是前面show主备已经进入了enable，这个如果判断当前设备是主设备，则先退出enable，再去走原来命令行的enable
//            expect.send("disable" + SendCommandStaticAndConstants.LINE_BREAK);
            // 思科特殊情况，思科show主备之后，退出enable模式，在输入enable的时候回显要输入username和password。这种情况下直接断开连接再重连设备
            log.info("思科特殊处理，思科show主备之后 退出再重新连接");
            pushCmdDTO.setIsMaster(false);
            // 清除上一次登陆信息
            cleanLastConnect(pushCmdDTO, pushCmdDTO.getRandomKey(), pushCmdDTO.getDeviceManagerIp(), pushCmdDTO.getExecutorType(), expect);
            String originalCommandLine = sendCommandBuilderService.delSshCommand(pushCmdDTO.getDeviceModelNumberEnum(), pushCmdDTO.getCommandline(), pushCmdDTO.getEnablePassword(), o);
            pushCmdDTO.setCommandline(originalCommandLine);
            // 防止短时间之后出现多次连接导致连接不上的情况。设置默认连接间隔时间2s
            int interval = (null == pushCmdDTO.getInterval() || 0 == pushCmdDTO.getInterval()) ? 2000 : pushCmdDTO.getInterval().intValue() * 1000;
            log.info("主备切换特殊处理下发间隔时间{}", interval);
            Thread.sleep(interval);
            sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        }
        pushCmdDTO.setCommandline(commandLine);
        return;
    }

    /**
     * 提取公共的重新下发代码
     *
     * @param pushCmdDTO
     */
    private void reCommandPush(PushCmdDTO pushCmdDTO, Expect4j expect4j) throws Exception {
        String lastDeviceManagerIp = pushCmdDTO.getDeviceManagerIp();
        String lastExecutorType = pushCmdDTO.getExecutorType();
        DeviceModelNumberEnum lastDeviceModelNumberEnum = pushCmdDTO.getDeviceModelNumberEnum();
        String lastEnablePassword = pushCmdDTO.getEnablePassword();


        // 找到备ip,凭据
        String currentIp = pushCmdDTO.getDeviceManagerIp();
        List<NodeEntity> nodeList = advancedSettingService.getAnotherDeviceByIp(AdvancedSettingsConstants.PARAM_NAME_ACTIVE_STANDBY, currentIp);
        if (CollectionUtils.isNotEmpty(nodeList)) {
            for (NodeEntity node : nodeList) {
                if (node == null || node.getOrigin() == 1) {
                    log.error(String.format("设备（%s）已被删除，无法下发命令行...", pushCmdDTO.getDeviceManagerIp()));
                    return;
                }

                // 重新设置命令行
                pushCmdDTO.setDeviceManagerIp(node.getIp());
                pushCmdDTO.setDeviceName(node.getDeviceName());
                Integer port = recommendTaskService.getDeviceGatherPort(node.getUuid());
                // 下发时如果有下发凭据则使用下发凭据，无下发凭据使用采集凭据
                String credentialUuid = StringUtils.isBlank(node.getPushCredentialUuid()) ? node.getCredentialUuid() : node.getPushCredentialUuid();
                CredentialEntity entity = pushTaskService.getCredentialEntity(credentialUuid);
                if (entity == null) {
                    log.error(String.format("设备（%s）凭据为空，无法下发命令行...", pushCmdDTO.getDeviceManagerIp()));
                    return;
                }
                pushCmdDTO.setPort(port);
                pushCmdDTO.setUsername(entity.getLoginName());
                pushCmdDTO.setPassword(entity.getLoginPassword());
                pushCmdDTO.setEnableUsername(entity.getEnableUserName());
                pushCmdDTO.setEnablePassword(entity.getEnablePassword());
                pushCmdDTO.setCharset(node.getCharset());
                pushCmdDTO.setCredentialName(entity.getName());
                log.info("确认需要主备切换,清除上一次登陆信息----");
                cleanLastConnect(pushCmdDTO, pushCmdDTO.getRandomKey(), lastDeviceManagerIp, lastExecutorType, expect4j);
                log.info("确认需要主备切换,清除上一次buildComandLine----");
                String commandLine = sendCommandBuilderService.delSshCommand(lastDeviceModelNumberEnum, pushCmdDTO.getCommandline(), lastEnablePassword, null);
                pushCmdDTO.setCommandline(commandLine);
                pushCmdDTO.setIsMaster(false);
                log.info("检测到当前防火墙为standby，重新跳回到切入点 重新下发命令");
                int interval = pushCmdDTO.getInterval().intValue() * 1000;
                //例如山石特殊处理，就算保持单链接，也在一定时间内不能过频繁，再例如农信华为需要特殊处理
                log.info("主备切换特殊处理下发间隔时间{}", interval);
                Thread.sleep(interval);
                sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
            }
        }
    }

    /**
     * 清除上一次登陆信息
     *
     * @param pushCmdDTO
     * @param expect4j
     */
    private void cleanLastConnect(PushCmdDTO pushCmdDTO, String randomKey, String deviceManagerIp, String executorType, Expect4j expect4j) {
        StringBuffer buffer = new StringBuffer();
        List<String> echoCmd = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
        if (CollectionUtils.isNotEmpty(echoCmd)) {
            for (String command : echoCmd) {
                byte[] b = command.getBytes();
                String cmd = StringUtils.toEncodedString(b, Charset.forName("UTF-8"));
                buffer.append(cmd);
            }
        }
        // 保留上一次连接执行步骤
        pushCmdDTO.setBeforeCmdEcho(buffer.toString());
        SendCommandStaticAndConstants.echoCmdMap.remove(randomKey);
        if (expect4j != null) {
            expect4j.close();

        }
    }
}
