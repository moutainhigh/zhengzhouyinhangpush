package com.abtnetworks.totems.issued.send.impl;

import com.abtnetworks.totems.common.config.ShowStandbyCommandConfig;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.common.dto.PythonOutDTO;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.SendErrorEnum;
import com.abtnetworks.totems.issued.dto.PythonPushDTO;
import com.abtnetworks.totems.issued.dto.StandbyDeviceInfoDTO;
import com.abtnetworks.totems.issued.exception.ProxyRuntimeException;
import com.abtnetworks.totems.issued.send.IssuedEntrancePythonService;
import com.abtnetworks.totems.issued.send.SendCommandBeforeBuilderService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.enums.EchoConstantEnum;
import com.alibaba.fastjson.JSONObject;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.exec.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lifei
 * @desc python下发入口
 * @date 2022/1/21 14:58
 */
@Service
@Log4j2
public class IssuedEntrancePythonServiceImpl implements IssuedEntrancePythonService {

    private static final String CISCO_FIREWALL_ACTIVE_COMMANDLINE = "primary";

    private static final String CISCO_FIREWALL_STANDBY_COMMANDLINE = "secondary";

    @Autowired
    ShowStandbyCommandConfig showStandbyCommandConfig;

    @Autowired
    SendCommandBeforeBuilderService sendCommandBeforeBuilderService;

    @Override
    public PushResultDTO commandExecuteByPython(PushCmdDTO pushCmdDTO) {
        PushResultDTO pushResultDTO = new PushResultDTO();
        String commandLineEcho = "";
        String errMsg = null;
        int successCode = ReturnCode.POLICY_MSG_OK;

        DeviceModelNumberEnum modelNumber = pushCmdDTO.getDeviceModelNumberEnum();
        Integer interval = null == pushCmdDTO.getInterval() ? 0 : pushCmdDTO.getInterval();
        try {
            String commandLine = null;
            PythonOutDTO pythonOutDTO = null;
            String showStandbyEcho = "";
            // 获取当前设备json
            JSONObject deviceInfo = buildDevideInfoJson(pushCmdDTO, false, pushCmdDTO.getPythonPushDTO().getStandbyDeviceInfoDTO());
            // 是否需要判断主备，如果高级设置配置了就需要判断，否则不判断
            if (pushCmdDTO.getNeedJudgeStandby()) {
                commandLine = pushCmdDTO.getQueryBeforeCommandLine();

                log.debug("特殊处理下发间隔时间{}", interval);
                Thread.sleep(interval);

                pythonOutDTO = sendCommandLineToPython(pushCmdDTO, deviceInfo, commandLine);
                commandLineEcho = pythonOutDTO.getPythonStdoutOutput();
                String showStandbyErrorMsg = pythonOutDTO.getPythonStderrOutput();
                if (StringUtils.isNotBlank(showStandbyErrorMsg)) {
                    successCode = ReturnCode.PUSH_TASK_ERROR;
                    SendErrorEnum.PYTHON_PUSH_ERROR.setMessage(showStandbyErrorMsg);
                    pushResultDTO.setSendErrorEnum(SendErrorEnum.PYTHON_PUSH_ERROR);
                    pushResultDTO.setResult(successCode);
                    return pushResultDTO;
                }
                showStandbyEcho = commandLineEcho;
                // 如果高级设备配置了主备，则判断第一次请求设备回显是不是备用设备
                log.info("根据show主备命令:{}查询主备的回显为:{}", commandLine, commandLineEcho);
                boolean isStandby = isStandby(commandLineEcho, modelNumber);
                commandLine = pushCmdDTO.getCommandline();

                log.debug("特殊处理下发间隔时间{}", interval);
                Thread.sleep(interval);
                // 如果是备用设备
                if (isStandby) {
                    // 转换登陆关系
                    JSONObject convertDeviceInfo = buildDevideInfoJson(pushCmdDTO, true, pushCmdDTO.getPythonPushDTO().getStandbyDeviceInfoDTO());
                    // 替换掉上一个设备的前置命令行enabel和密码
                    commandLine = sendCommandBeforeBuilderService.delSshCommand(modelNumber, commandLine, pushCmdDTO.getEnablePassword(), null);
                    // 拼接  当前要切换设备的前置命令行
                    pushCmdDTO.setCommandline(commandLine);
                    pushCmdDTO.setEnablePassword(pushCmdDTO.getPythonPushDTO().getStandbyDeviceInfoDTO().getEnablePassword());
                    commandLine = sendCommandBeforeBuilderService.getSshBuildCommand(pushCmdDTO);

                    pythonOutDTO = sendCommandLineToPython(pushCmdDTO, convertDeviceInfo, commandLine);
                } else {
                    // 主设备，则还是拿之前的设备信息去下发实际的命令行
                    pythonOutDTO = sendCommandLineToPython(pushCmdDTO, deviceInfo, commandLine);
                }

            } else {
                log.debug("特殊处理下发间隔时间{}", interval);
                Thread.sleep(interval);
                commandLine = pushCmdDTO.getCommandline();
                pythonOutDTO = sendCommandLineToPython(pushCmdDTO, deviceInfo, commandLine);
            }
            errMsg = pythonOutDTO.getPythonStderrOutput();
            commandLineEcho = showStandbyEcho + StringUtils.LF + pythonOutDTO.getPythonStdoutOutput();
            if (StringUtils.isNotBlank(errMsg)) {
                successCode = ReturnCode.PUSH_TASK_ERROR;
                SendErrorEnum.PYTHON_PUSH_ERROR.setMessage(errMsg);
                pushResultDTO.setSendErrorEnum(SendErrorEnum.PYTHON_PUSH_ERROR);
            }
        } catch (Exception e) {
            log.error("调用python下发失败,失败原因:{}", e);
            successCode = ReturnCode.PUSH_TASK_ERROR;
            SendErrorEnum.PYTHON_PUSH_ERROR.setMessage(e.getMessage());
            pushResultDTO.setSendErrorEnum(SendErrorEnum.PYTHON_PUSH_ERROR);
            pushResultDTO.setResult(successCode);
        } finally {
            pushResultDTO.setCmdEcho(StringUtils.isNotBlank(commandLineEcho) ? commandLineEcho : "");
            pushResultDTO.setResult(successCode);
        }
        return pushResultDTO;
    }


