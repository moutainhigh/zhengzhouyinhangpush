package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Executor;

/**
 * @author Administrator
 * @Title:
 * @Description: 请写注释类
 * @date 2021/6/30
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class J2sshConnectTest {

    @Autowired
    @Qualifier(value = "commandExecutor")
    private Executor pushExecutor;
    @Test
    public void testDemo() throws IOException {
        DefaultConfig defaultConfig = new DefaultConfig();

        final SSHClient client = new SSHClient( defaultConfig);

        String host = "192.168.215.32";
        String user = "admin";
        String password = "sapling.123";
        client.setTimeout(60000);
        client.loadKnownHosts();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect(host);
        try {
            client.authPassword(user, password);
            final SessionChannel session = (SessionChannel) client.startSession();
            session.allocateDefaultPTY();
            //这里的session 类似 jsch 里面的exec ，可以直接执行命令。
            //session.exec("pwd");
            SessionChannel shell = (SessionChannel) session.startShell();
            OutputStream outputStream = shell.getOutputStream();
            outputStream.write("enable\n".getBytes());
            outputStream.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(shell.getInputStream(), "utf-8"));
            String line;
            while ( bufferedReader.read()>-1 && (line = bufferedReader.readLine()) != null ) {
                System.out.println(line);
            }
            shell.close();
            session.close();
        } finally {
            client.disconnect();
        }
    }


}
