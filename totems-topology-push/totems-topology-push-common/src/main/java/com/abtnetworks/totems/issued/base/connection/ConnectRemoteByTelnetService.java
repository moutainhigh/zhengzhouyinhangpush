package com.abtnetworks.totems.issued.base.connection;

import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import expect4j.Expect4j;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;

/**
 * @author zakyoung
 * @Title:
 * @Description: 请写注释类
 * @date 2020-03-15
 */
public interface ConnectRemoteByTelnetService {
    /***
     * 使用telnet连接远程
     * @param client 客户端
     * @param remoteConnectUserDTO 超时时间
     * @return
     * @throws IOException
     * @throws InvalidTelnetOptionException
     */
    Expect4j expectTelnet(TelnetClient client, RemoteConnectUserDTO remoteConnectUserDTO, String inputCharset, String outputCharset) throws IOException, InvalidTelnetOptionException;

    /***
     *    telnet 创建远程连接
     *       @param remoteConnectUserDTO 用户实体
     *       @return TelnetClient 返回
     *
     * @return
     * @throws IOException
     * @throws InvalidTelnetOptionException
     */
    TelnetClient createClientByTelnet(RemoteConnectUserDTO remoteConnectUserDTO) throws IOException, InvalidTelnetOptionException;
}
