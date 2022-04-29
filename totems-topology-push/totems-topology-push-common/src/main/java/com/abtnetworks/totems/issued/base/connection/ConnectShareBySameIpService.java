package com.abtnetworks.totems.issued.base.connection;

import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import com.jcraft.jsch.JSchException;
import expect4j.Expect4j;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;

import java.io.IOException;

/**
 * @author zakyoung
 * @Title:
 * @Description: 所有的连接共享层设计在这
 * @date 2020-03-15
 */
public interface ConnectShareBySameIpService {
    /***
     * 共享ip下的session利用
     * @param remoteConnectUserDTO
     * @return
     * @throws IOException
     * @throws InvalidTelnetOptionException
     * @throws JSchException
     */
    Expect4j connectShareBySameIp(RemoteConnectUserDTO remoteConnectUserDTO) throws IOException, InvalidTelnetOptionException, JSchException, InterruptedException;


}
