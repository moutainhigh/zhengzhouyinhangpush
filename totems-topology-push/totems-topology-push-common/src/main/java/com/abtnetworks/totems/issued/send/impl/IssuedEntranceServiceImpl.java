package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.common.utils.AliStringUtils;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.issued.annotation.ProxyEnable;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.GlobAndRegexElementDTO;
import com.abtnetworks.totems.issued.dto.PushCommandRegularParamDTO;
import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.abtnetworks.totems.issued.exception.IssuedExecutorException;
import com.abtnetworks.totems.issued.send.ClientExecuteBeforeService;
import com.abtnetworks.totems.issued.send.IssuedEntranceService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.JSchException;
import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.oro.text.regex.MalformedPatternException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/6/30
 */
@Service
public class IssuedEntranceServiceImpl implements IssuedEntranceService {
    /***
     * 日志
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(IssuedEntranceServiceImpl.class);
    /***
     * 现在的下发服务
     */
    @Resource
    ClientExecuteBeforeService expectClientExecute;
    @ProxyEnable
    @Override
    public PushResultDTO routeNewExecuteByRegular(PushCmdDTO pushCmdDTO, PushCommandRegularParamDTO pushCommandRegularParamDTO) {
        //现在的下发代码
        String charset = StringUtils.isEmpty(pushCmdDTO.getCharset()) ? Charset.defaultCharset().name() : pushCmdDTO.getCharset();
        //生成随机数key作为下发回显的生命周期扩大应用在整个下发流程的
        String randomKey = IdGen.getRandomNumberString(8);
        GlobAndRegexElementDTO linuxPromptRegEx = pushCommandRegularParamDTO.getLinuxPromptRegEx();
        LOGGER.info("开始现在的{}下发执行型号{},正则入参{}  , 字符编码{}", pushCmdDTO.getExecutorType(), pushCmdDTO.getDeviceModelNumberEnum().getKey(), JSONObject.toJSONString(linuxPromptRegEx), charset);
        PushResultDTO pushResultDTO = new PushResultDTO();
        pushResultDTO.setResult(ReturnCode.POLICY_MSG_OK);
        StringBuffer buffer = new StringBuffer();
        Expect4j expect4j = null;
        try {
            List<String> echoCmd = new ArrayList<>();
            Closure closure = (ExpectState expectState) -> {
                String expectBuffer = expectState.getBuffer();
                LOGGER.info("下发命令行回显{}", expectBuffer);
                List<String> list = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
                if (CollectionUtils.isNotEmpty(list)) {
                    list.add(expectBuffer);
                } else {
                    // 如果该key回显内容被清除，这个地方需要清除对应的list
                    echoCmd.clear();
                    echoCmd.add(expectBuffer);
                    SendCommandStaticAndConstants.echoCmdMap.put(randomKey, echoCmd);
                }
            };
            RemoteConnectUserDTO remoteConnectUserDTO = new RemoteConnectUserDTO();
            BeanUtils.copyProperties(pushCmdDTO, remoteConnectUserDTO);
            remoteConnectUserDTO.setTimeOut(pushCommandRegularParamDTO.getTimeOut());
            remoteConnectUserDTO.setInterval(pushCommandRegularParamDTO.getIntervalTime());
            linuxPromptRegEx.setClosure(closure);
            pushCmdDTO.setRandomKey(randomKey);
            pushCmdDTO.setInterval(pushCommandRegularParamDTO.getIntervalTime());
            expect4j = expectClientExecute.expectClientExecute(pushCmdDTO, remoteConnectUserDTO, closure,linuxPromptRegEx);

        } catch (JSchException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.JSH_TELNET_EXCEPTION);
            LOGGER.error("{}创建连接异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (InvalidTelnetOptionException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.JSH_TELNET_EXCEPTION);
            LOGGER.error("{}telnet连接io时异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (IOException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.UN_FILL_REG);
            LOGGER.error("{}匹配输入端命令异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (MalformedPatternException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.MATCH_CMD_ERROR);
            LOGGER.error("{}匹配下发命令异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (IssuedExecutorException issuedExecutorException) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(issuedExecutorException.getSendErrorEnum());
        } catch (Exception e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.SYSTEM_ERROR);
            LOGGER.error("{}匹配下发命令异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } finally {
            List<String> echoCmd = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
            if (CollectionUtils.isNotEmpty(echoCmd)) {
                for (String command : echoCmd) {
                    byte[] b = command.getBytes();
                    String cmd = StringUtils.toEncodedString(b, Charset.forName("UTF-8"));
                    buffer.append(cmd);
                }
            }
            if (AliStringUtils.isEmpty(buffer.toString())) {
                pushResultDTO.setCmdEcho(AliStringUtils.isEmpty(pushCmdDTO.getBeforeCmdEcho()) ? "" : pushCmdDTO.getBeforeCmdEcho());
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append(AliStringUtils.isEmpty(pushCmdDTO.getBeforeCmdEcho()) ? "" : pushCmdDTO.getBeforeCmdEcho()).append(SendCommandStaticAndConstants.LINE_BREAK);
                sb.append(buffer.toString());
                pushCmdDTO.setBeforeCmdEcho(sb.toString());
                pushResultDTO.setCmdEcho(sb.toString());
            }
            SendCommandStaticAndConstants.echoCmdMap.remove(randomKey);
            if (expect4j != null) {
                expect4j.close();


            }
        }
        LOGGER.info("现在的下发执行END,返回参数");
        return pushResultDTO;
    }

    @Override
    public PushResultDTO routeNewExecuteByRegular(PushCmdDTO pushCmdDTO, PushCommandRegularParamDTO pushCommandRegularParamDTO, String key) {
        String charset = StringUtils.isEmpty(pushCmdDTO.getCharset()) ? Charset.defaultCharset().name() : pushCmdDTO.getCharset();
        //生成随机数key作为下发回显的生命周期扩大应用在整个下发流程的
        String randomKey = key;
        GlobAndRegexElementDTO linuxPromptRegEx = pushCommandRegularParamDTO.getLinuxPromptRegEx();
        LOGGER.info("开始现在的{}下发执行型号{},正则入参{}  , 字符编码{}", pushCmdDTO.getExecutorType(), pushCmdDTO.getDeviceModelNumberEnum().getKey(), JSONObject.toJSONString(linuxPromptRegEx), charset);
        PushResultDTO pushResultDTO = new PushResultDTO();
        pushResultDTO.setResult(ReturnCode.POLICY_MSG_OK);
        StringBuffer buffer = new StringBuffer();
        Expect4j expect4j = null;
        try {
            List<String> echoCmd = new ArrayList<>();
            Closure closure = (ExpectState expectState) -> {
                String expectBuffer = expectState.getBuffer();
                LOGGER.info("下发命令行回显{}", expectBuffer);
                echoCmd.add(expectBuffer);
                SendCommandStaticAndConstants.echoCmdMap.put(randomKey, echoCmd);
            };
            RemoteConnectUserDTO remoteConnectUserDTO = new RemoteConnectUserDTO();
            BeanUtils.copyProperties(pushCmdDTO, remoteConnectUserDTO);
            remoteConnectUserDTO.setTimeOut(pushCommandRegularParamDTO.getTimeOut());
            remoteConnectUserDTO.setInterval(pushCommandRegularParamDTO.getIntervalTime());
            linuxPromptRegEx.setClosure(closure);
            pushCmdDTO.setRandomKey(randomKey);
            expect4j = expectClientExecute.expectClientExecute(pushCmdDTO, remoteConnectUserDTO, closure,linuxPromptRegEx);

        } catch (JSchException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.JSH_TELNET_EXCEPTION);
            LOGGER.error("{}创建连接异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (InvalidTelnetOptionException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.JSH_TELNET_EXCEPTION);
            LOGGER.error("{}telnet连接io时异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (IOException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.UN_FILL_REG);
            LOGGER.error("{}匹配输入端命令异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (MalformedPatternException e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.MATCH_CMD_ERROR);
            LOGGER.error("{}匹配下发命令异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } catch (IssuedExecutorException issuedExecutorException) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(issuedExecutorException.getSendErrorEnum());
        } catch (Exception e) {
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
            pushResultDTO.setSendErrorEnum(SendErrorEnum.SYSTEM_ERROR);
            LOGGER.error("{}匹配下发命令异常", pushCmdDTO.getDeviceModelNumberEnum().getKey(), e);
        } finally {
            List<String> echoCmd = SendCommandStaticAndConstants.echoCmdMap.get(randomKey);
            if (CollectionUtils.isNotEmpty(echoCmd)) {
                for (String command : echoCmd) {
                    byte[] b = command.getBytes();
                    String cmd = StringUtils.toEncodedString(b, Charset.forName("UTF-8"));
                    buffer.append(cmd);
                }
            }
            if (AliStringUtils.isEmpty(buffer.toString())) {
                pushResultDTO.setCmdEcho(AliStringUtils.isEmpty(pushCmdDTO.getBeforeCmdEcho()) ? "" : pushCmdDTO.getBeforeCmdEcho());
            } else {
                StringBuffer sb = new StringBuffer();
                sb.append(AliStringUtils.isEmpty(pushCmdDTO.getBeforeCmdEcho()) ? "" : pushCmdDTO.getBeforeCmdEcho()).append(SendCommandStaticAndConstants.LINE_BREAK);
                sb.append(buffer.toString());
                pushCmdDTO.setBeforeCmdEcho(sb.toString());
                pushResultDTO.setCmdEcho(sb.toString());
            }
            SendCommandStaticAndConstants.echoCmdMap.remove(randomKey);
            if (expect4j != null) {
                expect4j.close();
            }
        }
        LOGGER.info("现在的下发执行END,返回参数");
        return pushResultDTO;
    }
}
