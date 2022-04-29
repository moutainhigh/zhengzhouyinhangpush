package com.abtnetworks.totems.push.service.executor.impl;

import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.service.executor.Executor;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;


/**
 * SSH工具类
 *
 */
public class JuniperSRXExec implements Executor {
    private static Logger logger = Logger.getLogger(JuniperSRXExec.class);

    private final Integer BUF_LEN = 1024 * 8;

    private final String MODLE_NUMBER = "JuniperSRX";

    private final String COMMAND_COMMIT = "commit";

    private static final Integer SHELL_PORT = 22;

    private static final String LINE_SEPERATOR = "\n";

    Session session = null;
    ChannelShell openChannel = null;

    public JuniperSRXExec() {
    }

    @Override
    public PushResultDTO exec(PushCmdDTO pushCmdDTO) {
        PushResultDTO pushResultDTO = new PushResultDTO();

        try {
            initSession(pushCmdDTO.getDeviceManagerIp(), pushCmdDTO.getUsername(), pushCmdDTO.getPassword(), pushCmdDTO.getPort());

            if(canConnection()) {
                List<String> resultCmdList = execCommand(pushCmdDTO);

                StringBuilder sb = new StringBuilder();
                for(String str: resultCmdList) {
                    sb.append(str).append("\n");
                }
                pushResultDTO.setResult(ReturnCode.POLICY_MSG_OK);
                pushResultDTO.setCmdEcho(sb.toString());
            } else {
                pushResultDTO.setResult(ReturnCode.CONNECT_FAILED);
            }
        } catch(Exception e) {
            logger.error("下发异常：", e);
            pushResultDTO.setResult(ReturnCode.PUSH_TASK_ERROR);
        }

        return pushResultDTO;
    }

    /**
     * 是否连接成功,调用如果不需要调用execCommand方法那么必须调用 disconnect方法关闭session
     * @return
     */
    public boolean canConnection(){
        try {
            if(!session.isConnected()){
                session.connect();
            }
            return true;
        } catch (JSchException e) {
            logger.error("连接设备异常！", e);
            return false;
        }
    }

    /**
     * 关闭连接
     */
    public void disconnect(){
        if (openChannel != null && !openChannel.isClosed()) {
            openChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public List<String> execCommand(PushCmdDTO dto) throws IOException, JSchException {
        String command = dto.getCommandline();
        String[] commandLines = command.split(LINE_SEPERATOR);
        List<String> commandList = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        for(String commandLine: commandLines) {
            System.out.println(commandLine);
            commandList.add(commandLine);
        }

        //打开shell通道，准备好读数/写据流
        ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
        InputStream inputStream = channelShell.getInputStream();//从远端到达的数据  都能从这个流读取到
        channelShell.setPty(true);


        byte[] tmp = new byte[BUF_LEN]; //设置读缓存为8k
        OutputStream outputStream = channelShell.getOutputStream();//写入该流的数据  都将发送到远程端
        channelShell.connect();
        while(inputStream.available() > 0){
            int i = inputStream.read(tmp, 0, BUF_LEN);
            if(i < 0) {break;}
            String s = new String(tmp, 0, i);
            if(s.indexOf("--More--") >= 0){
                outputStream.write(("\n").getBytes());
                outputStream.flush();
            }
            System.out.println("回显：" + s);
            resultList.add(s);
        }
        try{
            sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用PrintWriter 就是为了使用println 这个方法
        //好处就是不需要每次手动给字符加\n
        PrintWriter printWriter = new PrintWriter(outputStream);

        for(String commandLine: commandList) {
            System.out.println("输入：" + commandLine);
            printWriter.write(commandLine + "\n");
            printWriter.flush();


            while(inputStream.available() > 0){
                int i = inputStream.read(tmp, 0, BUF_LEN);
                if(i < 0) {break;}
                String s = new String(tmp, 0, i);
                if(s.indexOf("--More--") >= 0){
                    outputStream.write(("\n").getBytes());
                    outputStream.flush();
                }
                System.out.println("回显：" + s);
                resultList.add(s);
            }

            try{
                sleep(2000);
            } catch (Exception e) {
                logger.info("睡眠错误：" + e);
            }

            //Juniper Srx commit操作会持续时间会很长，多睡眠10秒等待
            if(commandLine.trim().equals(COMMAND_COMMIT)) {
                try{
                    sleep(5000);
                } catch (Exception e) {
                    logger.info("睡眠错误：" + e);
                }
            }

            //获取commit回显
            while(inputStream.available() > 0){
                int i = inputStream.read(tmp, 0, BUF_LEN);
                if(i < 0) {break;}
                String s = new String(tmp, 0, i);
                if(s.indexOf("--More--") >= 0){
                    outputStream.write(("\n").getBytes());
                    outputStream.flush();
                }
                System.out.println("回显：" + s);
                resultList.add(s);
            }
        }

        //完成后关闭流
        outputStream.close();
        inputStream.close();
        channelShell.disconnect();
        session.disconnect();
        System.out.println("DONE");

        return resultList;
    }

    public String getMODLE_NUMBER() {
        return MODLE_NUMBER;
    }

    private void initSession(String host, String user, String pwd, int port) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setTimeout(100000);
        session.setConfig(config);
        session.setPassword(pwd);
    }
}