    /**
     * 发送命令到python去下发命令行
     *
     * @param pushCmdDTO
     * @return
     * @throws Exception
     */
    public PythonOutDTO sendCommandLineToPython(PushCmdDTO pushCmdDTO, JSONObject deviceInfo, String commandLine) throws Exception {

        PythonPushDTO pushDTO = pushCmdDTO.getPythonPushDTO();

        String filePath = pushDTO.getFilePath() + pushDTO.getPythonFileName();

        JSONObject object = new JSONObject();
        object.put("commandLine", commandLine);
        object.put("isRevert", pushCmdDTO.getRevert());
        object.put("deviceInfo", deviceInfo);


        String lineSeparator = System.lineSeparator();
        //参数准备
        StringBuffer paramStr = new StringBuffer(object.toJSONString()).append(lineSeparator).append(lineSeparator);
        // 发送
        PythonOutDTO pythonOutDTO = sendToPython(paramStr.toString(), filePath);

        String stdoutOut = pythonOutDTO.getPythonStdoutOutput();
        log.info("调用python下发结果为:{}", JSONObject.toJSONString(pythonOutDTO));
        if (StringUtils.isNotBlank(stdoutOut)) {
            if (stdoutOut.contains("ECHO-START") && stdoutOut.contains("ECHO-END")) {
                String commandLineEcho = stdoutOut.substring(stdoutOut.indexOf("##ECHO-START##\n") + 1, stdoutOut.indexOf("##ECHO-END##")).substring("##ECHO-START##\n".length());
                commandLineEcho = commandLineEcho.replace("\\n", "\n");
                commandLineEcho = commandLineEcho.replace("\\r", "\r");
                pythonOutDTO.setPythonStdoutOutput(commandLineEcho);
            }
        }
        return pythonOutDTO;
    }

