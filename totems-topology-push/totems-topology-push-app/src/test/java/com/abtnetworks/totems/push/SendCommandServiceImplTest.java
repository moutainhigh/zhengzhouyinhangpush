package com.abtnetworks.totems.push;

import com.abtnetworks.totems.TotemsTopologyPushApplication;
import com.abtnetworks.totems.common.enums.DeviceModelNumberEnum;
import com.abtnetworks.totems.common.enums.MoveSeatEnum;
import com.abtnetworks.totems.common.executor.ExecutorDto;
import com.abtnetworks.totems.common.executor.ExtendedRunnable;
import com.abtnetworks.totems.common.utils.IdGen;
import com.abtnetworks.totems.issued.common.SendCommandStaticAndConstants;
import com.abtnetworks.totems.issued.dto.MoveParamDTO;
import com.abtnetworks.totems.issued.dto.RecommendTask2IssuedDTO;
import com.abtnetworks.totems.issued.dto.SpecialParamDTO;
import com.abtnetworks.totems.issued.send.SendCommandService;
import com.abtnetworks.totems.push.dto.PushCmdDTO;
import com.abtnetworks.totems.push.dto.PushResultDTO;
import com.alibaba.fastjson.JSONObject;
import expect4j.Closure;
import expect4j.Expect4j;
import expect4j.ExpectState;
import expect4j.ExpectUtils;
import expect4j.matches.GlobMatch;
import expect4j.matches.Match;
import expect4j.matches.RegExpMatch;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.ehcache.EhCacheManagerUtils;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Administrator
 * @Date: 2019/12/17
 * @desc: 下发测试类
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TotemsTopologyPushApplication.class)
public class SendCommandServiceImplTest {

    private final Logger LOGGER = LoggerFactory.getLogger(SendCommandServiceImplTest.class);

    @Autowired
    SendCommandService sendCommandService;

    @Autowired
    @Qualifier(value = "commandExecutor")
    private Executor pushExecutor;

    @Value("${python-directory.fileDir}")
    private String pyFileBasedir;

