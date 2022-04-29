package com.abtnetworks.totems.push.utils;

import com.abtnetworks.totems.push.dto.MailServerConfDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Properties;

/**
 * @author zc
 * @date 2019/05/08
 */
@Slf4j
public class CustomMailTool {


    private static JavaMailSender mailServerConf(MailServerConfDTO conf) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        //指定用来发送Email的邮件服务器主机名
        javaMailSender.setHost(conf.getSmtpName());
        javaMailSender.setPort(conf.getPort());
        javaMailSender.setUsername(conf.getUserName());
        javaMailSender.setPassword(conf.getPassword());
        javaMailSender.setDefaultEncoding("UTF-8");
        javaMailSender.setProtocol("smtp");

        Properties javaMailProperties = new Properties();
        //设置认证开关
        javaMailProperties.put("mail.smtp.auth", true);
        //设置发送延迟
        javaMailProperties.put("mail.smtp.timeout", 0);
        javaMailProperties.put("mail.smtp.connectiontimeout", 5000);
        if (conf.getMailSsl() != null &&  conf.getMailSsl()) {
            log.info("邮件服务器启用ssl验证");
            javaMailProperties.put("mail.smtp.ssl.enable", true);
            javaMailProperties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        javaMailSender.setJavaMailProperties(javaMailProperties);
        return javaMailSender;
    }


    public static boolean sendEmail(MailServerConfDTO conf, String toAddress,
                             String subject, String content, String... attachmentArray) {
        boolean flag = true;
        try {
            JavaMailSender mailSender = mailServerConf(conf);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(conf.getUserName());
            helper.setTo(toAddress.split(","));
            helper.setSubject(subject);
            helper.setText(content, true);
            if (attachmentArray != null && attachmentArray.length > 0) {
                for (String f : attachmentArray) {
                    File file = new File(f);
                    String fileName = file.getName();
                    if (file.exists()) {
                        helper.addAttachment(fileName, file);
                    }
                }
            }
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            flag = false;
            log.error("邮件发送异常", e);
        }

        return flag;
    }

    public static void main(String[] args) {
        MailServerConfDTO dto = new MailServerConfDTO();
        dto.setUserName("luwei@sapling.com.cn");
        dto.setPassword("Lw@2018");
        dto.setPort(25);
        dto.setMailSsl(false);
        dto.setSmtpName("smtp.exmail.qq.com");

        String toAddress = "muyuanling@sapling.com.cn";
        String subject = "策略报表全_批量";
        String content = "报表订阅邮件发送" +
                "<table border=\"1\" style=\"width: 100%;" +
                "    font-size: 12px;" +
                "    border: 1px solid #eee;" +
                "    text-align: center;" +
                "    border-collapse: collapse;" +
                "    border-spacing: 1;" +
                "    border-spacing: 0;\">" +
                "    <thead style=\"background: #c6ddf7\">" +
                "      <tr>" +
                "        <th style=\"padding: 5px 0\">设备类型</th>" +
                "        <th>设备管理IP</th>" +
                "        <th>设备名称</th>" +
                "        <th>设备分组</th>" +
                "        <th>品牌</th>" +
                "        <th>型号</th>" +
                "        <th>最近采集时间</th>" +
                "      </tr>" +
                "    </thead>" +
                "    <tbody>" +
                "      <tr>" +
                "        <td style=\"padding: 5px 0\">防火墙</td>" +
                "        <td>192.168.203.1</td>" +
                "        <td>LT HWHW Test001</td>" +
                "        <td></td>" +
                "        <td>华为</td>" +
                "        <td>USG2000</td>" +
                "        <td>2019-06-10 14:41:56</td>" +
                "      </tr>" +
                "      <tr>" +
                "        <td style=\"padding: 5px 0\">防火墙</td>" +
                "        <td>192.168.203.2</td>" +
                "        <td>LT HWHW Test002</td>" +
                "        <td></td>" +
                "        <td>华为</td>" +
                "        <td>USG2000</td>" +
                "        <td>2019-06-10 14:41:56</td>" +
                "      </tr>" +
                "    </tbody>" +
                "  </table>";
        boolean flag = CustomMailTool.sendEmail(dto, toAddress, subject, content);
        System.out.println("邮件发送返回结果flag : " + flag);
    }

}