    /**
     * 构建设备信息
     *
     * @param pushCmdDTO
     * @return
     */
    private JSONObject buildDevideInfoJson(PushCmdDTO pushCmdDTO, boolean isStandby, StandbyDeviceInfoDTO standbyDeviceInfoDTO) {
        if (!isStandby) {
            JSONObject deviceInfo = new JSONObject();
            deviceInfo.put("deviceIp", pushCmdDTO.getDeviceManagerIp());
            deviceInfo.put("loginName", pushCmdDTO.getUsername());
            deviceInfo.put("loginPassword", pushCmdDTO.getPassword());
            deviceInfo.put("enableUserName", pushCmdDTO.getEnableUsername());
            deviceInfo.put("enablePassword", pushCmdDTO.getEnablePassword());
            deviceInfo.put("port", pushCmdDTO.getPort());
            deviceInfo.put("charset", pushCmdDTO.getCharset());
            deviceInfo.put("commType", StringUtils.isNotBlank(pushCmdDTO.getExecutorType()) ? pushCmdDTO.getExecutorType().toUpperCase() : "SSH");
            deviceInfo.put("interval", pushCmdDTO.getInterval());
            deviceInfo.put("modelNumber", pushCmdDTO.getDeviceModelNumberEnum().getKey());
            deviceInfo.put("isVSys", pushCmdDTO.getIsVSys());
            deviceInfo.put("vSysName", pushCmdDTO.getVSysName());
            return deviceInfo;
        }
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("deviceIp", standbyDeviceInfoDTO.getDeviceManagerIp());
        deviceInfo.put("loginName", standbyDeviceInfoDTO.getUsername());
        deviceInfo.put("loginPassword", standbyDeviceInfoDTO.getPassword());
        deviceInfo.put("enableUserName", standbyDeviceInfoDTO.getEnableUsername());
        deviceInfo.put("enablePassword", standbyDeviceInfoDTO.getEnablePassword());
        deviceInfo.put("port", standbyDeviceInfoDTO.getPort());
        deviceInfo.put("charset", standbyDeviceInfoDTO.getCharset());
        deviceInfo.put("commType", StringUtils.isNotBlank(standbyDeviceInfoDTO.getExecutorType()) ? standbyDeviceInfoDTO.getExecutorType().toUpperCase() : "SSH");
        deviceInfo.put("interval", standbyDeviceInfoDTO.getInterval());
        deviceInfo.put("modelNumber", standbyDeviceInfoDTO.getDeviceModelNumberEnum().getKey());
        deviceInfo.put("isVSys", standbyDeviceInfoDTO.getIsVSys());
        deviceInfo.put("vSysName", standbyDeviceInfoDTO.getVSysName());
        return deviceInfo;
    }

    /**
     * 发送命令给py
     *
     * @param paramStr
     * @param path
     * @return
     * @throws Exception
     */
    private PythonOutDTO sendToPython(String paramStr, String path) throws Exception {
        // 外部命令执行对象
        PythonOutDTO pythonOutDTO = new PythonOutDTO();
        CommandLine commandLine = new CommandLine("python");
        commandLine.addArgument(path).addArgument("-h").addArgument(paramStr);

        //默认执行结果处理程序
        DefaultExecuteResultHandler executeResultHandler = new DefaultExecuteResultHandler();
        int defaultPythonTimeout = 180;
        Integer timeout = 60;
        if (timeout != null && timeout >= 1) {
            defaultPythonTimeout = timeout;
        }

        //执行配置
        ExecuteWatchdog executeWatchdog = new ExecuteWatchdog((defaultPythonTimeout * 1000));
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        defaultExecutor.setWatchdog(executeWatchdog);


        log.info("执行python前脚本文件入参是{}", paramStr);
        byte[] paramByte = paramStr.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream input = new ByteArrayInputStream(paramByte);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        ByteArrayOutputStream err = new ByteArrayOutputStream(1024);
        PumpStreamHandler handler = new PumpStreamHandler(out, err, input);
        //获取系统环境变量
        Map<String, String> systemEnvMap = System.getenv();

        HashMap pythonMap = new HashMap(systemEnvMap);


        try {
            defaultExecutor.setStreamHandler(handler);
            log.info("正在运行的python脚本，info:{}", commandLine.toString());
            defaultExecutor.execute(commandLine, pythonMap, executeResultHandler);
            executeResultHandler.waitFor();
            int exitValue = executeResultHandler.getExitValue();
            ExecuteException executeException = executeResultHandler.getException();
            if (defaultExecutor.isFailure(exitValue) && executeWatchdog.killedProcess()) {
                log.error("python script {} timed out after {} seconds", path, defaultPythonTimeout);
                throw new ProxyRuntimeException("python script " + path + " timed out after " + defaultPythonTimeout + " seconds.");
            }
            if (executeException != null) {
                log.error("执行python脚本失败，产生异常", executeException);
                throw executeException;
            }
            if (defaultExecutor.isFailure(exitValue)) {
                throw new ProxyRuntimeException("Failure: Python script " + path + " exited with exit value " + exitValue);
            }

            log.info("python 执行完成");
        } finally {
            byte[] byteArray = out.toByteArray();
            String content = readByteArr(byteArray);
            log.info("获取到的输出内容,content:{}", content);

            pythonOutDTO.setPythonStdoutOutput(content);
            byteArray = err.toByteArray();
            content = readByteArr(byteArray);
            log.info("获取到的异常内容,content:{}", content);

            pythonOutDTO.setPythonStderrOutput(content);
            out.close();
            err.close();
            return pythonOutDTO;
        }
    }