    @Test
    public void sshClientExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setUsername("pix");
        pushCmdDTO.setPassword("123456");
        pushCmdDTO.setEnableUsername("enable");
        pushCmdDTO.setEnablePassword("123456");
        pushCmdDTO.setDeviceManagerIp("110.215.1.7");
        pushCmdDTO.setExecutorType("SSH");
        pushCmdDTO.setCommandline("configure terminal\n" +
                "access-list nana1 line 1 extended permit tcp host 1.13.5.4 host 1.13.5.41 eq 34\n" +
                "\n" +
                "end");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("Cisco ASA 9.9"));
        pushCmdDTO.setPushByPython(true);
        pushCmdDTO.setRevert(false);
        pushCmdDTO.getPythonPushDTO().setFilePath(pyFileBasedir);
        pushCmdDTO.getPythonPushDTO().setPythonFileName("cisco-asa.push.py");
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
//        PushCmdDTO pushCmdDTO1 = new PushCmdDTO();
//        pushCmdDTO1.setPort(22);
//        pushCmdDTO1.setUsername("pix");
//        pushCmdDTO1.setEnablePassword("123456");
//        pushCmdDTO1.setDeviceManagerIp("192.168.212.33");
//        pushCmdDTO1.setExecutorType("ssh");
//        pushCmdDTO1.setCommandline("configure terminal\n" +
//                "time-range c6_TR_8801 \n" +
//                "absolute start 00:00 10 Jun 2020 end 00:00 11 Jun 2020 \n" +
//                "exit\n" +
//                "\n" +
//                "access-list inside_in  extended permit tcp  192.168.2.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit tcp  192.168.2.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit tcp  192.168.3.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit tcp  192.168.3.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.2.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.2.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.3.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.3.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-group inside_in in interface inside\n" +
//                "end\n" +
//                "write");
//        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("Cisco ASA"));
//        PushResultDTO pushResultDTO1 = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO1);
//        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
//        PushCmdDTO pushCmdDTO2 = new PushCmdDTO();
//        pushCmdDTO2.setPort(22);
//        pushCmdDTO2.setUsername("pix");
//        pushCmdDTO2.setEnablePassword("123456");
//        pushCmdDTO2.setDeviceManagerIp("192.168.212.33");
//        pushCmdDTO2.setExecutorType("ssh");
//        pushCmdDTO2.setCommandline("configure terminal\n" +
//                "time-range c6_TR_8801 \n" +
//                "absolute start 00:00 10 Jun 2020 end 00:00 11 Jun 2020 \n" +
//                "exit\n" +
//                "\n" +
//                "access-list inside_in  extended permit tcp  192.168.2.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit tcp  192.168.2.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit tcp  192.168.3.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit tcp  192.168.3.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.2.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.2.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.3.0 255.255.255.0     192.168.4.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-list inside_in  extended permit udp  192.168.3.0 255.255.255.0     192.168.5.0 255.255.255.0    time-range c6_TR_8801\n" +
//                "access-group inside_in in interface inside\n" +
//                "end\n" +
//                "write");
//        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("Cisco ASA"));
//        PushResultDTO PushResultDTO2 = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO2);
//        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(PushResultDTO2));
    }


    @Test
    public void sshClientHillStoneExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        MoveParamDTO moveParamDTO  = new MoveParamDTO();
        pushCmdDTO.setMoveParamDTO(moveParamDTO);
        pushCmdDTO.setPort(22);
        pushCmdDTO.setUsername("root");
        pushCmdDTO.setPassword("123456");
        pushCmdDTO.setDeviceManagerIp("192.168.215.33");
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setCharset("GBK");
        pushCmdDTO.setCommandline("configure\n" +
                "address \"sda_AO_7596\"\n" +
                "ip 12.11.10.10/32\n" +
                "exit\n" +
                "\n" +
                "address \"sda_AO_3197\"\n" +
                "ip 12.11.10.101/32\n" +
                "exit\n" +
                "\n" +
                "rule top \n" +
                "name sda\n" +
                "src-zone trust\n" +
                "dst-zone dmz\n" +
                "src-addr \"sda_AO_7596\"\n" +
                "dst-addr \"sda_AO_3197\"\n" +
                "service T23\n" +
                "action permit\n" +
                "exit\n" +
                "end");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("HillstoneStoneOS"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }


    @Test
    public void telnetClientHillStoneExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("hillstone");
        pushCmdDTO.setPassword("hillstone");
        pushCmdDTO.setDeviceManagerIp("192.168.215.33");
        pushCmdDTO.setCommandline("configure\n" +
                "adess fh_AO_584\n" +
                "ip 1.2.3.5/32\n" +
                "exit\n" +
                "\n" +
                "address fh_AO_8221\n" +
                "ip 15.3.5.6/32\n" +
                "exit\n" +
                "\n" +
                "rule top \n" +
                "name fh\n" +
                "src-addr fh_AO_584\n" +
                "dst-addr fh_AO_8221\n" +
                "service \"tcp_udp_0\"\n" +
                "action permit\n" +
                "exit\n" +
                "end");

        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("HillstoneStoneOS"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));


    }

    @Test
    public void sshClientHillSSGExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("netscreen");
        pushCmdDTO.setPassword("netscreen");
        pushCmdDTO.setDeviceManagerIp("192.168.215.34");
        pushCmdDTO.setCommandline("\"set address \\\"Trust\\\" \\\"zy025_AO_3047\\\" 10.2.3.1 255.255.255.255 \\n\\nset address \\\"Trust\\\" \\\"zy025_AO_4579\\\" 10.3.2.5 255.255.255.255 \\n\\nset policy top name \\\"zy025\\\" from \\\"Trust\\\" to  \\\"Trust\\\" \\\"zy025_AO_3047\\\" \\\"zy025_AO_4579\\\" \\\"any\\\" permit \\nexit\\n\"");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("JuniperSSG"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }

    @Test
    public void sshClientFortinetExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("123456");
        pushCmdDTO.setDeviceManagerIp("192.168.215.186");
        pushCmdDTO.setRevert(true);
        pushCmdDTO.setCommandline("show firewall policy | grep -f aasd_AO_2520\n" +
                "config firewall policy \n" +
                "delete <policyId>\n" +
                "end\n" +
                "config firewall address\n" +
                "delete \"1.12.12.13/32\"\n" +
                "delete \"1.12.12.131/32\"\n" +
                "end\n" +
                "config firewall service custom\n" +
                "delete \"TCP23\"\n" +
                "end\n" +
                "\n");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("FortinetFortiOS"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }

    @Test
    public void sshClientSRXExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("root");
        pushCmdDTO.setPassword("sapling.123");
        pushCmdDTO.setDeviceManagerIp("192.168.215.31");
        pushCmdDTO.setCommandline("cli\n" +
                "configure\n" +
                "set security zones security-zone trust address-book address z2_AO_2056 10.2.3.1/32 \n" +
                "\n" +
                "set security zones security-zone trust address-book address z2_AO_700 10.23.1.5/32 \n" +
                "\n" +
                "set security policies from-zone trust to-zone trust policy z2 match source-address z2_AO_2056 \n" +
                "set security policies from-zone trust to-zone trust policy z2 match destination-address z2_AO_700 \n" +
                "set security policies from-zone trust to-zone trust policy z2 match application any \n" +
                "set security policies from-zone trust to-zone trust policy z2 then permit \n" +
                "commit");

        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("JuniperSRX"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }

    @Test
    public void sshJupSSGExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("netscreen");
        pushCmdDTO.setPassword("netscreen");
        pushCmdDTO.setDeviceManagerIp("192.168.215.34");
        pushCmdDTO.setCommandline("\"set address \\\"Trust\\\" \\\"zy025_AO_3047\\\" 10.2.3.1 255.255.255.255 \\n\\nset address \\\"Trust\\\" \\\"zy025_AO_4579\\\" 10.3.2.5 255.255.255.255 \\n\\nset policy top name \\\"zy025\\\" from \\\"Trust\\\" to  \\\"Trust\\\" \\\"zy025_AO_3047\\\" \\\"zy025_AO_4579\\\" \\\"any\\\" permit \\nexit\\n\"");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("JuniperSSG"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }

    @Test
    public void sshJupSrxExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(23);
        pushCmdDTO.setExecutorType("telnet");
        pushCmdDTO.setUsername("pix");
        pushCmdDTO.setPassword("sapling.123");
        pushCmdDTO.setDeviceManagerIp("192.168.215.31");
        pushCmdDTO.setCommandline(
                "configure\n" +
                        "set security policies from-zone any to-zone any policy ju-z2 match source-address any \n" +
                        "set security policies from-zone any to-zone any policy ju-z2 match destination-address any \n" +
                        "set security policies from-zone any to-zone any policy ju-z2 match application any \n" +
                        "set security policies from-zone any to-zone any policy ju-z2 then permit \n" +
                        "commit\n" +
                        "exit\n");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("JuniperSRX"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }

    @Test
    public void sshVenusExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(23);
        pushCmdDTO.setExecutorType("telnet");
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("sapling.123");
        pushCmdDTO.setDeviceManagerIp("192.168.215.40");
        pushCmdDTO.setCommandline("enable\n" +
                "configure terminal\n" +
                "address qm1_AO_6082\n" +
                "host-address 1.1.1.1\n" +
                "exit\n" +
                "address qm1_AO_590\n" +
                "host-address 3.1.1.1\n" +
                "exit\n" +
                "service udp_31\n" +
                "udp dest 31\n" +
                "exit\n" +
                "policy 101 trust untrust qm1_AO_6082 qm1_AO_590 udp_31 always permit\n" +
                "enable\n" +
                "end");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("Venustech VSOS"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }

    @Test
    public void sshDpExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("admin_default");
        pushCmdDTO.setDeviceManagerIp("192.168.202.35");
        pushCmdDTO.setCommandline("conf-mode\n" +
                "ip-obj-mask PD20200120-9577_0_AO_8434\n" +
                "ip-mask 21.1.1.1/32\n" +
                "exit\n" +
                "ip-obj-mask PD20200120-9577_0_AO_2172\n" +
                "ip-mask 34.1.1.1/32\n" +
                "exit\n" +
                "pf-policy PD20200120-9577_0\n" +
                "src-ip PD20200120-9577_0_AO_8434\n" +
                "dst-ip PD20200120-9577_0_AO_2172\n" +
                "action drop\n" +
                "\n" +
                "end");

        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("DPTech Firewall"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }

    @Test
    public void telnetDpExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(23);
        pushCmdDTO.setExecutorType("telnet");
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("admin_default");
        pushCmdDTO.setDeviceManagerIp("192.168.202.35");
        pushCmdDTO.setCommandline("conf-mode\n" +
                "pf-policy z2\n" +
                "src-zone Trust\n" +
                "dst-zone Trust\n" +
                "action pass\n" +
                "\n" +
                "end");

        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("DPTech Firewall R003"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }

    @Test
    public void testEHCache() {
        CacheManager cacheManager = EhCacheManagerUtils.buildCacheManager("cacheIssued");
        String name = cacheManager.getName();

        Ehcache ehCache = cacheManager.getEhcache("cacheIssued");

        Element element = new Element("1", 12);
        ehCache.put(element);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(ehCache.get("1")));
    }

    @Test
    public void sshH3V7Execute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("admin");
        MoveParamDTO moveParamDTO = new MoveParamDTO();
        moveParamDTO.setIpType(0);
