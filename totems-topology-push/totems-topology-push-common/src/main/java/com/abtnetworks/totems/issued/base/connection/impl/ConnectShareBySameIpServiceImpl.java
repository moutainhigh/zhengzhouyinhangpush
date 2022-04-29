package com.abtnetworks.totems.issued.base.connection.impl;


import com.abtnetworks.totems.common.config.JumpMachineConfig;
import com.abtnetworks.totems.common.constants.PolicyConstants;
import com.abtnetworks.totems.common.dto.JumpMachineDTO;
import com.abtnetworks.totems.common.utils.IpUtils;
import com.abtnetworks.totems.issued.annotation.ConnectDispose;
import com.abtnetworks.totems.issued.base.connection.ConnectRemoteBySshV2Service;
import com.abtnetworks.totems.issued.base.connection.ConnectRemoteByTelnetService;
import com.abtnetworks.totems.issued.base.connection.ConnectShareBySameIpService;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.alibaba.fastjson.JSONObject;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import expect4j.Expect4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author zakyoung
 * @Title:
 * @Description: 实现ip共享连接资源
 * @date 2020-03-15
 */
@Service
public class ConnectShareBySameIpServiceImpl implements ConnectShareBySameIpService {
    /***日志记录
     **/
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectShareBySameIpServiceImpl.class);
    /***ssh
     **/
    @Resource
    ConnectRemoteBySshV2Service connectRemoteBySshV2Service;
    /***telnet
     **/
    @Resource
    ConnectRemoteByTelnetService connectRemoteByTelnetService;


    @Value("${push.sshj.deviceIp:-.-.-.-}")
    private String sshJip;


    @Value("${relevant_subnet.jump_machine_a_ip:''}")
    private String machineARelevantIp;

    @Value("${relevant_subnet.jump_machine_b_ip:''}")
    private String machineBRelevantIp;

    @Resource
    JumpMachineConfig jumpMachineConfig;


    private Expect4j telnetWaitDisconnect(String hostName, RemoteConnectUserDTO remoteConnectUserDTO) throws IOException, InvalidTelnetOptionException, InterruptedException {

        String charset = remoteConnectUserDTO.getCharset();
        int interval = remoteConnectUserDTO.getInterval().intValue() * 1000;
        //超时时间ms,这里的redis是以s，所以后面的都需要乘100
        LOGGER.debug("特殊处理下发间隔时间{}", interval);
        Thread.sleep(interval);
        TelnetClient telnetClient = connectRemoteByTelnetService.createClientByTelnet(remoteConnectUserDTO);

        Expect4j expect4j = connectRemoteByTelnetService.expectTelnet(telnetClient, remoteConnectUserDTO, charset, charset);
        return expect4j;


    }

    @ConnectDispose(isUse = true)
    @Override
    public Expect4j connectShareBySameIp(RemoteConnectUserDTO remoteConnectUserDTO) throws IOException, InvalidTelnetOptionException, JSchException, InterruptedException {
        String hostName = remoteConnectUserDTO.getDeviceManagerIp();
        String charset = StringUtils.isEmpty(remoteConnectUserDTO.getCharset()) ? Charset.defaultCharset().name() : remoteConnectUserDTO.getCharset();
        remoteConnectUserDTO.setCharset(charset);
        int interval = remoteConnectUserDTO.getInterval().intValue() * 1000;
        Expect4j expect4j;

        int timeOut = remoteConnectUserDTO.getTimeOut();
        String cacheTypeKey = hostName + remoteConnectUserDTO.getExecutorType();

        // 判断是否走跳板机的流程
        JumpMachineDTO machineDTO = getJumpMachineInfo(hostName);
        if (null != machineDTO) {
            LOGGER.info("启用跳板机创建session,跳板机的ip为:{}", machineDTO.getIp());
            Thread.sleep(interval);
            JSch jsch = new JSch();
            Session sshSession = jsch.getSession(machineDTO.getUsername().trim(), machineDTO.getIp().trim(), Integer.valueOf(machineDTO.getPort()));
            Session session = connectRemoteBySshV2Service.createSessionByProxySSH(jsch, sshSession, machineDTO, remoteConnectUserDTO);
            return connectRemoteBySshV2Service.expectSSHByProxy(sshSession, session, timeOut, charset, charset);
        }

        if (!SendCommandStaticAndConstants.SSH_TYPE.equalsIgnoreCase(remoteConnectUserDTO.getExecutorType())) {
            expect4j = telnetWaitDisconnect(cacheTypeKey, remoteConnectUserDTO);
        } else {
            if(StringUtils.isNotBlank(sshJip) && sshJip.contains(hostName)){
                Thread.sleep(interval);
                LOGGER.info("启用jssh创建session连接START");
                expect4j = connectRemoteBySshV2Service.expectSShj(remoteConnectUserDTO);
                LOGGER.info("启用jssh创建session连接END{}", JSONObject.toJSONString(expect4j));
            }else {
                //例如山石特殊处理，就算保持单链接，也在一定时间内不能过频繁，再例如农信华为需要特殊处理
                LOGGER.debug("特殊处理下发间隔时间{}", interval);
                Thread.sleep(interval);
                Session session = connectRemoteBySshV2Service.createSessionBySSH(remoteConnectUserDTO);
                //超时时间ms,这里的redis是以s，所以后面的都需要乘100
                expect4j = connectRemoteBySshV2Service.expectSSH(session, timeOut, charset, charset);
            }

        }

        return expect4j;
    }


    /**
     * 获取跳板机详情
     *
     * @param hostName
     * @return
     */
    private JumpMachineDTO getJumpMachineInfo(String hostName) {
        if (StringUtils.isNotBlank(machineARelevantIp)) {
            String[] ips = machineARelevantIp.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String ip : ips) {
                // 判断当前要下发的设备的ip是否属于这个配置的网段
                if (StringUtils.isNotBlank(ip) && IpUtils.checkIpRange(hostName, ip)) {
                    return jumpMachineConfig.getMachineA();
                }
            }
        }
        if (StringUtils.isNotBlank(machineBRelevantIp)) {
            String[] ips = machineBRelevantIp.split(PolicyConstants.ADDRESS_SEPERATOR);
            for (String ip : ips) {
                // 判断当前要下发的设备的ip是否属于这个配置的网段
                if (StringUtils.isNotBlank(ip) && IpUtils.checkIpRange(hostName, ip)) {
                    return jumpMachineConfig.getMachineB();
                }
            }
        }
        LOGGER.info(String.format("当前下发的设备:{} 没有匹配到跳板机,跳过跳板机下发",hostName));
        return null;
    }

}
