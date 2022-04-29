package com.abtnetworks.totems.issued.base.connection.impl;

import com.abtnetworks.totems.common.dto.JumpMachineDTO;
import com.abtnetworks.totems.issued.base.connection.ConnectRemoteBySshV2Service;
import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import expect4j.Expect4j;
import lombok.SneakyThrows;

import lombok.extern.log4j.Log4j2;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Properties;

/**
 * @author zakyoung
 * @Title:
 * @Description: ssh-v2远程连接
 * @date 2020-03-15
 */
@Service
@Log4j2
public class ConnectRemoteBySshV2ServiceImpl implements ConnectRemoteBySshV2Service {

    private final Logger LOGGER = LoggerFactory.getLogger(ConnectRemoteBySshV2ServiceImpl.class);

    @Value("${push.client.size.col:1024}")
    private Integer clientCol;

    @Value("${push.client.size.row:1024}")
    private Integer clientRow;

    @Value("${push.client.size.wp:4096}")
    private Integer clientWp;

    @Value("${push.client.size.hp:4096}")
    private Integer clientHp;


    @Override
    public Expect4j expectSSH(Session session, int timeOut, String inputCharset, String outputCharset) throws IOException, JSchException {
        //第二打开通道
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        /**
         * 对终端命令行，宽度加大（超过流出来会有问题）
         * VT100：终端只负责显示和输入，程序在远程主机上运行。 在PC微机普及的今天，像VT100之类的专用的终端机已经逐渐退出舞台，不过仍有些特殊设备，如带有console口的路由器，网络交换机，10多年前的银行邮局柜台等需要终端机做用户界面。有特殊终端需求的用户可以在电脑上通过软件虚拟终端机配合串口来实现终端仿真。
         * VT102：具有广泛的应用终端仿真，标签会，键入命令历史，回溯，多窗口的支持。
         */
        channel.setPtyType("vt102", clientCol, clientRow, clientWp, clientHp);

        Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream(), inputCharset, outputCharset) {
            @Override
            public void close()  {
                channel.disconnect();
                super.close();
                session.disconnect();
            }
        };
        expect.setDefaultTimeout(timeOut * 1000);
        //超过时间会自动的选关闭 不能放在上面，在结尾时开始连接
        channel.connect(timeOut * 1000);
        return expect;
    }

    @Override
    public Expect4j expectSSHByProxy(Session targetSession, Session session, int timeOut, String inputCharset, String outputCharset) throws IOException, JSchException {
        //第二打开通道
        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        /**
         * 对终端命令行，宽度加大（超过流出来会有问题）
         * VT100：终端只负责显示和输入，程序在远程主机上运行。 在PC微机普及的今天，像VT100之类的专用的终端机已经逐渐退出舞台，不过仍有些特殊设备，如带有console口的路由器，网络交换机，10多年前的银行邮局柜台等需要终端机做用户界面。有特殊终端需求的用户可以在电脑上通过软件虚拟终端机配合串口来实现终端仿真。
         * VT102：具有广泛的应用终端仿真，标签会，键入命令历史，回溯，多窗口的支持。
         */
        channel.setPtyType("vt102", clientCol, clientRow, clientWp, clientHp);

        Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream(), inputCharset, outputCharset) {
            @Override
            public void close()  {
                channel.disconnect();
                super.close();
                targetSession.disconnect();
                session.disconnect();
            }
        };
        expect.setDefaultTimeout(timeOut * 1000);
        //超过时间会自动的选关闭 不能放在上面，在结尾时开始连接
        channel.connect(timeOut * 1000);
        return expect;
    }

    @Override
    public Session createSessionBySSH(RemoteConnectUserDTO remoteConnectUserDTO) throws JSchException {
        JSch jsch = new JSch();
        String password = remoteConnectUserDTO.getPassword();
        final Session session = jsch.getSession(remoteConnectUserDTO.getUsername(), remoteConnectUserDTO.getDeviceManagerIp(), remoteConnectUserDTO.getPort());
        if (password != null) {
            LOGGER.trace("Setting the Jsch password to the one provided (not shown)");
            session.setPassword(password);
        }
        java.util.Hashtable<String, String> config = new java.util.Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setDaemonThread(true);
        // making a connection with timeout.超过时间会自动的选关闭
        session.connect(remoteConnectUserDTO.getTimeOut() * 1000);
        return session;
    }

    @Override
    public Session createSessionByProxySSH(JSch jsch,Session sshSession,JumpMachineDTO machineDTO, RemoteConnectUserDTO remoteConnectUserDTO) throws JSchException {
        Session session = null;
        synchronized (ConnectRemoteBySshV2ServiceImpl.class) {
            Integer iPort = 0;
            try {
                if (machineDTO.getPassword() != null) {
                    LOGGER.trace("Setting the Jsch password to the one provided (not shown)");
                    sshSession.setPassword(StringUtils.isNotBlank(machineDTO.getPassword()) ? machineDTO.getPassword().trim() : null);
                }
                Properties sshConfig = new Properties();
                sshConfig.put("StrictHostKeyChecking", "no");
                sshSession.setConfig(sshConfig);
                //可设置超时时间
                sshSession.connect(remoteConnectUserDTO.getTimeOut() * 1000);
                //此处开始为端口映射到本地的部分
                iPort = sshSession.setPortForwardingL(iPort, remoteConnectUserDTO.getDeviceManagerIp(), remoteConnectUserDTO.getPort());
                //完成上诉映射之后，即可通过本地端口连接了
                session = jsch.getSession(remoteConnectUserDTO.getUsername().trim(), "127.0.0.1", iPort);
                Properties remoteCfg = new Properties();
                remoteCfg.put("StrictHostKeyChecking", "no");
                session.setConfig(remoteCfg);
                String password = StringUtils.isNotBlank(remoteConnectUserDTO.getPassword()) ? remoteConnectUserDTO.getPassword().trim() : null;

                if (password != null) {
                    LOGGER.trace("Setting the Jsch password to the one provided (not shown)");
                    session.setPassword(password);
                }
                //可设置超时时间
                session.connect(remoteConnectUserDTO.getTimeOut() * 1000);
            } catch (Exception e) {
                log.error("设备:{}通过跳板机映射本地端口:{}下发策略异常,异常原因:{}", remoteConnectUserDTO.getDeviceManagerIp(),iPort, e);
                // 删除本地端口的转发
                sshSession.delPortForwardingL(iPort);
                sshSession.disconnect();
                if (null != session) {
                    session.disconnect();
                }
                throw e;
            }
        }
        return session;
    }

    @Override
    public Expect4j expectSShj(RemoteConnectUserDTO remoteConnectUserDTO) throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig();
        Integer timeOut = remoteConnectUserDTO.getTimeOut();
        String charset = remoteConnectUserDTO.getCharset();

        SSHClient client = new SSHClient(defaultConfig);
        client.setTimeout(60000);
        client.loadKnownHosts();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect(remoteConnectUserDTO.getDeviceManagerIp());
        client.authPassword(remoteConnectUserDTO.getUsername(), remoteConnectUserDTO.getPassword());
        final SessionChannel session = (SessionChannel) client.startSession();
        session.allocateDefaultPTY();

        SessionChannel channel = (SessionChannel) session.startShell();

        Expect4j expect = new Expect4j(channel.getInputStream(), channel.getOutputStream(),charset,charset) {

            @SneakyThrows
            @Override
            public void close() {
                channel.close();

                session.close();
                client.close();
                super.close();
            }
        };
        expect.setDefaultTimeout(timeOut.longValue() * 1000L);
        return expect;

    }
}
