package com.abtnetworks.totems.proxy.fortress.service.impl;

import com.abtnetworks.totems.proxy.common.dto.PythonOutDTO;
import com.abtnetworks.totems.proxy.common.exceptions.ProxyRuntimeException;
import com.abtnetworks.totems.proxy.fortress.service.PythonFortressMachineService;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/8/6
 */

public class PythonFortressMachineServiceImpl implements PythonFortressMachineService {
    private final static Logger log = LoggerFactory.getLogger(PythonFortressMachineServiceImpl.class);

    @Override
    public String choseProxyPythonPath(String fileName, String deviceIp, String loginName, String prePath,String deviceName,String enableLoginName) throws Exception {
        String path = PythonFortressMachineService.super.choseProxyPythonPath(fileName, deviceIp, loginName, prePath,deviceName,enableLoginName);
        log.info("获取python路径{}", path);
        PythonOutDTO pythonOutDTO = null;
        try {
            pythonOutDTO = this.pythonFortressGetPassword(deviceIp, path, loginName,deviceName,enableLoginName);
        } catch (ExecuteException e) {
            //是执行中的异常，特此要进行清晰识别
            String stdout = pythonOutDTO.getPythonStderrOutput();
            if (StringUtils.isNotEmpty(stdout)) {

                log.error("Python stdout:\n{}", stdout);
                log.error("Python stderr:\n{}", stdout);
                if (stdout.contains("Wrong Password")) {
                    log.error("登录设备失败，用户名或密码错误");
                } else if (stdout.contains("TIMEOUT")) {
                    log.error("采集超时");
                } else if (stdout.contains("EOF") && stdout.contains("Broken pipe")) {
                    log.error("网络连接不稳定，会话中断");
                }
            }
        } catch (ProxyRuntimeException e) {
            log.error(e.getMessage());
        } finally {
            if (ObjectUtils.isNotEmpty(pythonOutDTO)) {
                String pythonStdoutOutput = pythonOutDTO.getPythonStdoutOutput();
                return pythonStdoutOutput;
            }
        }

        return "";
    }

    @Override
    public PythonOutDTO pythonFortressGetPassword(String deviceIp, String path, String loginName,String deviceName,String enableLoginName) throws Exception {
        //参数准备
        String lineSeparator = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        deviceName = URLEncoder.encode(deviceName,StandardCharsets.UTF_8.name());
        if(StringUtils.isNotBlank(enableLoginName)){
            sb.append("enableLoginName=" + enableLoginName);
            sb.append(lineSeparator);
        }
        if(StringUtils.isNotBlank(deviceName)){
            sb.append("deviceName=" + deviceName);
            sb.append(lineSeparator);
        }
        if(StringUtils.isNotBlank(deviceIp)){
            sb.append("ip=" + deviceIp);
            sb.append(lineSeparator);
        }
        if(StringUtils.isNotBlank(loginName)){
            sb.append("loginName=" + loginName);
            sb.append(lineSeparator);
        }

        sb.append(lineSeparator);
        String paramStr = sb.toString();

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


}
