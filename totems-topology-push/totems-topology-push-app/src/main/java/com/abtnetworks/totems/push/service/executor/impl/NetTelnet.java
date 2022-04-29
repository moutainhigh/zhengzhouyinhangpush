package com.abtnetworks.totems.push.service.executor.impl;
import java.io.InputStream;
import java.io.PrintStream;
import org.apache.commons.net.telnet.TelnetClient;

public class NetTelnet {
    private TelnetClient telnet = new TelnetClient();
    private InputStream in;
    private PrintStream out;
    private char prompt = '#';

    // 普通用户结束
    public NetTelnet(String ip, int port, String user, String password) {
        try {
            telnet.connect(ip, port);
            in = telnet.getInputStream();
            out = new PrintStream(telnet.getOutputStream());
            // 根据root用户设置结束符
            this.prompt = user.equals("root") ? '#' : '$';
            login(user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** * 登录 * * @param user * @param password */
    public void login(String user, String password) {
        readUntil("login:");
        write(user);
        readUntil("Password:");
        write(password);
    }

    /** * 读取分析结果 * * @param pattern * @return */
    public String readUntil(String pattern) {
        try {
            char lastChar = pattern.charAt(pattern.length() - 1);
            StringBuffer sb = new StringBuffer();
            char ch = (char) in.read();
            while (true) {
                sb.append(ch);
                if (ch == lastChar) {
                    if (sb.toString().endsWith(pattern)) {
                        System.out.println(sb.toString());
                        return sb.toString();
                    }
                }
                ch = (char) in.read();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** * 写操作 * * @param value */
    public void write(String value) {
        try {
            out.println(value);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** * 向目标发送命令字符串 * * @param command * @return */
    public String sendCommand(String command) {
        System.out.println("send:" + command);
        try {
            write(command);
            return readUntil(prompt + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** * 关闭连接 */
    public void disconnect() {
        try {
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("启动Telnet...");
            String ip = "192.168.215.175";
            int port = 23;
            String user = "nana";
            String password = "123456";
              NetTelnet telnet = new NetTelnet(ip, port, user, password);
            System.out.println("连接上...");
            String command = "config firewall address\n" +
                    "edit t1-211_AO_3432\n" +
                    "set subnet 192.168.30.1/32\n" +
                    "next\n" +
                    "\n" +
                    "edit t1-211_AO_9261\n" +
                    "set subnet 192.168.40.3/32\n" +
                    "next\n" +
                    "\n" +
                    "config firewall policy\n" +
                    "edit t1-211\n" +
                    "set srcintf untrust \n" +
                    "set dstintf trust \n" +
                    "set srcaddr t1-211_AO_3432 \n" +
                    "set dstaddr t1-211_AO_9261 \n" +
                    "set service ALL_TCP \n" +
                    "set schedule always\n" +
                    "set action accept\n" +
                    "set comment sadfafweddddf\n" +
                    "next\n";
//            String r1 = telnet.sendCommand(command);
String r1           = telnet.sendCommand("cd /home/project/\n");
//            String r2 = telnet.sendCommand("pwd");
//            String r3 = telnet.sendCommand("sh a.sh");
            System.out.println("显示结果");
            System.out.println(r1);
//            System.out.println(r2);
//            System.out.println(r3);
            telnet.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