//        moveParamDTO.setRelatedRule("0");
//        moveParamDTO.setRelatedName(AdvancedSettingsConstants.PARAM_NAME_MOVE_RULE_BEFORE);
        pushCmdDTO.setMoveParamDTO(moveParamDTO);
        pushCmdDTO.setDeviceManagerIp("192.168.212.34");
        RecommendTask2IssuedDTO recommendTask2IssuedDTO = new RecommendTask2IssuedDTO();
        recommendTask2IssuedDTO.setDstZone("");
        recommendTask2IssuedDTO.setSrcZone("");
        pushCmdDTO.setRecommendTask2IssuedDTO(recommendTask2IssuedDTO);
        pushCmdDTO.setCommandline("system-view\n" +
                "switchto context lwp\n" +
                " \n" +
                "system-view\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "object-group ip address fseg_AO_9709\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "0 network host address 1.23.5.5\n" +
                "\n" +
                "\n" +
                "\n" +
                "quit\n" +
                "\n" +
                "\n" +
                "object-policy ip <policySetName>\n" +
                "\n" +
                "\n" +
                "rule pass source-ip fseg_AO_9709 \n" +
                "\n" +
                "\n" +
                "quit");

        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("H3C SecPath V7 OP"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }


    @Test
    public void testThreadPoolHw() {
        for (int i = 0; i < 7; i++) {
            String id = "PT_" + IdGen.getRandomNumberString(3);
            pushExecutor.execute(new ExtendedRunnable(new ExecutorDto(id, "", "", new Date())) {
                @Override
                protected void start() throws InterruptedException, Exception {
                    PushCmdDTO pushCmdDTO = new PushCmdDTO();
                    pushCmdDTO.setPort(22);
                    pushCmdDTO.setExecutorType("ssh");
                    pushCmdDTO.setUsername("admin");
                    pushCmdDTO.setPassword("sapling.123");

                    pushCmdDTO.setDeviceManagerIp("192.168.215.32");
                    pushCmdDTO.setCommandline("system-view\n" +
                            "security-policy\n" +
                            "rule name zydeg\n" +
                            "action permit\n" +
                            "quit\n" +
                            "rule move zydeg top\n" +
                            "return");

                    pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("USG6000"));
                    PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
                    LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
                }
            });

        }
    }

    @Test
    public void testHw() {

        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("sapling.123");

        pushCmdDTO.setCharset("UTF-8");

        pushCmdDTO.setCharset("GBK");
        pushCmdDTO.setTaskType(1);
        pushCmdDTO.setDeviceManagerIp("192.168.215.32");
        MoveParamDTO moveParamDTO = new MoveParamDTO();
        pushCmdDTO.setMoveParamDTO(moveParamDTO);
        pushCmdDTO.setCommandline("system-view\n" +
                "ip address-set hw_9_AO_7019 type object\n" +
                "address 0 55.176.8.12 0\n" +
                "address 1 55.176.8.13 0\n" +
                "quit\n" +
                "\n" +
                "ip address-set DCJN_地址所属系统10234567_201222 type object\n" +
                "address 0 192.168.215.110 mask 30\n" +
                "address 1 range 192.168.215.110 192.168.215.120\n" +
                "quit\n" +
                "\n" +
                "ip service-set udp_200 type object\n" +
                "service 0 protocol udp destination-port 200  \n" +
                "quit\n" +
                "\n" +
                "security-policy\n" +
                "rule name hw_4\n" +
                "policy logging\n" +
                "session logging\n" +
                "source-zone untrust\n" +
                "destination-zone untrust\n" +
                "source-address address-set hw_4_AO_7019 \n" +
                "destination-address address-set DCJN_地址所属系统10234567_201222 \n" +
                "service udp_200 \n" +
                "service tcp_80 \n" +
                "service udp_90_100 \n" +
                "action permit\n" +
                "quit\n" +
                "return\n");

        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("USG6000 NoTop"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }

    @Test
    public void rollbackClientHillStoneExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setUsername("hillstone");
        pushCmdDTO.setPassword("hillstone");
        pushCmdDTO.setDeviceManagerIp("192.168.215.33");
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setRevert(true);
        pushCmdDTO.setPolicyFlag("PD20200623_9105");
        pushCmdDTO.setCommandline("configure\n" +
                "policy-global\n" +
                "no rule id #1\n" +
                "end\n" +
                "\n" +
                "configure\n" +
                "policy-global\n" +
                "no rule id #1\n" +
                "end");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("HillstoneStoneOS"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }

    @Test
    public void clientTopSecExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setUsername("superman");
        pushCmdDTO.setPassword("Talent@123");
        pushCmdDTO.setDeviceManagerIp("192.168.215.42");
        pushCmdDTO.setExecutorType("ssh");
        MoveParamDTO moveParamDTO = new MoveParamDTO();
        moveParamDTO.setRelatedName(MoveSeatEnum.AFTER.getKey());
        pushCmdDTO.setMoveParamDTO(moveParamDTO);
        pushCmdDTO.setRevert(false);
        pushCmdDTO.setPolicyFlag("");
        pushCmdDTO.setCommandline("firewall\n" +
                "policy add action accept src 'srcip_211028_1' dst 'srcip_211028_1' service 'ICMP' before 8270 \n" +
                "\n" +
                "end");

        pushCmdDTO.setCommandlineRevert("firewall\n" +
                "policy show src 'ccqq_AO_1234' dst 'ccqq_AO_5678'\n" +
                "policy delete id <policyId>\n" +
                "end\n");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("Topsec TOS 010-020"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }




    @Test
    public void clientCheckPointExecute() {
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("sapling.123");
        pushCmdDTO.setDeviceManagerIp("192.168.215.248");
        pushCmdDTO.setEnableUsername("admin");
        pushCmdDTO.setEnablePassword("sapling.123");
        pushCmdDTO.setExecutorType("ssh");
        MoveParamDTO moveParamDTO = new MoveParamDTO();

        pushCmdDTO.setMoveParamDTO(moveParamDTO);
        pushCmdDTO.setRevert(false);
        pushCmdDTO.setPolicyFlag("");
        SpecialParamDTO specialParamDTO = new SpecialParamDTO();
        specialParamDTO.setPolicyPackage("standard");
        specialParamDTO.setWebUrl("192.168.215.248");
        pushCmdDTO.setSpecialParamDTO(specialParamDTO);
        pushCmdDTO.setVSysName("gw-b8b25f");

        pushCmdDTO.setCommandline("mgmt add host name \"fsegsegfsegse_Host_7860\" ip-address \"1.3.2.3\"\n" +
                "mgmt add time name \"segse_T_222\" start-now \"false\" start.date \"04-Sep-2020\" start.time \"00:00\" end.date \"30-Oct-2020\" end.time \"00:00\" end-never \"false\"\n" +
                "mgmt add access-rule layer \"Network\" position \"top\" name \"fsegsegfsegse\" source.1 \"fsegsegfsegse_Host_7860\" service.1 \"icmp-proto\" time \"segse_T_222\" action \"Accept\" install-on #1\n");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.CHECK_POINT);
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }

    @Test
    public void clientCiscoExcute(){
        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setUsername("pix");
        pushCmdDTO.setEnablePassword("123456");
        pushCmdDTO.setUsername("pix");
        pushCmdDTO.setPassword("123456");
        pushCmdDTO.setDeviceManagerIp("192.168.215.236");
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setCommandline("configure terminal\n" +
                "ip access-list extended in_0/2\n" +
                "<policyId> permit tcp host 12.13.12.10   host 12.13.12.101 eq 12\n" +
                "<policyId> permit tcp host 12.13.12.10   host 12.13.12.103 eq 12\n" +
                "exit \n" +
                "end\n" +
                "write");
        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("Cisco IOS"));
        RecommendTask2IssuedDTO recommendTask2IssuedDTO =new RecommendTask2IssuedDTO();
        recommendTask2IssuedDTO.setRuleListName("in_0/2");
        recommendTask2IssuedDTO.setMatchRuleId("140");
        pushCmdDTO.setRecommendTask2IssuedDTO(recommendTask2IssuedDTO);
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));
    }



    @Test
    public void code() throws Exception {


        String hostname = "192.168.215.42";
        String username = "superman";
        String password = "Talent@123";
        Expect4j expect = ExpectUtils.SSH(hostname, username, password, 22);

        final StringBuffer rxStr = new StringBuffer();
        final StringBuffer txStr = new StringBuffer();
        Closure closure = (ExpectState expectState) -> {
            String expectBuffer = expectState.getBuffer();
            System.out.println(expectBuffer);


        };
        List<Match> lstPattern = new ArrayList<>();
//        Match mat = new GlobMatch(">", closure);
//        lstPattern.add(mat);
//        Match mat1 = new GlobMatch("password:", closure);
//        lstPattern.add(mat1);
        Match mat2 = new RegExpMatch("(?<!(<-18U.define))#", closure);
        lstPattern.add(mat2);
        int matchKillIndex = expect.expect(lstPattern);
        System.out.println(matchKillIndex);
        expect.send("firewall\n");

        matchKillIndex = expect.expect(lstPattern);
        System.out.println(matchKillIndex);
        expect.send("policy add action accept src 'srcip_211028_1' dst 'srcip_211028_1' service 'ICMP' before 8270 \n\n");
        expect.send(" \n");
        matchKillIndex = expect.expect(lstPattern);
        System.out.println(matchKillIndex);
        expect.send("end\n");
        expect.close();


    }

    @Test
    public void  pattarn(){
        //        Pattern RULE_NAME_RGE = Pattern.compile("[Error\\:\\s.*?&&[?!(Too)]]");Error:^((?!Too).)*$
        Pattern RULE_NAME_RGE = Pattern.compile("Error:(?! Unrecognized command)");
        Matcher matcher = RULE_NAME_RGE.matcher("address 0 55.176.8.86 0\n" +
                "                 ^\n" +
                "Error: Unrecognized command found at '^' position");
        System.out.println(matcher.find());
    }

