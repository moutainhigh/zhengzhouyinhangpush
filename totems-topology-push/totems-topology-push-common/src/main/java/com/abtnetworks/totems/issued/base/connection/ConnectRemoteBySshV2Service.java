package com.abtnetworks.totems.issued.base.connection;

import com.abtnetworks.totems.common.dto.JumpMachineDTO;
import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import expect4j.Expect4j;

import java.io.IOException;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-15
 */
public interface ConnectRemoteBySshV2Service {
    /***
     * 把会话中的交互桥接给Expect4j
     * @param session
     * @param timeOut
     * @return
     * @throws IOException
     * @throws JSchException
     */
    Expect4j expectSSH(Session session, int timeOut, String inputCharset, String outputCharset) throws IOException, JSchException;

    /***
     * 把会话中的交互桥接给Expect4j
     * @param session
     * @param timeOut
     * @return
     * @throws IOException
     * @throws JSchException
     */
    Expect4j expectSSHByProxy(Session targetSession,Session session, int timeOut, String inputCharset, String outputCharset) throws IOException, JSchException;

    /***
     * 创建ssh2连接
     * @param remoteConnectUserDTO
     * @return
     * @throws JSchException
     */
    Session createSessionBySSH(RemoteConnectUserDTO remoteConnectUserDTO) throws JSchException;

    /***
     * 创建跳板机ssh连接
     * @param remoteConnectUserDTO
     * @param machineDTO
     * @return
     * @throws JSchException
     */
    Session createSessionByProxySSH(JSch jsch,Session session, JumpMachineDTO machineDTO, RemoteConnectUserDTO remoteConnectUserDTO) throws JSchException;

    /**
     * 另外一个开源客户端
     * @param remoteConnectUserDTO
     * @return
     * @throws IOException
     */
    Expect4j expectSShj(RemoteConnectUserDTO remoteConnectUserDTO) throws IOException;
}