    public static String readByteArr(byte[] byteArr) throws Exception {
        if (byteArr != null && byteArr.length != 0) {
            Charset charset = getCharset(byteArr);
            InputStreamReader streamReader = new InputStreamReader(new ByteArrayInputStream(byteArr), charset);
            BufferedReader reader = new BufferedReader(streamReader);

            String content = getContent(reader, 0);
            return content;
        } else {
            return null;
        }
    }

    public static String getContent(Reader reader, int maxLimit)
            throws Exception {
        BufferedReader myReader = new BufferedReader(reader);

        StringBuilder sb = new StringBuilder();

        char[] charBuffer = new char['?'];

        int charRead = 0;
        while ((charRead = myReader.read(charBuffer, 0, charBuffer.length)) > 0) {
            sb.append(charBuffer, 0, charRead);
            if ((maxLimit > 0) && (sb.length() > maxLimit)) {
                throw new ProxyRuntimeException("Failed to read all content. Max size limit exceeded:" + maxLimit);
            }
        }
        return sb.toString();
    }


    public static Charset getCharset(byte[] byteArr) {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(byteArr);
        CharsetMatch match = detector.detect();
        String name = match.getName();
        log.debug("Detected charset : {}", name);
        if (!name.equals("GB18030") && !name.equals("GB2312") && !name.startsWith("UTF") && !name.equals("ISO-8859-1")) {
            log.warn("Detected file encoding as {}, is that correct?", name);
        }

        Charset charset = Charset.forName(name);
        return charset;
    }


    /**
     * 判断设备是否是主备
     *
     * @param commandLineEcho
     * @param modelNumber
     * @return
     */
    private boolean isStandby(String commandLineEcho, DeviceModelNumberEnum modelNumber) {
        boolean isStandby = false;
        switch (modelNumber) {
            case USG6000:
            case USG6000_NO_TOP:
                isStandby = judgeHwStandby(commandLineEcho.toLowerCase());
                break;
            case CISCO:
            case CISCO_ASA_86:
            case CISCO_ASA_99:
                isStandby = judgeCISCOStandby(commandLineEcho.toLowerCase());
                break;
            case HILLSTONE:
            case HILLSTONE_V5:
                isStandby = judgeHillstoneStandby(commandLineEcho.toLowerCase());
                break;
            case TOPSEC_TOS_005:
            case TOPSEC_TOS_010_020:
            case TOPSEC_NG:
                isStandby = judgeTopsecStandby(commandLineEcho.toLowerCase());
                break;
            case TOPSEC_NG2:
                isStandby = judgeTopsecNG2Standby(commandLineEcho.toLowerCase());
                break;
            case DPTECHR003:
            case DPTECHR004:
                isStandby = judgeDptechrStandby(commandLineEcho.toLowerCase());
                break;
            case SRX:
            case SRX_NoCli:
                isStandby = judgeSrxStandby(commandLineEcho.toLowerCase());
                break;
            case SSG:
                isStandby = judgeSsgStandby(commandLineEcho.toLowerCase());
                break;
            case FORTINET:
            case FORTINET_V5_2:
                isStandby = judgeFortinetStandby(commandLineEcho.toLowerCase());
                break;
            default:
        }
        return isStandby;
    }