//    public static void main(String[] args) {
//        Pattern RULE_NAME_RGE = Pattern.compile("Error:(?!Too| Unrecognized command)");
//        Matcher matcher = RULE_NAME_RGE.matcher("address 0 55.176.8.86 0\n" +
//                "                 ^\n" +
//                "Error: Unrecognized command found at '^' position" +
//                "Error:Too many");
//        System.out.println(matcher.find());
//    }


    @Test
    public void testGQun() {

        PushCmdDTO pushCmdDTO = new PushCmdDTO();
        pushCmdDTO.setPort(22);
        pushCmdDTO.setExecutorType("ssh");
        pushCmdDTO.setUsername("admin");
        pushCmdDTO.setPassword("X9@tZYpnZD");

        pushCmdDTO.setDeviceManagerIp("192.168.9.227");
        MoveParamDTO moveParamDTO = new MoveParamDTO();
        pushCmdDTO.setMoveParamDTO(moveParamDTO);
        pushCmdDTO.setCommandline("define firewall policy\n" +
                "edit 14\n" +
                "set srcintf any\n" +
                "set dstintf any\n" +
                "set srcaddr any \n" +
                "set dstaddr any \n" +
                "set service \"Windows AD\" \n" +
                "set schedule always\n" +
                "set action accept\n" +
                "next\n" +
                "end\n" +
                "exit");

        pushCmdDTO.setDeviceModelNumberEnum(DeviceModelNumberEnum.fromString("Kill Firewall"));
        PushResultDTO pushResultDTO = sendCommandService.routeNewOrOldExecuteByRegular(pushCmdDTO);
        LOGGER.error("返回命令信息{}", JSONObject.toJSONString(pushResultDTO));

    }
}
