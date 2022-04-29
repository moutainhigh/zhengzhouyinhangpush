package com.abtnetworks.totems.proxy;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.ObjectUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/8/6
 */
@Slf4j
public class FortressMainTest {

    public static void mainTest() throws IOException, InterruptedException {
        String lineSeparator = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        sb.append("username=" + "zy");
        sb.append(lineSeparator);
        sb.append("password=" + "12");
        sb.append(lineSeparator);
        sb.append("ip=" + "deviceIp3434");
        sb.append(lineSeparator);
        sb.append("account=" + "loginName4343");
        sb.append(lineSeparator);
        String paramStr = sb.toString();
        String path = "/opt/topology/service/push/test.py";
        // 外部命令执行对象
        CommandLine commandLine = new CommandLine("python");
        commandLine.addArgument(path).addArgument(paramStr);

        //默认执行结果处理程序
        DefaultExecuteResultHandler executeResultHandler = new DefaultExecuteResultHandler();
        int defaultPythonTimeout = 180;
        Integer timeout = 60;
        if (timeout != null && timeout >= 1) {
            defaultPythonTimeout = timeout;
        }

        //执行配置
        ExecuteWatchdog executeWatchdog = new ExecuteWatchdog((long) (defaultPythonTimeout * 1000));
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        defaultExecutor.setWatchdog(executeWatchdog);
        byte[] paramByte = paramStr.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream input = new ByteArrayInputStream(paramByte);
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        ByteArrayOutputStream err = new ByteArrayOutputStream(1024);
        PumpStreamHandler handler = new PumpStreamHandler(out, err, input);
        //获取系统环境变量
        Map<String, String> systemEnvMap = System.getenv();
        HashMap pythonMap = new HashMap(systemEnvMap);
        //增加环境变量，经验证 value冒号左侧的key值，任意即可，维持原值customPath
//        pythonMap.put("PYTHONPATH", fileConfig.getCustomPath() + ":" + fileConfig.getCommonLib());
//        log.info("增加到环境变量中的值,nev:{}", pythonMap.get("PYTHONPATH"));


        try {
            defaultExecutor.setStreamHandler(handler);
            System.out.println("正在运行的python脚本，info:{}"+commandLine.toString());
            defaultExecutor.execute(commandLine, pythonMap, executeResultHandler);
            executeResultHandler.waitFor();
            int exitValue = executeResultHandler.getExitValue();
            ExecuteException executeException = executeResultHandler.getException();
            if (defaultExecutor.isFailure(exitValue) && executeWatchdog.killedProcess()) {
                System.out.println("python script {} timed out after {} seconds"+ path+defaultPythonTimeout);
//                throw new WhaleRuntimeException(WhaleErrorName.TIMEOUT_EXCEPTION, null, "python script " + path + " timed out after " + defaultPythonTimeout + " seconds.");
            }

            if (executeException != null) {
                System.out.println("执行python脚本失败，产生异常"+executeException);

            }

            if (defaultExecutor.isFailure(exitValue)) {
//                throw new WhaleRuntimeException("Failure: Python script " + path + " exited with exit value " + exitValue);
            }

            System.out.println("python 执行完成");
        } finally {
            byte[] byteArray = out.toByteArray();
            if(ObjectUtils.isNotEmpty(byteArray)){
                String s = new String(byteArray,"UTF-8");
                System.out.println(s);

                log.info("执行输出的数据{}",s);
            }

            out.close();
            err.close();
        }
    }
}