    /**
     * 判断是主墙还是备墙
     *
     * @param cmdEcho
     */
    private boolean judgeHwStandby(String cmdEcho) {
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.HUAWEI.getEchoSign())) {
            log.info("检测到当前华为防火墙不支持show主备命令，继续原流程下发命令");
            return false;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.HUAWEI.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.HUAWEI.getPostField()));
        if (StringUtils.contains(subCmdStr, EchoConstantEnum.HUAWEI.getBackupSign())) {
            log.info("检测到当前华为防火墙为standby，切换主设备信息之后继续下发命令");
            return true;
        }
        return false;
    }


    /**
     * 处理山石命令行
     *
     * @param cmdEcho
     */
    private boolean judgeHillstoneStandby(String cmdEcho) {
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.HILLSTONE.getEchoSign())) {
            log.info("检测到当前山石防火墙不支持show主备命令，继续原流程下发命令");
            return false;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.HILLSTONE.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.HILLSTONE.getPostField()));
        if (StringUtils.contains(subCmdStr, EchoConstantEnum.HILLSTONE.getBackupSign())) {
            log.info("检测到当前山石防火墙为BACKUP，切换主设备信息之后继续下发命令");
            return true;
        }
        return false;
    }

    /**
     * 处理天融信是否切换主备重新下发
     *
     * @param cmdEcho
     */
    private boolean judgeTopsecStandby(String cmdEcho) {
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.TOPSEC.getEchoSign())) {
            log.info("检测到当前天融信防火墙不支持show主备命令，继续原流程下发命令");
            return false;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.TOPSEC.getPreField()));
        String[] subCmdStrs = subCmdStr.split("\n");
        // 取列表数据的第一行里面的主备标识
        String backupSign = null == subCmdStrs ? "" : subCmdStrs[1];
        if (StringUtils.contains(backupSign, EchoConstantEnum.TOPSEC.getBackupSign())) {
            log.info("检测到当前天融信防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            return true;
        }
        return false;
    }

    /**
     * 处理天融信NG2是否切换主备重新下发
     *
     * @param cmdEcho
     */
    private boolean judgeTopsecNG2Standby(String cmdEcho) {
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.TOPSECNG2.getEchoSign())) {
            log.info("检测到当前天融信防火墙不支持show主备命令，继续原流程下发命令");
            return false;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.TOPSECNG2.getPreField()));
        String[] subCmdStrs = subCmdStr.split("\n");
        // 取列表数据的第一行里面的主备标识
        String backupSign = null == subCmdStrs ? "" : subCmdStrs[1];
        if (StringUtils.contains(backupSign, EchoConstantEnum.TOPSECNG2.getBackupSign())) {
            log.info("检测到当前天融信NG2防火墙为STANDBY，切换主设备信息之后继续下发命令");
            return true;
        }
        return false;
    }

    /**
     * 处理迪普是否切换主备重新下发
     *
     * @param cmdEcho
     */
    private boolean judgeDptechrStandby(String cmdEcho) {
        // 判断是否主备关键字之前先判断是否正常回显，如果没有正常回显，则表示不支持show主备命令，走之前的流程下发
        if (!cmdEcho.contains(EchoConstantEnum.DPTECHR.getEchoSign())) {
            log.info("检测到当前迪普防火墙不支持show主备命令，继续原流程下发命令");
            return false;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.DPTECHR.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.DPTECHR.getPostField()));
        if (StringUtils.contains(subCmdStr, EchoConstantEnum.DPTECHR.getBackupSign())) {
            log.info("检测到当前迪普防火墙为BACKUP，切换主设备信息之后继续下发命令");
            return true;
        }
        return false;
    }

    /**
     * 处理Juniper-SRX是否切换主备重新下发
     *
     * @param cmdEcho
     */
    private boolean judgeSrxStandby(String cmdEcho) {
        // 在取值之前先去做有没有正确回显的判断。用回显的关键字符去判断
        if (!cmdEcho.contains(EchoConstantEnum.JUNIPER_SRX.getEchoSign())) {
            log.info("检测到当前Juniper-SRX防火墙为不支持主备切换命令,继续原流程下发命令");
            return false;
        }
        String backUpSign = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.JUNIPER_SRX.getPreField()),
                cmdEcho.indexOf(EchoConstantEnum.JUNIPER_SRX.getPostField()));
        if (StringUtils.contains(backUpSign, EchoConstantEnum.JUNIPER_SRX.getBackupSign())) {
            log.info("检测到当前Juniper-SRX防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            return true;
        }
        return false;
    }

    /**
     * 处理Juniper-SSG是否切换主备重新下发
     *
     * @param cmdEcho
     */
    private boolean judgeSsgStandby(String cmdEcho) {
        // 在取值之前先去做有没有正确回显的判断。用回显的关键字符去判断
        if (!cmdEcho.contains(EchoConstantEnum.JUNIPER_SSG.getEchoSign())) {
            log.info("检测到当前Juniper-SSG防火墙为不支持主备切换命令,继续原流程下发命令");
            return false;
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
            return true;
        }
        return false;
    }

    /**
     * 处理Fortinet是否切换主备重新下发
     *
     * @param cmdEcho
     */
    private boolean judgeFortinetStandby(String cmdEcho) {
        // 在取值之前先去做有没有正确回显的判断。用回显的关键字符去判断
        if (!cmdEcho.contains(EchoConstantEnum.FORTINET.getEchoSign())) {
            log.info("检测到当前飞塔防火墙为不支持主备切换命令,继续原流程下发命令");
            return false;
        }

        String subCmdStr = cmdEcho.substring(cmdEcho.indexOf(EchoConstantEnum.FORTINET.getPreField()));
        String[] subCmdStrs = subCmdStr.split("\n");
        // 取列表数据的第一行里面的主备标识
        String backupSign = null == subCmdStrs ? "" : subCmdStrs[0];

        if (StringUtils.contains(backupSign, EchoConstantEnum.FORTINET.getBackupSign())) {
            log.info("检测到当前飞塔防火墙为BACKUP，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            return true;
        }
        return false;

    }


    /**
     * 重新下发思科命令行
     *
     * @param cmdEcho
     */
    private boolean judgeCISCOStandby(String cmdEcho){
        // 对思科的查询主备命令show failover | in host回显做特殊处理
        Pattern pattern = Pattern.compile(showStandbyCommandConfig.getCiscoMatchStandby());
        Matcher matcher = pattern.matcher(cmdEcho);

        String standbyFlag = null;
        if (matcher.find()) {
            standbyFlag = matcher.group("obj1");
        }
        if (StringUtils.isBlank(standbyFlag)) {
            return false;
        }
        // 判断命令返回为备用
        if (StringUtils.contains(standbyFlag, CISCO_FIREWALL_STANDBY_COMMANDLINE)) {
            log.info("检测到当前思科防火墙为STANDBY，切换主设备信息之后继续下发命令");
            // 设置已经查询主备标签为true
            return true;
        } else if (StringUtils.contains(standbyFlag, CISCO_FIREWALL_ACTIVE_COMMANDLINE)) {
            log.info("检测到当前思科防火墙为active，继续下发命令：{}");
            return false;
        }
        return false;
    }

    public static void main(String[] args) {
        String commandLine = " \n" +
                "\\u001b]0;root@localhost:~\\u0007[root@localhost ~]#  \n" +
                "\\u001b]0;root@localhost:~\\u0007[root@localhost ~]#  show failover | in host\n" +
                "        this \\u001b[01;31m\\u001b[khost\\u001b[m\\u001b[k:secondary-standby ready\n" +
                "        other \\u001b[01;31m\\u001b[khost\\u001b[m\\u001b[k:primary - actice\n" +
                "\\u001b]0;root@localhost:~\\u0007[root@localhost ~]# \"\n";

        Pattern pattern = Pattern.compile("this.*?host.*?:.*?(?<obj1>.*?)-.*\\n");
        Matcher matcher = pattern.matcher(commandLine);
        if (matcher.find()) {
            String ruleId = matcher.group("obj1");
            System.out.println(ruleId);
        }
    }
}
