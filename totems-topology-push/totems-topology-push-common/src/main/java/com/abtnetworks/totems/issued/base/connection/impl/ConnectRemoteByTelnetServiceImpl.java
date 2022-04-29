package com.abtnetworks.totems.issued.base.connection.impl;

import com.abtnetworks.totems.issued.base.connection.ConnectRemoteByTelnetService;
import com.abtnetworks.totems.issued.dto.RemoteConnectUserDTO;
import expect4j.Expect4j;
import lombok.SneakyThrows;
import org.apache.commons.net.io.FromNetASCIIInputStream;
import org.apache.commons.net.io.ToNetASCIIOutputStream;
import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetOptionHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author zakyoung
 * @Title:
 * @Description: telnet远程连接
 * @date 2020-03-15
 */
@Service
public class ConnectRemoteByTelnetServiceImpl implements ConnectRemoteByTelnetService {


    @Override
    public Expect4j expectTelnet(TelnetClient client, RemoteConnectUserDTO remoteConnectUserDTO, String inputCharset, String outputCharset) throws UnsupportedEncodingException {
        // null until client connected
        InputStream is = new FromNetASCIIInputStream(client.getInputStream());
        OutputStream os = new ToNetASCIIOutputStream(client.getOutputStream());
        Expect4j expect = new Expect4j(is, os, inputCharset, outputCharset) {
            @SneakyThrows
            @Override
            public void close() {
                super.close();
                client.disconnect();

            }
        };
        expect.setDefaultTimeout(remoteConnectUserDTO.getTimeOut() * 1000);
        return expect;
    }

    @Override
    public TelnetClient createClientByTelnet(RemoteConnectUserDTO remoteConnectUserDTO) throws IOException, InvalidTelnetOptionException {
        final TelnetClient client = new TelnetClient();
        TerminalTypeOptionHandler terminalTypeOptionHandler = new TerminalTypeOptionHandler("VT100", false, false, true, true);
        TelnetOptionHandler sizeOpt = new WindowSizeOptionHandler(1024, 1024, false, false, true, false);
        EchoOptionHandler echoOptionHandler = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaOptionHandler = new SuppressGAOptionHandler(false, false, false, false);
        client.addOptionHandler(terminalTypeOptionHandler);
        client.addOptionHandler(echoOptionHandler);
        client.addOptionHandler(gaOptionHandler);
        client.addOptionHandler(sizeOpt);
        client.connect(remoteConnectUserDTO.getDeviceManagerIp(), remoteConnectUserDTO.getPort());
        client.setConnectTimeout(remoteConnectUserDTO.getTimeOut() * 1000);
        return client;
    }
}
