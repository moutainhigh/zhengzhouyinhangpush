package com.abtnetworks.totems.push.service.executor.impl;

import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.constants.ReturnCode;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.abtnetworks.totems.push.service.executor.Executor;
import com.abtnetworks.totems.recommend.dto.push.PushCommandlineDTO;
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
public class CiscoASAExec implements Executor {
    private static Logger logger = Logger.getLogger(CiscoASAExec.class);

    private static final Integer BUF_LEN = 1024 * 8;

    private static final String MODLE_NUMBER = "CiscoASA";

    private static final String COMMAND_ENABLE = "enable";

    private static final String LINE_SEPERATOR = "\n";

    private static final Integer SHELL_PORT = 22;

    Session session = null;
    ChannelShell openChannel = null;

    public CiscoASAExec() {
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
                    logger.info(str);
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
        String enablePassword = dto.getEnablePassword();
        if(enablePassword == null) {
            logger.info("Enable密码为空，无法下发命令行！");
            return null;
        }
        String command = dto.getCommandline();
        String[] commandLines = command.split(LINE_SEPERATOR);
        List<String> commandList = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        for(String commandLine: commandLines) {
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
            logger.info("输入：" + commandLine);
            printWriter.write(commandLine + "\n");
            printWriter.flush();

            //Cisco中enable以后需要输入密码
            if(commandLine.trim().equals(COMMAND_ENABLE)) {
                String password = dto.getEnablePassword();
                printWriter.write(password + "\n");
                printWriter.flush();
            }


            while(inputStream.available() > 0){
                int i = inputStream.read(tmp, 0, BUF_LEN);
                if(i < 0) {break;}
                String s = new String(tmp, 0, i);
                if(s.indexOf("--More--") >= 0){
                    outputStream.write(("\n").getBytes());
                    outputStream.flush();
                }
                resultList.add(s);
                logger.info("回显：" + s);
            }

            try{
                sleep(2000);
            } catch (Exception e) {
                logger.info("睡眠错误：" + e);
            }
        }

        while(inputStream.available() > 0){
            int i = inputStream.read(tmp, 0, BUF_LEN);
            if(i < 0) {break;}
            String s = new String(tmp, 0, i);
            if(s.indexOf("--More--") >= 0){
                outputStream.write(("\n").getBytes());
                outputStream.flush();
            }
            resultList.add(s);
            logger.info("回显：" + s);
        }

        try{
            sleep(2000);
        } catch (Exception e) {
            logger.info("睡眠错误：" + e);
        }


        //完成后关闭流
        outputStream.close();
        inputStream.close();
        channelShell.disconnect();
        session.disconnect();

        return resultList;
    }

    private void initSession(String host, String user, String pwd, int port) throws Exception {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setTimeout(5000);
        session.setConfig(config);
        session.setPassword(pwd);
    }

    public String getMODLE_NUMBER() {
        return MODLE_NUMBER;
    }


    public static void main(String[] args) throws Exception{
        CiscoASAExec ciscoASAExec = new CiscoASAExec();
        PushCmdDTO pushCmdDTO  = new PushCmdDTO();
        pushCmdDTO.setEnablePassword("123456");
        pushCmdDTO.setPassword("123456");
        pushCmdDTO.setUsername("pix");
        pushCmdDTO.setPort(22);
        pushCmdDTO.setDeviceManagerIp("192.168.201.97");
        pushCmdDTO.setCommandline("enable\n123456\n configure terminal   \n show running-config access-group  \n \n \n ");
        PushResultDTO resultDTO = ciscoASAExec.exec(pushCmdDTO);
//        List<String> list = ciscoASAExec.execCommand(pushCmdDTO);
        System.out.println("***************************");
        String[] arr = resultDTO.getCmdEcho().split("\n");
        String result = "";
        for(String s : arr) {
            int startIndex = s.indexOf(PolicyConstants.ACCESS_GROUP);
            int endIndex = s.indexOf(PolicyConstants.IN_INTERFACE);
            //出接口不为空
            if (startIndex != -1 && endIndex != -1) {
                result = s.substring(startIndex + PolicyConstants.ACCESS_GROUP.length(), endIndex);
                break;
            }
        }
        System.out.println(result);
    }

}
